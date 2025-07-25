package network;

import com.JSonic.uneg.*;
import com.JSonic.uneg.EntidadesVisuales.Player;
import com.JSonic.uneg.EntidadesVisuales.Sonic;
import com.JSonic.uneg.EntidadesVisuales.Tails;
import com.JSonic.uneg.Pantallas.EstadisticasJugador;
import com.JSonic.uneg.Pantallas.PantallaDeJuego;
import com.JSonic.uneg.State.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import network.interfaces.IGameClient;
import network.interfaces.IGameServer;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Una implementación de IGameServer que se ejecuta localmente en la memoria.
 * No utiliza la red real y actúa como el motor para el modo de un solo jugador.
 * Se encarga de toda la lógica del juego: IA de enemigos, generación de ítems,
 * reglas del juego y estado de los jugadores.
 */
public class LocalServer implements IGameServer {

    private final HashMap<Integer, PlayerState> jugadores = new HashMap<>();
    private final HashMap<Integer, EnemigoState> enemigosActivos = new HashMap<>();
    private final HashMap<Integer, ItemState> itemsActivos = new HashMap<>();
    private final HashMap<Integer, Integer> puntajesAnillos = new HashMap<>();
    private final HashMap<Integer, Integer> puntajesBasura = new HashMap<>();
    private final HashMap<Integer, EstadisticasJugador> estadisticasJugadores = new HashMap<>();
    private static final ContaminationState contaminationState = new ContaminationState();
    private final HashMap<Integer, String> destinosPortales = new HashMap<>();

    private static final float VELOCIDAD_ROBOTNIK = 60f;
    private static final float RANGO_DETENERSE_ROBOTNIK = 30f;
    private float tiempoGeneracionEnemigo = 0f;
    private final float INTERVALO_GENERACION_ENEMIGO = 5.0f;
    private int enemigosGeneradosEnNivelActual = 0;
    private int proximoIdEnemigo = 0;
    private float tiempoGeneracionTeleport = 0f;
    private boolean teleportGenerado = false;
    private String ultimoMapaProcesado = "";
    private int proximoIdItem = 0;
    private static final int MAX_ANILLOS = 50;
    private static final int MAX_BASURA = 12;
    private static final int MAX_PLASTICO = 12;
    private static final float INTERVALO_SPAWN_ANILLO = 1.0f;
    private static final float INTERVALO_SPAWN_BASURA = 5.0f;
    private static final float INTERVALO_SPAWN_PLASTICO = 5.0f;
    private float tiempoSpawnAnillo = 0f;
    private float tiempoSpawnBasura = 0f;
    private float tiempoSpawnPlastico = 0f;
    private static final float CONTAMINATION_RATE_PER_SECOND = 0.80f; // El % sube 0.80 puntos por segundo
    private static final float TRASH_CLEANUP_VALUE = 3f; // Cada basura recogida reduce el % en 2 puntos
    private float tiempoDesdeUltimaContaminacion = 0f;
    private static final float INTERVALO_ACTUALIZACION_CONTAMINACION = 1.0f; // 1 segundo
    private int esmeraldasRecogidasGlobal = 0;
    private final HashMap<String, Integer> enemigosPorMapa = new HashMap<>();

    private final HashMap<Integer, AnimalState> animalesActivos = new HashMap<>();
    private final HashMap<Integer, Rectangle> bloquesRompibles = new HashMap<>();
    private int proximoIdBloque = 30000; // Un rango de IDs para bloques
    private int proximoIdAnimal = 20000; // Usamos un ID base alto para evitar conflictos con otros IDs
    private float tiempoParaProximaMuerteAnimal = 20f; // Temporizador para la muerte secuencial (20 segundos)
    private boolean muertesAnimalesActivas = false;

    // Cola para paquetes que vienen "desde el cliente" hacia el servidor
    private final Queue<Object> paquetesEntrantes = new ConcurrentLinkedQueue<>();
    private boolean alMenosUnJugadorHaEnviadoPosicion = false;
    private float cooldownHabilidadLimpieza = 0f;
    private static final float COOLDOWN_HABILIDAD_SONIC = 40.0f;
    private LocalClient clienteLocal;
    private int proximoIdJugador = 1; // En modo local, siempre empezamos en 1
    private static final int ROBOT_SPEED = 1;
    private static final float ROBOT_DETECTION_RANGE = 300f;
    private static final float ROBOT_ATTACK_RANGE = 10f; // Usando el valor del código original
    private int basuraReciclada = 0;


    /**
     * Constructor del servidor local.
     * Aquí inicializamos los mapas de enemigos y otros estados necesarios.
     */
    public LocalServer() {
        // Poblamos el mapa con la cantidad de enemigos por nivel.
        enemigosPorMapa.put("maps/Zona1N1.tmx", 8);
        enemigosPorMapa.put("maps/ZonaJefeN1.tmx", 5);
        enemigosPorMapa.put("maps/Zona1N2.tmx", 10);
        enemigosPorMapa.put("maps/ZonaJefeN2.tmx", 8);
        enemigosPorMapa.put("maps/Zona1N3.tmx", 15);
        enemigosPorMapa.put("maps/Zona2N3.tmx", 20);
        enemigosPorMapa.put("maps/ZonaJefeN3.tmx", 10);
    }


    /**
     * Permite a la clase que nos crea (JSonicJuego) obtener la instancia del cliente.
     *
     * @return El cliente local asociado a este servidor.
     */
    public IGameClient getClient() {
        return this.clienteLocal;
    }

    /**
     * Reduce el porcentaje de contaminación global.
     *
     * @param porcentaje El valor porcentual a reducir.
     */
    public static void decreaseContamination(float porcentaje) {
        contaminationState.decrease(porcentaje);
    }

    /**
     * Inicia el servidor local. Este proceso incluye:
     * 1. Crear la instancia del cliente local.
     * 2. Crear el estado inicial del jugador (posición, ID, etc.).
     * 3. Inicializar las estadísticas del jugador.
     * 4. Enviar un paquete de bienvenida al cliente para confirmar la conexión.
     */
    @Override
    public void start() {
        System.out.println("[LOCAL SERVER] Servidor local iniciado.");

        // Crea la única instancia del cliente local y la guarda
        this.clienteLocal = new LocalClient(this);

        PlayerState nuevoEstado = new PlayerState();
        nuevoEstado.id = proximoIdJugador++; // Será el jugador 1
        nuevoEstado.x = 100; // Posición inicial X
        nuevoEstado.y = 100; // Posición inicial Y
        nuevoEstado.estadoAnimacion = Player.EstadoPlayer.IDLE_RIGHT;
        jugadores.put(nuevoEstado.id, nuevoEstado);
        puntajesAnillos.put(nuevoEstado.id, 0);
        puntajesBasura.put(nuevoEstado.id, 0);

        PlayerState.CharacterType tipoPersonaje = PantallaDeJuego.miPersonaje;
        String nombreDelPersonaje;
        if (tipoPersonaje != null) {
            // Convierte "SONIC" a "sonic"
            String nombreEnMinusculas = tipoPersonaje.toString().toLowerCase();
            // Convierte "sonic" a "Sonic"
            nombreDelPersonaje = nombreEnMinusculas.substring(0, 1).toUpperCase() + nombreEnMinusculas.substring(1);
        } else {
            nombreDelPersonaje = "Jugador";
        }

        // Creamos las estadísticas usando el nombre correcto
        EstadisticasJugador stats = new EstadisticasJugador(nombreDelPersonaje);
        estadisticasJugadores.put(nuevoEstado.id, stats);
        System.out.println("[STATS] Objeto de estadísticas creado para el jugador " + nuevoEstado.id);

        // Envia el paquete de bienvenida al cliente local
        Network.RespuestaAccesoPaquete respuesta = new Network.RespuestaAccesoPaquete();
        respuesta.mensajeRespuesta = "Bienvenido al modo local!";
        respuesta.tuEstado = nuevoEstado;
        this.clienteLocal.recibirPaqueteDelServidor(respuesta);
    }

