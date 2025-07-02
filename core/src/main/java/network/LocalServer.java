package network;

import com.JSonic.uneg.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import network.interfaces.IGameClient;
import network.interfaces.IGameServer;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Una implementación de IGameServer que se ejecuta localmente en la memoria.
 * No utiliza la red real y actúa como el motor para el modo de un solo jugador.
 */
public class LocalServer implements IGameServer {

    // Almacenes para los estados de todas las entidades del juego
    private final HashMap<Integer, PlayerState> jugadores = new HashMap<>();
    private final HashMap<Integer, EnemigoState> enemigosActivos = new HashMap<>();
    private final HashMap<Integer, ItemState> itemsActivos = new HashMap<>();

    private float tiempoGeneracionEnemigo = 0f;
    private final float INTERVALO_GENERACION_ENEMIGO = 5.0f;
    private int proximoIdEnemigo = 0;

    private int proximoIdItem = 0;
    private static final int MAX_ANILLOS = 50;
    private static final int MAX_BASURA = 10;
    private static final int MAX_PLASTICO = 10;
    private static final float INTERVALO_SPAWN_ANILLO = 1.0f;
    private static final float INTERVALO_SPAWN_BASURA = 5.0f;
    private static final float INTERVALO_SPAWN_PLASTICO = 5.0f;
    private float tiempoSpawnAnillo = 0f;
    private float tiempoSpawnBasura = 0f;
    private float tiempoSpawnPlastico = 0f;

    // Cola para paquetes que vienen "desde el cliente" hacia el servidor
    private final Queue<Object> paquetesEntrantes = new ConcurrentLinkedQueue<>();

    // Referencia directa al único cliente que existirá en este modo
    private LocalClient clienteLocal;
    private int proximoIdJugador = 1; // En modo local, siempre empezamos en 1
    private static final int ROBOT_SPEED = 1;
    private static final float ROBOT_DETECTION_RANGE = 300f;
    private static final float ROBOT_ATTACK_RANGE = 10f; // Usando el valor del código original


    public LocalServer() {
        // El constructor está vacío, la magia ocurre en start() y update()
    }

    @Override
    public void start() {
        System.out.println("[LOCAL SERVER] Servidor local iniciado.");

        // 1. Creamos la única instancia del cliente local y la guardamos.
        this.clienteLocal = new LocalClient(this);

        // 2. Simulamos la conexión del jugador inmediatamente.
        // Esto replica la lógica del listener "connected" de tu GameServer.
        PlayerState nuevoEstado = new PlayerState();
        nuevoEstado.id = proximoIdJugador++; // Será el jugador 1
        nuevoEstado.x = 100; // Posición inicial X
        nuevoEstado.y = 100; // Posición inicial Y
        nuevoEstado.estadoAnimacion = Player.EstadoPlayer.IDLE_RIGHT;
        jugadores.put(nuevoEstado.id, nuevoEstado);

        // 3. "Enviamos" el paquete de bienvenida al cliente local.
        Network.RespuestaAccesoPaquete respuesta = new Network.RespuestaAccesoPaquete();
        respuesta.mensajeRespuesta = "Bienvenido al modo local!";
        respuesta.tuEstado = nuevoEstado;
        this.clienteLocal.recibirPaqueteDelServidor(respuesta);
    }

    /**
     * Este es el "game loop" del servidor. Se llamará desde PantallaDeJuego.
     * @param deltaTime El tiempo transcurrido desde el último fotograma.
     */
    @Override
    public void update(float deltaTime, LevelManager manejadorNivel) {
        // --- 1. PROCESAR PAQUETES DEL CLIENTE ---
        while (!paquetesEntrantes.isEmpty()) {
            Object objeto = paquetesEntrantes.poll();

            if (objeto instanceof Network.PaquetePosicionJugador paquete) {
                PlayerState estadoJugador = jugadores.get(paquete.id);
                if (estadoJugador != null) {
                    estadoJugador.x = paquete.x;
                    estadoJugador.y = paquete.y;
                    estadoJugador.estadoAnimacion = paquete.estadoAnimacion;
                    // No necesitamos retransmitir porque solo hay un jugador.
                }
            } else if (objeto instanceof Network.PaqueteSolicitudRecogerItem paquete) {
                // El cliente solicita recoger un ítem. Verificamos si todavía existe.
                ItemState itemRecogido = itemsActivos.remove(paquete.idItem);

                if (itemRecogido != null) {
                    // El ítem existía y lo hemos eliminado de nuestra lista maestra.
                    System.out.println("[LOCAL SERVER] Ítem con ID " + paquete.idItem + " recogido. Notificando al cliente.");

                    // Ahora, creamos y enviamos el paquete de confirmación de eliminación.
                    Network.PaqueteItemEliminado paqueteEliminado = new Network.PaqueteItemEliminado();
                    paqueteEliminado.idItem = paquete.idItem;
                    clienteLocal.recibirPaqueteDelServidor(paqueteEliminado);
                }
            }  else if (objeto instanceof Network.PaqueteAnimacionEnemigoTerminada paquete) {
                // El cliente nos informa que la animación de un enemigo terminó.
                EnemigoState enemigo = enemigosActivos.get(paquete.idEnemigo);
                if (enemigo != null) {
                    // Lo ponemos en el estado especial para que la IA lo reevalúe.
                    enemigo.estadoAnimacion = EnemigoState.EstadoEnemigo.POST_ATAQUE;
                    enemigo.tiempoEnEstado = 0;
                }
            }

        }
        actualizarEnemigosAI(deltaTime, manejadorNivel);
        generarNuevosItems(deltaTime, manejadorNivel);
        generarNuevosEnemigos(deltaTime, manejadorNivel);

        if (!enemigosActivos.isEmpty()) {
            Network.PaqueteActualizacionEnemigos paqueteUpdate = new Network.PaqueteActualizacionEnemigos();
            paqueteUpdate.estadosEnemigos = this.enemigosActivos;
            clienteLocal.recibirPaqueteDelServidor(paqueteUpdate);
        }
    }

