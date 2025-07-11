// Archivo: src/com/JSonic/uneg/PantallaDeJuego.java
package com.JSonic.uneg;

// Imports...
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import network.Network;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import network.interfaces.IGameClient;
import network.interfaces.IGameServer;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
// ---[AGREGADO]--- Importaciones de la segunda clase
import com.badlogic.gdx.math.Vector2;

public class PantallaDeJuego extends PantallaBase {

    // --- VARIABLES ORIGINALES ---
    public static final float VIRTUAL_WIDTH = 900;
    public static final float VIRTUAL_HEIGHT = 505;
    private OrthographicCamera camaraJuego;
    private Viewport viewport;
    private final JSonicJuego juegoPrincipal;
    private final SpriteBatch batch;
    private LevelManager manejadorNivel;
    private final IGameClient gameClient;
    private final IGameServer localServer;
    private Player personajeJugable;
    private PlayerState personajeJugableEstado;
    private final HashMap<Integer, Player> otrosJugadores = new HashMap<>();
    private SoundManager soundManager;
    private static final String BACKGROUND_MUSIC_PATH2 = "SoundsBackground/Dating Fight.mp3";
    private AssetManager assetManager;
    private ShapeRenderer shapeRenderer;
    private ContadorUI contadorAnillos;
    private ContadorUI contadorBasura;
    private AnillosVisual anilloVisual;
    private int basuraRecicladaTotal = 0;
    private final HashMap<Integer, RobotVisual> enemigosEnPantalla = new HashMap<>();
    private final HashMap<Integer, ItemVisual> itemsEnPantalla = new HashMap<>();

    private int anillosTotal = 0;
    private int basuraTotal = 0;
    private float porcentajeContaminacionActual = 0f;

    //para el label de los aniamles
    private Label animalCountLabel; // El label para mostrar el conteo
    private int animalesVivos = 0;
    private int animalesMuertos = 0;
    private int totalAnimalesMapa = 0;

    //private final HashMap<Integer, AnimalVisual> animalesEnPantalla = new HashMap<>(); // <-- AÑADIR ESTA LÍNEA
    private OrthographicCamera uiCamera;

    private ShaderProgram shaderNeblina;
    private Mesh quadMesh;
    private BitmapFont font;
    private Label contaminationLabel;
    private Vector3 screenCoords = new Vector3();

    //Para la seleccion de personaje
    public static PlayerState.CharacterType miPersonaje = PlayerState.CharacterType.SONIC;

    //para el teletransporte
    private float tiempoTranscurrido = 0f;
    private boolean teletransporteCreado = false;

    // ---[AGREGADO]--- Instancia de RobotnikVisual (eggman)
    private RobotnikVisual eggman;

    public PantallaDeJuego(JSonicJuego juego, IGameClient client, IGameServer server) {
        super();
        this.juegoPrincipal = juego;
        this.batch = juego.batch;
        this.gameClient = client; // Asignamos el cliente recibido
        this.localServer = server; // Asignamos el servidor recibido (puede ser null)

    }

    public PantallaDeJuego(JSonicJuego juego) {
        this(juego, null, null); // Llamamos al constructor con nulls si no hay cliente/servidor
    }

    @Override
    public void inicializar() {
        camaraJuego = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camaraJuego);
        manejadorNivel = new LevelManager(camaraJuego, batch);
        manejadorNivel.cargarNivel(ConfiguracionJuego.mapaSeleccionado);
        personajeJugableEstado = new PlayerState();

        //para tomar lo metodos de LevelManager
        Vector2 llegada = manejadorNivel.obtenerPosicionLlegada();
        personajeJugableEstado.x = llegada.x;
        personajeJugableEstado.y = llegada.y;
        // 1. Asigna el tipo de personaje al estado que se usará para el jugador local.
        personajeJugableEstado.characterType = miPersonaje;
        System.out.println("[JUEGO] Personaje recibido del menú: " + miPersonaje);

        // 2. Crea la instancia del personaje jugable localmente.
        // Este switch se asegura de que TÚ estás controlando al personaje correcto en tu pantalla.


        switch (miPersonaje) {
            case SONIC:
                personajeJugable = new Sonic(personajeJugableEstado, manejadorNivel);
                break;
            case TAILS:
                // Asegúrate de que tienes una clase Tails que hereda de Player
                personajeJugable = new Tails(personajeJugableEstado, manejadorNivel, this.gameClient);
                break;
            case KNUCKLES:
                personajeJugable = new Knuckles(personajeJugableEstado, manejadorNivel);
                break;
            default:
                // Si algo sale mal, por defecto será Sonic
                personajeJugable = new Sonic(personajeJugableEstado, manejadorNivel);
                break;
        }

