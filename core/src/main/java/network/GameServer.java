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

    private final Server servidor;
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
    private static final float CONTAMINATION_RATE_PER_SECOND = 1; // El % sube 0.65 puntos por segundo.
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
    private static final int ROBOT_SPEED = 1;
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
        enemigosPorMapa.put("maps/Zona1N1.tmx", 8);
        enemigosPorMapa.put("maps/ZonaJefeN1.tmx", 3);
        enemigosPorMapa.put("maps/Zona1N2.tmx", 15);
        enemigosPorMapa.put("maps/ZonaJefeN2.tmx", 5);
        enemigosPorMapa.put("maps/Zona1N3.tmx", 25);
        enemigosPorMapa.put("maps/Zona2N3.tmx", 35);
        enemigosPorMapa.put("maps/ZonaJefeN3.tmx", 7);
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

                // --- [CAMBIO PROFESOR] --- Lógica de asignación de personajes mejorada.
                // Ahora es dinámica y no depende del número de jugadores, sino de qué personaje está libre.
                if (!personajesEnUso.contains(PlayerState.CharacterType.SONIC)) {
                    nuevoEstado.characterType = PlayerState.CharacterType.SONIC;
                } else if (!personajesEnUso.contains(PlayerState.CharacterType.TAILS)) {
                    nuevoEstado.characterType = PlayerState.CharacterType.TAILS;
                } else if (!personajesEnUso.contains(PlayerState.CharacterType.KNUCKLES)) {
                    nuevoEstado.characterType = PlayerState.CharacterType.KNUCKLES;
                } else {
                    // --- [CAMBIO PROFESOR] --- Lógica para cuando el servidor está lleno.
                    // Aquí podrías enviar un paquete de "ServidorLleno" y cerrar la conexión.
                    // Por ahora, simplemente lo logueamos y no asignamos personaje.
                    System.out.println("[SERVER] Intento de conexión rechazado: No hay personajes disponibles.");
                    conexion.close();
                    return; // Salimos del método para no procesar a este jugador.
                }

                personajesEnUso.add(nuevoEstado.characterType); // Marcamos el personaje como "en uso".
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
                    // Llamamos a nuestro nuevo método centralizado de desconexión.
                    desconectarJugador(conexion);
                    return; // Importante para no seguir procesando más paquetes de este jugador.
                }
                if (objeto instanceof Network.SolicitudAccesoPaquete) {
                    Network.SolicitudAccesoPaquete solicitud = (Network.SolicitudAccesoPaquete) objeto;
                    System.out.println("[SERVER] Recibida solicitud de acceso del jugador: " + solicitud.nombreJugador);

                    // Obtenemos el estado del jugador que ya fue creado en connected().
                    PlayerState estadoAsignado = jugadores.get(conexion.getID());
                    if (estadoAsignado == null) return;

                    // Le asignamos el nombre que viene en el paquete.
                    estadoAsignado.nombreJugador = solicitud.nombreJugador;
                    EstadisticasJugador stats = estadisticasJugadores.get(conexion.getID());
                    if (stats != null) {
                        stats.setNombreJugador(solicitud.nombreJugador);
                    }

                    // Enviamos la respuesta de bienvenida al cliente.
                    Network.RespuestaAccesoPaquete respuesta = new Network.RespuestaAccesoPaquete();
                    respuesta.mensajeRespuesta = "¡Bienvenido, " + solicitud.nombreJugador + "!";
                    respuesta.tuEstado = estadoAsignado; // Le enviamos su estado completo, incluyendo el personaje que le asignamos.
                    conexion.sendTCP(respuesta);

                    // --- AHORA SÍ: ANUNCIAMOS AL JUGADOR AL RESTO DEL MUNDO ---
                    // La información está completa (ID, Personaje, Nombre).
                    Network.PaqueteJugadorConectado packetNuevoJugador = new Network.PaqueteJugadorConectado();
                    packetNuevoJugador.nuevoJugador = estadoAsignado;
                    servidor.sendToAllExceptTCP(conexion.getID(), packetNuevoJugador);

                    // Y le informamos al nuevo jugador de los que ya estaban.
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

                  //  System.out.println("-----> [DEBUGGER 4 - RECEPTOR SERVIDOR] Recibida posición del jugador " + paquete.id + ": (" + paquete.x + ", " + paquete.y + ")");
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
                    // Usamos 'synchronized' para evitar que dos jugadores interactúen con el mismo ítem a la vez.
                    synchronized (itemsActivos) {
                        // 1. PRIMERO VERIFICAMOS el ítem con .get() en lugar de .remove() para poder saber su tipo.
                        ItemState itemRecogido = itemsActivos.get(paquete.idItem);

                        // Si el ítem realmente existe...
                        if (itemRecogido != null) {

                            if (itemRecogido.tipo == ItemState.ItemType.ESMERALDA) {

                                itemsActivos.remove(paquete.idItem); // 1. La quitamos del juego.
                                esmeraldasRecogidasGlobal++; // 2. Incrementamos el contador global.
                                System.out.println("[GAMESERVER] ¡Esmeralda recogida! Total global: " + esmeraldasRecogidasGlobal);

                                // 3. Notificamos a TODOS los clientes del nuevo total de esmeraldas.
                                Network.PaqueteActualizacionEsmeraldas paqueteEsmeraldas = new Network.PaqueteActualizacionEsmeraldas();
                                paqueteEsmeraldas.totalEsmeraldas = esmeraldasRecogidasGlobal;
                                servidor.sendToAllTCP(paqueteEsmeraldas);

                                // 4. Notificamos a TODOS que el ítem específico fue eliminado del mapa.
                                Network.PaqueteItemEliminado paqueteEliminado = new Network.PaqueteItemEliminado();
                                paqueteEliminado.idItem = paquete.idItem;
                                servidor.sendToAllTCP(paqueteEliminado);

                                if (esmeraldasRecogidasGlobal >= 7) {
                                    System.out.println("[GAMESERVER] ¡LAS 7 ESMERALDAS REUNIDAS! Activando Super Sonic...");

                                    // Buscamos a todos los jugadores que son Sonic.
                                    for (PlayerState jugador : jugadores.values()) {
                                        if (jugador.characterType == PlayerState.CharacterType.SONIC) {

                                            // 1. Actualizamos el estado del jugador EN EL SERVIDOR.
                                            jugador.isSuper = true;

                                            jugador.vida = Player.MAX_VIDA;
                                            System.out.println("[GAMESERVER] Vida del jugador " + jugador.id + " restaurada al máximo: " + jugador.vida);

                                            // 2. Notificamos al cliente de Sonic sobre su nueva vida.
                                            Network.PaqueteActualizacionVida paqueteVida = new Network.PaqueteActualizacionVida();
                                            paqueteVida.idJugador = jugador.id;
                                            paqueteVida.nuevaVida = jugador.vida;
                                            servidor.sendToTCP(jugador.id, paqueteVida);

                                            // 3. Creamos el "anuncio oficial".
                                            Network.PaqueteTransformacionSuper paqueteSuper = new Network.PaqueteTransformacionSuper();
                                            paqueteSuper.idJugador = jugador.id;
                                            paqueteSuper.esSuper = true;

                                            // 4. Lo enviamos a TODOS los jugadores.
                                            servidor.sendToAllTCP(paqueteSuper);
                                            System.out.println("[GAMESERVER] Notificando a todos que el jugador " + jugador.id + " es ahora Super Sonic.");
                                        }
                                    }
                                }

                            }

                            // 2. AHORA DECIDIMOS QUÉ HACER BASADO EN SU TIPO.
                            // CASO ESPECIAL: Es un teletransportador.
                           else if (itemRecogido.tipo == ItemState.ItemType.TELETRANSPORTE) {

                                System.out.println("[SERVER] Jugador " + conexion.getID() + " ha activado el teletransportador.");

                                // --- INICIO DE LA MODIFICACIÓN ---
                                // Construimos la clave a partir de las coordenadas del ítem que fue tocado.
                                String claveCoordenadas = itemRecogido.x + "," + itemRecogido.y;
                                Network.PortalInfo infoDestino = infoPortales.get(claveCoordenadas);

                                if (infoDestino == null) {
                                    // El mensaje de error ahora es más claro sobre qué está buscando.
                                    System.err.println("[SERVER] Error: No se encontró información de destino para el portal en las coordenadas: " + claveCoordenadas);
                                    return; // Salimos para evitar un crash.
                                }

                                // Eliminamos el ítem de la lista de ítems activos del mundo.
                                itemsActivos.remove(paquete.idItem);

                                // Eliminamos la información del portal de nuestro mapa de consulta usando la misma clave de coordenadas.
                                infoPortales.remove(claveCoordenadas);
                                // --- FIN DE LA MODIFICACIÓN ---


                                // Creamos la ORDEN de cambio de mapa.
                                Network.PaqueteOrdenCambiarMapa orden = new Network.PaqueteOrdenCambiarMapa();
                                orden.nuevoMapa = infoDestino.destinoMapa;
                                mapaActualServidor = orden.nuevoMapa;
                                reiniciarContadoresDeNivel();
                                // Enviamos la orden de cambio de mapa a TODOS los jugadores.
                                servidor.sendToAllTCP(orden);

                                // Limpiamos los enemigos del mapa anterior.
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

                                // ¡LÓGICA CLAVE! Comprobamos si el nuevo mapa es una zona de jefe.
                                if (orden.nuevoMapa.contains("ZonaJefe")) {
                                    System.out.println("[SERVER] Detectado mapa de jefe. ¡Creando a Robotnik!");

                                    // Solo si es un mapa de jefe, creamos y enviamos la instancia del jefe.
                                    EnemigoState estadoRobotnik = new EnemigoState(999, 300, 100, 100, EnemigoState.EnemigoType.ROBOTNIK);
                                    enemigosActivos.put(estadoRobotnik.id, estadoRobotnik);

                                    Network.PaqueteEnemigoNuevo paqueteRobotnik = new Network.PaqueteEnemigoNuevo();
                                    paqueteRobotnik.estadoEnemigo = estadoRobotnik;
                                    servidor.sendToAllTCP(paqueteRobotnik);
                                }

                                // Notificamos a todos que el portal ha sido eliminado.
                                Network.PaqueteItemEliminado paqueteEliminado = new Network.PaqueteItemEliminado();
                                paqueteEliminado.idItem = paquete.idItem;
                                servidor.sendToAllTCP(paqueteEliminado);

                                // El servidor "olvida" las paredes del mapa anterior para poder cargar las nuevas.
                                paredesDelMapa = null;
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

                                    int anillosAhora = puntajesAnillosIndividuales.get(conexion.getID());
                                    if (anillosAhora >= 100) {
                                        System.out.println("[GAMESERVER] Jugador " + conexion.getID() + " tiene 100 anillos. Canjeando por vida.");

                                        // 1. Restamos los 100 anillos al jugador EN EL SERVIDOR.
                                        puntajesAnillosIndividuales.put(conexion.getID(), anillosAhora - 100);

                                        // 2. Obtenemos el estado del jugador y aumentamos su vida EN EL SERVIDOR.
                                        PlayerState estadoJugador = jugadores.get(conexion.getID());
                                        if (estadoJugador != null) {
                                            estadoJugador.vida = Math.min(estadoJugador.vida + 100, Player.MAX_VIDA);

                                            // 3. Notificamos al jugador de su nueva vida.
                                            Network.PaqueteActualizacionVida paqueteVida = new Network.PaqueteActualizacionVida();
                                            paqueteVida.idJugador = conexion.getID();
                                            paqueteVida.nuevaVida = estadoJugador.vida;
                                            servidor.sendToTCP(conexion.getID(), paqueteVida);
                                        }
                                    }

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

                            // Creamos el estado del ítem para la esmeralda.
                            ItemState estadoEsmeralda = new ItemState(proximoIdItem++, paquete.posEsmeralda.x, paquete.posEsmeralda.y, ItemState.ItemType.ESMERALDA);
                            itemsActivos.put(estadoEsmeralda.id, estadoEsmeralda);

                            // Notificamos a TODOS los clientes para que la dibujen en sus pantallas.
                            Network.PaqueteItemNuevo paqueteItem = new Network.PaqueteItemNuevo();
                            paqueteItem.estadoItem = estadoEsmeralda;
                            servidor.sendToAllTCP(paqueteItem);
                        }

                        // --- INICIO DE LA LÓGICA AÑADIDA ---
                        if (paquete.portales != null && !paquete.portales.isEmpty()) {
                            System.out.println("[SERVER] Recibida información de " + paquete.portales.size() + " portales. Creándolos...");

                            // Iteramos sobre la información de cada portal recibida.
                            infoPortales.clear();

                            // Solo guardamos la información, NO creamos los ítems.
                            for (Network.PortalInfo info : paquete.portales) {
                                String claveCoordenadas = info.x + "," + info.y;
                                infoPortales.put(claveCoordenadas, info);
                            }
                        }
                        // --- FIN DE LA LÓGICA AÑADIDA ---

                        // La lógica para generar bloques y animales se mantiene.
                        generarBloquesParaElNivel();
                        generarAnimales();
                        sincronizarBloquesConClientes();
                    }
                }
                if (objeto instanceof Network.PaqueteInvocarDron) {
                    int jugadorId = conexion.getID();
                    PlayerState jugador = jugadores.get(jugadorId);

                    // Solo se puede invocar si el jugador existe y no tiene un dron ya activo.
                    if (jugador != null && !dronesActivos.containsKey(jugadorId)) {
                        // Creamos un nuevo DronState usando el constructor del servidor
                        DronState nuevoDron = new DronState(proximoIdDron++, jugadorId, jugador.x, jugador.y);
                        dronesActivos.put(jugadorId, nuevoDron);

                        System.out.println("[SERVER DEBUG] Dron CREADO para jugador " + jugadorId + ". Timer inicial: " + nuevoDron.temporizador);
                        Network.PaqueteDronEstado paqueteEstado = new Network.PaqueteDronEstado();
                        paqueteEstado.ownerId = jugador.id;
                        paqueteEstado.nuevoEstado = DronState.EstadoDron.APARECIENDO;
                        paqueteEstado.x = jugador.x; // Posición inicial
                        paqueteEstado.y = jugador.y;
                        servidor.sendToAllTCP(paqueteEstado);
                        // Notificamos al dueño que su petición fue exitosa y debe mostrar el mensaje
                        Network.PaqueteMensajeUI msg = new Network.PaqueteMensajeUI();
                        msg.mensaje = "Sembrando árbol...";
                        conexion.sendTCP(msg);

                        System.out.println("[SERVER] Jugador " + jugadorId + " ha invocado un dron.");
                    }
                } if (objeto instanceof Network.PaqueteBasuraDepositada) {
                    System.out.println("[SERVER] ¡Recibido PaqueteBasuraDepositada!");

                    int idJugadorQueActivo = conexion.getID();
                    PlayerState estadoJugador = jugadores.get(idJugadorQueActivo);

                    // 1. VALIDACIÓN: Nos aseguramos de que fue Tails quien tocó la planta.
                    if (estadoJugador != null ) {
                        System.out.println("[SERVER] Tails (ID: " + idJugadorQueActivo + ") ha activado la planta de tratamiento.");

                        // 2. CÁLCULO: Sumamos toda la basura que tienen todos los jugadores.
                        int basuraDepositadaEstaVez = 0;
                        for (int basuraDeJugador : puntajesBasuraIndividuales.values()) {
                            basuraDepositadaEstaVez += basuraDeJugador;
                        }

                        // 3. ACTUALIZACIÓN DE TOTALES:
                        basuraReciclada += basuraDepositadaEstaVez;
                        EstadisticasJugador stats = estadisticasJugadores.get(idJugadorQueActivo);
                        if (stats != null && basuraDepositadaEstaVez > 0) {
                            // Actualizamos sus puntos sumando la cantidad de basura reciclada.
                            stats.sumarObjetosReciclados(basuraDepositadaEstaVez);
                            System.out.println("[STATS] Jugador " + idJugadorQueActivo + " recicló " + basuraDepositadaEstaVez + " objetos.");
                        }
                        // 4. REINICIO DE CONTADORES:
                        puntajesBasuraIndividuales.replaceAll((id, valorActual) -> 0);
                        System.out.println("[SERVER DEBUG] Mapa de basuras después del reinicio: " + puntajesBasuraIndividuales.toString());

                        // 5. NOTIFICACIÓN A TODOS:
                        for (Integer idJugadorConectado : jugadores.keySet()) {
                            Network.PaqueteActualizacionPuntuacion paquetePuntaje = new Network.PaqueteActualizacionPuntuacion();
                            paquetePuntaje.nuevosAnillos = puntajesAnillosIndividuales.getOrDefault(idJugadorConectado, 0);
                            paquetePuntaje.nuevaBasura = puntajesBasuraIndividuales.getOrDefault(idJugadorConectado, 0);
                            paquetePuntaje.totalBasuraReciclada = basuraReciclada;
                            servidor.sendToTCP(idJugadorConectado, paquetePuntaje);
                        }
                        System.out.println("[SERVER] Paquetes de actualización de puntuación enviados a todos los jugadores.");
                    }
                } if (objeto instanceof Network.PaqueteBloqueDestruido paquete) {
                    PlayerState jugador = jugadores.get(paquete.idJugador);

                    // --- VALIDACIÓN (¡Muy importante para la seguridad!) ---
                    // Aquí comprobamos si la petición es válida.
                    // Por ahora, solo comprobaremos que el bloque exista en nuestra lista.
                    if (jugador != null && bloquesRompibles.containsKey(paquete.idBloque)) {

                        // ¡Petición válida! Destruimos el bloque en el estado del servidor.
                        bloquesRompibles.remove(paquete.idBloque);
                        System.out.println("[SERVER] El jugador " + paquete.idJugador + " (Knuckles) ha destruido el bloque ID: " + paquete.idBloque);

                        // --- LÓGICA DE JUEGO (copiada de tu LocalServer) ---
                        // Le damos al jugador los puntos de basura por destruir el bloque.
                        int puntajeActual = puntajesBasuraIndividuales.getOrDefault(paquete.idJugador, 0);
                        puntajesBasuraIndividuales.put(paquete.idJugador, puntajeActual + 1);
                        totalBasuraGlobal++; // Actualizamos el total del equipo
                        contaminationState.decrease(TRASH_CLEANUP_VALUE);
                        System.out.println("[SERVER] Basura recogida. Contaminación reducida a " + contaminationState.getPercentage());
                        // (Aquí deberías enviar los paquetes de actualización de puntuación y contaminación a todos)
                        EstadisticasJugador stats = estadisticasJugadores.get(paquete.idJugador);
                        if (stats != null) {
                            // Le damos los puntos correspondientes a limpiar una zona.
                            stats.sumarZonaLimpiada();
                            System.out.println("[STATS] Jugador " + paquete.idJugador + " (Knuckles) limpió una zona al destruir un bloque.");
                        }
                        // --- ORDENAR A TODOS LOS CLIENTES QUE DESTRUYAN EL BLOQUE ---
                        Network.PaqueteBloqueConfirmadoDestruido respuesta = new Network.PaqueteBloqueConfirmadoDestruido();
                        respuesta.idBloque = paquete.idBloque;
                        servidor.sendToAllTCP(respuesta); // ¡Enviamos la confirmación a todos!
                    }
                }  if (objeto instanceof Network.PaqueteSolicitudMatarAnimal paquete) {
                    // La lógica es prácticamente idéntica a la del LocalServer.
                    // Usamos 'synchronized' para evitar condiciones de carrera si dos eventos
                    // intentan matar al mismo animal a la vez.
                    synchronized (animalesActivos) {
                        AnimalState animal = animalesActivos.get(paquete.idAnimal);
                        if (animal != null && animal.estaVivo) {
                            animal.estaVivo = false;
                            System.out.println("[SERVER] Recibida solicitud para matar al animal ID: " + animal.id);

                            // No necesitamos crear un paquete nuevo. Como nuestro bucle principal ya envía
                            // el estado completo de 'animalesActivos' constantemente, este cambio
                            // se propagará automáticamente a todos los clientes en el siguiente tick.
                        }
                    }
                }
                if (objeto instanceof Network.PaqueteSolicitudHabilidadLimpieza) {
                    int jugadorId = conexion.getID();
                    float cooldownActual = cooldownsHabilidadLimpieza.getOrDefault(jugadorId, 0f);

                    // ¡El servidor es la autoridad! Comprueba si el cooldown ha terminado.
                    if (cooldownActual <= 0) {
                        System.out.println("[SERVER] Jugador " + jugadorId + " usó la habilidad de limpieza. ¡Aprobado!");

                        // 1. Aplicar el efecto al estado del juego.
                        contaminationState.decrease(100.0f);

                        // 2. RECOGER TODOS LOS ÍTEMS DE BASURA DEL MAPA.
                        // Creamos una copia de las claves para iterar de forma segura mientras eliminamos elementos.
                        ArrayList<Integer> idsItemsARecoger = new ArrayList<>();
                        synchronized (itemsActivos) { // Sincronizamos para evitar problemas de concurrencia
                            for (ItemState item : itemsActivos.values()) {
                                if (item.tipo == ItemState.ItemType.BASURA || item.tipo == ItemState.ItemType.PIEZA_PLASTICO) {
                                    idsItemsARecoger.add(item.id);
                                }
                            }
                        }

                        // Procesamos la recogida de cada ítem.
                        for (Integer idItem : idsItemsARecoger) {
                            // Para cada ítem de basura, aplicamos la misma lógica que si el jugador
                            // lo hubiera recogido normalmente.
                            procesarRecogidaItem(jugadorId, idItem);
                        }

                        // 2. Reiniciar el cooldown para este jugador.
                        cooldownsHabilidadLimpieza.put(jugadorId, COOLDOWN_HABILIDAD_SONIC);
                        EstadisticasJugador stats = estadisticasJugadores.get(jugadorId);
                        if (stats != null) {
                            stats.sumarZonaLimpiada(); // Suma los puntos definidos en EstadisticasJugador
                            System.out.println("[STATS] Jugador " + jugadorId + " (Sonic) limpió una zona con su habilidad.");
                        }
                        // 3. Notificar a TODOS los clientes del efecto.
                        // Usamos el paquete que ya tenías para esto.
                        Network.PaqueteHabilidadLimpiezaSonic notificacion = new Network.PaqueteHabilidadLimpiezaSonic();
                        servidor.sendToAllTCP(notificacion);

                        // También enviamos la actualización de contaminación inmediatamente.
                        Network.PaqueteActualizacionContaminacion paqueteContaminacion = new Network.PaqueteActualizacionContaminacion();
                        paqueteContaminacion.contaminationPercentage = contaminationState.getPercentage();
                        servidor.sendToAllTCP(paqueteContaminacion);

                    } else {
                        // Si el jugador intenta usar la habilidad antes de tiempo, el servidor simplemente lo ignora.
                        System.out.println("[SERVER] Jugador " + jugadorId + " intentó usar la habilidad, pero está en cooldown. ¡Ignorado!");
                    }
                }  if (objeto instanceof Network.PaqueteAtaqueJugadorAEnemigo paquete) {
                    synchronized (enemigosActivos) {
                        EnemigoState enemigo = enemigosActivos.get(paquete.idEnemigo);
                        if (enemigo != null && enemigo.vida > 0) {
                            enemigo.vida -= paquete.danio;
                            System.out.println("[SERVER] Enemigo ID " + enemigo.id + " recibió " + paquete.danio + " de daño. Vida restante: " + enemigo.vida);

                            // 1. Creamos el paquete de actualización de vida del enemigo.
                            Network.PaqueteActualizacionVidaEnemigo paqueteVida = new Network.PaqueteActualizacionVidaEnemigo();
                            paqueteVida.idEnemigo = enemigo.id;
                            paqueteVida.nuevaVida = enemigo.vida;

                            // 2. Lo enviamos a TODOS los clientes para que actualicen sus barras de vida.
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
                                    // Y si estamos en el mapa correcto.
                                    if ("maps/ZonaJefeN3.tmx".equals(mapaActualServidor)) {
                                        finalizarPartidaYEnviarResultados();
                                        return; // Salimos para no procesar más lógica (como generar portales).
                                    }
                                }
                                comprobarYGenerarPortalSiCorresponde();
                            }
                        }
                    }
                } if (objeto instanceof Network.ForzarFinDeJuegoDebug) {
                    System.out.println("[GAMESERVER] ¡Recibida orden de forzar fin de juego!");
                    // Simplemente llamamos a la función que ya hace todo el trabajo.
                    finalizarPartidaYEnviarResultados();
                }
            }

            public void disconnected(Connection conexion) {
                System.out.println("[SERVER] Conexión física perdida con el cliente ID: " + conexion.getID());
                desconectarJugador(conexion);
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
                        jugadorMasCercano.vida -= 5; // Daño del jefe.
                        System.out.println("[SERVER] JEFE atacó a Jugador ID " + jugadorMasCercano.id + ". Vida restante: " + jugadorMasCercano.vida);

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
                    jugadorMasCercano.vida -= 1;
                    System.out.println("[SERVER] Robot atacó a Jugador ID " + jugadorMasCercano.id + ". Vida restante: " + jugadorMasCercano.vida);

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
                EnemigoState nuevoEstado = new EnemigoState(proximoIdEnemigo++, bounds.x, bounds.y, 3, EnemigoState.EnemigoType.ROBOT);
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
                        contaminationState.decrease(5);
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
