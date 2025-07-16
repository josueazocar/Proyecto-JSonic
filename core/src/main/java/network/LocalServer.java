package network;

import com.JSonic.uneg.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import network.interfaces.IGameClient;
import network.interfaces.IGameServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.Random;
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
    private final HashMap<Integer, Integer> puntajesAnillos = new HashMap<>();
    private final HashMap<Integer, Integer> puntajesBasura = new HashMap<>();
    private static final ContaminationState contaminationState = new ContaminationState();
    //Declara un HashMap para asociar el ID del portal con su destino (para los portales
    private final HashMap<Integer, String> destinosPortales = new HashMap<>();

    private static final float VELOCIDAD_ROBOTNIK = 60f;
    private static final float RANGO_DETENERSE_ROBOTNIK = 30f;
    private float tiempoGeneracionEnemigo = 0f;
    private final float INTERVALO_GENERACION_ENEMIGO = 5.0f;
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
    private static final float CONTAMINATION_RATE_PER_SECOND = 0.65f; // El % sube 0.65 puntos por segundo
    private static final float TRASH_CLEANUP_VALUE = 3f; // Cada basura recogida reduce el % en 2 puntos
    private float tiempoDesdeUltimaContaminacion = 0f;
    private static final float INTERVALO_ACTUALIZACION_CONTAMINACION = 1.0f; // 1 segundo

    //declaraciones de HashMap
    private final HashMap<Integer, AnimalState> animalesActivos = new HashMap<>();
    private final HashMap<Integer, Rectangle> bloquesRompibles = new HashMap<>();
    private int proximoIdBloque = 30000; // Un rango de IDs para bloques
    private int proximoIdAnimal = 20000; // Usamos un ID base alto para evitar conflictos con otros IDs
    // Variables para la lógica de muerte por contaminación
    private float tiempoParaProximaMuerteAnimal = 20f; // Temporizador para la muerte secuencial (20 segundos)
    private boolean muertesAnimalesActivas = false;

    // Cola para paquetes que vienen "desde el cliente" hacia el servidor
    private final Queue<Object> paquetesEntrantes = new ConcurrentLinkedQueue<>();

    // Referencia directa al único cliente que existirá en este modo
    private float cooldownHabilidadLimpieza = 0f;
    private static final float COOLDOWN_HABILIDAD_SONIC = 40.0f;
    private LocalClient clienteLocal;
    private int proximoIdJugador = 1; // En modo local, siempre empezamos en 1
    private static final int ROBOT_SPEED = 1;
    private static final float ROBOT_DETECTION_RANGE = 300f;
    private static final float ROBOT_ATTACK_RANGE = 10f; // Usando el valor del código original
    private int basuraReciclada = 0;

    public LocalServer() {
        // El constructor está vacío, la magia ocurre en start() y update()
    }

    public static void decreaseContamination(float porcentaje) {
        contaminationState.decrease(porcentaje);
    }

    @Override
    public void start() {
        System.out.println("[LOCAL SERVER] Servidor local iniciado.");

        // 1. Creamos la única instancia del cliente local y la guardamos.
        this.clienteLocal = new LocalClient(this);

        /*EnemigoState estadoRobotnik = new EnemigoState(999, 300, 100, 100, EnemigoState.EnemigoType.ROBOTNIK);
        this.enemigosActivos.put(estadoRobotnik.id, estadoRobotnik);
        System.out.println("[LOCAL SERVER] Robotnik ha sido creado en el servidor local.");

        Network.PaqueteEnemigoNuevo paqueteRobotnik = new Network.PaqueteEnemigoNuevo();
        paqueteRobotnik.estadoEnemigo = estadoRobotnik;
        this.clienteLocal.recibirPaqueteDelServidor(paqueteRobotnik);*/ // "Enviamos" el paquete

        // 2. Simulamos la conexión del jugador inmediatamente.
        // Esto replica la lógica del listener "connected" de tu GameServer.
        PlayerState nuevoEstado = new PlayerState();
        nuevoEstado.id = proximoIdJugador++; // Será el jugador 1
        nuevoEstado.x = 100; // Posición inicial X
        nuevoEstado.y = 100; // Posición inicial Y
        nuevoEstado.estadoAnimacion = Player.EstadoPlayer.IDLE_RIGHT;
        jugadores.put(nuevoEstado.id, nuevoEstado);
        puntajesAnillos.put(nuevoEstado.id, 0);
        puntajesBasura.put(nuevoEstado.id, 0);

        // 3. "Enviamos" el paquete de bienvenida al cliente local.
        Network.RespuestaAccesoPaquete respuesta = new Network.RespuestaAccesoPaquete();
        respuesta.mensajeRespuesta = "Bienvenido al modo local!";
        respuesta.tuEstado = nuevoEstado;
        this.clienteLocal.recibirPaqueteDelServidor(respuesta);
    }

    //funcion para portales generar portales
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
    //esta funcion es para los animales
    // Nuevo método para generar animales en el mapa actual
    private void generarAnimales(LevelManager manejadorNivel) {
        // Limpiamos la lista para que no se acumulen animales entre mapas
       // animalesActivos.clear();
        proximoIdAnimal = 20000; // Reiniciamos el contador de ID
        muertesAnimalesActivas = false; // Reseteamos la bandera al cambiar de mapa
        tiempoParaProximaMuerteAnimal = 20f; // Reiniciamos el temporizador

        // Queremos 5 animales por mapa, como solicitaste
        int cantidadAnimales = 10;
        for (int i = 0; i < cantidadAnimales; i++) {
            int intentos = 0;
            boolean colocado = false;
            while (!colocado && intentos < 50) { // Aumentamos los intentos para encontrar un lugar
                // Genera una posición aleatoria dentro de los límites del mapa
                float x = MathUtils.random(0, manejadorNivel.getAnchoMapaPixels() - 32); // Restamos el tamaño del animal para que no se salga
                float y = MathUtils.random(0, manejadorNivel.getAltoMapaPixels() - 32);

                // Crea un rectángulo temporal para verificar colisiones.
                // Asumimos un tamaño de 32x32 para el animal (ajusta si es diferente).
                Rectangle animalBounds = new Rectangle(x, y, 32, 32);

                // Verifica colisión con objetos del mapa (capa "Colisiones")
                if (!manejadorNivel.colisionaConMapa(animalBounds)) {
                    String texturaPath = "Items/Conejo1.png"; // Asegúrate de que esta ruta sea correcta
                    AnimalState nuevoAnimal = new AnimalState(proximoIdAnimal++, x, y, texturaPath);

                    // 1. Lo guardamos en la lista del servidor
                    animalesActivos.put(nuevoAnimal.id, nuevoAnimal);

                    // 2. Enviamos la notificación de creación al cliente
                    // Para que el cliente pueda crear el AnimalVisual correspondiente
                    /*Network.PaqueteAnimalNuevo paquete = new Network.PaqueteAnimalNuevo();
                    paquete.estadoAnimal = nuevoAnimal;
                    clienteLocal.recibirPaqueteDelServidor(paquete);*/
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

        // Después de generar todos los animales, enviamos la lista completa al cliente UNA SOLA VEZ.
        if (!animalesActivos.isEmpty()) {
            Network.PaqueteActualizacionAnimales paqueteInicial = new Network.PaqueteActualizacionAnimales();
            // Se crea una copia para evitar problemas de concurrencia.
            paqueteInicial.estadosAnimales = new HashMap<>(this.animalesActivos);
            clienteLocal.recibirPaqueteDelServidor(paqueteInicial);
            System.out.println("[LOCAL SERVER] Enviando paquete inicial con " + animalesActivos.size() + " animales al cliente.");
        }

    }

    //-------------------------------------------------------------------------------

    //Para matar al siguiente animal

    /**
     * [NUEVO MÉTODO AUXILIAR]
     * Busca el primer animal vivo en la lista, lo marca como muerto y notifica al cliente.
     */
    private void matarSiguienteAnimalVivo() {
        // Buscamos el primer animal que todavía esté vivo
        for (AnimalState animal : animalesActivos.values()) {
            if (animal.estaVivo) {
                animal.estaVivo = false; // El animal muere
                System.out.println("[LOCAL SERVER] Contaminación alta. Matando animal ID: " + animal.id);

                // Notificamos al cliente del cambio de estado.
                // Usaremos PaqueteActualizacionAnimales para mantener la consistencia.
                // Esto asegura que el cliente recibe el estado completo y actualizado.
                Network.PaqueteActualizacionAnimales paquete = new Network.PaqueteActualizacionAnimales();
                paquete.estadosAnimales = new HashMap<>();
                paquete.estadosAnimales.put(animal.id, animal); // Enviamos solo el estado del animal que cambió
                clienteLocal.recibirPaqueteDelServidor(paquete);

                return; // Salimos del método una vez que hemos matado a un animal.
            }
        }
        System.out.println("[LOCAL SERVER] No se encontraron más animales vivos para matar.");
    }
    //-------------------------------------

    //Logica para la muerte de los aniamales

    private void actualizarEstadoAnimalesPorContaminacion(float deltaTime) {
        if (contaminationState.getPercentage() >= 50) {
            // Si la contaminación es alta pero la bandera de muertes aún no está activa...
            if (!muertesAnimalesActivas) {
                muertesAnimalesActivas = true; // 1. Activamos la bandera.
                matarSiguienteAnimalVivo();    // 2. Matamos un animal INMEDIATAMENTE.
                tiempoParaProximaMuerteAnimal = 20f; // 3. Reiniciamos el temporizador para la SIGUIENTE muerte.
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
    //-----------------------------------------------------------------------------------------------


    /**
     * Este es el "game loop" del servidor. Se llamará desde PantallaDeJuego.
     *
     *
     * @param deltaTime        El tiempo transcurrido desde el último fotograma.
     * @param personajeJugable
     */
    @Override
    public void update(float deltaTime, LevelManager manejadorNivel, Player personajeJugable) {
        // --- 1. PROCESAR PAQUETES DEL CLIENTE ---
        while (!paquetesEntrantes.isEmpty()) {
            Object objeto = paquetesEntrantes.poll();
//ver si funciona bien
            if (objeto instanceof Network.PaquetePosicionJugador paquete) {
                PlayerState estadoJugador = jugadores.get(paquete.id);
                if (estadoJugador != null) {
                    // Asumimos un tamaño para el jugador, por ejemplo 32x48. Ajusta estos valores.
                    Rectangle nuevosLimites = new Rectangle(paquete.x, paquete.y, 32, 48);

                    // Verificamos que la nueva posición no colisione ni con el mapa ni con un animal.
                    if (!manejadorNivel.colisionaConMapa(nuevosLimites)) {
                        estadoJugador.x = paquete.x;
                        estadoJugador.y = paquete.y;
                    }
                    // Siempre actualizamos la animación, incluso si el movimiento fue bloqueado.
                    estadoJugador.estadoAnimacion = paquete.estadoAnimacion;
                    // No necesitamos retransmitir porque solo hay un jugador.
                }
            }
            // --- INICIO DE LA SOLUCIÓN CORRECTA ---
            else if (objeto instanceof Network.PaqueteHabilidadLimpiezaSonic) {
                System.out.println("[SERVER] ¡Recibida notificación de habilidad de limpieza de Sonic!");
                // Usamos el método que ya tienes para reducir la contaminación.
                // Poner un valor alto como 100 asegura que llegue a 0.
                decreaseContamination(100.0f);
                // El servidor ahora enviará automáticamente la actualización a todos los clientes
                // con el nuevo valor 0, porque la variable de contaminación ha cambiado.
            }
            // --- FIN DE LA SOLUCIÓN CORRECTA ---
            else if (objeto instanceof Network.PaqueteSolicitudRecogerItem paquete) {
                // Primero, verificamos si el ítem existe con .get()
                ItemState itemRecogido = itemsActivos.get(paquete.idItem);

                if (itemRecogido != null) {

                    // CASO ESPECIAL: Es un teletransportador
                    if (itemRecogido.tipo == ItemState.ItemType.TELETRANSPORTE) {
                        System.out.println("[LOCAL SERVER] Jugador ha activado el teletransportador.");
                        //itemsActivos.remove(paquete.idItem); // Lo eliminamos
                        //usar destino para lograr guardar los de los maps
                        String destinoMapa = destinosPortales.get(paquete.idItem);
                        if (destinoMapa == null) {
                            System.err.println("[LOCAL SERVER] Error: El portal con ID " + paquete.idItem + " no tiene un mapa de destino definido.");
                            return; // Salimos para evitar el error.
                        }
                        // 3. AHORA sí, eliminamos el portal de las listas activas.
                        itemsActivos.remove(paquete.idItem);
                        destinosPortales.remove(paquete.idItem); // Limpieza del mapa de destinos.

                        manejadorNivel.cargarNivel(destinoMapa);
                        com.badlogic.gdx.math.Vector2 llegada = manejadorNivel.obtenerPosicionLlegada();
                        float llegadaX = llegada.x;
                        float llegadaY = llegada.y; // Valor por defecto

                        // Creamos la ORDEN de cambio de mapa
                        Network.PaqueteOrdenCambiarMapa orden = new Network.PaqueteOrdenCambiarMapa();
                        orden.nuevoMapa = destinoMapa;
                        orden.nuevaPosX = llegadaX;
                        orden.nuevaPosY = llegadaY;

                        // "Enviamos" la orden al cliente local
                        clienteLocal.recibirPaqueteDelServidor(orden);

                        // También "enviamos" la notificación de que el portal fue eliminado
                        Network.PaqueteItemEliminado paqueteEliminado = new Network.PaqueteItemEliminado();
                        paqueteEliminado.idItem = paquete.idItem;
                        clienteLocal.recibirPaqueteDelServidor(paqueteEliminado);

                        //para teletransporte
                         // destinosPortales.remove(paquete.idItem); // Limpieza
                    }
                    // CASO GENERAL: Es un ítem normal
                    else {
                        itemsActivos.remove(paquete.idItem); // Lo eliminamos
                        System.out.println("[LOCAL SERVER] Ítem con ID " + paquete.idItem + " recogido.");

                        int idJugador = 1;
                        if (itemRecogido.tipo == ItemState.ItemType.ANILLO) {
                            int puntajeActual = puntajesAnillos.getOrDefault(idJugador, 0);
                            puntajesAnillos.put(idJugador, puntajeActual + 1);
                        } else if (itemRecogido.tipo == ItemState.ItemType.BASURA || itemRecogido.tipo == ItemState.ItemType.PIEZA_PLASTICO) {
                            int puntajeActual = puntajesBasura.getOrDefault(idJugador, 0);
                            puntajesBasura.put(idJugador, puntajeActual + 1);
                            contaminationState.decrease(TRASH_CLEANUP_VALUE);
                            System.out.println("[LOCAL SERVER] Basura recogida. Contaminación reducida a: " + contaminationState.getPercentage() + "%");
                        }

                        // Creamos el paquete de actualización de puntuación
                        Network.PaqueteActualizacionPuntuacion paquetePuntaje = new Network.PaqueteActualizacionPuntuacion();
                        paquetePuntaje.nuevosAnillos = puntajesAnillos.get(idJugador);
                        paquetePuntaje.nuevaBasura = puntajesBasura.get(idJugador);

                        // "Enviamos" el paquete de puntuación al cliente local
                        clienteLocal.recibirPaqueteDelServidor(paquetePuntaje);

                        // "Enviamos" la notificación de eliminación
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

            // --- FIN: CÓDIGO A AÑADIR ---

            //Para que los bloques de basura sean contados como basura
            else if (objeto instanceof Network.PaqueteBloqueDestruido paquete) {
                System.out.println("[LOCAL SERVER] Bloque destruido por jugador ID: " + paquete.idJugador);

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
            }
            //--------------------------------------------------------------------------

            else if (objeto instanceof Network.PaqueteBasuraDepositada paquete) {
                System.out.println("[LOCAL SERVER] Solicitud para depositar " + paquete.cantidad + " de basura recibida.");
                int idJugador = 1;

                // 1. Añadimos la cantidad depositada al total reciclado.
                this.basuraReciclada += paquete.cantidad;

                // 2. Reiniciamos el contador de basura del jugador a 0.
                puntajesBasura.put(idJugador, 0);

                // 3. Enviamos la actualización completa al cliente.
                Network.PaqueteActualizacionPuntuacion paquetePuntaje = new Network.PaqueteActualizacionPuntuacion();
                paquetePuntaje.nuevosAnillos = puntajesAnillos.getOrDefault(idJugador, 0);
                paquetePuntaje.nuevaBasura = puntajesBasura.get(idJugador); // Será 0
                paquetePuntaje.totalBasuraReciclada = this.basuraReciclada; // Enviamos el nuevo total
                clienteLocal.recibirPaqueteDelServidor(paquetePuntaje);
            }
           else if (objeto instanceof Network.PaqueteSolicitudHabilidadLimpieza) {
                // --- INICIO DE LA MODIFICACIÓN ---
                // ¡El servidor ahora comprueba su propio temporizador!
                if (cooldownHabilidadLimpieza <= 0) {
                    System.out.println("[LOCAL SERVER] Habilidad de limpieza de Sonic activada.");

                    // 1. Reiniciamos el cooldown del servidor.
                    cooldownHabilidadLimpieza = COOLDOWN_HABILIDAD_SONIC;

                    // 2. Aplicamos el efecto al estado del juego.
                    decreaseContamination(100.0f);

                    // 3. Recogemos la basura (lógica que ya tenías).
                    for (ItemState item : new ArrayList<>(itemsActivos.values())) {
                        if (item.tipo == ItemState.ItemType.BASURA || item.tipo == ItemState.ItemType.PIEZA_PLASTICO) {
                            procesarRecogidaItem(1, item.id);
                        }
                    }

                    // 4. Enviamos la notificación de éxito al cliente.
                    Network.PaqueteHabilidadLimpiezaSonic notificacion = new Network.PaqueteHabilidadLimpiezaSonic();
                    clienteLocal.recibirPaqueteDelServidor(notificacion);
                } else {
                    // Si el jugador lo intenta antes de tiempo, el servidor local lo ignora.
                    System.out.println("[LOCAL SERVER] Habilidad en cooldown. Solicitud ignorada.");
                }
            }


        }
        // --- LÓGICA DE AUMENTO DE CONTAMINACIÓN ---
        contaminationState.increase(CONTAMINATION_RATE_PER_SECOND * deltaTime);

        tiempoDesdeUltimaContaminacion += deltaTime;
        if (tiempoDesdeUltimaContaminacion >= INTERVALO_ACTUALIZACION_CONTAMINACION) {
            Network.PaqueteActualizacionContaminacion paquete = new Network.PaqueteActualizacionContaminacion();
            paquete.contaminationPercentage = contaminationState.getPercentage();
            clienteLocal.recibirPaqueteDelServidor(paquete); // "Enviamos" el paquete

            tiempoDesdeUltimaContaminacion = 0f; // Reseteamos el temporizador
        }

        if (cooldownHabilidadLimpieza > 0) {
            cooldownHabilidadLimpieza -= deltaTime;
        }
        //para que se genere mas de un portal y en diferentes mapas
        // --- LÓGICA DE CAMBIO DE MAPA ---
        String mapaActual = manejadorNivel.getNombreMapaActual();
        if (!mapaActual.equals(ultimoMapaProcesado)) {
            System.out.println("[LOCAL SERVER] Detectado cambio de mapa a: " + mapaActual);
            ultimoMapaProcesado = mapaActual;

            // 1. Limpiar entidades del mapa anterior
            enemigosActivos.clear();
            itemsActivos.clear();
            destinosPortales.clear(); // Importante para los portales
            animalesActivos.clear(); //para los animales
            bloquesRompibles.clear();

            // 2. Reiniciar temporizadores de generación
            teleportGenerado = false;
            tiempoGeneracionTeleport = 0f;
            tiempoGeneracionEnemigo = 0f;
            tiempoSpawnAnillo = 0f;
            tiempoSpawnBasura = 0f;
            tiempoSpawnPlastico = 0f;

            // 3. Regenerar entidades para el nuevo mapa
            generarAnimales(manejadorNivel); // Esto ya lo tenías, y está bien
            generarBloquesParaElNivel(manejadorNivel);

            Network.PaqueteSincronizarBloques paqueteSync = new Network.PaqueteSincronizarBloques();
            paqueteSync.todosLosBloques = new HashMap<>(this.bloquesRompibles);
            clienteLocal.recibirPaqueteDelServidor(paqueteSync);
            System.out.println("[LOCAL SERVER] Enviando estado de bloques al cliente local.");

            // 4. Volver a crear a Robotnik en el nuevo mapa (si es necesario)
            // Nota: Esto es opcional si quieres que Robotnik aparezca en todos los mapas.
            // Si no, puedes eliminar estas líneas.
            EnemigoState estadoRobotnik = new EnemigoState(999, 300, 100, 100, EnemigoState.EnemigoType.ROBOTNIK);
            this.enemigosActivos.put(estadoRobotnik.id, estadoRobotnik);
            Network.PaqueteEnemigoNuevo paqueteRobotnik = new Network.PaqueteEnemigoNuevo();
            paqueteRobotnik.estadoEnemigo = estadoRobotnik;
            this.clienteLocal.recibirPaqueteDelServidor(paqueteRobotnik);


        }

        // --- FIN DE LÓGICA DE CAMBIO DE MAPA ---
        //----------------------------------------------------
        //aqui se cambio para que la logica donde se llamaba al servidor, fuera una funcion

        this.tiempoGeneracionTeleport += deltaTime;
        if (!this.teleportGenerado && this.tiempoGeneracionTeleport >= 20f) {

            System.out.println("[LOCAL SERVER] Generando teletransportador...");
            System.out.println("[DEBUG] Intentando generar portales...");
            //llamamos a la funcion generar portales
            generarPortales(manejadorNivel);
            this.teleportGenerado = true;
        }

        actualizarEstadoAnimalesPorContaminacion(deltaTime);
        actualizarEnemigosAI(deltaTime, manejadorNivel,personajeJugable);
        generarNuevosItems(deltaTime, manejadorNivel);
        generarNuevosEnemigos(deltaTime, manejadorNivel);
//para que los animales se puedan generar varias veces en el mapa
        if (!animalesActivos.isEmpty()) {
            Network.PaqueteActualizacionAnimales paqueteUpdateAnimales = new Network.PaqueteActualizacionAnimales();
            paqueteUpdateAnimales.estadosAnimales = this.animalesActivos; // Envía todo el HashMap
            clienteLocal.recibirPaqueteDelServidor(paqueteUpdateAnimales);
        }

        if (!enemigosActivos.isEmpty()) {
            Network.PaqueteActualizacionEnemigos paqueteUpdate = new Network.PaqueteActualizacionEnemigos();
            paqueteUpdate.estadosEnemigos = this.enemigosActivos;
            clienteLocal.recibirPaqueteDelServidor(paqueteUpdate);
        }
    }

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

     private void actualizarEnemigosAI(float deltaTime, LevelManager manejadorNivel, Player personajeJugable)  {
        PlayerState jugador = jugadores.get(1);
        if (jugador == null)
            return;

        if( personajeJugable.getVida() == 100){
            for (EnemigoState enemigo : enemigosActivos.values()) {
                enemigo.estadoAnimacion = EnemigoState.EstadoEnemigo.IDLE_RIGHT;
            }
            return;
        }

        for (EnemigoState enemigo : enemigosActivos.values()) {

            if (enemigo.tipo == EnemigoState.EnemigoType.ROBOTNIK) {
                float distanciaX = jugador.x - enemigo.x;
                float distanciaY = jugador.y - enemigo.y;
                // Usamos la clase Vector2 de LibGDX para calcular la distancia y la dirección fácilmente.
                float distancia = new com.badlogic.gdx.math.Vector2(distanciaX, distanciaY).len();

                // Usamos las constantes que ya definimos en este archivo
                if (distancia > RANGO_DETENERSE_ROBOTNIK) {
                    float velocidadMovimiento = VELOCIDAD_ROBOTNIK * deltaTime;

                    // Normalizamos el vector para obtener solo la dirección (un vector de longitud 1)
                    com.badlogic.gdx.math.Vector2 direccionDeseada = new com.badlogic.gdx.math.Vector2(distanciaX, distanciaY).nor();

                    enemigo.x += direccionDeseada.x * velocidadMovimiento;
                    enemigo.y += direccionDeseada.y * velocidadMovimiento;

                    enemigo.mirandoDerecha = (direccionDeseada.x > 0);
                    enemigo.estadoAnimacion = enemigo.mirandoDerecha ? EnemigoState.EstadoEnemigo.RUN_RIGHT : EnemigoState.EstadoEnemigo.RUN_LEFT;
                } else {
                    enemigo.estadoAnimacion = enemigo.mirandoDerecha ? EnemigoState.EstadoEnemigo.IDLE_RIGHT : EnemigoState.EstadoEnemigo.IDLE_LEFT;
                }

                // Ya procesamos a Robotnik, así que saltamos al siguiente enemigo en el bucle
                // para evitar que se le aplique la lógica de los robots normales.
                continue;
            }

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
                float targetX = enemigo.x;
                float targetY = enemigo.y;

                if (dx > 0) targetX += ROBOT_SPEED;
                else if (dx < 0) targetX -= ROBOT_SPEED;
                if (dy > 0) targetY += ROBOT_SPEED;
                else if (dy < 0) targetY -= ROBOT_SPEED;

                if (manejadorNivel != null) {
                    Rectangle robotBounds = new Rectangle(enemigo.x, enemigo.y, 48, 48);

                    // Comprobar movimiento en X
                    robotBounds.setX(targetX);
                    if (!manejadorNivel.colisionaConMapa(robotBounds)) {
                        enemigo.x = targetX;
                    }

                    // Comprobar movimiento en Y
                    robotBounds.setX(enemigo.x);
                    robotBounds.setY(targetY);
                    if (!manejadorNivel.colisionaConMapa(robotBounds)) {
                        enemigo.y = targetY;
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
     *
     * @param paquete El paquete enviado por el cliente.
     */
    public void recibirPaqueteDelCliente(Object paquete) {
        this.paquetesEntrantes.add(paquete);
    }

    /**
     * Permite a la clase que nos crea (JSonicJuego) obtener la instancia del cliente.
     *
     * @return El cliente local asociado a este servidor.
     */
    public IGameClient getClient() {
        return this.clienteLocal;
    }

}