    /**
     * Genera los portales de teletransporte para el nivel actual basándose en la información
     * del LevelManager y notifica al cliente.
     *
     * @param manejadorNivel El gestor del nivel actual.
     */
    private void generarPortales(LevelManager manejadorNivel) {
        int idBase = 10000;
        for (LevelManager.PortalInfo portal : manejadorNivel.obtenerPortales()) {
            ItemState estadoTele = new ItemState(idBase++, portal.x, portal.y, ItemState.ItemType.TELETRANSPORTE);
            itemsActivos.put(estadoTele.id, estadoTele);
            destinosPortales.put(estadoTele.id, portal.destinoMapa);

            Network.PaqueteItemNuevo paquete = new Network.PaqueteItemNuevo();
            paquete.estadoItem = estadoTele;
            clienteLocal.recibirPaqueteDelServidor(paquete);
        }
    }

    /**
     * Genera una cantidad fija de bloques rompibles en posiciones aleatorias
     * no colisionables del mapa actual.
     *
     * @param manejadorNivel El gestor del nivel actual.
     */
    private void generarBloquesParaElNivel(LevelManager manejadorNivel) {
        bloquesRompibles.clear();
        int cantidad = 5;
        Random random = new Random();
        System.out.println("[LOCAL SERVER] Generando " + cantidad + " bloques para el mapa: " + manejadorNivel.getNombreMapaActual());

        for (int i = 0; i < cantidad; i++) {
            int intentos = 0;
            boolean colocado = false;
            while (!colocado && intentos < 100) {
                float x = random.nextFloat() * (manejadorNivel.getAnchoMapaPixels() - 100f);
                float y = random.nextFloat() * (manejadorNivel.getAltoMapaPixels() - 100f);
                Rectangle bounds = new Rectangle(x, y, 100f, 100f);

                // Usamos la ventaja del LocalServer: ¡puede comprobar colisiones!
                if (!manejadorNivel.colisionaConMapa(bounds)) {
                    bloquesRompibles.put(proximoIdBloque++, bounds);
                    colocado = true;
                }
                intentos++;
            }
        }
    }


    /**
     * Genera una cantidad fija de animales en posiciones aleatorias no colisionables
     * del mapa actual y notifica al cliente.
     *
     * @param manejadorNivel El gestor del nivel actual.
     */
    private void generarAnimales(LevelManager manejadorNivel) {
        // Limpiamos la lista para que no se acumulen animales entre mapas
        // animalesActivos.clear();
        proximoIdAnimal = 20000; // Reiniciamos el contador de ID
        muertesAnimalesActivas = false; // Reseteamos la bandera al cambiar de mapa
        tiempoParaProximaMuerteAnimal = 20f; // Reiniciamos el temporizador

        int cantidadAnimales = 10;
        for (int i = 0; i < cantidadAnimales; i++) {
            int intentos = 0;
            boolean colocado = false;
            while (!colocado && intentos < 50) {
                // Genera una posición aleatoria dentro de los límites del mapa
                float x = MathUtils.random(0, manejadorNivel.getAnchoMapaPixels() - 32); // Restamos el tamaño del animal para que no se salga
                float y = MathUtils.random(0, manejadorNivel.getAltoMapaPixels() - 32);

                // Crea un rectángulo temporal para verificar colisiones.
                Rectangle animalBounds = new Rectangle(x, y, 32, 32);

                // Verifica colisión con objetos del mapa (capa "Colisiones")
                if (!manejadorNivel.colisionaConMapa(animalBounds)) {
                    String texturaPath = "Items/Conejo1.png"; // Asegúrate de que esta ruta sea correcta
                    AnimalState nuevoAnimal = new AnimalState(proximoIdAnimal++, x, y, texturaPath);

                    //  Lo guardamos en la lista del servidor
                    animalesActivos.put(nuevoAnimal.id, nuevoAnimal);

                    System.out.println("[LOCAL SERVER] Generando animal en X: " + x + ", Y: " + y);
                    colocado = true;
                }
                intentos++;
            }
            if (!colocado) {
                System.err.println("[LOCAL SERVER] No se pudo colocar el animal " + i + " después de " + intentos + " intentos.");
            }
        }
        System.out.println("[LOCAL SERVER] Generados y guardados " + animalesActivos.size() + " animales.");

        if (!animalesActivos.isEmpty()) {
            Network.PaqueteActualizacionAnimales paqueteInicial = new Network.PaqueteActualizacionAnimales();
            // Se crea una copia para evitar problemas de concurrencia.
            paqueteInicial.estadosAnimales = new HashMap<>(this.animalesActivos);
            clienteLocal.recibirPaqueteDelServidor(paqueteInicial);
            System.out.println("[LOCAL SERVER] Enviando paquete inicial con " + animalesActivos.size() + " animales al cliente.");
        }

    }


    /**
     * Busca el primer animal que sigue vivo en la lista de animales activos,
     * cambia su estado a "muerto" y notifica al cliente sobre este cambio.
     * Se utiliza cuando el nivel de contaminación es alto.
     */
    private void matarSiguienteAnimalVivo() {
        // Buscamos el primer animal que todavía esté vivo
        for (AnimalState animal : animalesActivos.values()) {
            if (animal.estaVivo) {
                animal.estaVivo = false; // El animal muere
                System.out.println("[LOCAL SERVER] Contaminación alta. Matando animal ID: " + animal.id);

                // Notificamos al cliente del cambio de estado.
                Network.PaqueteActualizacionAnimales paquete = new Network.PaqueteActualizacionAnimales();
                paquete.estadosAnimales = new HashMap<>();
                paquete.estadosAnimales.put(animal.id, animal); // Enviamos solo el estado del animal que cambió
                clienteLocal.recibirPaqueteDelServidor(paquete);

                return; // Salimos del metodo una vez que hemos matado a un animal.
            }
        }
        System.out.println("[LOCAL SERVER] No se encontraron más animales vivos para matar.");
    }

