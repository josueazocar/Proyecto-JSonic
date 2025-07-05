package network;

import com.JSonic.uneg.*;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import network.interfaces.IGameServer;

import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;

public class GameServer implements IGameServer {

    private final Server servidor;
    // --- ALMACENES DE ESTADO ---
    private final HashMap<Integer, PlayerState> jugadores = new HashMap<>();
    private final HashMap<Integer, EnemigoState> enemigosActivos = new HashMap<>();
    private final HashMap<Integer, ItemState> itemsActivos = new HashMap<>();
    private volatile ArrayList<com.badlogic.gdx.math.Rectangle> paredesDelMapa = null;
    private final HashMap<Integer, Integer> puntajesAnillosIndividuales = new HashMap<>();
    private final HashMap<Integer, Integer> puntajesBasuraIndividuales = new HashMap<>();

    // Estado global compartido por todo el equipo.
    private final ContaminationState contaminationState = new ContaminationState();
    public int totalAnillosGlobal = 0;
    public int totalBasuraGlobal = 0;

    //  Variables de control para la contaminación.
    private static final float CONTAMINATION_RATE_PER_SECOND = 0.65f; // El % sube 0.65 puntos por segundo.
    private static final float TRASH_CLEANUP_VALUE = 3f; // Cada basura recogida reduce el % en 3 puntos.
    private float tiempoDesdeUltimaContaminacion = 0f;
    private static final float INTERVALO_ACTUALIZACION_CONTAMINACION = 1.0f; // Enviaremos una actualización cada segundo.

    // --- VARIABLES DE CONTROL DE SPAWNING ---
    private static final float VELOCIDAD_ROBOTNIK = 60f;
    private static final float RANGO_DETENERSE_ROBOTNIK = 30f;
    private int proximoIdEnemigo = 0;
    private int proximoIdItem = 0;
    private float tiempoGeneracionEnemigo = 0f;
    private final float INTERVALO_GENERACION_ENEMIGO = 5.0f;
    private static final int MAX_ANILLOS = 50;
    private static final int MAX_BASURA = 10;
    private static final int MAX_PLASTICO = 10;
    private static final float INTERVALO_SPAWN_ANILLO = 1.0f;
    private static final float INTERVALO_SPAWN_BASURA = 5.0f;
    private static final float INTERVALO_SPAWN_PLASTICO = 5.0f;
    private float tiempoSpawnAnillo = 0f;
    private float tiempoSpawnBasura = 0f;
    private float tiempoSpawnPlastico = 0f;
    private static final float ROBOT_SPEED = 1.0f;
    private static final float ROBOT_DETECTION_RANGE = 300f;
    private static final float ROBOT_ATTACK_RANGE = 10f;
    private float tiempoGeneracionTeleport = 0f;
    private boolean teleportGenerado = false;


    public GameServer() {
        servidor = new Server();
    }