        manejadorNivel.setPlayer(personajeJugable);
        assetManager = new AssetManager();
        soundManager = new SoundManager(assetManager);
        soundManager.loadMusic(BACKGROUND_MUSIC_PATH2);
        soundManager.playBackgroundMusic(BACKGROUND_MUSIC_PATH2, 0.5f, true);
        assetManager.finishLoading();
        shapeRenderer = new ShapeRenderer();

        uiCamera = new OrthographicCamera();

        ShaderProgram.pedantic = false; // Evita errores si no se usan todos los uniformes
        shaderNeblina = new ShaderProgram(
            Gdx.files.internal("shaders/neblina.vert"),
            Gdx.files.internal("shaders/neblina.frag")
        );

        if (!shaderNeblina.isCompiled()) {
            Gdx.app.error("Shader Error", "No se pudo compilar el shader de neblina: " + shaderNeblina.getLog());
        }

        quadMesh = new Mesh(
            true, // es estático, no cambiará
            4,    // 4 vértices
            4,    // 4 índices
            new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position") // 3 floats por vértice (x,y,z)
        );

        // Definimos los 4 vértices de un rectángulo que cubre la pantalla
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float[] vertices = {
            0, 0, 0,                      // Vértice 0: Abajo-izquierda
            screenWidth, 0, 0,            // Vértice 1: Abajo-derecha
            0, screenHeight, 0,           // Vértice 2: Arriba-izquierda
            screenWidth, screenHeight, 0  // Vértice 3: Arriba-derecha
        };

        // Definimos el orden en que se dibujan los vértices para formar dos triángulos
        short[] indices = { 0, 1, 2, 3 };

        quadMesh.setVertices(vertices);
        quadMesh.setIndices(indices);

        // CONFIGURACIÓN UI Anillos ---
        String numerosTexturaPath = "Fondos/numerosContadorAnillos.png";
        contadorAnillos = new ContadorUI(numerosTexturaPath);
        contadorBasura = new ContadorUI(numerosTexturaPath);

        Table tablaUI = new Table();
        tablaUI.top().right();
        tablaUI.setFillParent(true);
        tablaUI.pad(10);

        // Iconos y contadores
        anilloVisual = new AnillosVisual(new ItemState(0, 0, 0, ItemState.ItemType.ANILLO));
        Image anilloIcono = new Image(anilloVisual.animacion.getKeyFrame(0));
        Texture basuraIcono = new Texture("Items/basura.png");

        tablaUI.add(anilloIcono).size(45, 45);
        tablaUI.add(contadorAnillos.getTabla()).padLeft(5);
        tablaUI.add(new Image(basuraIcono)).size(55, 55).padLeft(20);
        tablaUI.add(contadorBasura.getTabla()).padLeft(5);

        mainStage.addActor(tablaUI);