    /**
     * Actualiza el estado de los animales basado en el nivel de contaminación.
     * Si la contaminación es alta, inicia un proceso de muerte secuencial de animales.
     *
     * @param deltaTime El tiempo transcurrido desde el último fotograma.
     */
    private void actualizarEstadoAnimalesPorContaminacion(float deltaTime) {
        if (contaminationState.getPercentage() >= 50) {
            if (!muertesAnimalesActivas) {
                muertesAnimalesActivas = true; // Activamos la bandera.
                matarSiguienteAnimalVivo();    // Matamos un animal INMEDIATAMENTE.
                tiempoParaProximaMuerteAnimal = 20f; // Reiniciamos el temporizador para la SIGUIENTE muerte.
            } else {
                // Si la bandera ya está activa, continuamos con el temporizador normal.
                tiempoParaProximaMuerteAnimal -= deltaTime;
                if (tiempoParaProximaMuerteAnimal <= 0) {
                    matarSiguienteAnimalVivo(); // Matamos otro animal.
                    tiempoParaProximaMuerteAnimal = 20f; // Reiniciamos el temporizador.
                }
            }
        } else {
            // Si la contaminación baja del 50%, detenemos el proceso.
            if (muertesAnimalesActivas) {
                muertesAnimalesActivas = false;
                tiempoParaProximaMuerteAnimal = 20f; // Reiniciamos el temporizador para la próxima vez.
            }
        }
    }

    /**
     * Genera una Esmeralda del Caos en la posición definida en el mapa, si existe.
     *
     * @param manejadorNivel El gestor del nivel actual.
     */
    private void generarEsmeralda(LevelManager manejadorNivel) {
        Vector2 posEsmeralda = manejadorNivel.obtenerPosicionEsmeralda();

        // Solo la generamos si el LevelManager encontró una posición en el mapa
        if (posEsmeralda != null) {
            System.out.println("[LOCAL SERVER] Generando Esmeralda en el mapa.");
            ItemState estadoEsmeralda = new ItemState(proximoIdItem++, posEsmeralda.x, posEsmeralda.y, ItemState.ItemType.ESMERALDA);
            itemsActivos.put(estadoEsmeralda.id, estadoEsmeralda);

            // Notificamos al cliente para que la dibuje
            Network.PaqueteItemNuevo paquete = new Network.PaqueteItemNuevo();
            paquete.estadoItem = estadoEsmeralda;
            clienteLocal.recibirPaqueteDelServidor(paquete);
        } else {
            System.out.println("[LOCAL SERVER] No hay esmeralda definida para este mapa.");
        }
    }