    private void actualizarEnemigosAI(float deltaTime, LevelManager manejadorNivel) {
        PlayerState jugador = jugadores.get(1);
        if (jugador == null) return;

        for (EnemigoState enemigo : enemigosActivos.values()) {
            int dx = (int) (jugador.x - enemigo.x);
            int dy = (int) (jugador.y - enemigo.y);
            int distance = (int) Math.sqrt(dx * dx + dy * dy);

            // Si el robot está en medio de la animación de GOLPE, el servidor no hace NADA.
            // Simplemente espera a que el cliente le notifique que la animación ha terminado.
            if (enemigo.estadoAnimacion == EnemigoState.EstadoEnemigo.HIT_RIGHT || enemigo.estadoAnimacion == EnemigoState.EstadoEnemigo.HIT_LEFT) {
                continue; // Saltar al siguiente enemigo
            }

            // Si el robot NO está golpeando (está en IDLE, RUN, o acaba de terminar un golpe),
            // decidimos cuál debe ser su siguiente acción.

            EnemigoState.EstadoEnemigo estadoAnterior = enemigo.estadoAnimacion;

            // Decisión de IA
            if (distance <= ROBOT_ATTACK_RANGE) {
                enemigo.estadoAnimacion = dx > 0 ? EnemigoState.EstadoEnemigo.HIT_RIGHT : EnemigoState.EstadoEnemigo.HIT_LEFT;
            } else if (distance <= ROBOT_DETECTION_RANGE) {
                enemigo.mirandoDerecha = dx > 0;
                enemigo.estadoAnimacion = enemigo.mirandoDerecha ? EnemigoState.EstadoEnemigo.RUN_RIGHT : EnemigoState.EstadoEnemigo.RUN_LEFT;
            } else {
                enemigo.mirandoDerecha = dx > 0;
                enemigo.estadoAnimacion = enemigo.mirandoDerecha ? EnemigoState.EstadoEnemigo.IDLE_RIGHT : EnemigoState.EstadoEnemigo.IDLE_LEFT;
            }

            // Si el estado ha cambiado, reiniciamos el temporizador.
            if (estadoAnterior != enemigo.estadoAnimacion) {
                enemigo.tiempoEnEstado = 0;
            }

            // Lógica de Movimiento (solo se ejecuta si el estado es RUN)
            if (enemigo.estadoAnimacion == EnemigoState.EstadoEnemigo.RUN_RIGHT || enemigo.estadoAnimacion == EnemigoState.EstadoEnemigo.RUN_LEFT) {
                float nextX = enemigo.x;
                float nextY = enemigo.y;

                if (dx > 0) nextX += ROBOT_SPEED; else if (dx < 0) nextX -= ROBOT_SPEED;
                if (dy > 0) nextY += ROBOT_SPEED; else if (dy < 0) nextY -= ROBOT_SPEED;

                Rectangle boundsXY = new Rectangle(nextX, nextY, 48, 48);
                if (!manejadorNivel.colisionaConMapa(boundsXY)) {
                    enemigo.x = nextX;
                    enemigo.y = nextY;
                } else {
                    Rectangle boundsX = new Rectangle(nextX, enemigo.y, 48, 48);
                    if (!manejadorNivel.colisionaConMapa(boundsX)) {
                        enemigo.x = nextX;
                    } else {
                        Rectangle boundsY = new Rectangle(enemigo.x, nextY, 48, 48);
                        if (!manejadorNivel.colisionaConMapa(boundsY)) {
                            enemigo.y = nextY;
                        }
                    }
                }
            }
        }
    }


