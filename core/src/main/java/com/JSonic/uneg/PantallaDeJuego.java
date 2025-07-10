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
    private final HashMap<Integer, RobotVisual> enemigosEnPantalla = new HashMap<>();
    private final HashMap<Integer, ItemVisual> itemsEnPantalla = new HashMap<>();

    private int anillosTotal = 0;
    private int basuraTotal = 0;
    private float porcentajeContaminacionActual = 0f;
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

        System.out.println("[JUEGO] Personaje recibido del menú: " + miPersonaje);



        // 1. Asigna el tipo de personaje al estado que se usará para el jugador local.
        personajeJugableEstado.characterType = miPersonaje;

        // 2. Crea la instancia del personaje jugable localmente.
        // Este switch se asegura de que TÚ estás controlando al personaje correcto en tu pantalla.
        System.out.println("[CLIENT] Creando jugador local como: " + miPersonaje);
        switch (miPersonaje) {
            case SONIC:
                personajeJugable = new Sonic(personajeJugableEstado, manejadorNivel);
                break;
            case TAILS:
                // Asegúrate de que tienes una clase Tails que hereda de Player
                personajeJugable = new Tails(personajeJugableEstado, manejadorNivel);
                break;
            case KNUCKLES:
                personajeJugable = new Knuckles(personajeJugableEstado, manejadorNivel);
                break;
            default:
                // Si algo sale mal, por defecto será Sonic
                personajeJugable = new Sonic(personajeJugableEstado, manejadorNivel);
                break;
        }

        assetManager = new AssetManager();
        soundManager = new SoundManager(assetManager);
        manejadorNivel.setPlayer(personajeJugable);
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

        if (!shaderNeblina.isCompiled()) {
            Gdx.app.error("Shader Error", "No se pudo compilar el shader: " + shaderNeblina.getLog());
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


    }

    //para poder crear varios portales se necesita reiniciar el teletransporte
    private void reiniciarTeletransporte() {
        teletransporteCreado = false;
        tiempoTranscurrido = 0f;
    }

    @Override
    public void actualizar(float deltat) {

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
                System.out.println("[CLIENT_DEBUG] Procesando paquete de tipo: " + paquete.getClass().getSimpleName());

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
                    // Recorremos todos los estados de enemigos que nos envió el servidor
                    for (EnemigoState estadoServidor : p.estadosEnemigos.values()) {

                        // CASO ESPECIAL: ¿Es la actualización para Robotnik?
                        if (estadoServidor.tipo == EnemigoState.EnemigoType.ROBOTNIK) {
                            if (eggman != null) { // Si ya hemos creado el objeto visual de eggman...
                                // ...le aplicamos el estado que nos manda el servidor.
                                eggman.estado.x = estadoServidor.x;
                                eggman.estado.y = estadoServidor.y;
                                eggman.setEstadoActual(estadoServidor.estadoAnimacion);
                            }
                        }
                        // CASO GENERAL: Es un robot normal
                        else {
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

                    // Ejecutamos la lógica de cambio de mapa que eliminamos antes, pero ahora usando los datos del servidor.
                    manejadorNivel.cargarNivel(p.nuevoMapa);

                    // Limpiamos las entidades visuales del mapa anterior
                    limpiarEnemigosEItems();

                    // Reseteamos el temporizador para que el portal pueda volver a generarse en el futuro
                    reiniciarTeletransporte();

                    // Reposicionamos a nuestro jugador en las coordenadas que nos dictó el servidor
                    personajeJugable.estado.x = p.nuevaPosX;
                    personajeJugable.estado.y = p.nuevaPosY;

                    // Ahora que el nuevo mapa está cargado, enviamos su plano al servidor.
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

                    // Actualizamos nuestras variables locales y la UI con los datos del servidor.
                    this.anillosTotal = p.nuevosAnillos;
                    this.basuraTotal = p.nuevaBasura;
                    contadorAnillos.setValor(this.anillosTotal);
                    contadorBasura.setValor(this.basuraTotal);

                } else if (paquete instanceof Network.PaqueteActualizacionContaminacion p) {
                    // Guardamos el porcentaje recibido del servidor
                    this.porcentajeContaminacionActual = p.contaminationPercentage;
                    contaminationLabel.setText("TOXIC: " + Math.round(this.porcentajeContaminacionActual) + "%");

                }
            }
        }

        for (ItemVisual item : itemsEnPantalla.values()) {
            item.update(deltat);
        }

        Integer idTeletransporteAEliminar = null;
        Iterator<Map.Entry<Integer, ItemVisual>> iter = itemsEnPantalla.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, ItemVisual> entry = iter.next();
            ItemVisual item = entry.getValue();

            if (personajeJugable.getBounds() != null && item.getBounds() != null && Intersector.overlaps(personajeJugable.getBounds(), item.getBounds())) {

                Network.PaqueteSolicitudRecogerItem paquete = new Network.PaqueteSolicitudRecogerItem();
                paquete.idItem = item.estado.id;
                gameClient.send(paquete);
                System.out.println("[CLIENT_DEBUG] Colisión detectada con item tipo: " + item.estado.tipo);

                boolean itemRecogido = false;
                if (item.estado.tipo == ItemState.ItemType.ANILLO) {
                    itemRecogido = true;
                } else if (item.estado.tipo == ItemState.ItemType.BASURA || item.estado.tipo == ItemState.ItemType.PIEZA_PLASTICO) {
                    itemRecogido = true;
                }
                // Teletransporte: solo guardamos el ID y salimos del ciclo
                else if (item.estado.tipo == ItemState.ItemType.TELETRANSPORTE) {
                    // YA NO cambiamos de mapa. Solo informamos al servidor que hemos tocado el portal.
                    System.out.println("[CLIENT] Tocado el teletransportador. Solicitando viaje al servidor...");
                    paquete = new Network.PaqueteSolicitudRecogerItem();
                    paquete.idItem = item.estado.id;
                    gameClient.send(paquete);
                    // Para evitar enviar múltiples solicitudes, salimos del bucle una vez que tocamos el portal.
                    break;
                }

                if (itemRecogido) {
                    iter.remove();
                    item.dispose();
                    System.out.println("[CLIENT_DEBUG] Ítem " + item.estado.id + " eliminado visualmente para la prueba.");
                    break;
                }
            }
        }

        // Fuera del ciclo: limpiar enemigos/ítems y eliminar el teletransporte
        if (idTeletransporteAEliminar != null) {
            limpiarEnemigosEItems();
            ItemVisual item = itemsEnPantalla.remove(idTeletransporteAEliminar);
            if (item != null) item.dispose();
        }

        personajeJugable.KeyHandler();
        personajeJugable.update(deltat);

        for (Player otro : otrosJugadores.values()) {
            otro.update(deltat);
        }

        for (RobotVisual enemigo : enemigosEnPantalla.values()) enemigo.update(deltat);

        // Lógica de actualización de RobotnikVisual (eggman)
        if (eggman != null) {
            eggman.update(deltat); // Primero actualizamos su animación
        }


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
        personajeJugable.draw(batch);
        for (Player otro : otrosJugadores.values()) otro.draw(batch);
        for (RobotVisual enemigo : enemigosEnPantalla.values()) enemigo.draw(batch);

        // Dibujado de RobotnikVisual (eggman)
        if (eggman != null) {
            eggman.draw(batch);
        }

        for (ItemVisual item : itemsEnPantalla.values()) item.draw(batch);
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