    /**
     * Este es el "game loop" del servidor. Se llamará desde PantallaDeJuego.
     * Procesa paquetes entrantes, actualiza la lógica del juego (IA, generación de ítems, etc.)
     * y envía actualizaciones de estado al cliente.
     *
     * @param deltaTime        El tiempo transcurrido desde el último fotograma.
     * @param manejadorNivel   El gestor del nivel actual, para colisiones y datos del mapa.
     * @param personajeJugable La instancia del jugador principal para interacción directa.
     */
    @Override
    public void update(float deltaTime, LevelManager manejadorNivel, Player personajeJugable) {

        while (!paquetesEntrantes.isEmpty()) {
            Object objeto = paquetesEntrantes.poll();

            if (objeto instanceof Network.PaquetePosicionJugador paquete) {
                PlayerState estadoJugador = jugadores.get(paquete.id);
                if (estadoJugador != null) {
                    Rectangle nuevosLimites = new Rectangle(paquete.x, paquete.y, 32, 48);

                    // Verificamos que la nueva posición no colisione ni con el mapa ni con un animal.
                    if (!manejadorNivel.colisionaConMapa(nuevosLimites)) {
                        estadoJugador.x = paquete.x;
                        estadoJugador.y = paquete.y;
                    }
                    // Siempre actualizamos la animación, incluso si el movimiento fue bloqueado.
                    estadoJugador.estadoAnimacion = paquete.estadoAnimacion;
                    // No necesitamos retransmitir porque solo hay un jugador.
                    if (!alMenosUnJugadorHaEnviadoPosicion) {
                        System.out.println("[LOCAL SERVER] Primera posición real recibida. ¡La IA puede comenzar!");
                        alMenosUnJugadorHaEnviadoPosicion = true;
                    }
                }
            } else if (objeto instanceof Network.PaqueteHabilidadLimpiezaSonic) {
                System.out.println("[SERVER] ¡Recibida notificación de habilidad de limpieza de Sonic!");
                decreaseContamination(100.0f);
            } else if (objeto instanceof Network.PaqueteSolicitudRecogerItem paquete) {
                ItemState itemRecogido = itemsActivos.get(paquete.idItem);

                if (itemRecogido != null) {

                    if (itemRecogido.tipo == ItemState.ItemType.ESMERALDA) {
                        itemsActivos.remove(paquete.idItem); // La quitamos del juego
                        esmeraldasRecogidasGlobal++; // Incrementamos el contador global
                        System.out.println("[LOCAL SERVER] ¡Esmeralda recogida! Total: " + esmeraldasRecogidasGlobal);

                        if (esmeraldasRecogidasGlobal >= 7) {
                            // Obtenemos el estado del único jugador en modo local (su ID es 1).
                            PlayerState estadoJugador = jugadores.get(1);

                            // Comprobamos si el personaje es Sonic y si no está ya transformado.
                            if (estadoJugador != null && !estadoJugador.isSuper) {
                                System.out.println("[LOCAL SERVER] ¡7 Esmeraldas! Activando Super Sonic en modo local.");

                                // Actualizamos el estado del jugador en el servidor.
                                estadoJugador.isSuper = true;
                                estadoJugador.vida = Player.MAX_VIDA;
                                System.out.println("[LOCAL SERVER] Vida del jugador restaurada al máximo: " + estadoJugador.vida);
                                Network.PaqueteActualizacionVida paqueteVida = new Network.PaqueteActualizacionVida();
                                paqueteVida.idJugador = estadoJugador.id;
                                paqueteVida.nuevaVida = estadoJugador.vida;
                                clienteLocal.recibirPaqueteDelServidor(paqueteVida);
                                if (personajeJugable instanceof Sonic) {
                                    personajeJugable.setSuper(true);
                                }
                            }
                        }

                        // Notifica a TODOS los clientes del nuevo total de esmeraldas
                        Network.PaqueteActualizacionEsmeraldas paqueteEsmeraldas = new Network.PaqueteActualizacionEsmeraldas();
                        paqueteEsmeraldas.totalEsmeraldas = esmeraldasRecogidasGlobal;
                        clienteLocal.recibirPaqueteDelServidor(paqueteEsmeraldas);

                        // Notifica que el ítem específico fue eliminado
                        Network.PaqueteItemEliminado paqueteEliminado = new Network.PaqueteItemEliminado();
                        paqueteEliminado.idItem = paquete.idItem;
                        clienteLocal.recibirPaqueteDelServidor(paqueteEliminado);

                    } else if (itemRecogido.tipo == ItemState.ItemType.TELETRANSPORTE) {
                        System.out.println("[LOCAL SERVER] Jugador ha activado el teletransportador.");
                        String destinoMapa = destinosPortales.get(paquete.idItem);

                        String mapaActual = manejadorNivel.getNombreMapaActual();
                        if (mapaActual.equals("maps/ZonaJefeN1.tmx")) {
                            GestorDeProgreso.guardarProgresoDeNivel(2);
                        } else if (mapaActual.equals("maps/ZonaJefeN2.tmx")) {
                            GestorDeProgreso.guardarProgresoDeNivel(3);
                        }
                        if (destinoMapa == null) {
                            System.err.println("[LOCAL SERVER] Error: El portal con ID " + paquete.idItem + " no tiene un mapa de destino definido.");
                            return;
                        }
                        itemsActivos.remove(paquete.idItem);
                        destinosPortales.remove(paquete.idItem); // Limpieza del mapa de destinos.

                        manejadorNivel.cargarNivel(destinoMapa);
                        com.badlogic.gdx.math.Vector2 llegada = manejadorNivel.obtenerPosicionLlegada();

                        // Crea la ORDEN de cambio de mapa
                        Network.PaqueteOrdenCambiarMapa orden = new Network.PaqueteOrdenCambiarMapa();
                        orden.nuevoMapa = destinoMapa;

                        // Envia la orden al cliente local
                        clienteLocal.recibirPaqueteDelServidor(orden);

                        // notificación de que el portal fue eliminado
                        Network.PaqueteItemEliminado paqueteEliminado = new Network.PaqueteItemEliminado();
                        paqueteEliminado.idItem = paquete.idItem;
                        clienteLocal.recibirPaqueteDelServidor(paqueteEliminado);

                    }
                    // CASO GENERAL: Es un ítem normal
                    else {
                        itemsActivos.remove(paquete.idItem); // Lo eliminamos
                        System.out.println("[LOCAL SERVER] Ítem con ID " + paquete.idItem + " recogido.");

                        int idJugador = 1;
                        if (itemRecogido.tipo == ItemState.ItemType.ANILLO) {
                            int puntajeActual = puntajesAnillos.getOrDefault(idJugador, 0);
                            puntajesAnillos.put(idJugador, puntajeActual + 1);
                            int anillosAhora = puntajesAnillos.get(idJugador);
                            if (anillosAhora >= 100) {
                                System.out.println("[LOCAL SERVER] ¡100 anillos! Canjeando por vida.");

                                // Restamos los anillos EN EL SERVIDOR.
                                puntajesAnillos.put(idJugador, anillosAhora - 100);

                                // Aumentamos la vida del jugador EN EL SERVIDOR.
                                PlayerState estadoJugador = jugadores.get(idJugador);
                                if (estadoJugador != null) {
                                    estadoJugador.vida = Math.min(estadoJugador.vida + 100, Player.MAX_VIDA);

                                    // ENVIAMOS un paquete para notificar al cliente de su nueva vida.
                                    Network.PaqueteActualizacionVida paqueteVida = new Network.PaqueteActualizacionVida();
                                    paqueteVida.idJugador = idJugador;
                                    paqueteVida.nuevaVida = estadoJugador.vida;
                                    clienteLocal.recibirPaqueteDelServidor(paqueteVida);
                                }
                            }
                        } else if (itemRecogido.tipo == ItemState.ItemType.BASURA || itemRecogido.tipo == ItemState.ItemType.PIEZA_PLASTICO) {
                            int puntajeActual = puntajesBasura.getOrDefault(idJugador, 0);
                            puntajesBasura.put(idJugador, puntajeActual + 1);
                            contaminationState.decrease(TRASH_CLEANUP_VALUE);
                            EstadisticasJugador stats = estadisticasJugadores.get(idJugador);
                            if (stats != null) {
                                stats.sumarObjetosReciclados(1); // Sumamos 1 por cada pieza
                                System.out.println("[STATS] Jugador " + idJugador + " recogió 1 pieza de basura. Puntos actualizados.");
                            }
                            System.out.println("[LOCAL SERVER] Basura recogida. Contaminación reducida a: " + contaminationState.getPercentage() + "%");
                        }

                        // Crea el paquete de actualización de puntuación
                        Network.PaqueteActualizacionPuntuacion paquetePuntaje = new Network.PaqueteActualizacionPuntuacion();
                        paquetePuntaje.nuevosAnillos = puntajesAnillos.get(idJugador);
                        paquetePuntaje.nuevaBasura = puntajesBasura.get(idJugador);

                        // Envia el paquete de puntuación al cliente local
                        clienteLocal.recibirPaqueteDelServidor(paquetePuntaje);

                        // Envia la notificación de eliminación
                        Network.PaqueteItemEliminado paqueteEliminado = new Network.PaqueteItemEliminado();
                        paqueteEliminado.idItem = paquete.idItem;
                        clienteLocal.recibirPaqueteDelServidor(paqueteEliminado);
                    }
                }
            } else if (objeto instanceof Network.PaqueteAnimacionEnemigoTerminada paquete) {
                // El cliente nos informa que la animación de un enemigo terminó.
                EnemigoState enemigo = enemigosActivos.get(paquete.idEnemigo);
                if (enemigo != null) {
                    // Lo ponemos en el estado especial para que la IA lo reevalúe.
                    enemigo.estadoAnimacion = EnemigoState.EstadoEnemigo.POST_ATAQUE;
                    enemigo.tiempoEnEstado = 0;
                }
            } else if (objeto instanceof Network.PaqueteSolicitudMatarAnimal paquete) {
                AnimalState animal = animalesActivos.get(paquete.idAnimal);
                if (animal != null && animal.estaVivo) {
                    animal.estaVivo = false; // Marcamos el animal como muerto
                    System.out.println("[LOCAL SERVER] Enemigo mató al animal ID: " + animal.id);

                    // Notificamos al cliente del cambio de estado.
                    Network.PaqueteActualizacionAnimales paqueteUpdate = new Network.PaqueteActualizacionAnimales();
                    paqueteUpdate.estadosAnimales = new HashMap<>();
                    paqueteUpdate.estadosAnimales.put(animal.id, animal);
                    clienteLocal.recibirPaqueteDelServidor(paqueteUpdate);
                }
            }

            //Para que los bloques de basura sean contados como basura
            else if (objeto instanceof Network.PaqueteBloqueDestruido paquete) {
                System.out.println("[LOCAL SERVER] Bloque destruido por jugador ID: " + paquete.idJugador);
                EstadisticasJugador stats = estadisticasJugadores.get(paquete.idJugador);
                if (stats != null) {
                    stats.sumarZonaLimpiada(); // ¡+100 puntos por cada bloque!
                    System.out.println("[STATS] Jugador " + paquete.idJugador + " (Knuckles) destruyó un bloque. Puntos actualizados.");
                }
                int puntajeActual = puntajesBasura.getOrDefault(paquete.idJugador, 0);
                puntajesBasura.put(paquete.idJugador, puntajeActual + 1);

                contaminationState.decrease(TRASH_CLEANUP_VALUE);
                System.out.println("[LOCAL SERVER] Contaminación reducida a: " + contaminationState.getPercentage() + "%");

                Network.PaqueteActualizacionPuntuacion paquetePuntaje = new Network.PaqueteActualizacionPuntuacion();
                paquetePuntaje.nuevosAnillos = puntajesAnillos.getOrDefault(paquete.idJugador, 0);
                paquetePuntaje.nuevaBasura = puntajesBasura.get(paquete.idJugador);
                paquetePuntaje.totalBasuraReciclada = this.basuraReciclada;
                clienteLocal.recibirPaqueteDelServidor(paquetePuntaje);

                System.out.println("[LOCAL SERVER] Enviando confirmación de destrucción para el bloque ID: " + paquete.idBloque);
                Network.PaqueteBloqueConfirmadoDestruido confirmacion = new Network.PaqueteBloqueConfirmadoDestruido();
                confirmacion.idBloque = paquete.idBloque;
                clienteLocal.recibirPaqueteDelServidor(confirmacion); // "Enviamos" la confirmación.
            } else if (objeto instanceof Network.PaqueteBasuraDepositada paquete) {
                System.out.println("[LOCAL SERVER] Solicitud para depositar " + paquete.cantidad + " de basura recibida.");
                int idJugador = 1;

                // Añadimos la cantidad depositada al total reciclado.
                this.basuraReciclada += paquete.cantidad;

                // Reiniciamos el contador de basura del jugador a 0.
                puntajesBasura.put(idJugador, 0);

                EstadisticasJugador stats = estadisticasJugadores.get(idJugador);
                if (stats != null) {
                    stats.sumarObjetosReciclados(paquete.cantidad);
                    System.out.println("[STATS] Jugador 1 recicló " + paquete.cantidad + " objetos.");
                }

                if (paquete.cantidad >= 5 && personajeJugable instanceof Tails) {
                    // Lógica de Curación: Si se cumplió la condición, curamos al jugador.
                    PlayerState estadoJugador = jugadores.get(idJugador);
                    if (estadoJugador != null) {
                        // Sumamos 10 a la vida actual.
                        int vidaNueva = estadoJugador.vida + 10;
                        personajeJugable.mostrarMensaje("Basura reciclada +10 SALUD!");
                        estadoJugador.vida = Math.min(vidaNueva, Player.MAX_VIDA);

                        System.out.println("[LOCAL SERVER] Jugador curado. Vida nueva: " + estadoJugador.vida);

                        // Notificación de Vida: Enviamos un paquete para que la UI de la barra de vida se actualice.
                        Network.PaqueteActualizacionVida paqueteVida = new Network.PaqueteActualizacionVida();
                        paqueteVida.idJugador = idJugador;
                        paqueteVida.nuevaVida = estadoJugador.vida;
                        clienteLocal.recibirPaqueteDelServidor(paqueteVida);
                    }
                    // Enviamos la actualización completa al cliente.
                    Network.PaqueteActualizacionPuntuacion paquetePuntaje = new Network.PaqueteActualizacionPuntuacion();
                    paquetePuntaje.nuevosAnillos = puntajesAnillos.getOrDefault(idJugador, 0);
                    paquetePuntaje.nuevaBasura = puntajesBasura.get(idJugador); // Será 0
                    paquetePuntaje.totalBasuraReciclada = this.basuraReciclada; // Enviamos el nuevo total
                    clienteLocal.recibirPaqueteDelServidor(paquetePuntaje);
                } else personajeJugable.mostrarMensaje("Recoge por lo menos 5 basuras");

            } else if (objeto instanceof Network.PaqueteSolicitudHabilidadLimpieza) {
                if (cooldownHabilidadLimpieza <= 0) {
                    System.out.println("[LOCAL SERVER] Habilidad de limpieza de Sonic activada.");

                    // Reiniciamos el cooldown del servidor.
                    cooldownHabilidadLimpieza = COOLDOWN_HABILIDAD_SONIC;

                    // Aplicamos el efecto al estado del juego.
                    decreaseContamination(100.0f);
                    EstadisticasJugador stats = estadisticasJugadores.get(1);
                    if (stats != null) {
                        stats.sumarZonaLimpiada();
                        System.out.println("[STATS] Jugador 1 limpió una zona con habilidad especial.");
                    }


                    // Recogemos la basura (lógica que ya tenías).
                    for (ItemState item : new ArrayList<>(itemsActivos.values())) {
                        if (item.tipo == ItemState.ItemType.BASURA || item.tipo == ItemState.ItemType.PIEZA_PLASTICO) {
                            procesarRecogidaItem(1, item.id);
                        }
                    }

                    // Enviamos la notificación de éxito al cliente.
                    Network.PaqueteHabilidadLimpiezaSonic notificacion = new Network.PaqueteHabilidadLimpiezaSonic();
                    clienteLocal.recibirPaqueteDelServidor(notificacion);
                } else {
                    // Si el jugador lo intenta antes de tiempo, el servidor local lo ignora.
                    System.out.println("[LOCAL SERVER] Habilidad en cooldown. Solicitud ignorada.");
                }
            } else if (objeto instanceof Network.PaqueteAtaqueJugadorAEnemigo paquete) {
                EnemigoState enemigo = enemigosActivos.get(paquete.idEnemigo);

                if (enemigo != null && enemigo.vida > 0) {
                    enemigo.vida -= paquete.danio;

                    if (enemigo.vida <= 0) {
                        System.out.println("[LOCAL SERVER] ¡Enemigo ID " + enemigo.id + " derrotado!");
                        enemigosActivos.remove(enemigo.id);

                        EstadisticasJugador stats = estadisticasJugadores.get(1); // En local, es el jugador 1
                        if (stats != null) {
                            stats.sumarEnemigoDerrotado();
                            System.out.println("[STATS] Jugador 1 derrotó a un enemigo.");
                        }

                        if (enemigo.tipo == EnemigoState.EnemigoType.ROBOTNIK) {
                            if (manejadorNivel.getNombreMapaActual().equals("maps/ZonaJefeN3.tmx")) {
                                System.out.println("[VICTORIA FINAL] ¡Jefe final derrotado en la última zona!");
                                finalizarPartidaYEnviarResultados(); // Se muestran las estadísticas.
                                return;
                            } else {
                                // Si no es el jefe final, solo generamos el portal.
                                System.out.println("[VICTORIA PARCIAL] Jefe de zona derrotado. Abriendo portal...");
                                comprobarYGenerarPortalSiCorresponde(manejadorNivel);
                            }

                        } else {
                            // Si el enemigo derrotado NO era Robotnik, comprobamos si era el último del mapa.
                            comprobarYGenerarPortalSiCorresponde(manejadorNivel);
                        }

                        Network.PaqueteEntidadEliminada notificacionMuerte = new Network.PaqueteEntidadEliminada();
                        notificacionMuerte.idEntidad = enemigo.id;
                        notificacionMuerte.esJugador = false;

                        // Enviamos la orden al cliente local.
                        clienteLocal.recibirPaqueteDelServidor(notificacionMuerte);
                    }
                }
            } else if (objeto instanceof Network.ForzarFinDeJuegoDebug) {
                System.out.println("[LOCAL SERVER] ¡Recibida orden de forzar fin de juego!");

                finalizarPartidaYEnviarResultados();

                break;
            } else if (objeto instanceof Network.PaqueteHabilidadDronUsada) {
                System.out.println("[LOCAL SERVER] Recibida notificación de uso de habilidad de dron.");
                int idJugador = 1; // En modo local, el jugador siempre es el ID 1

                // a. Obtenemos la basura actual DESDE EL SERVIDOR
                int basuraActual = puntajesBasura.getOrDefault(idJugador, 0);

                // b. Comprobamos de nuevo la condición (por si acaso) y aplicamos el coste
                if (basuraActual >= 20) {
                    puntajesBasura.put(idJugador, basuraActual - 20);
                }

                // c. Creamos y enviamos el paquete de actualización AHORA SÍ con el valor correcto
                Network.PaqueteActualizacionPuntuacion paquetePuntaje = new Network.PaqueteActualizacionPuntuacion();
                paquetePuntaje.nuevosAnillos = puntajesAnillos.getOrDefault(idJugador, 0);
                paquetePuntaje.nuevaBasura = puntajesBasura.get(idJugador); // El nuevo valor con el coste restado
                paquetePuntaje.totalBasuraReciclada = this.basuraReciclada;

                // d. Enviamos el paquete al cliente para que la UI se actualice de forma autoritaria
                clienteLocal.recibirPaqueteDelServidor(paquetePuntaje);
            }

        }
        contaminationState.increase(CONTAMINATION_RATE_PER_SECOND * deltaTime);

        tiempoDesdeUltimaContaminacion += deltaTime;
        if (tiempoDesdeUltimaContaminacion >= INTERVALO_ACTUALIZACION_CONTAMINACION) {
            Network.PaqueteActualizacionContaminacion paquete = new Network.PaqueteActualizacionContaminacion();
            paquete.contaminationPercentage = contaminationState.getPercentage();
            clienteLocal.recibirPaqueteDelServidor(paquete);

            tiempoDesdeUltimaContaminacion = 0f; // Reseteamos el temporizador
        }

        if (cooldownHabilidadLimpieza > 0) {
            cooldownHabilidadLimpieza -= deltaTime;
        }

        // LÓGICA DE CAMBIO DE MAPA
        String mapaActual = manejadorNivel.getNombreMapaActual();
        if (!mapaActual.equals(ultimoMapaProcesado)) {
            System.out.println("[LOCAL SERVER] Detectado cambio de mapa a: " + mapaActual);
            ultimoMapaProcesado = mapaActual;

            // Limpiar entidades del mapa anterior
            enemigosActivos.clear();
            itemsActivos.clear();
            destinosPortales.clear(); // Importante para los portales
            animalesActivos.clear(); //para los animales
            bloquesRompibles.clear();


            // Reiniciar temporizadores de generación
            teleportGenerado = false;
            tiempoGeneracionTeleport = 0f;
            tiempoGeneracionEnemigo = 0f;
            enemigosGeneradosEnNivelActual = 0;
            tiempoSpawnAnillo = 0f;
            tiempoSpawnBasura = 0f;
            tiempoSpawnPlastico = 0f;

            // Regenerar entidades para el nuevo mapa
            generarAnimales(manejadorNivel);
            generarBloquesParaElNivel(manejadorNivel);
            generarNuevosItems(0f, manejadorNivel);
            generarEsmeralda(manejadorNivel);

            Network.PaqueteSincronizarBloques paqueteSync = new Network.PaqueteSincronizarBloques();
            paqueteSync.todosLosBloques = new HashMap<>(this.bloquesRompibles);
            clienteLocal.recibirPaqueteDelServidor(paqueteSync);
            System.out.println("[LOCAL SERVER] Enviando estado de bloques al cliente local.");

            //  Volver a crear a Robotnik en el nuevo mapa (si es necesario)
            if (mapaActual.contains("ZonaJefe")) {
                EnemigoState estadoRobotnik = new EnemigoState(999, 300, 100, 100, EnemigoState.EnemigoType.ROBOTNIK);
                this.enemigosActivos.put(estadoRobotnik.id, estadoRobotnik);
                Network.PaqueteEnemigoNuevo paqueteRobotnik = new Network.PaqueteEnemigoNuevo();
                paqueteRobotnik.estadoEnemigo = estadoRobotnik;
                this.clienteLocal.recibirPaqueteDelServidor(paqueteRobotnik);
            }


        }


        if (alMenosUnJugadorHaEnviadoPosicion) {
            actualizarEstadoAnimalesPorContaminacion(deltaTime);
            actualizarEnemigosAI(deltaTime, manejadorNivel, personajeJugable);
        }
        generarNuevosItems(deltaTime, manejadorNivel);
        generarEnemigosControlados(deltaTime, manejadorNivel);
        if (!animalesActivos.isEmpty()) {
            Network.PaqueteActualizacionAnimales paqueteUpdateAnimales = new Network.PaqueteActualizacionAnimales();
            paqueteUpdateAnimales.estadosAnimales = this.animalesActivos;
            clienteLocal.recibirPaqueteDelServidor(paqueteUpdateAnimales);
        }

        if (!enemigosActivos.isEmpty()) {
            Network.PaqueteActualizacionEnemigos paqueteUpdate = new Network.PaqueteActualizacionEnemigos();
            paqueteUpdate.estadosEnemigos = this.enemigosActivos;
            clienteLocal.recibirPaqueteDelServidor(paqueteUpdate);
        }
    }

