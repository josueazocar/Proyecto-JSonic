package network;

import com.JSonic.uneg.*;
import com.JSonic.uneg.EntidadesVisuales.Player;
import com.JSonic.uneg.Pantallas.EstadisticasJugador;
import com.JSonic.uneg.State.*;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import network.interfaces.IGameServer;


import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameServer implements IGameServer {

    private Server servidor;
    private volatile boolean serverThreadActive = false;
    private Thread gameLoopThread;
    // --- ALMACENES DE ESTADO ---
   // private final HashMap<Integer, PlayerState> jugadores = new HashMap<>();
    private final Map<Integer, PlayerState> jugadores = new ConcurrentHashMap<>();
    private final EnumSet<PlayerState.CharacterType> personajesEnUso = EnumSet.noneOf(PlayerState.CharacterType.class);
    private final Map<Integer, EnemigoState> enemigosActivos = new ConcurrentHashMap<>();
    private final Map<Integer, ItemState> itemsActivos = new ConcurrentHashMap<>();
    private volatile ArrayList<com.badlogic.gdx.math.Rectangle> paredesDelMapa = null;
    private final HashMap<Integer, Integer> puntajesAnillosIndividuales = new HashMap<>();
    private final HashMap<Integer, Integer> puntajesBasuraIndividuales = new HashMap<>();
    private final HashMap<Integer, DronState> dronesActivos = new HashMap<>();
    private final ArrayList<Rectangle> colisionesDinamicas = new ArrayList<>();
    private final HashMap<String, Network.PortalInfo> infoPortales = new HashMap<>();
    private int proximoIdDron = 20000; // Un rango de IDs para los drones
    private final HashMap<Integer, Rectangle> bloquesRompibles = new HashMap<>();
    private int proximoIdBloque = 30000; // Un rango de IDs para bloques

    // Estado global compartido por todo el equipo.
    private final ContaminationState contaminationState = new ContaminationState();
    public int totalAnillosGlobal = 0;
    public int totalBasuraGlobal = 0;

    //  Variables de control para la contaminación.
    private static final float CONTAMINATION_RATE_PER_SECOND = 1.25f; // El % sube 0.65 puntos por segundo.
    private static final float TRASH_CLEANUP_VALUE = 2f; // Cada basura recogida reduce el % en 3 puntos.
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
    private static final float ROBOT_SPEED = 1.3f;
    private static final float ROBOT_DETECTION_RANGE = 300f;
    private static final float ROBOT_ATTACK_RANGE = 10f;
    private float tiempoGeneracionTeleport = 0f;
    private boolean teleportGenerado = false;
    private int basuraReciclada = 0;

    private final HashMap<Integer, AnimalState> animalesActivos = new HashMap<>();
    private final HashMap<Integer, Float> cooldownsHabilidadLimpieza = new HashMap<>();
    private final HashMap<Integer, EstadisticasJugador> estadisticasJugadores = new HashMap<>();
    private static final float COOLDOWN_HABILIDAD_SONIC = 40.0f; // Cooldown real
    private int proximoIdAnimal = 20000; // ID base para evitar colisiones con otros IDs

    // Variables para la lógica de muerte por contaminación
    private float tiempoParaProximaMuerteAnimal = 20f; // Temporizador (ej: 20 segundos)
    private boolean muertesAnimalesActivas = false;
    private volatile boolean alMenosUnJugadorHaEnviadoPosicion = false;

    private int enemigosGeneradosEnNivelActual = 0;
    private final HashMap<String, Integer> enemigosPorMapa = new HashMap<>();
    private String mapaActualServidor = ""; // Para saber en qué mapa estamos
    public int esmeraldasRecogidasGlobal = 0;

    public GameServer() {
        servidor = new Server();
        enemigosPorMapa.put("maps/Zona1N1.tmx", 12);
        enemigosPorMapa.put("maps/ZonaJefeN1.tmx", 8);
        enemigosPorMapa.put("maps/Zona1N2.tmx", 15);
        enemigosPorMapa.put("maps/ZonaJefeN2.tmx", 8);
        enemigosPorMapa.put("maps/Zona1N3.tmx", 25);
        enemigosPorMapa.put("maps/Zona2N3.tmx", 35);
        enemigosPorMapa.put("maps/ZonaJefeN3.tmx", 8);
    }

    @Override
    public void start() {
        System.out.println("[SERVER] Iniciando GameServer...");
        iniciarInstanciaServidor();
    }

    private void procesarRecogidaItem(int idJugador, int idItem) {
        synchronized (itemsActivos) {
            ItemState itemRecogido = itemsActivos.remove(idItem);
            if (itemRecogido == null) return; // El ítem ya fue recogido por otro evento.

            System.out.println("[SERVER] Ítem con ID " + idItem + " recogido por jugador " + idJugador);

            if (itemRecogido.tipo == ItemState.ItemType.ANILLO) {
                int puntaje = puntajesAnillosIndividuales.getOrDefault(idJugador, 0);
                puntajesAnillosIndividuales.put(idJugador, puntaje + 1);
                totalAnillosGlobal++;
            } else if (itemRecogido.tipo == ItemState.ItemType.BASURA || itemRecogido.tipo == ItemState.ItemType.PIEZA_PLASTICO) {
                int puntaje = puntajesBasuraIndividuales.getOrDefault(idJugador, 0);
                puntajesBasuraIndividuales.put(idJugador, puntaje + 1);
                totalBasuraGlobal++;
                contaminationState.decrease(TRASH_CLEANUP_VALUE);
            }

            // Notificar al jugador específico sobre su nuevo puntaje.
            Network.PaqueteActualizacionPuntuacion paquetePuntaje = new Network.PaqueteActualizacionPuntuacion();
            paquetePuntaje.nuevosAnillos = puntajesAnillosIndividuales.getOrDefault(idJugador, 0);
            paquetePuntaje.nuevaBasura = puntajesBasuraIndividuales.getOrDefault(idJugador, 0);
            servidor.sendToTCP(idJugador, paquetePuntaje);

            // Notificar a TODOS los jugadores que el ítem ha desaparecido del mundo.
            Network.PaqueteItemEliminado paqueteEliminado = new Network.PaqueteItemEliminado();
            paqueteEliminado.idItem = idItem;
            servidor.sendToAllTCP(paqueteEliminado);
        }
    }

    private void actualizarEnemigosAI(float deltaTime) {
        // Si no hay jugadores en el servidor, no hay nada que hacer.
        if (jugadores.isEmpty()) {
            return;
        }

        // Iteramos sobre cada enemigo activo en el servidor.
        for (EnemigoState enemigo : enemigosActivos.values()) {
            // 1. Actualizamos el estado interno del enemigo (incluido su cooldown de ataque).
            enemigo.actualizar(deltaTime);

            // 2. Buscamos al jugador VIVO más cercano.
            PlayerState jugadorMasCercano = null;
            float distanciaMinima = Float.MAX_VALUE;

            for (PlayerState jugador : jugadores.values()) {
                // Los enemigos solo persiguen a jugadores que no han sido derrotados.
                if (jugador.vida > 0) {
                    float d = (float) Math.sqrt(Math.pow(jugador.x - enemigo.x, 2) + Math.pow(jugador.y - enemigo.y, 2));
                    if (d < distanciaMinima) {
                        distanciaMinima = d;
                        jugadorMasCercano = jugador;
                    }
                }
            }

            // Si no se encontró ningún jugador vivo, el enemigo se queda quieto.
            if (jugadorMasCercano == null) {
                enemigo.estadoAnimacion = enemigo.mirandoDerecha ? EnemigoState.EstadoEnemigo.IDLE_RIGHT : EnemigoState.EstadoEnemigo.IDLE_LEFT;
                continue; // Pasamos al siguiente enemigo.
            }

            // 3. Se aplica la lógica específica según el tipo de enemigo.

            // --- Lógica para Robotnik (Jefe) ---
            if (enemigo.tipo == EnemigoState.EnemigoType.ROBOTNIK) {
                float distanciaX = jugadorMasCercano.x - enemigo.x;
                float distanciaY = jugadorMasCercano.y - enemigo.y;

                if (distanciaMinima <= RANGO_DETENERSE_ROBOTNIK) {
                    // Si está en rango, ataca.
                    enemigo.estadoAnimacion = enemigo.mirandoDerecha ? EnemigoState.EstadoEnemigo.IDLE_RIGHT : EnemigoState.EstadoEnemigo.IDLE_LEFT;
                    if (enemigo.puedeAtacar()) {
                        enemigo.reiniciarCooldownAtaque();

                        int idJugador = jugadorMasCercano.id;
                        int anillosActuales = puntajesAnillosIndividuales.getOrDefault(idJugador, 0);

                        if (anillosActuales == 0) {
                            jugadorMasCercano.vida -= 15; // Daño aumentado sin anillos
                        } else {
                            jugadorMasCercano.vida -= 5;  // Daño normal del jefe con anillos
                        }

                        System.out.println("[SERVER] JEFE atacó a Jugador ID " + idJugador + ". Vida restante: " + jugadorMasCercano.vida);

                        // Notifica al jugador sobre su nueva vida.
                        Network.PaqueteActualizacionVida paqueteVida = new Network.PaqueteActualizacionVida();
                        paqueteVida.idJugador = jugadorMasCercano.id;
                        paqueteVida.nuevaVida = jugadorMasCercano.vida;
                        servidor.sendToTCP(jugadorMasCercano.id, paqueteVida);

                        // Comprueba si el jugador ha sido derrotado.
                        if (jugadorMasCercano.vida <= 0) {
                            comprobarDerrotaDelEquipo();
                            jugadores.remove(jugadorMasCercano.id);
                            Network.PaqueteEntidadEliminada notificacionMuerte = new Network.PaqueteEntidadEliminada();
                            notificacionMuerte.idEntidad = jugadorMasCercano.id;
                            notificacionMuerte.esJugador = true;
                            servidor.sendToAllTCP(notificacionMuerte);
                        }
                    }
                } else {
                    // Si está lejos, se mueve hacia el jugador.
                    Vector2 direccionDeseada = new Vector2(distanciaX, distanciaY).nor();
                    enemigo.x += direccionDeseada.x * VELOCIDAD_ROBOTNIK * deltaTime;
                    enemigo.y += direccionDeseada.y * VELOCIDAD_ROBOTNIK * deltaTime;

                    if (Math.abs(direccionDeseada.x) > 0.1f) {
                        enemigo.mirandoDerecha = direccionDeseada.x > 0;
                    }
                    enemigo.estadoAnimacion = enemigo.mirandoDerecha ? EnemigoState.EstadoEnemigo.RUN_RIGHT : EnemigoState.EstadoEnemigo.RUN_LEFT;
                }
                continue; // La lógica para el jefe termina aquí.
            }

            // --- Lógica para Robots Normales ---
            float dx = jugadorMasCercano.x - enemigo.x;
            float distance = distanciaMinima;

            // Si el enemigo ya está en su animación de ataque, no hacemos nada hasta que termine.
            if (enemigo.estadoAnimacion == EnemigoState.EstadoEnemigo.HIT_RIGHT || enemigo.estadoAnimacion == EnemigoState.EstadoEnemigo.HIT_LEFT) {
                continue;
            }

            EnemigoState.EstadoEnemigo estadoAnterior = enemigo.estadoAnimacion;

            if (distance <= ROBOT_ATTACK_RANGE) {
                // Si está en rango, ataca.
                if (Math.abs(dx) > 1.0f) {
                    enemigo.mirandoDerecha = dx > 0;
                }
                enemigo.estadoAnimacion = enemigo.mirandoDerecha ? EnemigoState.EstadoEnemigo.HIT_RIGHT : EnemigoState.EstadoEnemigo.HIT_LEFT;

                if (enemigo.puedeAtacar()) {
                    enemigo.reiniciarCooldownAtaque();

                    int idJugador = jugadorMasCercano.id;
                    int anillosActuales = puntajesAnillosIndividuales.getOrDefault(idJugador, 0);

                    if (anillosActuales == 0) {
                        jugadorMasCercano.vida -= 15; // Daño aumentado sin anillos
                    } else {
                        jugadorMasCercano.vida -= 1;  // Daño normal con anillos
                    }

                    System.out.println("[SERVER] Robot atacó a Jugador ID " + idJugador + ". Vida restante: " + jugadorMasCercano.vida);

                    Network.PaqueteActualizacionVida paqueteVida = new Network.PaqueteActualizacionVida();
                    paqueteVida.idJugador = jugadorMasCercano.id;
                    paqueteVida.nuevaVida = jugadorMasCercano.vida;
                    servidor.sendToTCP(jugadorMasCercano.id, paqueteVida);


                    if (jugadorMasCercano.vida <= 0) {
                        comprobarDerrotaDelEquipo();
                        jugadores.remove(jugadorMasCercano.id);
                        Network.PaqueteEntidadEliminada notificacionMuerte = new Network.PaqueteEntidadEliminada();
                        notificacionMuerte.idEntidad = jugadorMasCercano.id;
                        notificacionMuerte.esJugador = true;
                        servidor.sendToAllTCP(notificacionMuerte);
                    }
                }

            } else if (distance <= ROBOT_DETECTION_RANGE) {
                // Si está en rango de persecución, corre.
                if (Math.abs(dx) > 1.0f) {
                    enemigo.mirandoDerecha = dx > 0;
                }
                enemigo.estadoAnimacion = enemigo.mirandoDerecha ? EnemigoState.EstadoEnemigo.RUN_RIGHT : EnemigoState.EstadoEnemigo.RUN_LEFT;
            } else {
                // Si está lejos, se queda quieto.
                enemigo.estadoAnimacion = enemigo.mirandoDerecha ? EnemigoState.EstadoEnemigo.IDLE_RIGHT : EnemigoState.EstadoEnemigo.IDLE_LEFT;
            }

            // Si el estado ha cambiado, reinicia el temporizador de la animación.
            if (estadoAnterior != enemigo.estadoAnimacion) {
                enemigo.tiempoEnEstado = 0;
            }

            // Lógica de Movimiento (se ejecuta solo si el estado es RUN).
            if (enemigo.estadoAnimacion == EnemigoState.EstadoEnemigo.RUN_RIGHT || enemigo.estadoAnimacion == EnemigoState.EstadoEnemigo.RUN_LEFT) {
                float nextX = enemigo.x;
                float nextY = enemigo.y;
                if (dx > 0) nextX += ROBOT_SPEED; else if (dx < 0) nextX -= ROBOT_SPEED;
                float dy = jugadorMasCercano.y - enemigo.y; // Recalculamos dy aquí para el movimiento vertical.
                if (dy > 0) nextY += ROBOT_SPEED; else if (dy < 0) nextY -= ROBOT_SPEED;

                Rectangle robotBounds = new Rectangle(enemigo.x, enemigo.y, 48, 48);
                robotBounds.setX(nextX);
                if (!hayColision(robotBounds)) {
                    enemigo.x = nextX;
                }
                robotBounds.setX(enemigo.x);
                robotBounds.setY(nextY);
                if (!hayColision(robotBounds)) {
                    enemigo.y = nextY;
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

        if (alMenosUnJugadorHaEnviadoPosicion) {
            actualizarDrones(deltaTime);
            actualizarEnemigosAI(deltaTime);
            actualizarEstadoAnimalesPorContaminacion(deltaTime);
        }
        generarNuevosItems(deltaTime);
        generarEnemigosControlados(deltaTime);

        // Creamos un nuevo paquete y le metemos la lista completa de enemigos.
        Network.PaqueteActualizacionEnemigos paqueteUpdate = new Network.PaqueteActualizacionEnemigos();
        paqueteUpdate.estadosEnemigos = this.enemigosActivos;
        servidor.sendToAllTCP(paqueteUpdate);

        if (animalesActivos != null && !animalesActivos.isEmpty()) {
            Network.PaqueteActualizacionAnimales paqueteAnimales = new Network.PaqueteActualizacionAnimales();
            paqueteAnimales.estadosAnimales = this.animalesActivos; // Usa el mapa principal directamente
            servidor.sendToAllTCP(paqueteAnimales);
        }

        for (Integer jugadorId : cooldownsHabilidadLimpieza.keySet()) {
            float cooldownActual = cooldownsHabilidadLimpieza.get(jugadorId);
            if (cooldownActual > 0) {
                cooldownsHabilidadLimpieza.put(jugadorId, cooldownActual - deltaTime);
            }
        }
    }

    private void comprobarYGenerarPortalSiCorresponde() {
        // 1. Si el portal ya se generó en este mapa, no hacemos nada más.
        if (teleportGenerado) {
            return;
        }

        // 2. Obtenemos el límite de enemigos para el mapa actual.
        int limiteEnemigos = enemigosPorMapa.getOrDefault(mapaActualServidor, 0);

        // 3. LA CONDICIÓN CLAVE:
        //    Comprobamos si la lista de enemigos activos está vacía Y
        //    si ya hemos generado todos los enemigos que correspondían a este nivel.
        if (enemigosActivos.isEmpty() && enemigosGeneradosEnNivelActual >= limiteEnemigos) {

            System.out.println("[GAMESERVER] ¡Todos los enemigos derrotados! Generando portal de salida.");

            // 4. GENERAMOS LOS PORTALES:
            //    Reutilizamos la información que el cliente nos envió al cargar el mapa.
            if (infoPortales != null && !infoPortales.isEmpty()) {
                for (Network.PortalInfo info : infoPortales.values()) {
                    // Creamos el estado del ítem para el portal.
                    ItemState estadoPortal = new ItemState(proximoIdItem++, info.x, info.y, ItemState.ItemType.TELETRANSPORTE);

                    // Lo añadimos a la lista de ítems del servidor.
                    itemsActivos.put(estadoPortal.id, estadoPortal);

                    // Creamos el paquete para notificar a TODOS los clientes.
                    Network.PaqueteItemNuevo paqueteNuevoItem = new Network.PaqueteItemNuevo();
                    paqueteNuevoItem.estadoItem = estadoPortal;
                    servidor.sendToAllTCP(paqueteNuevoItem);
                }
            }

            // 5. ¡Activamos el seguro para no volver a generar el portal en este nivel!
            teleportGenerado = true;
        }
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

    private void generarEnemigosControlados(float deltaTime) {
        // Si no sabemos en qué mapa estamos, no generamos nada.
        if (mapaActualServidor == null || mapaActualServidor.isEmpty()) {
            return;
        }

        int limiteEnemigos = enemigosPorMapa.getOrDefault(mapaActualServidor, 0);

        // Si ya hemos generado todos los enemigos para este nivel, no hacemos nada más.
        if (enemigosGeneradosEnNivelActual >= limiteEnemigos) {
            return;
        }

        // Si todavía no hemos alcanzado el límite, aplicamos el temporizador.
        tiempoGeneracionEnemigo += deltaTime;
        if (tiempoGeneracionEnemigo >= INTERVALO_GENERACION_ENEMIGO) {

            // Intentamos generar un nuevo enemigo usando el método que ahora devuelve boolean.
            boolean enemigoGenerado = spawnNuevoEnemigo();

            // Si se pudo generar con éxito...
            if (enemigoGenerado) {
                enemigosGeneradosEnNivelActual++; // Incrementamos el contador de este nivel.
                System.out.println("[GAMESERVER] Enemigo generado (" + enemigosGeneradosEnNivelActual + "/" + limiteEnemigos + ")");
            }

            // Reiniciamos el temporizador en cualquier caso, para no intentarlo en cada frame.
            tiempoGeneracionEnemigo = 0f;
        }
    }

    private void generarBloquesParaElNivel() {
        // Limpiamos los bloques del nivel anterior.
        bloquesRompibles.clear();

        // MUY IMPORTANTE: Si aún no tenemos el plano, no podemos generar nada.
        if (paredesDelMapa == null) {
            System.out.println("[SERVER] No se pueden generar bloques. Esperando el plano del mapa del cliente...");
            return;
        }

        Random random = new Random();
        int cantidad = 5;
        float anchoMapa = 1920f;
        float altoMapa = 1280f;

        System.out.println("[SERVER] Generando " + cantidad + " bloques usando el plano del mapa...");
        for (int i = 0; i < cantidad; i++) {
            int intentos = 0;
            boolean colocado = false;
            while (!colocado && intentos < 100) {
                float x = random.nextFloat() * (anchoMapa - 100f);
                float y = random.nextFloat() * (altoMapa - 100f);
                Rectangle bounds = new Rectangle(x, y, 100f, 100f);

                // Comprobamos si el nuevo bloque choca con alguna pared del plano.
                boolean hayColision = false;
                for (Rectangle pared : paredesDelMapa) {
                    if (pared.overlaps(bounds)) {
                        hayColision = true;
                        break;
                    }
                }

                if (!hayColision) {
                    bloquesRompibles.put(proximoIdBloque++, bounds);
                    colocado = true;
                }
                intentos++;
            }
        }
        System.out.println("[SERVER] Bloques generados. Total: " + bloquesRompibles.size());
    }
    private void sincronizarBloquesConClientes() {
        Network.PaqueteSincronizarBloques paqueteSync = new Network.PaqueteSincronizarBloques();
        paqueteSync.todosLosBloques = new HashMap<>(this.bloquesRompibles); // Enviamos una copia
        servidor.sendToAllTCP(paqueteSync);
        System.out.println("[SERVER] Enviando estado de bloques a todos los clientes.");
    }

    private boolean spawnNuevoEnemigo() {
        // Si todavía no hemos recibido el plano del mapa, no generamos nada.
        if (paredesDelMapa == null) {
            System.out.println("[SERVER] Aún no tengo el plano del mapa, no puedo generar enemigos.");
            return false;
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
                EnemigoState nuevoEstado = new EnemigoState(proximoIdEnemigo++, bounds.x, bounds.y, 4, EnemigoState.EnemigoType.ROBOT);
                enemigosActivos.put(nuevoEstado.id, nuevoEstado);
                Network.PaqueteEnemigoNuevo paquete = new Network.PaqueteEnemigoNuevo();
                paquete.estadoEnemigo = nuevoEstado;
                servidor.sendToAllTCP(paquete);
                colocado = true;
            }
            intentos++;
        }
        return colocado;
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
    private void actualizarDrones(float deltaTime) {
        if (dronesActivos.isEmpty()) return;

        // Estas son las mismas constantes de tu clase Dron_Tails para calcular la posición correcta.
        final float OFFSET_X = -60;
        final float OFFSET_Y = 50;

        DronState primerDron = dronesActivos.values().iterator().next();
        System.out.println("[SERVER DEBUG] Tiempo restante del dron de jugador " + primerDron.ownerId + ": " + String.format("%.2f", primerDron.temporizador));

        ArrayList<Integer> dronesParaEliminar = new ArrayList<>();
        for (DronState dron : dronesActivos.values()) {
            // El metodo update del dron ahora devuelve 'true' si su tiempo ha terminado
            if (dron.update(deltaTime)) {
                PlayerState propietario = jugadores.get(dron.ownerId);
                if (propietario != null) {
                    // 1. Calculamos la posición correcta del árbol usando la posición del jugador + el offset del dron.
                    float arbolX = propietario.x + OFFSET_X;
                    float arbolY = propietario.y + OFFSET_Y;

                    // 2. Usamos esa posición corregida para crear el hitbox de prueba.
                    Rectangle hitboxArbol = new Rectangle(arbolX, arbolY, 64, 64);

                    // Verificamos colisión usando el método centralizado del servidor
                    if (hayColision(hitboxArbol)) {
                        // LUGAR OCUPADO: Enviar mensaje de error SOLO al propietario
                        Network.PaqueteMensajeUI paqueteMsg = new Network.PaqueteMensajeUI();
                        paqueteMsg.mensaje = "Lugar no apto para sembrar";
                        servidor.sendToTCP(dron.ownerId, paqueteMsg);
                    } else {

                        // 1. Añadimos la colisión al mundo del servidor
                        colisionesDinamicas.add(hitboxArbol);

                        // 2. Ordenamos a TODOS los clientes que creen el árbol visual
                        Network.PaqueteArbolNuevo paqueteArbol = new Network.PaqueteArbolNuevo();
                        paqueteArbol.x = arbolX;
                        paqueteArbol.y = arbolY;
                        servidor.sendToAllTCP(paqueteArbol);

                        // 3. Enviamos el mensaje de éxito SOLO al propietario
                        Network.PaqueteMensajeUI paqueteMsg = new Network.PaqueteMensajeUI();
                        paqueteMsg.mensaje = "¡Árbol sembrado!";
                        contaminationState.decrease(10);
                        servidor.sendToTCP(dron.ownerId, paqueteMsg);
                    }
                }
                // Marcamos el dron para ser eliminado de la lista de activos
                dronesParaEliminar.add(dron.ownerId);
            }
        }
        // Limpiamos los drones que han terminado su ciclo
        for (Integer id : dronesParaEliminar) {
            dronesActivos.remove(id);
        }
    }

    private void finalizarPartidaYEnviarResultados() {
        System.out.println("[GAMESERVER] ¡VICTORIA! Finalizando partida y enviando resultados a todos...");

        Network.PaqueteResultadosFinales paqueteResultados = new Network.PaqueteResultadosFinales();
        // Creamos una nueva lista a partir de los valores del HashMap de estadísticas.
        paqueteResultados.estadisticasFinales = new ArrayList<>(estadisticasJugadores.values());

        // Enviamos el paquete final a TODOS los clientes conectados.
        servidor.sendToAllTCP(paqueteResultados);

        // Opcional: podrías añadir aquí una lógica para reiniciar el servidor.
    }

    private boolean hayColision(Rectangle bounds) {
        if (paredesDelMapa != null) {
            for (Rectangle pared : paredesDelMapa) {
                if (pared.overlaps(bounds)) return true;
            }
        }


        if (!colisionesDinamicas.isEmpty()) {
            System.out.println("[SERVER DEBUG] Comprobando colisión contra " + colisionesDinamicas.size() + " árbol(es)."); // Descomenta esta línea si quieres ver un log muy verboso
            for (Rectangle obstaculo : colisionesDinamicas) {
                if (obstaculo.overlaps(bounds)) {
                    return true;
                }
            }
        }
        // 3. Comprobación contra los bloques de basura de Knuckles
        // Nos aseguramos de que la lista exista y no esté vacía.
        if (bloquesRompibles != null && !bloquesRompibles.isEmpty()) {
            // Iteramos sobre cada 'Rectangle' de bloque que el servidor tiene en memoria.
            for (Rectangle bloque : bloquesRompibles.values()) {
                // Si el 'bounds' del robot se superpone con el del bloque...
                if (bloque.overlaps(bounds)) {
                    return true; // ¡Hay colisión! El robot no puede moverse aquí.
                }
            }
        }

        return false;
    }

    private void generarAnimales() {
        // No podemos generar animales si el servidor aún no conoce el mapa.
        if (paredesDelMapa == null) {
            System.out.println("[SERVER] No se pueden generar animales. Esperando el plano del mapa del cliente...");
            return;
        }

        animalesActivos.clear();
        proximoIdAnimal = 20000;
        muertesAnimalesActivas = false;
        tiempoParaProximaMuerteAnimal = 20f;

        int cantidadAnimales = 10; // La cantidad de animales que quieres por mapa.
        Random random = new Random();
        float anchoMapa = 1920f;  // Debes tener estas dimensiones accesibles.
        float altoMapa = 1280f; // O pasarlas como parámetro si varían.

        for (int i = 0; i < cantidadAnimales; i++) {
            int intentos = 0;
            boolean colocado = false;
            while (!colocado && intentos < 100) {
                float x = random.nextFloat() * (anchoMapa - 32);
                float y = random.nextFloat() * (altoMapa - 32);
                Rectangle animalBounds = new Rectangle(x, y, 32, 32);

                if (!hayColision(animalBounds)) { // Usamos tu método de colisión existente
                    String texturaPath = "Items/Conejo1.png"; // El cliente usará esta ruta
                    AnimalState nuevoAnimal = new AnimalState(proximoIdAnimal++, x, y, texturaPath);
                    animalesActivos.put(nuevoAnimal.id, nuevoAnimal);
                    colocado = true;
                }
                intentos++;
            }
        }

        System.out.println("[SERVER] Generados " + animalesActivos.size() + " animales para el nuevo nivel.");

        // Una vez generados, enviamos la lista completa a TODOS los clientes.
        if (!animalesActivos.isEmpty()) {
            Network.PaqueteActualizacionAnimales paqueteInicial = new Network.PaqueteActualizacionAnimales();
            paqueteInicial.estadosAnimales = new HashMap<>(this.animalesActivos);
            servidor.sendToAllTCP(paqueteInicial);
            System.out.println("[SERVER] Enviando estado inicial de animales a todos los clientes.");
        }
    }

    private void matarSiguienteAnimalVivo() {
        for (AnimalState animal : animalesActivos.values()) {
            if (animal.estaVivo) {
                animal.estaVivo = false; // El estado del animal cambia en el servidor.
                System.out.println("[SERVER] Contaminación alta. Matando animal ID: " + animal.id);

                // Notificamos a TODOS los clientes sobre el estado actualizado del animal.
                // Usamos el mismo paquete para consistencia.
                Network.PaqueteActualizacionAnimales paquete = new Network.PaqueteActualizacionAnimales();
                paquete.estadosAnimales = new HashMap<>(this.animalesActivos);
                servidor.sendToAllTCP(paquete);

                return; // Salimos del bucle una vez que matamos a uno.
            }
        }
    }

    /**
     * Revisa el nivel de contaminación y gestiona la muerte secuencial de animales.
     */
    private void actualizarEstadoAnimalesPorContaminacion(float deltaTime) {
        // Si no hay animales, no hay nada que hacer.
        if (animalesActivos.isEmpty()) return;

        if (contaminationState.getPercentage() >= 50) {
            if (!muertesAnimalesActivas) {
                muertesAnimalesActivas = true;
                matarSiguienteAnimalVivo();
                tiempoParaProximaMuerteAnimal = 20f;
            } else {
                tiempoParaProximaMuerteAnimal -= deltaTime;
                if (tiempoParaProximaMuerteAnimal <= 0) {
                    matarSiguienteAnimalVivo();
                    tiempoParaProximaMuerteAnimal = 20f;
                }
            }
        } else {
            if (muertesAnimalesActivas) {
                muertesAnimalesActivas = false;
                tiempoParaProximaMuerteAnimal = 20f;
            }
        }
    }

// --- [CAMBIO PROFESOR] --- Nuevo método privado para centralizar la lógica
    /**
     * Centraliza toda la lógica de limpieza y notificación cuando un jugador
     * se desconecta, ya sea voluntariamente o por pérdida de conexión.
     * @param conexion La conexión del jugador que se va.
     */
    private void desconectarJugador(Connection conexion) {
        int jugadorId = conexion.getID();

        // 1. OBTENEMOS EL ESTADO DEL JUGADOR ANTES DE BORRARLO.
        PlayerState jugadorDesconectado = jugadores.get(jugadorId);

        // 2. SI EL JUGADOR REALMENTE EXISTÍA EN NUESTRA LISTA...
        if (jugadorDesconectado != null) {
            // 3. LIBERAMOS SU PERSONAJE.
            personajesEnUso.remove(jugadorDesconectado.characterType);
            System.out.println("[SERVER] El personaje " + jugadorDesconectado.characterType + " ha sido liberado.");

            // 4. CREAMOS EL PAQUETE DE NOTIFICACIÓN PARA LOS DEMÁS.
            Network.PaqueteJugadorDesconectado paqueteNotificacion = new Network.PaqueteJugadorDesconectado();
            paqueteNotificacion.idJugador = jugadorId;

            // 5. ENVIAMOS LA NOTIFICACIÓN A TODOS LOS DEMÁS JUGADORES.
            servidor.sendToAllExceptTCP(jugadorId, paqueteNotificacion); // Usamos sendToAllExcept para no enviárselo a sí mismo.
            System.out.println("[SERVER] Notificando a los clientes restantes sobre la desconexión del jugador " + jugadorId);
        }

        // 6. FINALMENTE, LIMPIAMOS TODOS LOS DATOS DEL JUGADOR DEL SERVIDOR.
        jugadores.remove(jugadorId);
        puntajesAnillosIndividuales.remove(jugadorId);
        puntajesBasuraIndividuales.remove(jugadorId);
        cooldownsHabilidadLimpieza.remove(jugadorId);

        // 7. CERRAMOS LA CONEXIÓN (si no estuviera ya cerrada).
        // Esto asegura que el servidor no mantenga conexiones inactivas.
        conexion.close();

        if (jugadores.isEmpty()) {
            // Si no quedan jugadores, llamamos al método para reiniciar todo el estado del juego.
            reiniciarServidor();
        }
    }

    private void comprobarDerrotaDelEquipo() {
        // Asumimos que la partida no ha terminado todavía.
        boolean unJugadorHaSidoDerrotado = false;

        // Recorremos a todos los jugadores conectados.
        for (PlayerState jugador : jugadores.values()) {
            // Si encontramos al menos UN jugador cuya vida sea 0 o menos...
            if (jugador.vida <= 0) {
                // ...la condición se cumple.
                unJugadorHaSidoDerrotado = true;
                break; // Salimos del bucle, ya no necesitamos buscar más.
            }
        }

        // Si, después de revisar, encontramos un jugador derrotado...
        if (unJugadorHaSidoDerrotado) {
            System.out.println("[GAMESERVER] ¡Un miembro del equipo ha caído! Enviando orden de Game Over.");

            // Creamos y enviamos el paquete de derrota a todos los clientes.
            servidor.sendToAllTCP(new Network.PaqueteGameOver());
        }
    }

    private void reiniciarContadoresDeNivel() {
        System.out.println("[GAMESERVER] Reiniciando contadores para el nuevo mapa: " + this.mapaActualServidor);

        // Limpiamos todas las listas de entidades del mapa anterior
        this.enemigosActivos.clear();
        this.itemsActivos.clear();
        this.infoPortales.clear();
        this.animalesActivos.clear();
        this.bloquesRompibles.clear();

        // Reiniciamos contadores y temporizadores
        this.teleportGenerado = false;
        this.tiempoGeneracionTeleport = 0f;
        this.tiempoGeneracionEnemigo = 0f;
        this.enemigosGeneradosEnNivelActual = 0;
        this.tiempoSpawnAnillo = 0f;
        this.tiempoSpawnBasura = 0f;
        this.tiempoSpawnPlastico = 0f;

        // MUY IMPORTANTE: Forzamos a que el primer cliente del nuevo mapa envíe su plano.
        this.paredesDelMapa = null;
    }

    /**
     * Reinicia el servidor completamente, limpiando todo el estado del juego
     * y volviendo a iniciar una nueva instancia del servidor KryoNet.
     */
    private void reiniciarServidor() {
        System.out.println("------------------------------------------------------");
        System.out.println("[SERVER RESTART] No hay jugadores conectados. Reiniciando servidor...");
        System.out.println("------------------------------------------------------");

        // --- 1. DETENER EL HILO DE LÓGICA DE JUEGO ---
        serverThreadActive = false; // Le decimos al bucle 'while' que debe parar
        try {
            if (gameLoopThread != null) {
                gameLoopThread.join(1000); // Esperamos hasta 1 segundo a que el hilo muera
            }
        } catch (InterruptedException e) {
            System.err.println("[SERVER RESTART] El hilo fue interrumpido mientras se esperaba su finalización.");
            Thread.currentThread().interrupt();
        }
        System.out.println("[SERVER RESTART] Hilo de juego anterior detenido.");

        // --- 2. DETENER Y CERRAR LA INSTANCIA ACTUAL DEL SERVIDOR KRYONET ---
        if (servidor != null) {
            servidor.close(); // Cierra las conexiones y libera el puerto
            servidor.stop();
            System.out.println("[SERVER RESTART] Instancia de KryoNet detenida y liberada.");
        }

        // --- 3. LIMPIEZA DE TODO EL ESTADO DEL JUEGO ---
        jugadores.clear();
        personajesEnUso.clear();
        enemigosActivos.clear();
        itemsActivos.clear();
        paredesDelMapa = null;
        puntajesAnillosIndividuales.clear();
        puntajesBasuraIndividuales.clear();
        dronesActivos.clear();
        colisionesDinamicas.clear();
        infoPortales.clear();
        bloquesRompibles.clear();
        animalesActivos.clear();
        cooldownsHabilidadLimpieza.clear();
        estadisticasJugadores.clear();

        // -- Reinicio de IDs de entidades
        proximoIdDron = 20000;
        proximoIdBloque = 30000;
        proximoIdEnemigo = 0;
        proximoIdItem = 0;
        proximoIdAnimal = 20000;

        // -- Reinicio de estado global
        contaminationState.decrease(1000);
        totalAnillosGlobal = 0;
        totalBasuraGlobal = 0;
        esmeraldasRecogidasGlobal = 0;
        basuraReciclada = 0;

        // -- Reinicio de flags y contadores
        alMenosUnJugadorHaEnviadoPosicion = false;
        enemigosGeneradosEnNivelActual = 0;
        mapaActualServidor = "";
        teleportGenerado = false;

        System.out.println("[SERVER RESTART] Estado de la partida limpiado.");

        // --- 4. INICIAR UNA INSTANCIA COMPLETAMENTE NUEVA DEL SERVIDOR ---
        iniciarInstanciaServidor();
    }

    private void iniciarInstanciaServidor() {
        System.out.println("[SERVER BOOT] Creando nueva instancia del servidor...");
        // 1. CREACIÓN Y CONFIGURACIÓN DEL SERVIDOR
        servidor = new Server(); // Creamos un objeto Server NUEVO
        Network.registrar(servidor);

        // 2. CONFIGURACIÓN DE LOS LISTENERS (Eventos de conexión, recepción, etc.)
        servidor.addListener(new Listener() {
            // --- COPIA AQUÍ TODO EL CONTENIDO DE TU .addListener() ORIGINAL ---
            // Desde: public void connected(Connection conexion) { ... }
            // Hasta la llave de cierre: }
            public void connected(Connection conexion) {
                System.out.println("[SERVER] Un cliente se ha conectado: ID " + conexion.getID());
                PlayerState nuevoEstado = new PlayerState();
                nuevoEstado.id = conexion.getID();
                nuevoEstado.x = 100;
                nuevoEstado.y = 100;

                if (!personajesEnUso.contains(PlayerState.CharacterType.SONIC)) {
                    nuevoEstado.characterType = PlayerState.CharacterType.SONIC;
                } else if (!personajesEnUso.contains(PlayerState.CharacterType.TAILS)) {
                    nuevoEstado.characterType = PlayerState.CharacterType.TAILS;
                } else if (!personajesEnUso.contains(PlayerState.CharacterType.KNUCKLES)) {
                    nuevoEstado.characterType = PlayerState.CharacterType.KNUCKLES;
                } else {
                    System.out.println("[SERVER] Intento de conexión rechazado: No hay personajes disponibles.");
                    conexion.close();
                    return;

                }

                personajesEnUso.add(nuevoEstado.characterType);
                System.out.println("[SERVER] Asignado " + nuevoEstado.characterType + " al jugador " + nuevoEstado.id);

                jugadores.put(conexion.getID(), nuevoEstado);
                puntajesAnillosIndividuales.put(conexion.getID(), 0);
                puntajesBasuraIndividuales.put(conexion.getID(), 0);
                cooldownsHabilidadLimpieza.put(conexion.getID(), 0f);

                EstadisticasJugador stats = new EstadisticasJugador("Jugador " + nuevoEstado.characterType.toString());
                estadisticasJugadores.put(conexion.getID(), stats);
                System.out.println("[STATS] Objeto de estadísticas creado para el jugador " + nuevoEstado.characterType.toString());

                Network.PaqueteTuID paqueteID = new Network.PaqueteTuID();
                paqueteID.id = conexion.getID();
                conexion.sendTCP(paqueteID);
            }

            public void received(Connection conexion, Object objeto) {
                if (objeto instanceof Network.PaqueteSalidaDePartida) {
                    System.out.println("[SERVER] Recibida notificación de salida voluntaria del jugador ID: " + conexion.getID());

                    desconectarJugador(conexion);
                    return;
                }
                if (objeto instanceof Network.SolicitudAccesoPaquete) {
                    Network.SolicitudAccesoPaquete solicitud = (Network.SolicitudAccesoPaquete) objeto;
                    System.out.println("[SERVER] Recibida solicitud de acceso del jugador: " + solicitud.nombreJugador);


                    PlayerState estadoAsignado = jugadores.get(conexion.getID());
                    if (estadoAsignado == null) return;


                    estadoAsignado.nombreJugador = solicitud.nombreJugador;
                    EstadisticasJugador stats = estadisticasJugadores.get(conexion.getID());
                    if (stats != null) {
                        stats.setNombreJugador(solicitud.nombreJugador);
                    }


                    Network.RespuestaAccesoPaquete respuesta = new Network.RespuestaAccesoPaquete();
                    respuesta.mensajeRespuesta = "¡Bienvenido, " + solicitud.nombreJugador + "!";
                    respuesta.tuEstado = estadoAsignado;
                    conexion.sendTCP(respuesta);

                    Network.PaqueteJugadorConectado packetNuevoJugador = new Network.PaqueteJugadorConectado();
                    packetNuevoJugador.nuevoJugador = estadoAsignado;
                    servidor.sendToAllExceptTCP(conexion.getID(), packetNuevoJugador);


                    for (PlayerState jugadorExistente : jugadores.values()) {
                        if (jugadorExistente.id != conexion.getID() && jugadorExistente.characterType != null) {
                            Network.PaqueteJugadorConectado packetJugadorExistente = new Network.PaqueteJugadorConectado();
                            packetJugadorExistente.nuevoJugador = jugadorExistente;
                            conexion.sendTCP(packetJugadorExistente);
                        }
                    }
                }
                if (objeto instanceof Network.PaquetePosicionJugador paquete) {
                    PlayerState estadoJugador = jugadores.get(paquete.id);

                    if (estadoJugador != null) {
                        estadoJugador.x = paquete.x;
                        estadoJugador.y = paquete.y;
                        estadoJugador.estadoAnimacion = paquete.estadoAnimacion;
                        servidor.sendToAllExceptTCP(conexion.getID(), paquete);

                        if (!alMenosUnJugadorHaEnviadoPosicion) {
                            System.out.println("[SERVER] Primera posición real recibida. ¡La IA puede comenzar!");
                            alMenosUnJugadorHaEnviadoPosicion = true;
                        }
                    }
                }
                if (objeto instanceof Network.PaqueteSolicitudRecogerItem paquete) {
                    synchronized (itemsActivos) {
                        ItemState itemRecogido = itemsActivos.get(paquete.idItem);


                        if (itemRecogido != null) {

                            if (itemRecogido.tipo == ItemState.ItemType.ESMERALDA) {

                                itemsActivos.remove(paquete.idItem);
                                esmeraldasRecogidasGlobal++;
                                System.out.println("[GAMESERVER] ¡Esmeralda recogida! Total global: " + esmeraldasRecogidasGlobal);


                                Network.PaqueteActualizacionEsmeraldas paqueteEsmeraldas = new Network.PaqueteActualizacionEsmeraldas();
                                paqueteEsmeraldas.totalEsmeraldas = esmeraldasRecogidasGlobal;
                                servidor.sendToAllTCP(paqueteEsmeraldas);


                                Network.PaqueteItemEliminado paqueteEliminado = new Network.PaqueteItemEliminado();
                                paqueteEliminado.idItem = paquete.idItem;
                                servidor.sendToAllTCP(paqueteEliminado);

                                if (esmeraldasRecogidasGlobal >= 7) {
                                    System.out.println("[GAMESERVER] ¡LAS 7 ESMERALDAS REUNIDAS! Activando Super Sonic...");


                                    for (PlayerState jugador : jugadores.values()) {
                                        if (jugador.characterType == PlayerState.CharacterType.SONIC) {


                                            jugador.isSuper = true;

                                            jugador.vida = Player.MAX_VIDA;
                                            System.out.println("[GAMESERVER] Vida del jugador " + jugador.id + " restaurada al máximo: " + jugador.vida);


                                            Network.PaqueteActualizacionVida paqueteVida = new Network.PaqueteActualizacionVida();
                                            paqueteVida.idJugador = jugador.id;
                                            paqueteVida.nuevaVida = jugador.vida;
                                            servidor.sendToTCP(jugador.id, paqueteVida);


                                            Network.PaqueteTransformacionSuper paqueteSuper = new Network.PaqueteTransformacionSuper();
                                            paqueteSuper.idJugador = jugador.id;
                                            paqueteSuper.esSuper = true;

                                            servidor.sendToAllTCP(paqueteSuper);
                                            System.out.println("[GAMESERVER] Notificando a todos que el jugador " + jugador.id + " es ahora Super Sonic.");
                                        }
                                    }
                                }

                            }

                            else if (itemRecogido.tipo == ItemState.ItemType.TELETRANSPORTE) {

                                System.out.println("[SERVER] Jugador " + conexion.getID() + " ha activado el teletransportador.");


                                String claveCoordenadas = itemRecogido.x + "," + itemRecogido.y;
                                Network.PortalInfo infoDestino = infoPortales.get(claveCoordenadas);

                                if (infoDestino == null) {
                                    System.err.println("[SERVER] Error: No se encontró información de destino para el portal en las coordenadas: " + claveCoordenadas);
                                    return;
                                }


                                itemsActivos.remove(paquete.idItem);

                                infoPortales.remove(claveCoordenadas);

                                Network.PaqueteOrdenCambiarMapa orden = new Network.PaqueteOrdenCambiarMapa();
                                orden.nuevoMapa = infoDestino.destinoMapa;
                                mapaActualServidor = orden.nuevoMapa;
                                reiniciarContadoresDeNivel();
                                servidor.sendToAllTCP(orden);

                                enemigosActivos.clear();

                                System.out.println("[SERVER] Enviando paquetes de posición autoritativos para forzar la sincronización.");
                                for (PlayerState jugador : jugadores.values()) {
                                    Network.PaquetePosicionJugador paquetePosicion = new Network.PaquetePosicionJugador();
                                    paquetePosicion.id = jugador.id;
                                    paquetePosicion.x = jugador.x;
                                    paquetePosicion.y = jugador.y;
                                    paquetePosicion.estadoAnimacion = jugador.estadoAnimacion;
                                    servidor.sendToAllTCP(paquetePosicion);
                                }

                                if (orden.nuevoMapa.contains("ZonaJefe")) {
                                    System.out.println("[SERVER] Detectado mapa de jefe. ¡Creando a Robotnik!");

                                    EnemigoState estadoRobotnik = new EnemigoState(999, 300, 100, 100, EnemigoState.EnemigoType.ROBOTNIK);
                                    enemigosActivos.put(estadoRobotnik.id, estadoRobotnik);

                                    Network.PaqueteEnemigoNuevo paqueteRobotnik = new Network.PaqueteEnemigoNuevo();
                                    paqueteRobotnik.estadoEnemigo = estadoRobotnik;
                                    servidor.sendToAllTCP(paqueteRobotnik);
                                }

                                Network.PaqueteItemEliminado paqueteEliminado = new Network.PaqueteItemEliminado();
                                paqueteEliminado.idItem = paquete.idItem;
                                servidor.sendToAllTCP(paqueteEliminado);

                                paredesDelMapa = null;
                            }
                            else {
                                itemsActivos.remove(paquete.idItem);
                                System.out.println("[SERVER] Ítem con ID " + paquete.idItem + " recogido por jugador " + conexion.getID());

                                if (itemRecogido.tipo == ItemState.ItemType.ANILLO) {
                                    int puntajeActual = puntajesAnillosIndividuales.getOrDefault(conexion.getID(), 0);
                                    puntajesAnillosIndividuales.put(conexion.getID(), puntajeActual + 1);


                                    totalAnillosGlobal++;
                                    System.out.println("[SERVER] Anillo recogido. Total de equipo: " + totalAnillosGlobal);

                                    int anillosAhora = puntajesAnillosIndividuales.get(conexion.getID());
                                    if (anillosAhora >= 100) {
                                        System.out.println("[GAMESERVER] Jugador " + conexion.getID() + " tiene 100 anillos. Canjeando por vida.");

                                        puntajesAnillosIndividuales.put(conexion.getID(), anillosAhora - 100);

                                        PlayerState estadoJugador = jugadores.get(conexion.getID());
                                        if (estadoJugador != null) {
                                            estadoJugador.vida = Math.min(estadoJugador.vida + 100, Player.MAX_VIDA);

                                            Network.PaqueteActualizacionVida paqueteVida = new Network.PaqueteActualizacionVida();
                                            paqueteVida.idJugador = conexion.getID();
                                            paqueteVida.nuevaVida = estadoJugador.vida;
                                            servidor.sendToTCP(conexion.getID(), paqueteVida);
                                        }
                                    }

                                } else if (itemRecogido.tipo == ItemState.ItemType.BASURA || itemRecogido.tipo == ItemState.ItemType.PIEZA_PLASTICO) {
                                    int puntajeActual = puntajesBasuraIndividuales.getOrDefault(conexion.getID(), 0);
                                    puntajesBasuraIndividuales.put(conexion.getID(), puntajeActual + 1);

                                    totalBasuraGlobal++;

                                    contaminationState.decrease(TRASH_CLEANUP_VALUE);
                                    System.out.println("[SERVER] Basura recogida. Total de equipo: " + totalBasuraGlobal +
                                        ". Contaminación GLOBAL reducida a: " + String.format("%.2f", contaminationState.getPercentage()) + "%!");

                                    Network.PaqueteActualizacionContaminacion paqueteContaminacion = new Network.PaqueteActualizacionContaminacion();
                                    paqueteContaminacion.contaminationPercentage = contaminationState.getPercentage();
                                    servidor.sendToAllTCP(paqueteContaminacion);
                                }


                                Network.PaqueteActualizacionPuntuacion paquetePuntaje = new Network.PaqueteActualizacionPuntuacion();
                                paquetePuntaje.nuevosAnillos = puntajesAnillosIndividuales.get(conexion.getID());
                                paquetePuntaje.nuevaBasura = puntajesBasuraIndividuales.get(conexion.getID());
                                conexion.sendTCP(paquetePuntaje);


                                Network.PaqueteItemEliminado paqueteEliminado = new Network.PaqueteItemEliminado();
                                paqueteEliminado.idItem = paquete.idItem;
                                servidor.sendToAllTCP(paqueteEliminado);
                            }
                        }
                    }
                }
                if (objeto instanceof Network.PaqueteAnimacionEnemigoTerminada paquete) {
                    EnemigoState enemigo = enemigosActivos.get(paquete.idEnemigo);
                    if (enemigo != null) {
                        enemigo.estadoAnimacion = EnemigoState.EstadoEnemigo.POST_ATAQUE;
                        enemigo.tiempoEnEstado = 0;
                    }
                }
                if (objeto instanceof Network.PaqueteInformacionMapa paquete) {

                    System.out.println("[SERVER] <== PAQUETE DE MAPA RECIBIDO!");
                    if (paredesDelMapa == null) {
                        paredesDelMapa = paquete.paredes;

                        if (mapaActualServidor == null || mapaActualServidor.isEmpty()) {
                            mapaActualServidor = paquete.nombreMapa;
                            System.out.println("[GAMESERVER] Establecido mapa inicial a: " + mapaActualServidor);
                        }
                        System.out.println("[SERVER] Plano del mapa recibido con " + paredesDelMapa.size() + " paredes.");

                        if (paquete.posEsmeralda != null) {
                            System.out.println("[GAMESERVER] Posición de esmeralda recibida. Generando ítem en el mapa.");

                            ItemState estadoEsmeralda = new ItemState(proximoIdItem++, paquete.posEsmeralda.x, paquete.posEsmeralda.y, ItemState.ItemType.ESMERALDA);
                            itemsActivos.put(estadoEsmeralda.id, estadoEsmeralda);


                            Network.PaqueteItemNuevo paqueteItem = new Network.PaqueteItemNuevo();
                            paqueteItem.estadoItem = estadoEsmeralda;
                            servidor.sendToAllTCP(paqueteItem);
                        }

                        if (paquete.portales != null && !paquete.portales.isEmpty()) {
                            System.out.println("[SERVER] Recibida información de " + paquete.portales.size() + " portales. Creándolos...");

                            infoPortales.clear();

                            for (Network.PortalInfo info : paquete.portales) {
                                String claveCoordenadas = info.x + "," + info.y;
                                infoPortales.put(claveCoordenadas, info);
                            }
                        }

                        generarBloquesParaElNivel();
                        generarAnimales();
                        sincronizarBloquesConClientes();
                    }
                }
                if (objeto instanceof Network.PaqueteInvocarDron) {
                    int jugadorId = conexion.getID();
                    PlayerState jugador = jugadores.get(jugadorId);

                    if (jugador != null && jugador.characterType == PlayerState.CharacterType.TAILS && !dronesActivos.containsKey(jugadorId)) {

                        int basuraActual = puntajesBasuraIndividuales.getOrDefault(jugadorId, 0);

                        if (basuraActual >= 20) {

                            puntajesBasuraIndividuales.put(jugadorId, basuraActual - 20);

                            Network.PaqueteActualizacionPuntuacion paquetePuntaje = new Network.PaqueteActualizacionPuntuacion();
                            paquetePuntaje.nuevosAnillos = puntajesAnillosIndividuales.getOrDefault(jugadorId, 0);
                            paquetePuntaje.nuevaBasura = puntajesBasuraIndividuales.get(jugadorId);
                            paquetePuntaje.totalBasuraReciclada = basuraReciclada;
                            conexion.sendTCP(paquetePuntaje);

                            DronState nuevoDron = new DronState(proximoIdDron++, jugadorId, jugador.x, jugador.y);
                            dronesActivos.put(jugadorId, nuevoDron);

                            System.out.println("[SERVER DEBUG] Dron CREADO para jugador " + jugadorId + ". Timer inicial: " + nuevoDron.temporizador);

                            Network.PaqueteDronEstado paqueteEstado = new Network.PaqueteDronEstado();
                            paqueteEstado.ownerId = jugador.id;
                            paqueteEstado.nuevoEstado = DronState.EstadoDron.APARECIENDO;
                            paqueteEstado.x = jugador.x;
                            paqueteEstado.y = jugador.y;
                            servidor.sendToAllTCP(paqueteEstado);

                            Network.PaqueteMensajeUI msg = new Network.PaqueteMensajeUI();
                            msg.mensaje = "Sembrando árbol...";
                            conexion.sendTCP(msg);

                            System.out.println("[SERVER] Jugador " + jugadorId + " ha invocado un dron.");

                        } else {
                            Network.PaqueteMensajeUI msg = new Network.PaqueteMensajeUI();
                            msg.mensaje = "Necesitas recoger 20 basuras";
                            conexion.sendTCP(msg);
                        }
                    }
                } if (objeto instanceof Network.PaqueteBasuraDepositada) {
                    System.out.println("[SERVER] ¡Recibido PaqueteBasuraDepositada!");

                    int idJugadorQueActivo = conexion.getID();
                    PlayerState estadoJugador = jugadores.get(idJugadorQueActivo);

                    if (estadoJugador != null ) {
                        System.out.println("[SERVER] Tails (ID: " + idJugadorQueActivo + ") ha activado la planta de tratamiento.");

                        int basuraDepositadaEstaVez = 0;
                        for (int basuraDeJugador : puntajesBasuraIndividuales.values()) {
                            basuraDepositadaEstaVez += basuraDeJugador;
                        }
                        if (basuraDepositadaEstaVez >=5) {
                            System.out.println("[SERVER] Reciclando " + basuraDepositadaEstaVez + " de basura. Curando a todos los jugadores.");

                            for (PlayerState jugadorACurar : jugadores.values()) {
                                int vidaNueva = jugadorACurar.vida + 10;
                                jugadorACurar.vida = Math.min(vidaNueva, Player.MAX_VIDA);

                                Network.PaqueteActualizacionVida paqueteVida = new Network.PaqueteActualizacionVida();
                                paqueteVida.idJugador = jugadorACurar.id;
                                paqueteVida.nuevaVida = jugadorACurar.vida;

                                servidor.sendToTCP(jugadorACurar.id, paqueteVida);
                            }

                            Network.PaqueteMensajeUI msg = new Network.PaqueteMensajeUI();
                            msg.mensaje = "Basura reciclada +10 SALUD A TODOS!";
                            conexion.sendTCP(msg);

                            basuraReciclada += basuraDepositadaEstaVez;
                            EstadisticasJugador stats = estadisticasJugadores.get(idJugadorQueActivo);
                            if (stats != null && basuraDepositadaEstaVez > 0) {
                                stats.sumarObjetosReciclados(basuraDepositadaEstaVez);
                                System.out.println("[STATS] Jugador " + idJugadorQueActivo + " recicló " + basuraDepositadaEstaVez + " objetos.");
                            }
                            puntajesBasuraIndividuales.replaceAll((id, valorActual) -> 0);
                            System.out.println("[SERVER DEBUG] Mapa de basuras después del reinicio: " + puntajesBasuraIndividuales.toString());

                            for (Integer idJugadorConectado : jugadores.keySet()) {
                                Network.PaqueteActualizacionPuntuacion paquetePuntaje = new Network.PaqueteActualizacionPuntuacion();
                                paquetePuntaje.nuevosAnillos = puntajesAnillosIndividuales.getOrDefault(idJugadorConectado, 0);
                                paquetePuntaje.nuevaBasura = puntajesBasuraIndividuales.getOrDefault(idJugadorConectado, 0);
                                paquetePuntaje.totalBasuraReciclada = basuraReciclada;
                                servidor.sendToTCP(idJugadorConectado, paquetePuntaje);
                            }
                            System.out.println("[SERVER] Paquetes de actualización de puntuación enviados a todos los jugadores.");
                        } else {
                            Network.PaqueteMensajeUI msg = new Network.PaqueteMensajeUI();
                            msg.mensaje = "Necesitan recoger 5 Basuras para Curarse!";
                            conexion.sendTCP(msg);
                        }

                    }
                } if (objeto instanceof Network.PaqueteBloqueDestruido paquete) {
                    PlayerState jugador = jugadores.get(paquete.idJugador);

                    if (jugador != null && bloquesRompibles.containsKey(paquete.idBloque)) {

                        bloquesRompibles.remove(paquete.idBloque);
                        System.out.println("[SERVER] El jugador " + paquete.idJugador + " (Knuckles) ha destruido el bloque ID: " + paquete.idBloque);

                        int puntajeActual = puntajesBasuraIndividuales.getOrDefault(paquete.idJugador, 0);
                        puntajesBasuraIndividuales.put(paquete.idJugador, puntajeActual + 1);
                        totalBasuraGlobal++;
                        contaminationState.decrease(TRASH_CLEANUP_VALUE);
                        System.out.println("[SERVER] Basura recogida. Contaminación reducida a " + contaminationState.getPercentage());
                        EstadisticasJugador stats = estadisticasJugadores.get(paquete.idJugador);
                        if (stats != null) {
                            stats.sumarZonaLimpiada();
                            System.out.println("[STATS] Jugador " + paquete.idJugador + " (Knuckles) limpió una zona al destruir un bloque.");
                        }
                        Network.PaqueteBloqueConfirmadoDestruido respuesta = new Network.PaqueteBloqueConfirmadoDestruido();
                        respuesta.idBloque = paquete.idBloque;
                        servidor.sendToAllTCP(respuesta);
                    }
                }  if (objeto instanceof Network.PaqueteSolicitudMatarAnimal paquete) {
                    synchronized (animalesActivos) {
                        AnimalState animal = animalesActivos.get(paquete.idAnimal);
                        if (animal != null && animal.estaVivo) {
                            animal.estaVivo = false;
                            System.out.println("[SERVER] Recibida solicitud para matar al animal ID: " + animal.id);

                        }
                    }
                }
                if (objeto instanceof Network.PaqueteSolicitudHabilidadLimpieza) {
                    int jugadorId = conexion.getID();
                    float cooldownActual = cooldownsHabilidadLimpieza.getOrDefault(jugadorId, 0f);

                    if (cooldownActual <= 0) {
                        System.out.println("[SERVER] Jugador " + jugadorId + " usó la habilidad de limpieza. ¡Aprobado!");

                        contaminationState.decrease(100.0f);

                        ArrayList<Integer> idsItemsARecoger = new ArrayList<>();
                        synchronized (itemsActivos) {
                            for (ItemState item : itemsActivos.values()) {
                                if (item.tipo == ItemState.ItemType.BASURA || item.tipo == ItemState.ItemType.PIEZA_PLASTICO) {
                                    idsItemsARecoger.add(item.id);
                                }
                            }
                        }

                        for (Integer idItem : idsItemsARecoger) {
                            procesarRecogidaItem(jugadorId, idItem);
                        }

                        cooldownsHabilidadLimpieza.put(jugadorId, COOLDOWN_HABILIDAD_SONIC);
                        EstadisticasJugador stats = estadisticasJugadores.get(jugadorId);
                        if (stats != null) {
                            stats.sumarZonaLimpiada();
                            System.out.println("[STATS] Jugador " + jugadorId + " (Sonic) limpió una zona con su habilidad.");
                        }
                        Network.PaqueteHabilidadLimpiezaSonic notificacion = new Network.PaqueteHabilidadLimpiezaSonic();
                        servidor.sendToAllTCP(notificacion);

                        Network.PaqueteActualizacionContaminacion paqueteContaminacion = new Network.PaqueteActualizacionContaminacion();
                        paqueteContaminacion.contaminationPercentage = contaminationState.getPercentage();
                        servidor.sendToAllTCP(paqueteContaminacion);

                    } else {
                        System.out.println("[SERVER] Jugador " + jugadorId + " intentó usar la habilidad, pero está en cooldown. ¡Ignorado!");
                    }
                }  if (objeto instanceof Network.PaqueteAtaqueJugadorAEnemigo paquete) {
                    synchronized (enemigosActivos) {
                        EnemigoState enemigo = enemigosActivos.get(paquete.idEnemigo);
                        if (enemigo != null && enemigo.vida > 0) {
                            enemigo.vida -= paquete.danio;
                            System.out.println("[SERVER] Enemigo ID " + enemigo.id + " recibió " + paquete.danio + " de daño. Vida restante: " + enemigo.vida);

                            Network.PaqueteActualizacionVidaEnemigo paqueteVida = new Network.PaqueteActualizacionVidaEnemigo();
                            paqueteVida.idEnemigo = enemigo.id;
                            paqueteVida.nuevaVida = enemigo.vida;

                            servidor.sendToAllTCP(paqueteVida);

                            if (enemigo.vida <= 0) {
                                System.out.println("[SERVER] ¡Enemigo ID " + enemigo.id + " ha sido derrotado!");
                                enemigosActivos.remove(enemigo.id);
                                Network.PaqueteEntidadEliminada notificacionMuerte = new Network.PaqueteEntidadEliminada();
                                notificacionMuerte.idEntidad = enemigo.id;
                                notificacionMuerte.esJugador = false;
                                servidor.sendToAllTCP(notificacionMuerte);
                                EstadisticasJugador stats = estadisticasJugadores.get(paquete.idJugador);
                                if (stats != null) {
                                    stats.sumarEnemigoDerrotado();
                                    System.out.println("[STATS] Jugador " + paquete.idJugador + " derrotó a un enemigo.");
                                }
                                if (enemigo.tipo == EnemigoState.EnemigoType.ROBOTNIK) {
                                    if ("maps/ZonaJefeN3.tmx".equals(mapaActualServidor)) {
                                        finalizarPartidaYEnviarResultados();
                                        return;
                                    }
                                }
                                comprobarYGenerarPortalSiCorresponde();
                            }
                        }
                    }
                } if (objeto instanceof Network.ForzarFinDeJuegoDebug) {
                    System.out.println("[GAMESERVER] ¡Recibida orden de forzar fin de juego!");

                    finalizarPartidaYEnviarResultados();
                }
            }

            public void disconnected(Connection conexion) {
                System.out.println("[SERVER] Conexión física perdida con el cliente ID: " + conexion.getID());
                desconectarJugador(conexion);
            }
        });

        // 3. ARRANQUE DEL SERVIDOR Y VINCULACIÓN AL PUERTO
        try {
            servidor.bind(Network.PORT);
            servidor.start();
            System.out.println("[SERVER BOOT] Servidor escuchando en el puerto " + Network.PORT);
        } catch (IOException e) {
            System.err.println("[SERVER BOOT] ERROR: No se pudo vincular el servidor al puerto. ¿Ya está en uso?");
            e.printStackTrace();
            return;
        }

        // 4. CREACIÓN Y ARRANQUE DEL HILO DE LÓGICA DE JUEGO
        serverThreadActive = true;
        gameLoopThread = new Thread(() -> {
            System.out.println("[GAMELOOP] Hilo de juego iniciado. Esperando el plano del mapa del primer cliente...");

            while (paredesDelMapa == null && serverThreadActive) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    serverThreadActive = false; // Salimos si el hilo es interrumpido
                }
            }

            if(serverThreadActive) {
                System.out.println("[GAMELOOP] ¡Plano del mapa detectado! Iniciando bucle de juego principal.");
            }

            final float FIXED_DELTA_TIME = 1 / 60f;
            while (serverThreadActive) { // El bucle ahora depende de nuestra variable de control
                try {
                    updateServerLogic(FIXED_DELTA_TIME);
                    Thread.sleep((long) (FIXED_DELTA_TIME * 1000));
                } catch (InterruptedException e) {
                    serverThreadActive = false; // Salimos si el hilo es interrumpido
                }
            }
            System.out.println("[GAMELOOP] Hilo de juego detenido.");
        });
        gameLoopThread.setName("GameLoopThread");
        gameLoopThread.start();
    }

    @Override
    public void update(float deltaTime, LevelManager manejadorNivel, Player personajeJugable) {

    }

    @Override
    public void dispose() {
        if (servidor != null) {
            servidor.close();
            System.out.println("[SERVER] Servidor detenido.");
        }
    }
}