    @Override
    public void start() {
        Network.registrar(servidor);
        servidor.addListener(new Listener() {
            public void connected(Connection conexion) {
                System.out.println("[SERVER] Un cliente se ha conectado: ID " + conexion.getID());
                PlayerState nuevoEstado = new PlayerState();
                nuevoEstado.id = conexion.getID();
                nuevoEstado.x = 100;
                nuevoEstado.y = 100;
                nuevoEstado.estadoAnimacion = Player.EstadoPlayer.IDLE_RIGHT;
                jugadores.put(conexion.getID(), nuevoEstado);
                puntajesAnillosIndividuales.put(conexion.getID(), 0);
                puntajesBasuraIndividuales.put(conexion.getID(), 0);

                Network.PaqueteJugadorConectado packetNuevoJugador = new Network.PaqueteJugadorConectado();
                packetNuevoJugador.nuevoJugador = nuevoEstado;
                servidor.sendToAllExceptTCP(conexion.getID(), packetNuevoJugador);

                for (PlayerState jugadorExistente : jugadores.values()) {
                    if (jugadorExistente.id != conexion.getID()) {
                        Network.PaqueteJugadorConectado packetJugadorExistente = new Network.PaqueteJugadorConectado();
                        packetJugadorExistente.nuevoJugador = jugadorExistente;
                        conexion.sendTCP(packetJugadorExistente);
                    }
                }

                // Sincronizar ítems y enemigos existentes al nuevo cliente
                for (ItemState itemExistente : itemsActivos.values()) {
                    Network.PaqueteItemNuevo paqueteItem = new Network.PaqueteItemNuevo();
                    paqueteItem.estadoItem = itemExistente;
                    conexion.sendTCP(paqueteItem);
                }
                for (EnemigoState enemigoExistente : enemigosActivos.values()) {
                    Network.PaqueteEnemigoNuevo paqueteEnemigo = new Network.PaqueteEnemigoNuevo();
                    paqueteEnemigo.estadoEnemigo = enemigoExistente;
                    conexion.sendTCP(paqueteEnemigo);
                }

                // Sincronizamos el estado de la contaminación para el nuevo jugador.
                Network.PaqueteActualizacionContaminacion paqueteContaminacion = new Network.PaqueteActualizacionContaminacion();
                paqueteContaminacion.contaminationPercentage = contaminationState.getPercentage();
                conexion.sendTCP(paqueteContaminacion);
            }

            public void received(Connection conexion, Object objeto) {
                if (objeto instanceof Network.PaquetePosicionJugador paquete) {
                    PlayerState estadoJugador = jugadores.get(paquete.id);
                    if (estadoJugador != null) {
                        estadoJugador.x = paquete.x;
                        estadoJugador.y = paquete.y;
                        estadoJugador.estadoAnimacion = paquete.estadoAnimacion;
                        servidor.sendToAllExceptTCP(conexion.getID(), paquete);
                    }
                }
                if (objeto instanceof Network.SolicitudAccesoPaquete solicitud) {
                    System.out.println("[SERVER] Peticion de login recibida de: " + solicitud.nombreJugador);
                    Network.RespuestaAccesoPaquete respuesta = new Network.RespuestaAccesoPaquete();
                    respuesta.mensajeRespuesta = "Bienvenido al servidor, " + solicitud.nombreJugador + "!";
                    PlayerState estadoAsignado = jugadores.get(conexion.getID());

                    estadoAsignado.characterType = solicitud.characterType;

                    respuesta.tuEstado = estadoAsignado;
                    conexion.sendTCP(respuesta);
                }
                if (objeto instanceof Network.PaqueteSolicitudRecogerItem paquete) {
                    // Usamos 'synchronized' para evitar que dos jugadores interactúen con el mismo ítem a la vez.
                    synchronized (itemsActivos) {
                        // 1. PRIMERO VERIFICAMOS el ítem con .get() en lugar de .remove() para poder saber su tipo.
                        ItemState itemRecogido = itemsActivos.get(paquete.idItem);

                        // Si el ítem realmente existe...
                        if (itemRecogido != null) {

                            // 2. AHORA DECIDIMOS QUÉ HACER BASADO EN SU TIPO.
                            // CASO ESPECIAL: Es un teletransportador.
                            if (itemRecogido.tipo == ItemState.ItemType.TELETRANSPORTE) {
                                System.out.println("[SERVER] Jugador " + conexion.getID() + " ha activado el teletransportador.");
                                itemsActivos.remove(paquete.idItem); // Ahora sí lo eliminamos de la lista.

                                // Creamos la ORDEN de cambio de mapa.
                                Network.PaqueteOrdenCambiarMapa orden = new Network.PaqueteOrdenCambiarMapa();
                                orden.nuevoMapa = "maps/ZonaJefeN1.tmx";
                                orden.nuevaPosX = 70f;   // Coordenada X de llegada en el nuevo mapa.
                                orden.nuevaPosY = 250f;  // Coordenada Y de llegada en el nuevo mapa.

                                // Enviamos la orden A TODOS los jugadores conectados para cambiar de mapa.
                                servidor.sendToAllTCP(orden);

                                // Adicionalmente, notificamos a TODOS los jugadores que este ítem (el portal) ya no existe.
                                Network.PaqueteItemEliminado paqueteEliminado = new Network.PaqueteItemEliminado();
                                paqueteEliminado.idItem = paquete.idItem;
                                servidor.sendToAllTCP(paqueteEliminado);
                                paredesDelMapa = null; // El servidor "olvida" el mapa anterior.
                            }
                            // CASO GENERAL: Es cualquier otro ítem (anillo, basura, etc.).
                            else {
                                itemsActivos.remove(paquete.idItem); // Lo eliminamos de la lista.
                                System.out.println("[SERVER] Ítem con ID " + paquete.idItem + " recogido por jugador " + conexion.getID());

                                if (itemRecogido.tipo == ItemState.ItemType.ANILLO) {
                                    int puntajeActual = puntajesAnillosIndividuales.getOrDefault(conexion.getID(), 0);
                                    puntajesAnillosIndividuales.put(conexion.getID(), puntajeActual + 1);

                                    // Actualizamos el puntaje GLOBAL del equipo.
                                    totalAnillosGlobal++;
                                    System.out.println("[SERVER] Anillo recogido. Total de equipo: " + totalAnillosGlobal);

                                } else if (itemRecogido.tipo == ItemState.ItemType.BASURA || itemRecogido.tipo == ItemState.ItemType.PIEZA_PLASTICO) {
                                    int puntajeActual = puntajesBasuraIndividuales.getOrDefault(conexion.getID(), 0);
                                    puntajesBasuraIndividuales.put(conexion.getID(), puntajeActual + 1);

                                    // Actualizamos el puntaje GLOBAL del equipo.
                                    totalBasuraGlobal++;

                                    // ¡La acción individual impacta el estado GLOBAL de la contaminación!
                                    contaminationState.decrease(TRASH_CLEANUP_VALUE);
                                    System.out.println("[SERVER] Basura recogida. Total de equipo: " + totalBasuraGlobal +
                                        ". Contaminación GLOBAL reducida a: " + String.format("%.2f", contaminationState.getPercentage()) + "%!");

                                    // Como la contaminación cambió, enviamos una actualización inmediata a TODOS.
                                    Network.PaqueteActualizacionContaminacion paqueteContaminacion = new Network.PaqueteActualizacionContaminacion();
                                    paqueteContaminacion.contaminationPercentage = contaminationState.getPercentage();
                                    servidor.sendToAllTCP(paqueteContaminacion);
                                }

                                // Creamos y enviamos el paquete de actualización de puntuación SOLO al jugador que recogió el ítem.
                                Network.PaqueteActualizacionPuntuacion paquetePuntaje = new Network.PaqueteActualizacionPuntuacion();
                                paquetePuntaje.nuevosAnillos = puntajesAnillosIndividuales.get(conexion.getID());
                                paquetePuntaje.nuevaBasura = puntajesBasuraIndividuales.get(conexion.getID());
                                conexion.sendTCP(paquetePuntaje);

                                // Notificamos a TODOS los jugadores que el ítem ya no existe.
                                Network.PaqueteItemEliminado paqueteEliminado = new Network.PaqueteItemEliminado();
                                paqueteEliminado.idItem = paquete.idItem;
                                servidor.sendToAllTCP(paqueteEliminado);
                            }
                        }
                    }
                }
                if (objeto instanceof Network.PaqueteAnimacionEnemigoTerminada paquete) {
                    // Un cliente nos informa que la animación de un enemigo terminó.
                    EnemigoState enemigo = enemigosActivos.get(paquete.idEnemigo);
                    if (enemigo != null) {
                        // Ponemos al enemigo en el estado especial para que la IA lo reevalúe.
                        enemigo.estadoAnimacion = EnemigoState.EstadoEnemigo.POST_ATAQUE;
                        enemigo.tiempoEnEstado = 0;
                    }
                }
                if (objeto instanceof Network.PaqueteInformacionMapa paquete) {
                    // El primer cliente que se conecta nos envía el plano del mapa.
                    // Solo lo guardamos si aún no lo tenemos.
                    if (paredesDelMapa == null) {
                        paredesDelMapa = paquete.paredes;
                        System.out.println("[SERVER] Plano del mapa recibido con " + paredesDelMapa.size() + " paredes. ¡Ahora puedo ver las paredes!");
                    }
                }
            }

            public void disconnected(Connection conexion) {
                System.out.println("[SERVER] Un cliente se ha desconectado.");
                jugadores.remove(conexion.getID());
            }
        });

        try {
            servidor.bind(Network.PORT);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        servidor.start();

        // Damos vida a Robotnik en el servidor.
        // Usamos un ID alto como 999 para distinguirlo fácilmente.
        EnemigoState estadoRobotnik = new EnemigoState(999, 300, 100, 100, EnemigoState.EnemigoType.ROBOTNIK);
        this.enemigosActivos.put(estadoRobotnik.id, estadoRobotnik);
        System.out.println("[SERVER] Robotnik ha sido creado en el servidor.");

        Network.PaqueteEnemigoNuevo paqueteRobotnik = new Network.PaqueteEnemigoNuevo();
        paqueteRobotnik.estadoEnemigo = estadoRobotnik;
        servidor.sendToAllTCP(paqueteRobotnik);

        new Thread(() -> {
            System.out.println("[SERVER] Hilo de juego iniciado. Esperando el plano del mapa del primer cliente...");

            // Este bucle se ejecutará hasta que la variable 'paredesDelMapa' sea llenada
            // por el paquete que envía el primer cliente.
            while (paredesDelMapa == null) {
                try {
                    // Esperamos un corto tiempo para no consumir 100% de la CPU mientras esperamos.
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                    return; // Salimos del hilo si es interrumpido.
                }
            }

            System.out.println("[SERVER] ¡Plano del mapa detectado! Iniciando bucle de juego principal.");

            final float FIXED_DELTA_TIME = 1 / 60f;
            while (true) {
                try {
                    updateServerLogic(FIXED_DELTA_TIME);
                    Thread.sleep((long) (FIXED_DELTA_TIME * 1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }).start();

        System.out.println("[SERVER] Servidor online iniciado y escuchando en el puerto " + Network.PORT);
    }
    private void actualizarEnemigosAI(float deltaTime) {
        // Si no hay jugadores conectados, no hay nada que hacer.
        if (jugadores.isEmpty()) {
            return;
        }

        for (EnemigoState enemigo : enemigosActivos.values()) {

            if (enemigo.tipo == EnemigoState.EnemigoType.ROBOTNIK) { // Asumiendo que tienes un tipo ROBOTNIK
                PlayerState jugadorMasCercano = null;
                float distanciaMinima = Float.MAX_VALUE;

                for (PlayerState jugador : jugadores.values()) {
                    float d = (float) Math.sqrt(Math.pow(jugador.x - enemigo.x, 2) + Math.pow(jugador.y - enemigo.y, 2));
                    if (d < distanciaMinima) {
                        distanciaMinima = d;
                        jugadorMasCercano = jugador;
                    }
                }

                if (jugadorMasCercano == null) {
                    continue; // Si no hay a quién perseguir, no hacemos nada con él
                }

                float distanciaX = jugadorMasCercano.x - enemigo.x;

                // Lógica de movimiento (adaptada de tu PantallaDeJuego)
                if (distanciaMinima > RANGO_DETENERSE_ROBOTNIK) { // Usa una constante que debes definir en el servidor
                    float velocidadMovimiento = VELOCIDAD_ROBOTNIK * deltaTime; // Define VELOCIDAD_ROBOTNIK
                    Vector2 direccionDeseada = new Vector2(distanciaX, jugadorMasCercano.y - enemigo.y).nor();

                    enemigo.x += direccionDeseada.x * velocidadMovimiento;
                    enemigo.y += direccionDeseada.y * velocidadMovimiento;

                    enemigo.mirandoDerecha = (direccionDeseada.x > 0);
                    enemigo.estadoAnimacion = enemigo.mirandoDerecha ? EnemigoState.EstadoEnemigo.RUN_RIGHT : EnemigoState.EstadoEnemigo.RUN_LEFT;
                } else {
                    enemigo.estadoAnimacion = enemigo.mirandoDerecha ? EnemigoState.EstadoEnemigo.IDLE_RIGHT : EnemigoState.EstadoEnemigo.IDLE_LEFT;
                }

                // Ya que este es Robotnik, no queremos que la lógica genérica de abajo lo afecte.
                // Así que saltamos al siguiente enemigo en el bucle.
                continue;
            }

            // --- Lógica para encontrar al jugador más cercano ---
            PlayerState jugadorMasCercano = null;
            float distanciaMinima = Float.MAX_VALUE;

            for (PlayerState jugador : jugadores.values()) {
                float d = (float) Math.sqrt(Math.pow(jugador.x - enemigo.x, 2) + Math.pow(jugador.y - enemigo.y, 2));
                if (d < distanciaMinima) {
                    distanciaMinima = d;
                    jugadorMasCercano = jugador;
                }
            }

            // Si por alguna razón no encontramos a nadie, saltamos a la siguiente iteración.
            if (jugadorMasCercano == null) {
                continue;
            }


            float dx = jugadorMasCercano.x - enemigo.x;
            float dy = jugadorMasCercano.y - enemigo.y;
            float distance = distanciaMinima; // Ya la calculamos

            if (enemigo.estadoAnimacion == EnemigoState.EstadoEnemigo.HIT_RIGHT || enemigo.estadoAnimacion == EnemigoState.EstadoEnemigo.HIT_LEFT) {
                continue;
            }

            EnemigoState.EstadoEnemigo estadoAnterior = enemigo.estadoAnimacion;

            if (distance <= ROBOT_ATTACK_RANGE) {
                enemigo.estadoAnimacion = dx > 0 ? EnemigoState.EstadoEnemigo.HIT_RIGHT : EnemigoState.EstadoEnemigo.HIT_LEFT;
            } else if (distance <= ROBOT_DETECTION_RANGE) {
                enemigo.mirandoDerecha = dx > 0;
                enemigo.estadoAnimacion = enemigo.mirandoDerecha ? EnemigoState.EstadoEnemigo.RUN_RIGHT : EnemigoState.EstadoEnemigo.RUN_LEFT;
            } else {
                enemigo.mirandoDerecha = dx > 0;
                enemigo.estadoAnimacion = enemigo.mirandoDerecha ? EnemigoState.EstadoEnemigo.IDLE_RIGHT : EnemigoState.EstadoEnemigo.IDLE_LEFT;
            }

            if (estadoAnterior != enemigo.estadoAnimacion) {
                enemigo.tiempoEnEstado = 0;
            }

            if (enemigo.estadoAnimacion == EnemigoState.EstadoEnemigo.RUN_RIGHT || enemigo.estadoAnimacion == EnemigoState.EstadoEnemigo.RUN_LEFT) {
                float nextX = enemigo.x;
                float nextY = enemigo.y;
                if (dx > 0) nextX += ROBOT_SPEED; else if (dx < 0) nextX -= ROBOT_SPEED;
                if (dy > 0) nextY += ROBOT_SPEED; else if (dy < 0) nextY -= ROBOT_SPEED;

                com.badlogic.gdx.math.Rectangle robotBounds = new com.badlogic.gdx.math.Rectangle(enemigo.x, enemigo.y, 48, 48);

                // Comprobamos el movimiento en X
                robotBounds.setX(nextX);
                boolean colisionEnX = false;
                for (com.badlogic.gdx.math.Rectangle pared : paredesDelMapa) {
                    if (pared.overlaps(robotBounds)) {
                        colisionEnX = true;
                        break;
                    }
                }
                if (!colisionEnX) {
                    enemigo.x = nextX; // Si no hay colisión, aplicamos el movimiento en X
                }

                // Comprobamos el movimiento en Y
                robotBounds.setX(enemigo.x); // Volvemos a la X actual (que puede haber cambiado) para comprobar Y
                robotBounds.setY(nextY);
                boolean colisionEnY = false;
                for (com.badlogic.gdx.math.Rectangle pared : paredesDelMapa) {
                    if (pared.overlaps(robotBounds)) {
                        colisionEnY = true;
                        break;
                    }
                }
                if (!colisionEnY) {
                    enemigo.y = nextY; // Si no hay colisión, aplicamos el movimiento en Y
                }
            }
        }
    }
    private void updateServerLogic(float deltaTime) {
        //Lógica de Aumento y Sincronización de la Contaminación
        contaminationState.increase(CONTAMINATION_RATE_PER_SECOND * deltaTime);
        tiempoDesdeUltimaContaminacion += deltaTime;

        // Si ha pasado un segundo, enviamos una actualización a TODOS los jugadores.
        if (tiempoDesdeUltimaContaminacion >= INTERVALO_ACTUALIZACION_CONTAMINACION) {
            Network.PaqueteActualizacionContaminacion paquete = new Network.PaqueteActualizacionContaminacion();
            paquete.contaminationPercentage = contaminationState.getPercentage();
            servidor.sendToAllTCP(paquete); // Transmisión a todos

            tiempoDesdeUltimaContaminacion = 0f; // Reseteamos el temporizador
        }

        this.tiempoGeneracionTeleport += deltaTime;
        if ((int)this.tiempoGeneracionTeleport % 2 == 0 && (int)this.tiempoGeneracionTeleport != 0) {
            System.out.println(
                "[GAMESERVER DEBUG] Tiempo: " + String.format("%.2f", this.tiempoGeneracionTeleport) +
                    " | Portal Generado?: " + this.teleportGenerado +
                    " | Jugadores Conectados: " + jugadores.size()
            );
        }

// Suponiendo que el teletransportador solo debe aparecer si hay jugadores
        if (!this.teleportGenerado && this.tiempoGeneracionTeleport >= 5f && !jugadores.isEmpty()) {
            System.out.println("[GAMESERVER] Generando teletransportador...");

            // Como el servidor no lee el mapa, definimos aquí las coordenadas.
            // DEBES AJUSTAR ESTAS COORDENADAS a la posición donde quieres que aparezca.
            float teleX = 1848.0f;
            float teleY = 1190.0f;
            int teleId = 10000;

            // Creamos el estado del item
            ItemState estadoTele = new ItemState(teleId, teleX, teleY, ItemState.ItemType.TELETRANSPORTE);
            itemsActivos.put(estadoTele.id, estadoTele);

            // Creamos el paquete para notificar a TODOS los clientes
            Network.PaqueteItemNuevo paquete = new Network.PaqueteItemNuevo();
            paquete.estadoItem = estadoTele;
            servidor.sendToAllTCP(paquete);

            this.teleportGenerado = true; // Marcamos como generado
        }

        actualizarEnemigosAI(deltaTime);

        generarNuevosItems(deltaTime);
        generarNuevosEnemigos(deltaTime);

        // Creamos un nuevo paquete y le metemos la lista completa de enemigos.
        Network.PaqueteActualizacionEnemigos paqueteUpdate = new Network.PaqueteActualizacionEnemigos();
        paqueteUpdate.estadosEnemigos = this.enemigosActivos;
        servidor.sendToAllTCP(paqueteUpdate);
    }

    private void generarNuevosItems(float deltaTime) {
        int anillos = 0, basura = 0, plastico = 0;
        for (ItemState item : itemsActivos.values()) {
            if (item.tipo == ItemState.ItemType.ANILLO) anillos++;
            if (item.tipo == ItemState.ItemType.BASURA) basura++;
            if (item.tipo == ItemState.ItemType.PIEZA_PLASTICO) plastico++;
        }
        tiempoSpawnAnillo += deltaTime;
        if (tiempoSpawnAnillo >= INTERVALO_SPAWN_ANILLO && anillos < MAX_ANILLOS) {
            spawnNuevoItem(ItemState.ItemType.ANILLO);
            tiempoSpawnAnillo = 0f;
        }
        tiempoSpawnBasura += deltaTime;
        if (tiempoSpawnBasura >= INTERVALO_SPAWN_BASURA && basura < MAX_BASURA) {
            spawnNuevoItem(ItemState.ItemType.BASURA);
            tiempoSpawnBasura = 0f;
        }
        tiempoSpawnPlastico += deltaTime;
        if (tiempoSpawnPlastico >= INTERVALO_SPAWN_PLASTICO && plastico < MAX_PLASTICO) {
            spawnNuevoItem(ItemState.ItemType.PIEZA_PLASTICO);
            tiempoSpawnPlastico = 0f;
        }
    }

    private void generarNuevosEnemigos(float deltaTime) {
        tiempoGeneracionEnemigo += deltaTime;
        if (tiempoGeneracionEnemigo >= INTERVALO_GENERACION_ENEMIGO) {
            spawnNuevoEnemigo();
            tiempoGeneracionEnemigo = 0f;
        }

        if (tiempoGeneracionEnemigo >= INTERVALO_GENERACION_ENEMIGO) {
            spawnNuevoEnemigo();
            tiempoGeneracionEnemigo = 0f;
        }
    }


    private void spawnNuevoEnemigo() {
        // Si todavía no hemos recibido el plano del mapa, no generamos nada.
        if (paredesDelMapa == null) {
            System.out.println("[SERVER] Aún no tengo el plano del mapa, no puedo generar enemigos.");
            return;
        }

        int intentos = 0;
        boolean colocado = false;
        while (!colocado && intentos < 20) { // Hacemos hasta 20 intentos para encontrar un lugar libre
            float x = (float) (Math.random() * 1920);
            float y = (float) (Math.random() * 1280);
            com.badlogic.gdx.math.Rectangle bounds = new com.badlogic.gdx.math.Rectangle(x, y, 48, 48);

            // Comprobamos si la nueva posición choca con alguna pared
            boolean hayColision = false;
            for (com.badlogic.gdx.math.Rectangle pared : paredesDelMapa) {
                if (pared.overlaps(bounds)) {
                    hayColision = true;
                    break;
                }
            }

            if (!hayColision) {
                EnemigoState nuevoEstado = new EnemigoState(proximoIdEnemigo++, bounds.x, bounds.y, 100, EnemigoState.EnemigoType.ROBOT);
                enemigosActivos.put(nuevoEstado.id, nuevoEstado);
                Network.PaqueteEnemigoNuevo paquete = new Network.PaqueteEnemigoNuevo();
                paquete.estadoEnemigo = nuevoEstado;
                servidor.sendToAllTCP(paquete);
                colocado = true;
            }
            intentos++;
        }
    }


    private void spawnNuevoItem(ItemState.ItemType tipo) {
        // Si todavía no hemos recibido el plano del mapa, no generamos nada.
        if (paredesDelMapa == null) {
            System.out.println("[SERVER] Aún no tengo el plano del mapa, no puedo generar ítems.");
            return;
        }

        int intentos = 0;
        boolean colocado = false;
        while (!colocado && intentos < 20) {
            float x = (float) (Math.random() * 1920);
            float y = (float) (Math.random() * 1280);
            com.badlogic.gdx.math.Rectangle nuevoBounds = new com.badlogic.gdx.math.Rectangle(x, y, 32, 32);

            // Comprobamos si la nueva posición choca con alguna pared O con otro ítem
            boolean hayColision = false;
            for (com.badlogic.gdx.math.Rectangle pared : paredesDelMapa) {
                if (pared.overlaps(nuevoBounds)) {
                    hayColision = true;
                    break;
                }
            }
            // Añadimos la comprobación de superposición con otros ítems
            if (!hayColision) {
                for (ItemState itemExistente : itemsActivos.values()) {
                    if (new com.badlogic.gdx.math.Rectangle(itemExistente.x, itemExistente.y, 32, 32).overlaps(nuevoBounds)) {
                        hayColision = true;
                        break;
                    }
                }
            }

            if (!hayColision) {
                ItemState nuevoEstado = new ItemState(proximoIdItem++, nuevoBounds.x, nuevoBounds.y, tipo);
                itemsActivos.put(nuevoEstado.id, nuevoEstado);
                Network.PaqueteItemNuevo paquete = new Network.PaqueteItemNuevo();
                paquete.estadoItem = nuevoEstado;
                servidor.sendToAllTCP(paquete);
                colocado = true;
            }
            intentos++;
        }
    }

    @Override
    public void update(float deltaTime, com.JSonic.uneg.LevelManager manejadorNivel) {

    }

    @Override
    public void dispose() {
        if (servidor != null) {
            servidor.close();
            System.out.println("[SERVER] Servidor detenido.");
        }
    }
}