    /**
     * Procesa la acción de un jugador al recoger un ítem.
     * Elimina el ítem del mundo del juego y aplica su efecto (puntos, reducción de contaminación, etc.).
     * Notifica al cliente para que actualice la interfaz y elimine el ítem visualmente.
     *
     * @param idJugador El ID del jugador que recoge el ítem.
     * @param idItem    El ID del ítem recogido.
     */
    private void procesarRecogidaItem(int idJugador, int idItem) {
        ItemState itemRecogido = itemsActivos.remove(idItem);
        if (itemRecogido == null) return;

        if (itemRecogido.tipo == ItemState.ItemType.ANILLO) {
            int puntaje = puntajesAnillos.getOrDefault(idJugador, 0);
            puntajesAnillos.put(idJugador, puntaje + 1);
        } else if (itemRecogido.tipo == ItemState.ItemType.BASURA || itemRecogido.tipo == ItemState.ItemType.PIEZA_PLASTICO) {
            int puntaje = puntajesBasura.getOrDefault(idJugador, 0);
            puntajesBasura.put(idJugador, puntaje + 1);
            contaminationState.decrease(TRASH_CLEANUP_VALUE);
        }

        // Notificar al cliente (actualización de puntaje y eliminación de ítem)
        Network.PaqueteActualizacionPuntuacion paquetePuntaje = new Network.PaqueteActualizacionPuntuacion();
        paquetePuntaje.nuevosAnillos = puntajesAnillos.getOrDefault(idJugador, 0);
        paquetePuntaje.nuevaBasura = puntajesBasura.getOrDefault(idJugador, 0);
        clienteLocal.recibirPaqueteDelServidor(paquetePuntaje);

        Network.PaqueteItemEliminado paqueteEliminado = new Network.PaqueteItemEliminado();
        paqueteEliminado.idItem = idItem;
        clienteLocal.recibirPaqueteDelServidor(paqueteEliminado);
    }

