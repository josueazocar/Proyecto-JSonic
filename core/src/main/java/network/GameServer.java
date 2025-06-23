package network;

import com.JSonic.uneg.*;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import network.interfaces.IGameServer;

import java.io.IOException;
import java.util.HashMap;

public class GameServer implements IGameServer {

    private final Server servidor;
    private LevelManager manejadorNivel;
    // --- ALMACENES DE ESTADO ---
    private final HashMap<Integer, PlayerState> jugadores = new HashMap<>();
    private final HashMap<Integer, EnemigoState> enemigosActivos = new HashMap<>();
    private final HashMap<Integer, ItemState> itemsActivos = new HashMap<>();

    // --- VARIABLES DE CONTROL DE SPAWNING ---
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
    private static final float DURACION_ANIM_GOLPE = 0.64f;


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
                    respuesta.tuEstado = estadoAsignado;
                    conexion.sendTCP(respuesta);
                }
                if (objeto instanceof Network.PaqueteSolicitudRecogerItem paquete) {
                    // Un cliente solicita recoger un ítem. Verificamos si todavía existe.
                    // Usamos 'synchronized' para evitar que dos jugadores recojan el mismo ítem a la vez.
                    synchronized (itemsActivos) {
                        ItemState itemRecogido = itemsActivos.remove(paquete.idItem);

                        if (itemRecogido != null) {
                            // El ítem existía. Lo hemos eliminado de nuestra lista maestra.
                            System.out.println("[SERVER] Ítem con ID" +
                                " " + paquete.idItem + " recogido por jugador " + conexion.getID() + ". Notificando a todos.");

                            // Creamos el paquete de confirmación.
                            Network.PaqueteItemEliminado paqueteEliminado = new Network.PaqueteItemEliminado();
                            paqueteEliminado.idItem = paquete.idItem;

                            // ¡Enviamos la orden de eliminación a TODOS los clientes!
                            servidor.sendToAllTCP(paqueteEliminado);
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

        new Thread(() -> {
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
            // --- Fin de la lógica del jugador más cercano ---


            // El resto del código es IDÉNTICO al que ya probamos, pero usando "jugadorMasCercano".
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

                // Aquí puedes añadir tu lógica de colisión con el mapa si la tienes disponible en el servidor.
                // Por ahora, actualizamos la posición directamente.
                enemigo.x = nextX;
                enemigo.y = nextY;
            }
        }
    }
    private void updateServerLogic(float deltaTime) {
        actualizarEnemigosAI(deltaTime);

        // --- 2. Generar nuevas entidades (tu código existente) ---
        generarNuevosItems(deltaTime);
        generarNuevosEnemigos(deltaTime);

        // --- 3. Enviar el paquete de actualización ---
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
        // --- ESPÍA #1 ---
        // Vamos a ver si el cronómetro está avanzando. Descomenta esta línea si quieres ver un log muy verboso.
         System.out.println("[SERVER_DEBUG] Tiempo para enemigo: " + tiempoGeneracionEnemigo);

        if (tiempoGeneracionEnemigo >= INTERVALO_GENERACION_ENEMIGO) {

            // --- ESPÍA #2 ---
            // Si vemos este mensaje, significa que la condición del tiempo se cumplió.
            System.out.println("[SERVER_DEBUG] ¡Tiempo cumplido! Intentando generar un enemigo...");

            spawnNuevoEnemigo();
            tiempoGeneracionEnemigo = 0f;
        }

        if (tiempoGeneracionEnemigo >= INTERVALO_GENERACION_ENEMIGO) {
            spawnNuevoEnemigo();
            tiempoGeneracionEnemigo = 0f;
        }
    }

    private void spawnNuevoEnemigo() {
        System.out.println("[SERVER_DEBUG] Dentro de spawnNuevoEnemigo(). Creando paquete...");

        float x = (float) (Math.random() * 1920);
        float y = (float) (Math.random() * 1280);
        EnemigoState nuevoEstado = new EnemigoState(proximoIdEnemigo++, x, y, 100, EnemigoState.EnemigoType.ROBOT);
        enemigosActivos.put(nuevoEstado.id, nuevoEstado);

        System.out.println("[SERVER] Generando nuevo enemigo con ID: " + nuevoEstado.id); // Log de depuración

        Network.PaqueteEnemigoNuevo paquete = new Network.PaqueteEnemigoNuevo();
        paquete.estadoEnemigo = nuevoEstado;
        servidor.sendToAllTCP(paquete); // Esta es la línea clave que asegura la notificación
    }

    private void spawnNuevoItem(ItemState.ItemType tipo) {
        float x = (float) (Math.random() * 1920);
        float y = (float) (Math.random() * 1280);
        ItemState nuevoEstado = new ItemState(proximoIdItem++, x, y, tipo);
        itemsActivos.put(nuevoEstado.id, nuevoEstado);
        Network.PaqueteItemNuevo paquete = new Network.PaqueteItemNuevo();
        paquete.estadoItem = nuevoEstado;
        servidor.sendToAllTCP(paquete);
    }

    @Override
    public void update(float deltaTime, com.JSonic.uneg.LevelManager manejadorNivel) {
        // Correcto: se deja vacío para cumplir la interfaz.
    }

    @Override
    public void dispose() {
        if (servidor != null) {
            servidor.close();
            System.out.println("[SERVER] Servidor detenido.");
        }
    }

    /**
     * Permite que el contexto que crea el servidor (como la PantallaDeJuego)
     * le entregue el mapa para que pueda realizar comprobaciones de colisión.
     * @param manejador El LevelManager ya inicializado del juego.
     */
    public void setManejadorNivel(LevelManager manejador) {
        this.manejadorNivel = manejador;
        System.out.println("[SERVER] Mapa recibido. Ahora puedo ver las colisiones.");
    }
}