    private void generarNuevosItems(float deltaTime, LevelManager manejadorNivel) {
        int anillos = 0, basura = 0, plastico = 0;
        for (ItemState item : itemsActivos.values()) {
            if (item.tipo == ItemState.ItemType.ANILLO) anillos++;
            if (item.tipo == ItemState.ItemType.BASURA) basura++;
            if (item.tipo == ItemState.ItemType.PIEZA_PLASTICO) plastico++;
        }

        tiempoSpawnAnillo += deltaTime;
        if (tiempoSpawnAnillo >= INTERVALO_SPAWN_ANILLO && anillos < MAX_ANILLOS) {
            spawnNuevoItem(ItemState.ItemType.ANILLO, manejadorNivel);
            tiempoSpawnAnillo = 0f;
        }
        tiempoSpawnBasura += deltaTime;
        if (tiempoSpawnBasura >= INTERVALO_SPAWN_BASURA && basura < MAX_BASURA) {
            spawnNuevoItem(ItemState.ItemType.BASURA, manejadorNivel);
            tiempoSpawnBasura = 0f;
        }
        tiempoSpawnPlastico += deltaTime;
        if (tiempoSpawnPlastico >= INTERVALO_SPAWN_PLASTICO && plastico < MAX_PLASTICO) {
            spawnNuevoItem(ItemState.ItemType.PIEZA_PLASTICO, manejadorNivel);
            tiempoSpawnPlastico = 0f;
        }
    }

    private void generarNuevosEnemigos(float deltaTime, LevelManager manejadorNivel) {
        tiempoGeneracionEnemigo += deltaTime;
        if (tiempoGeneracionEnemigo >= INTERVALO_GENERACION_ENEMIGO) {
            spawnNuevoEnemigo(manejadorNivel);
            tiempoGeneracionEnemigo = 0f;
        }
    }

    private void spawnNuevoEnemigo(LevelManager manejadorNivel) {
        int intentos = 0;
        boolean colocado = false;
        while (!colocado && intentos < 20) {
            float x = MathUtils.random(0, manejadorNivel.getAnchoMapaPixels());
            float y = MathUtils.random(0, manejadorNivel.getAltoMapaPixels());
            Rectangle bounds = new Rectangle(x, y, 48, 48);

            if (!manejadorNivel.colisionaConMapa(bounds)) {
                EnemigoState nuevoEstado = new EnemigoState(proximoIdEnemigo++, bounds.x, bounds.y, 100, EnemigoState.EnemigoType.ROBOT);
                enemigosActivos.put(nuevoEstado.id, nuevoEstado);
                Network.PaqueteEnemigoNuevo paquete = new Network.PaqueteEnemigoNuevo();
                paquete.estadoEnemigo = nuevoEstado;
                clienteLocal.recibirPaqueteDelServidor(paquete);
                colocado = true;
            }
            intentos++;
        }
    }

    private void spawnNuevoItem(ItemState.ItemType tipo, LevelManager manejadorNivel) {
        int intentos = 0;
        boolean colocado = false;
        while (!colocado && intentos < 20) {
            float x = MathUtils.random(0, manejadorNivel.getAnchoMapaPixels());
            float y = MathUtils.random(0, manejadorNivel.getAltoMapaPixels());
            Rectangle nuevoBounds = new Rectangle(x, y, 32, 32);

            boolean superpuesto = false;
            for (ItemState item : itemsActivos.values()) {
                if (new Rectangle(item.x, item.y, 32, 32).overlaps(nuevoBounds)) {
                    superpuesto = true;
                    break;
                }
            }

            if (!superpuesto && !manejadorNivel.colisionaConMapa(nuevoBounds)) {
                ItemState nuevoEstado = new ItemState(proximoIdItem++, x, y, tipo);
                itemsActivos.put(nuevoEstado.id, nuevoEstado);
                Network.PaqueteItemNuevo paquete = new Network.PaqueteItemNuevo();
                paquete.estadoItem = nuevoEstado;
                clienteLocal.recibirPaqueteDelServidor(paquete);
                colocado = true;
            }
            intentos++;
        }
    }

    @Override
    public void dispose() {
        jugadores.clear();
        System.out.println("[LOCAL SERVER] Servidor local detenido.");
    }

    /**
     * Método para que el LocalClient nos "envíe" paquetes.
     * @param paquete El paquete enviado por el cliente.
     */
    public void recibirPaqueteDelCliente(Object paquete) {
        this.paquetesEntrantes.add(paquete);
    }

    /**
     * Permite a la clase que nos crea (JSonicJuego) obtener la instancia del cliente.
     * @return El cliente local asociado a este servidor.
     */
    public IGameClient getClient() {
        return this.clienteLocal;
    }

}