    /**
     * Actualiza la inteligencia artificial (IA) de todos los enemigos activos.
     * Determina el comportamiento del enemigo (perseguir, atacar, estar inactivo)
     * basándose en la posición del jugador y las reglas del juego.
     *
     * @param deltaTime        El tiempo desde el último fotograma.
     * @param manejadorNivel   El gestor del nivel para comprobaciones de colisión.
     * @param personajeJugable El objeto del jugador principal para obtener su estado.
     */
    private void actualizarEnemigosAI(float deltaTime, LevelManager manejadorNivel, Player personajeJugable) {
        PlayerState jugador = jugadores.get(1);
        if (jugador == null)
            return;

        for (EnemigoState enemigo : enemigosActivos.values()) {
            // Actualiza el temporizador de ataque de cada enemigo.
            enemigo.actualizar(deltaTime);
            if (jugador == null) {
                enemigo.estadoAnimacion = enemigo.mirandoDerecha ? EnemigoState.EstadoEnemigo.IDLE_RIGHT : EnemigoState.EstadoEnemigo.IDLE_LEFT;
                continue; // Pasa al siguiente enemigo.
            }

            // --- Lógica para Robotnik (Jefe) ---
            if (enemigo.tipo == EnemigoState.EnemigoType.ROBOTNIK) {
                float distanciaX = jugador.x - enemigo.x;
                float distanciaY = jugador.y - enemigo.y;
                float distancia = new com.badlogic.gdx.math.Vector2(distanciaX, distanciaY).len();

                if (distancia > RANGO_DETENERSE_ROBOTNIK) {
                    // Lógica de movimiento (sin cambios).
                    float velocidadMovimiento = VELOCIDAD_ROBOTNIK * deltaTime;
                    com.badlogic.gdx.math.Vector2 direccionDeseada = new com.badlogic.gdx.math.Vector2(distanciaX, distanciaY).nor();
                    enemigo.x += direccionDeseada.x * velocidadMovimiento;
                    enemigo.y += direccionDeseada.y * velocidadMovimiento;
                    enemigo.mirandoDerecha = (direccionDeseada.x > 0);
                    enemigo.estadoAnimacion = enemigo.mirandoDerecha ? EnemigoState.EstadoEnemigo.RUN_RIGHT : EnemigoState.EstadoEnemigo.RUN_LEFT;
                } else {

                    // El jefe está en rango de ataque.
                    enemigo.estadoAnimacion = enemigo.mirandoDerecha ? EnemigoState.EstadoEnemigo.IDLE_RIGHT : EnemigoState.EstadoEnemigo.IDLE_LEFT;


                    // Lógica de Ataque y Daño para Robotnik
                    if (enemigo.puedeAtacar()) {
                        enemigo.reiniciarCooldownAtaque();

                        int idJugador = jugador.id;
                        int anillosActuales = puntajesAnillos.getOrDefault(idJugador, 0);

                        if (anillosActuales == 0) {
                            // Sin anillos, recibe 15 de daño del jefe.
                            jugador.vida -= 15;
                            System.out.println("[LOCAL SERVER] JEFE golpeó a Jugador " + idJugador + " sin anillos. Vida restante: " + jugador.vida);
                        } else {
                            // Con anillos, recibe el daño normal del jefe (que eran 5 puntos).
                            jugador.vida -= 2;
                            System.out.println("[LOCAL SERVER] JEFE golpeó a Jugador " + idJugador + " con anillos. Vida restante: " + jugador.vida);
                        }

                        // Notifica al cliente de su nueva vida.
                        Network.PaqueteActualizacionVida paqueteVida = new Network.PaqueteActualizacionVida();
                        paqueteVida.idJugador = jugador.id;
                        paqueteVida.nuevaVida = jugador.vida;
                        clienteLocal.recibirPaqueteDelServidor(paqueteVida);

                        // Comprueba si el jugador ha sido derrotado.
                        if (jugador.vida <= 0) {
                            jugadores.remove(jugador.id);
                            Network.PaqueteEntidadEliminada notificacionMuerte = new Network.PaqueteEntidadEliminada();
                            notificacionMuerte.idEntidad = jugador.id;
                            notificacionMuerte.esJugador = true;
                            clienteLocal.recibirPaqueteDelServidor(notificacionMuerte);
                        }
                    }
                }

                continue; // Finaliza la lógica para Robotnik.
            }

            //  Lógica para Robots Normales
            int dx = (int) (jugador.x - enemigo.x);
            int dy = (int) (jugador.y - enemigo.y);
            int distance = (int) Math.sqrt(dx * dx + dy * dy);

            if (enemigo.estadoAnimacion == EnemigoState.EstadoEnemigo.HIT_RIGHT || enemigo.estadoAnimacion == EnemigoState.EstadoEnemigo.HIT_LEFT) {
                continue;
            }

            EnemigoState.EstadoEnemigo estadoAnterior = enemigo.estadoAnimacion;

            if (distance <= ROBOT_ATTACK_RANGE) {
                enemigo.estadoAnimacion = dx > 0 ? EnemigoState.EstadoEnemigo.HIT_RIGHT : EnemigoState.EstadoEnemigo.HIT_LEFT;

                // Lógica de Ataque y Daño para Robots ---
                if (enemigo.puedeAtacar()) {
                    enemigo.reiniciarCooldownAtaque();
                    int idJugador = jugador.id;
                    int anillosActuales = puntajesAnillos.getOrDefault(idJugador, 0);

                    // Comprobamos si el jugador tiene 0 anillos
                    if (anillosActuales == 0) {
                        // Si NO tiene anillos, recibe 15 de daño.
                        jugador.vida -= 15;
                        System.out.println("[LOCAL SERVER] Jugador " + idJugador + " sin anillos fue golpeado. Vida restante: " + jugador.vida);
                    } else {
                        // Si SÍ tiene anillos, recibe el daño normal de 1 punto.
                        jugador.vida -= 1;
                        System.out.println("[LOCAL SERVER] Jugador " + idJugador + " con anillos fue golpeado. Vida restante: " + jugador.vida);
                    }

                    // Notifica al cliente de su nueva vida.
                    Network.PaqueteActualizacionVida paqueteVida = new Network.PaqueteActualizacionVida();
                    paqueteVida.idJugador = jugador.id;
                    paqueteVida.nuevaVida = jugador.vida;
                    clienteLocal.recibirPaqueteDelServidor(paqueteVida);

                    // Comprueba si el jugador ha sido derrotado.
                    if (jugador.vida <= 0) {
                        jugadores.remove(jugador.id);
                        Network.PaqueteEntidadEliminada notificacionMuerte = new Network.PaqueteEntidadEliminada();
                        notificacionMuerte.idEntidad = jugador.id;
                        notificacionMuerte.esJugador = true;
                        clienteLocal.recibirPaqueteDelServidor(notificacionMuerte);
                    }
                }
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
                float targetX = enemigo.x;
                float targetY = enemigo.y;

                if (dx > 0) targetX += ROBOT_SPEED;
                else if (dx < 0) targetX -= ROBOT_SPEED;
                if (dy > 0) targetY += ROBOT_SPEED;
                else if (dy < 0) targetY -= ROBOT_SPEED;

                if (manejadorNivel != null) {
                    Rectangle robotBounds = new Rectangle(enemigo.x, enemigo.y, 48, 48);

                    robotBounds.setX(targetX);
                    if (!manejadorNivel.colisionaConMapa(robotBounds)) {
                        enemigo.x = targetX;
                    }

                    robotBounds.setX(enemigo.x);
                    robotBounds.setY(targetY);
                    if (!manejadorNivel.colisionaConMapa(robotBounds)) {
                        enemigo.y = targetY;
                    }
                }
            }
        }
    }