        font = new BitmapFont(Gdx.files.internal("Fuentes/juego_fuente2.fnt"));
        font.getData().setScale(0.65f);
        // 2. Define un estilo para nuestro texto (Label).
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.PURPLE);

        // 3. Crea el objeto Label con un texto inicial.
        contaminationLabel = new Label("TOXIC: 0%", labelStyle);

        // 4. Crea una NUEVA tabla para los elementos de la parte inferior de la pantalla.
        //    Es buena práctica tener tablas separadas para distintas esquinas de la UI.
        Table tablaInferior = new Table();
        tablaInferior.setFillParent(true); // Hace que la tabla ocupe todo el Stage
        tablaInferior.bottom().right();    // ¡La alinea abajo a la derecha!

        // 5. Añade un poco de espacio para que no quede pegado a los bordes.
        tablaInferior.pad(15);

        // 6. Añade nuestro Label a la tabla.
        tablaInferior.add(contaminationLabel);

        // 7. Finalmente, añade la nueva tabla al Stage principal.
        mainStage.addActor(tablaInferior);

        // CONFIGURACIÓN UI Animales ---
        // Puedes reutilizar la misma fuente si quieres.
        Label.LabelStyle animalCountLabelStyle = new Label.LabelStyle(font, Color.WHITE); // Cambia el color si lo deseas
        animalCountLabel = new Label("Animales: 0/0", animalCountLabelStyle); // Texto inicial

        // Crea una NUEVA tabla para la esquina inferior izquierda
        Table tablaInferiorIzquierda = new Table();
        tablaInferiorIzquierda.setFillParent(true);
        tablaInferiorIzquierda.bottom().left(); // Alineada abajo a la izquierda
        tablaInferiorIzquierda.pad(15); // Un poco de padding

        tablaInferiorIzquierda.add(animalCountLabel);

        mainStage.addActor(tablaInferiorIzquierda);
        //---------------------------------------------

    }

    //para poder crear varios portales se necesita reiniciar el teletransporte
    private void reiniciarTeletransporte() {
        teletransporteCreado = false;
        tiempoTranscurrido = 0f;
    }

    @Override
    public void actualizar(float deltat) {
        if (personajeJugable != null && this.gameClient != null) {
            // Como ya sabemos que el cliente no es nulo, lo asignamos.
            personajeJugable.setGameClient(this.gameClient);

            // Ahora, dentro de este mismo bloque, comprobamos si es modo online.
            // Ya no es necesario volver a preguntar si gameClient es nulo.
            if (this.localServer == null) {
                if (personajeJugable instanceof Tails) {
                    ((Tails) personajeJugable).isOnlineMode = true;
                    ((Tails) personajeJugable).setOnlineMode(true);
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            this.pause();
            PantallaMenu pantallaMenu = new PantallaMenu(juegoPrincipal, true);
            pantallaMenu.setEstadoMenu(PantallaMenu.EstadoMenu.JUGAR);
            juegoPrincipal.setPantallaActiva(pantallaMenu);
            return;
        }

        if (localServer != null) {
            localServer.update(deltat, this.manejadorNivel);
        }

        if (gameClient != null) {
            while (!gameClient.getPaquetesRecibidos().isEmpty()) {
                Object paquete = gameClient.getPaquetesRecibidos().poll();

                if (paquete instanceof Network.RespuestaAccesoPaquete p) {
                    if (p.tuEstado != null) {
                        inicializarJugadorLocal(p.tuEstado);
                        System.out.println("[CLIENT] Conexión aceptada. Extrayendo y enviando plano del mapa...");
                        java.util.ArrayList<Rectangle> paredes = new java.util.ArrayList<>();
                        MapObjects objetosColision = manejadorNivel.getCollisionObjects();
                        if (objetosColision != null) {
                            for (com.badlogic.gdx.maps.MapObject obj : objetosColision) {
                                if (obj instanceof com.badlogic.gdx.maps.objects.RectangleMapObject) {
                                    paredes.add(((com.badlogic.gdx.maps.objects.RectangleMapObject) obj).getRectangle());
                                }
                            }
                        }
                        Network.PaqueteInformacionMapa paqueteMapa = new Network.PaqueteInformacionMapa();
                        paqueteMapa.paredes = paredes;
                        gameClient.send(paqueteMapa);
                        System.out.println("[CLIENT] Plano del mapa con " + paredes.size() + " paredes enviado.");
                    }
                } else if (paquete instanceof Network.PaqueteJugadorConectado p) {
                    if (personajeJugableEstado != null && p.nuevoJugador.id != personajeJugableEstado.id) {
                        agregarOActualizarOtroJugador(p.nuevoJugador);
                    }
                } else if (paquete instanceof Network.PaquetePosicionJugador p) {
                    if (personajeJugableEstado != null && p.id != personajeJugableEstado.id) {
                        actualizarPosicionOtroJugador(p.id, p.x, p.y, p.estadoAnimacion);
                    }
                } else if (paquete instanceof Network.PaqueteEnemigoNuevo p) {
                    crearEnemigoVisual(p.estadoEnemigo);
                } else if (paquete instanceof Network.PaqueteItemNuevo p) {
                    crearItemVisual(p.estadoItem);
                } else if (paquete instanceof Network.PaqueteItemEliminado p) {
                    ItemVisual itemEliminado = itemsEnPantalla.remove(p.idItem);
                    if (itemEliminado != null) {
                        System.out.println("[CLIENT] Obedeciendo orden de eliminar ítem con ID: " + p.idItem);
                        itemEliminado.dispose();
                    }
                } else if (paquete instanceof Network.PaqueteActualizacionEnemigos p) {
                    for (EnemigoState estadoServidor : p.estadosEnemigos.values()) {
                        if (estadoServidor.tipo == EnemigoState.EnemigoType.ROBOTNIK) {
                            if (eggman != null) {
                                eggman.estado.x = estadoServidor.x;
                                eggman.estado.y = estadoServidor.y;
                                eggman.setEstadoActual(estadoServidor.estadoAnimacion);
                            }
                        } else {
                            RobotVisual enemigoVisual = enemigosEnPantalla.get(estadoServidor.id);
                            if (enemigoVisual != null) {
                                enemigoVisual.estado.x = estadoServidor.x;
                                enemigoVisual.estado.y = estadoServidor.y;
                                enemigoVisual.setEstadoActual(estadoServidor.estadoAnimacion);
                            }
                        }
                    }
                } else if (paquete instanceof Network.PaqueteOrdenCambiarMapa p) {
                    System.out.println("[CLIENT] ¡Recibida orden del servidor para cambiar al mapa: " + p.nuevoMapa + "!");
                    manejadorNivel.cargarNivel(p.nuevoMapa);
                    limpiarEnemigosEItems();
                    reiniciarTeletransporte();
                    personajeJugable.estado.x = p.nuevaPosX;
                    personajeJugable.estado.y = p.nuevaPosY;
                    System.out.println("[CLIENT] Enviando nuevo plano del mapa al servidor...");
                    java.util.ArrayList<com.badlogic.gdx.math.Rectangle> paredes = new java.util.ArrayList<>();
                    MapObjects objetosColision = manejadorNivel.getCollisionObjects();
                    if (objetosColision != null) {
                        for (com.badlogic.gdx.maps.MapObject obj : objetosColision) {
                            if (obj instanceof com.badlogic.gdx.maps.objects.RectangleMapObject) {
                                paredes.add(((com.badlogic.gdx.maps.objects.RectangleMapObject) obj).getRectangle());
                            }
                        }
                    }
                    Network.PaqueteInformacionMapa paqueteMapa = new Network.PaqueteInformacionMapa();
                    paqueteMapa.paredes = paredes;
                    gameClient.send(paqueteMapa);
                    System.out.println("[CLIENT] Nuevo plano del mapa con " + paredes.size() + " paredes enviado.");
                } else if (paquete instanceof Network.PaqueteActualizacionPuntuacion p) {
                    System.out.println("[CLIENT] ¡Recibida actualización de puntuación! Anillos: " + p.nuevosAnillos + ", Basura: " + p.nuevaBasura);
                    this.anillosTotal = p.nuevosAnillos;
                    this.basuraTotal = p.nuevaBasura;
                    this.basuraRecicladaTotal = p.totalBasuraReciclada;
                    contadorAnillos.setValor(this.anillosTotal);
                    contadorBasura.setValor(this.basuraTotal);
//develop
                } else if (paquete instanceof Network.PaqueteActualizacionContaminacion p) {
                    this.porcentajeContaminacionActual = p.contaminationPercentage;
                    contaminationLabel.setText("TOXIC: " + Math.round(this.porcentajeContaminacionActual) + "%");
                    if (this.porcentajeContaminacionActual >= 50 && totalAnimalesMapa > 0) {
                        actualizarAnimalCountLabel();
                    } else {
                        animalCountLabel.setText("");
                    }
                } else if (paquete instanceof Network.PaqueteActualizacionAnimales p) {
                    manejadorNivel.actualizarAnimalesDesdePaquete(p.estadosAnimales);

                    animalesVivos = 0;
                    animalesMuertos = 0;
                    for (AnimalVisual animal : manejadorNivel.getAnimalesVisuales()) {
                        if (animal.estaVivo()) {
                            animalesVivos++;
                        } else {
                            animalesMuertos++;
                        }
                    }
                    totalAnimalesMapa = manejadorNivel.getAnimalesVisuales().size();
                    actualizarAnimalCountLabel();
                }
                if (paquete instanceof Network.PaqueteArbolNuevo p) {
                    if (manejadorNivel != null) {
                        // El servidor nos ordena crear un árbol en estas coordenadas.
                        manejadorNivel.generarArbol(p.x, p.y);
                        System.out.println("[CLIENT] Orden recibida para generar árbol en " + p.x + "," + p.y);
                    }
                }

                else if (paquete instanceof Network.PaqueteMensajeUI p) {
                    if (personajeJugable != null) {
                        // El servidor nos ordena mostrar un mensaje.
                        personajeJugable.mostrarMensaje(p.mensaje);
                    }
                } else if (paquete instanceof Network.PaqueteDronEstado p) {
                    // 1. Buscamos al jugador dueño del dron
                    Player dueño = null;
                    if (personajeJugable.estado.id == p.ownerId) {
                        dueño = personajeJugable;
                    } else {
                        dueño = otrosJugadores.get(p.ownerId);
                    }

                    // 2. Si encontramos al dueño y es un Tails...
                    if (dueño instanceof Tails) {
                        Tails tailsDueño = (Tails) dueño;
                        Dron_Tails dron = tailsDueño.getDron();

                        if (dron != null) {
                            // ¡LÓGICA CLAVE! Asignamos el objetivo.
                            dron.objetivo = tailsDueño;

                            // Ahora llamamos a tu método para que el dron reaccione
                            tailsDueño.gestionarDronDesdeRed(p.nuevoEstado, p.x, p.y);
                        }
                    }
                }
            }
        }

        // --- Toda la lógica de juego local va aquí, fuera del if (gameClient != null) ---

        for (ItemVisual item : itemsEnPantalla.values()) {
            item.update(deltat);
        }

        Integer idTeletransporteAEliminar = null;
        Iterator<Map.Entry<Integer, ItemVisual>> iter = itemsEnPantalla.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, ItemVisual> entry = iter.next();
            ItemVisual item = entry.getValue();

            if (personajeJugable.getBounds() != null && item.getBounds() != null && Intersector.overlaps(personajeJugable.getBounds(), item.getBounds())) {
                System.out.println("[CLIENT_DEBUG] Colisión detectada con item tipo: " + item.estado.tipo);

                Network.PaqueteSolicitudRecogerItem paquete = new Network.PaqueteSolicitudRecogerItem();
                paquete.idItem = item.estado.id;

                // --- CAMBIO CLAVE AQUÍ ---
                // Se envía el paquete a través del cliente, que gestionará si es local o remoto.
                if (gameClient != null) {
                    gameClient.send(paquete);
                }

                // Si es un teletransporte, salimos del bucle para que el servidor procese el cambio de mapa.
                if (item.estado.tipo == ItemState.ItemType.TELETRANSPORTE) {
                    break;
                }

                // Eliminamos el ítem visualmente para una respuesta más rápida.
                // El servidor se encargará de la lógica real.
                iter.remove();
                item.dispose();
                break; // Salimos para procesar solo un ítem por fotograma.
            }
        }

        if (idTeletransporteAEliminar != null) {
            limpiarEnemigosEItems();
            ItemVisual item = itemsEnPantalla.remove(idTeletransporteAEliminar);
            if (item != null) item.dispose();
        }

        // 1. Preguntamos al LevelManager si hay una planta en este mapa.
        if (manejadorNivel != null) {
            Rectangle planta = manejadorNivel.obtenerPlantaDeTratamiento();

            if (planta != null && personajeJugable != null && personajeJugable instanceof Tails && personajeJugable.getBounds() != null && Intersector.overlaps(personajeJugable.getBounds(), planta)) {
                if (this.basuraTotal > 0) {
                    System.out.println("[CLIENTE] Tocando planta. Solicitando depositar basura al servidor...");

                    if (gameClient != null) {
                        Network.PaqueteBasuraDepositada paquete = new Network.PaqueteBasuraDepositada();
                        paquete.cantidad = this.basuraTotal;
                        gameClient.send(paquete);
                    }
                }
            }
        }
        //para actualiza los bloques
        manejadorNivel.actualizar(deltat);
        //--------------------------------
        personajeJugable.KeyHandler();
        personajeJugable.update(deltat);

        // --- INICIO DEL CÓDIGO A AÑADIR ---
        // Este bloque hace de intermediario entre Knuckles y los bloques rompibles
        if (personajeJugable instanceof Knuckles) {
            Knuckles knuckles = (Knuckles) personajeJugable;

            // Preguntamos si Knuckles acaba de iniciar un golpe.
            if (knuckles.haIniciadoGolpe()) {
                Gdx.app.log("PantallaDeJuego", "Knuckles ha iniciado un golpe. Buscando bloque cercano...");

                ObjetoRomperVisual bloqueMasCercano = null;
                float distanciaMinima = Float.MAX_VALUE;
                float rangoMaximoDeGolpe = 60f; // Rango del puñetazo en píxeles. ¡Puedes ajustar este valor!

                // Buscamos el bloque rompible más cercano a Knuckles.
                for (ObjetoRomperVisual bloque : manejadorNivel.getBloquesRompibles()) {
                    // Usamos Vector2 para calcular la distancia entre el centro de Knuckles y el centro del bloque.
                    Vector2 posKnuckles = new Vector2(knuckles.estado.x + knuckles.getTileSize()/2f, knuckles.estado.y + knuckles.getTileSize()/2f);
                    Vector2 posBloque = new Vector2(bloque.x + bloque.bounds.width/2f, bloque.y + bloque.bounds.height/2f);
                    float distancia = posKnuckles.dst(posBloque);

                    if (distancia < distanciaMinima) {
                        distanciaMinima = distancia;
                        bloqueMasCercano = bloque;
                    }
                }

                // Si encontramos un bloque cercano y está dentro del rango del golpe...
                if (bloqueMasCercano != null && distanciaMinima <= rangoMaximoDeGolpe) {
                    Gdx.app.log("PantallaDeJuego", "¡Bloque encontrado en rango! ID: " + bloqueMasCercano.id + ". Dando orden de destruir.");
                    bloqueMasCercano.destruir();
                } else {
                    Gdx.app.log("PantallaDeJuego", "Golpe al aire. Ningún bloque en rango.");
                }
            }
        }
        // --- FIN DEL CÓDIGO A AÑADIR ---

        for (Player otro : otrosJugadores.values()) {
            otro.update(deltat);
        }

        for (RobotVisual enemigo : enemigosEnPantalla.values()) enemigo.update(deltat);

        if (eggman != null) {
            eggman.update(deltat);
        }


// --- INICIO: LÓGICA DE COLISIÓN CON ANIMALES ---
        Iterator<AnimalVisual> iteradorAnimales = manejadorNivel.getAnimalesVisuales().iterator();
        while (iteradorAnimales.hasNext()) {
            AnimalVisual animal = iteradorAnimales.next();

            // CORRECCIÓN: Comprobar que las cajas de colisión no son nulas antes de usarlas.
            if (animal.estaVivo() && personajeJugable.getBounds() != null && animal.getBounds() != null) {
                // 1. Colisión: Jugador vs Animal
                if (Intersector.overlaps(personajeJugable.getBounds(), animal.getBounds())) {
                    System.out.println("[CLIENT] Colisión con animal vivo ID: " + animal.getId() + ". Solicitando liberación.");
                    Network.PaqueteSolicitudLiberarAnimal paquete = new Network.PaqueteSolicitudLiberarAnimal();
                    paquete.idAnimal = animal.getId();

                    if (gameClient != null) {
                        gameClient.send(paquete);
                    }

                    animal.setVivo(false);
                    continue;
                }

                // 2. Colisión: Enemigos vs Animal
                for (RobotVisual enemigo : enemigosEnPantalla.values()) {
                    // También se añade la comprobación para los enemigos.
                    if (enemigo.getBounds() != null && Intersector.overlaps(enemigo.getBounds(), animal.getBounds())) {
                        System.out.println("[CLIENT] Colisión de enemigo con animal vivo ID: " + animal.getId() + ". Solicitando muerte.");
                        Network.PaqueteSolicitudMatarAnimal paquete = new Network.PaqueteSolicitudMatarAnimal();
                        paquete.idAnimal = animal.getId();

                        if (gameClient != null) {
                            gameClient.send(paquete);
                        }

                        animal.setVivo(false);
                        break;
                    }
                }
            }
        }
        // --- FIN: LÓGICA DE COLISIÓN CON ANIMALES ---

        camaraJuego.position.x = personajeJugable.estado.x;
        camaraJuego.position.y = personajeJugable.estado.y;
        manejadorNivel.limitarCamaraAMapa(camaraJuego);
        camaraJuego.update();
        mainStage.act(Math.min(deltat, 1 / 30f));
    }


    @Override
    public void render(float delta) {
        super.render(delta);
        if (viewport != null) viewport.apply();

        batch.setProjectionMatrix(camaraJuego.combined);
        manejadorNivel.dibujar();

        batch.begin();

        //for (ItemVisual item : itemsEnPantalla.values()) item.draw(batch);
        manejadorNivel.dibujarArboles(batch);
        manejadorNivel.dibujarBloques(batch);

        personajeJugable.draw(batch);
        for (Player otro : otrosJugadores.values()) otro.draw(batch);
        for (RobotVisual enemigo : enemigosEnPantalla.values()) enemigo.draw(batch);

        // Dibujado de RobotnikVisual (eggman)
        if (eggman != null) {
            eggman.draw(batch);
        }



        // ---[CAMBIO]--- Se dibujan los animales obteniéndolos del LevelManager.
        manejadorNivel.dibujarAnimales(batch, delta);

        for (ItemVisual item : itemsEnPantalla.values()) item.draw(batch);
//develop
        batch.end();

        renderizarNeblinaConShader();

        mainStage.getViewport().apply();
        mainStage.draw();

        if (gameClient != null && personajeJugable != null && personajeJugable.estado != null && personajeJugable.estado.id != 0) {
            Network.PaquetePosicionJugador paquete = new Network.PaquetePosicionJugador();
            paquete.id = personajeJugable.estado.id;
            paquete.x = personajeJugable.estado.x;
            paquete.y = personajeJugable.estado.y;
            paquete.estadoAnimacion = personajeJugable.getEstadoActual();
            gameClient.send(paquete); // Usa el método de la interfaz
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (viewport != null) {
            viewport.update(width, height, true);
        }
        mainStage.getViewport().update(width, height, true);
        if (uiCamera != null) {
            uiCamera.setToOrtho(false, width, height);
            uiCamera.update();
        }
    }

    @Override
    public void pause() {
        if (soundManager != null) {
            soundManager.pauseBackgroundMusic();
        }
    }

    @Override
    public void resume() {
        if (soundManager != null) {
            soundManager.resumeBackgroundMusic();
        }
    }
    private void renderizarNeblinaConShader() {
        float maxRadius = (float) Math.sqrt(Math.pow(Gdx.graphics.getWidth(), 2) + Math.pow(Gdx.graphics.getHeight(), 2)) / 2f;
        float minRadius = 45.0f;
        float factorLimpieza = 1.0f - (porcentajeContaminacionActual / 100.0f);
        float radioActual = minRadius + (maxRadius - minRadius) * (factorLimpieza * factorLimpieza);

        // Usamos la cámara del juego (camaraJuego) para saber dónde se está dibujando el personaje.
        camaraJuego.project(screenCoords.set(personajeJugable.estado.x, personajeJugable.estado.y, 0));

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shaderNeblina.begin();

        // Pasamos nuestros uniforms de siempre
        shaderNeblina.setUniformf("u_resolution", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shaderNeblina.setUniformf("u_radius", radioActual);
        shaderNeblina.setUniformf("u_smoothness", 0.3f);
        shaderNeblina.setUniformf("u_fogColor", 0.1f, 0.6f, 0.2f, 0.65f);
        shaderNeblina.setUniformf("u_fogCenter", screenCoords.x, screenCoords.y);

        // Pasamos la matriz de la cámara al shader
        shaderNeblina.setUniformMatrix("u_projTrans", uiCamera.combined);

        // Dibujamos nuestra malla pre-construida. Es mucho más eficiente.
        quadMesh.render(shaderNeblina, GL20.GL_TRIANGLE_STRIP, 0, 4);

        shaderNeblina.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public void inicializarJugadorLocal(PlayerState estadoRecibido) {
        this.personajeJugable.estado = estadoRecibido;
        System.out.println("[CLIENT] ID asignado por el servidor: " + this.personajeJugable.estado.id);
    }

    private void crearEnemigoVisual(EnemigoState estadoEnemigo) {
        // Primero, comprobamos si es el caso especial de Robotnik.
        if (estadoEnemigo.tipo == EnemigoState.EnemigoType.ROBOTNIK) {
            if (this.eggman == null) { // Solo lo creamos si no lo tenemos ya.
                System.out.println("[CLIENT] Recibida orden de crear a ROBOTNIK (ID: " + estadoEnemigo.id + ")");
                this.eggman = new RobotnikVisual(estadoEnemigo, manejadorNivel);
            }
            return; // Importante: Salimos del método para no tratarlo como un enemigo normal.
        }

        // Si NO es Robotnik, aplicamos la lógica para los enemigos normales.
        if (!enemigosEnPantalla.containsKey(estadoEnemigo.id)) {
            System.out.println("[CLIENT] Recibida orden de crear enemigo normal con ID: " + estadoEnemigo.id);
            RobotVisual nuevoRobot = new RobotVisual(estadoEnemigo, manejadorNivel, this.gameClient);
            enemigosEnPantalla.put(estadoEnemigo.id, nuevoRobot);
        }
    }
    //para actualizar el label de los animales
    private void actualizarAnimalCountLabel() {
        animalCountLabel.setText("Animals: " + animalesVivos + "/" + totalAnimalesMapa + " (" + animalesMuertos + " Muertes)");
    }

    //para limpiar los enemigos en los otros mapasaaaaaaaa
    private void limpiarEnemigosEItems() {
        for (RobotVisual enemigo : enemigosEnPantalla.values()) {
            enemigo.dispose();
        }
        enemigosEnPantalla.clear();

        for (ItemVisual item : itemsEnPantalla.values()) {
            item.dispose();
        }
        itemsEnPantalla.clear();
        //para animales
        if (manejadorNivel != null) {

            manejadorNivel.limpiarAnimales(); // Si AnimalVisual tiene recursos que liberar
        }

            animalesVivos = 0; // Reiniciar contadores al limpiar el mapa
            animalesMuertos = 0;
            totalAnimalesMapa = 0;
            actualizarAnimalCountLabel(); // Refrescar el label

    }

    //para delimitar los robots y crearlos por mapa
    private void crearRobotsPorNivel(String nombreNivel) {
        limpiarEnemigosEItems();
        if (nombreNivel.equals("maps/Zona1N1.tmx")) {
            for (int i = 0; i < 5; i++) {
                EnemigoState estado = new EnemigoState(
                    i,
                    100 + i * 50,
                    200,
                    100, // vida
                    EnemigoState.EnemigoType.ROBOT
                );
                crearEnemigoVisual(estado);
            }
        } else if (nombreNivel.equals("maps/ZonaJefeN1.tmx")) {
            for (int i = 10; i < 15; i++) {
                EnemigoState estado = new EnemigoState(
                    i,
                    300 + (i - 10) * 60,
                    250,
                    200, // vida
                    EnemigoState.EnemigoType.ROBOT
                );
                crearEnemigoVisual(estado);
            }
        }
    }

    private void crearItemVisual(ItemState estadoItem) {
        System.out.println("[CLIENT DEBUG] Recibida orden para crear item. TIPO: " + estadoItem.tipo + ", ID: " + estadoItem.id);
        if (!itemsEnPantalla.containsKey(estadoItem.id)) {
            System.out.println("[CLIENT] Recibida orden de crear ítem tipo " + estadoItem.tipo + " con ID: " + estadoItem.id);
            ItemVisual nuevoItem = null;
            // Creamos el tipo de ítem visual correcto según la información del servidor
            switch (estadoItem.tipo) {
                case ANILLO:
                    nuevoItem = new AnillosVisual(estadoItem);
                    break;
                case BASURA:
                    nuevoItem = new BasuraVisual(estadoItem);
                    break;
                case PIEZA_PLASTICO:
                    nuevoItem = new PiezaDePlasticoVisual(estadoItem);
                    break;
                case TELETRANSPORTE:
                    nuevoItem = new TeletransporteVisual(estadoItem);
                    break;
            }

            if (nuevoItem != null) {
                itemsEnPantalla.put(estadoItem.id, nuevoItem);
            }
        }
    }


    public void agregarOActualizarOtroJugador(PlayerState estadoRecibido) {
        Player jugadorVisual = otrosJugadores.get(estadoRecibido.id);
        if (jugadorVisual == null) {
            System.out.println("Creando nuevo jugador gráfico con ID: " + estadoRecibido.id);

            switch (personajeJugableEstado.characterType) {
                case SONIC:
                    jugadorVisual = new Sonic(estadoRecibido);
                    break;
                case TAILS:
                    // Asegúrate de que tienes una clase Tails que hereda de Player
                    jugadorVisual = new Tails(estadoRecibido);
                    break;
                case KNUCKLES:
                    jugadorVisual = new Knuckles(estadoRecibido);
                    break;
                default:
                    // Si algo sale mal, por defecto será Sonic
                    personajeJugable = new Sonic(personajeJugableEstado, manejadorNivel);
                    break;
            }

            if (jugadorVisual instanceof Tails) {
                ((Tails) jugadorVisual).setOnlineMode(true);
            }

            otrosJugadores.put(estadoRecibido.id, jugadorVisual);
        } else {
            jugadorVisual.estado.x = estadoRecibido.x;
            jugadorVisual.estado.y = estadoRecibido.y;
            jugadorVisual.setEstadoActual(estadoRecibido.estadoAnimacion);
        }
    }

    public void actualizarPosicionOtroJugador(int id, float x, float y, Entity.EstadoPlayer estadoAnim) {
        Player jugador = otrosJugadores.get(id);
        if (jugador != null) {
            jugador.estado.x = x;
            jugador.estado.y = y;
            jugador.setEstadoActual(estadoAnim);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        manejadorNivel.dispose();
        personajeJugable.dispose();
        if (assetManager != null) assetManager.dispose();
        if (soundManager != null) soundManager.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        for (RobotVisual enemigo : enemigosEnPantalla.values()) enemigo.dispose();

        // Dispose de RobotnikVisual (eggman)
        if (eggman != null) {
            eggman.dispose();
        }

        for (ItemVisual item : itemsEnPantalla.values()) {
            item.dispose();
        }

        // ---[AÑADIR EN dispose()]---

        if (animalCountLabel != null) {
            animalCountLabel.remove(); // Elimina el actor del Stage
        }


        if (contadorAnillos != null) contadorAnillos.dispose();
        if (contadorBasura != null) contadorBasura.dispose();
        if (shaderNeblina != null) shaderNeblina.dispose();
        if (quadMesh != null) quadMesh.dispose();
        if (font != null) font.dispose();
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }
}