    /**
     * Gestiona la generación continua de ítems (anillos, basura) en el mapa.
     * Utiliza temporizadores para generar nuevos ítems a intervalos regulares,
     * respetando un límite máximo de ítems por tipo en el mapa.
     *
     * @param deltaTime      El tiempo desde el último fotograma.
     * @param manejadorNivel El gestor del nivel para colocar los ítems.
     */
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

    /**
     * Controla la generación de nuevos enemigos en el nivel.
     * Genera enemigos a intervalos de tiempo definidos hasta alcanzar
     * el límite de enemigos especificado para el mapa actual.
     *
     * @param deltaTime      El tiempo desde el último fotograma.
     * @param manejadorNivel El gestor de nivel para colocar los enemigos.
     */
    private void generarEnemigosControlados(float deltaTime, LevelManager manejadorNivel) {
        String mapaActual = manejadorNivel.getNombreMapaActual();
        int limiteEnemigos = enemigosPorMapa.getOrDefault(mapaActual, 0);

        if (enemigosGeneradosEnNivelActual >= limiteEnemigos) {
            return;
        }

        tiempoGeneracionEnemigo += deltaTime;
        if (tiempoGeneracionEnemigo >= INTERVALO_GENERACION_ENEMIGO) {

            boolean enemigoGenerado = spawnNuevoEnemigo(manejadorNivel);

            if (enemigoGenerado) {
                enemigosGeneradosEnNivelActual++; // Incrementamos el contador de este nivel.
                System.out.println("[LOCAL SERVER] Enemigo generado (" + enemigosGeneradosEnNivelActual + "/" + limiteEnemigos + ")");
            }

            tiempoGeneracionEnemigo = 0f;
        }
    }

    /**
     * Comprueba si se cumplen las condiciones para generar el portal de fin de nivel.
     * La condición principal es que todos los enemigos del mapa hayan sido derrotados.
     * Si es así, invoca la generación del portal.
     *
     * @param manejadorNivel El gestor de nivel, necesario para crear el portal.
     */
    private void comprobarYGenerarPortalSiCorresponde(LevelManager manejadorNivel) {
        if (teleportGenerado) {
            return;
        }
        // La condición es simple: si la lista de enemigos activos está vacía, es hora.
        if (enemigosActivos.isEmpty()) {
            System.out.println("[LOCAL SERVER] ¡Todos los enemigos derrotados! Generando portal de salida.");
            generarPortales(manejadorNivel);
            // 3. ¡Activamos el seguro para no volver a generar el portal en este nivel!
            teleportGenerado = true;
        }
    }

    /**
     * Intenta generar un nuevo enemigo en una posición aleatoria y válida del mapa.
     * Realiza varios intentos para encontrar una ubicación que no colisione con
     * la geometría del nivel.
     *
     * @param manejadorNivel El gestor de nivel para verificar colisiones.
     * @return {@code true} si el enemigo fue generado con éxito, {@code false} en caso contrario.
     */
    private boolean spawnNuevoEnemigo(LevelManager manejadorNivel) {
        int intentos = 0;
        boolean colocado = false;
        while (!colocado && intentos < 20) {
            float x = MathUtils.random(0, manejadorNivel.getAnchoMapaPixels());
            float y = MathUtils.random(0, manejadorNivel.getAltoMapaPixels());
            Rectangle bounds = new Rectangle(x, y, 48, 48);

            if (!manejadorNivel.colisionaConMapa(bounds)) {
                EnemigoState nuevoEstado = new EnemigoState(proximoIdEnemigo++, bounds.x, bounds.y, 3, EnemigoState.EnemigoType.ROBOT);
                enemigosActivos.put(nuevoEstado.id, nuevoEstado);
                Network.PaqueteEnemigoNuevo paquete = new Network.PaqueteEnemigoNuevo();
                paquete.estadoEnemigo = nuevoEstado;
                clienteLocal.recibirPaqueteDelServidor(paquete);
                colocado = true;
            }
            intentos++;
        }
        return colocado;
    }

    /**
     * Genera un nuevo ítem de un tipo específico en una posición aleatoria y válida.
     * Se asegura de que el nuevo ítem no se superponga con ítems existentes ni
     * con la geometría del mapa.
     *
     * @param tipo           El tipo de ítem a generar (ANILLO, BASURA, etc.).
     * @param manejadorNivel El gestor de nivel para verificar colisiones.
     */
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

    /**
     * Finaliza la partida actual y envía los resultados finales al cliente.
     * Empaqueta las estadísticas acumuladas de los jugadores y las envía
     * para que se muestren en la pantalla de fin de juego.
     */
    private void finalizarPartidaYEnviarResultados() {
        System.out.println("[LOCAL SERVER] Finalizando partida y enviando resultados...");

        Network.PaqueteResultadosFinales paqueteResultados = new Network.PaqueteResultadosFinales();
        // Crea una nueva lista a partir de los valores del HashMap
        paqueteResultados.estadisticasFinales = new ArrayList<>(estadisticasJugadores.values());

        // Envia el paquete final al cliente local.
        clienteLocal.recibirPaqueteDelServidor(paqueteResultados);
    }

    /**
     * Libera los recursos utilizados por el servidor al cerrar el juego.
     * Limpia las listas de jugadores y otros estados para asegurar una
     * terminación limpia.
     */
    @Override
    public void dispose() {
        jugadores.clear();
        System.out.println("[LOCAL SERVER] Servidor local detenido.");
    }

    /**
     * Recibe un paquete de datos desde el cliente local y lo añade a la cola
     * de procesamiento. Este método es el punto de entrada para toda la comunicación
     * del cliente hacia el servidor en el modo local.
     *
     * @param paquete El objeto de paquete enviado por el cliente.
     */
    public void recibirPaqueteDelCliente(Object paquete) {
        this.paquetesEntrantes.add(paquete);
    }
}
