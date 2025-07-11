// Archivo: src/com/JSonic/uneg/PantallaDeJuego.java
package com.JSonic.uneg;

// Tus imports se mantienen igual...
import com.JSonic.uneg.EnemigoState.EnemigoType;
import com.JSonic.uneg.Entity.EstadoPlayer;
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

import java.util.*;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.math.Vector2;

public class PantallaDeJuego extends PantallaBase {

    // --- TUS VARIABLES SE MANTIENEN IGUAL ---
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
    private float tiempoTranscurrido = 0f;
    private boolean teletransporteCreado = false;
    private RobotnikVisual eggman;
    private final ArrayList<Bomba> listaDeBombas = new ArrayList<>();
    private float tiempoParaProximaBomba = 4.0f;
    private static final float CADENCIA_BOMBA = 4.0f;

    private boolean sonicFlashActivoEnFrameAnterior = false;


    public PantallaDeJuego(JSonicJuego juego, IGameClient client, IGameServer server) {
        super();
        this.juegoPrincipal = juego;
        this.batch = juego.batch;
        this.gameClient = client;
        this.localServer = server;
    }

    public PantallaDeJuego(JSonicJuego juego) {
        this(juego, null, null);
    }

    @Override
    public void inicializar() {
        camaraJuego = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camaraJuego);
        manejadorNivel = new LevelManager(camaraJuego, batch);
        manejadorNivel.cargarNivel("maps/ZonaJefeN2.tmx");
        personajeJugableEstado = new PlayerState();

        Vector2 llegada = manejadorNivel.obtenerPosicionLlegada();
        personajeJugableEstado.x = llegada.x;
        personajeJugableEstado.y = llegada.y;

        PlayerState.CharacterType miPersonaje = PlayerState.CharacterType.SONIC;
        personajeJugableEstado.characterType = miPersonaje;

        System.out.println("[CLIENT] Creando jugador local como: " + miPersonaje);
        switch (miPersonaje) {
            case SONIC:
                personajeJugable = new Sonic(personajeJugableEstado, manejadorNivel);
                break;
            case TAILS:
                personajeJugable = new Tails(personajeJugableEstado, manejadorNivel);
                break;
            case KNUCKLES:
                personajeJugable = new Knuckles(personajeJugableEstado, manejadorNivel);
                break;
            default:
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

        ShaderProgram.pedantic = false;
        shaderNeblina = new ShaderProgram(
            Gdx.files.internal("shaders/neblina.vert"),
            Gdx.files.internal("shaders/neblina.frag")
        );

        if (!shaderNeblina.isCompiled()) {
            Gdx.app.error("Shader Error", "No se pudo compilar el shader de neblina: " + shaderNeblina.getLog());
        }

        quadMesh = new Mesh(
            true, 4, 4,
            new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position")
        );

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float[] vertices = {
            0, 0, 0,
            screenWidth, 0, 0,
            0, screenHeight, 0,
            screenWidth, screenHeight, 0
        };
        short[] indices = { 0, 1, 2, 3 };

        quadMesh.setVertices(vertices);
        quadMesh.setIndices(indices);

        String numerosTexturaPath = "Fondos/numerosContadorAnillos.png";
        contadorAnillos = new ContadorUI(numerosTexturaPath);
        contadorBasura = new ContadorUI(numerosTexturaPath);

        Table tablaUI = new Table();
        tablaUI.top().right();
        tablaUI.setFillParent(true);
        tablaUI.pad(10);

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
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.PURPLE);

        contaminationLabel = new Label("TOXIC: 0%", labelStyle);

        Table tablaInferior = new Table();
        tablaInferior.setFillParent(true);
        tablaInferior.bottom().right();
        tablaInferior.pad(15);
        tablaInferior.add(contaminationLabel);
        mainStage.addActor(tablaInferior);
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
            localServer.update(deltat, this.manejadorNivel, personajeJugable);
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
                        if (estadoServidor.tipo == EnemigoType.ROBOTNIK) {
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
                    this.anillosTotal = p.nuevosAnillos;
                    this.basuraTotal = p.nuevaBasura;
                    contadorAnillos.setValor(this.anillosTotal);
                    contadorBasura.setValor(this.basuraTotal);

                    if (anillosTotal == 100){
                        personajeJugable.setVida(personajeJugable.getVida() + 100);
                    }

                } else if (paquete instanceof Network.PaqueteActualizacionContaminacion p) {
                    this.porcentajeContaminacionActual = p.contaminationPercentage;
                    if (porcentajeContaminacionActual >= 10 && porcentajeContaminacionActual < 30) {
                        personajeJugable.setVida(personajeJugable.getVida() - 3);
                    }
                    if (porcentajeContaminacionActual >= 30 && porcentajeContaminacionActual < 60) {
                        personajeJugable.setVida(personajeJugable.getVida() - 5);
                    } else if (porcentajeContaminacionActual >= 60 && porcentajeContaminacionActual < 70) {
                        personajeJugable.setVida(personajeJugable.getVida() - 7);
                    } else if (porcentajeContaminacionActual >= 70 && porcentajeContaminacionActual <= 99) {
                        personajeJugable.setVida(personajeJugable.getVida() - 10);
                    } else if (porcentajeContaminacionActual == 100) {
                        personajeJugable.setVida(0);
                        personajeJugable = null;
                    }
                    if(contaminationLabel != null) {
                        contaminationLabel.setText("TOXIC: " + Math.round(this.porcentajeContaminacionActual) + "%");
                    }
                }
            }
        }

        if (eggman != null) {
            if(manejadorNivel.getNombreMapaActual().equals("maps/ZonaJefeN1.tmx") || manejadorNivel.getNombreMapaActual().equals("maps/ZonaJefeN2.tmx") || manejadorNivel.getNombreMapaActual().equals("maps/ZonaJefeN3.tmx")) {
                tiempoParaProximaBomba -= deltat;
                if (tiempoParaProximaBomba <= 0) {
                    lanzarBombaDesdeEggman();
                    tiempoParaProximaBomba = CADENCIA_BOMBA;
                }
            }
        }

        Iterator<Bomba> iterBombas = listaDeBombas.iterator();
        while (iterBombas.hasNext()) {
            Bomba bomba = iterBombas.next();
            bomba.update(deltat,personajeJugable);

            if (bomba.isExplotando() && !bomba.yaHaHechoDanio()) {
                if (bomba.getBounds().overlaps(personajeJugable.getBounds())) {
                    personajeJugable.setVida(personajeJugable.getVida() - 20);
                    bomba.marcarComoDanioHecho();
                }
            }
            if (bomba.isParaEliminar()) {
                bomba.dispose();
                iterBombas.remove();
            }
        }

        for (ItemVisual item : itemsEnPantalla.values()) {
            item.update(deltat);
        }

        Iterator<Map.Entry<Integer, ItemVisual>> iter = itemsEnPantalla.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, ItemVisual> entry = iter.next();
            ItemVisual item = entry.getValue();
            if (personajeJugable.getBounds() != null && item.getBounds() != null && Intersector.overlaps(personajeJugable.getBounds(), item.getBounds())) {
                Network.PaqueteSolicitudRecogerItem paquete = new Network.PaqueteSolicitudRecogerItem();
                paquete.idItem = item.estado.id;
                gameClient.send(paquete);
                if (item.estado.tipo == ItemState.ItemType.TELETRANSPORTE) {
                    break;
                }
            }
        }

        personajeJugable.KeyHandler();
        personajeJugable.update(deltat);

        gestionarHabilidadDeLimpiezaDeSonic();

        for (Player otro : otrosJugadores.values()){
            otro.update(deltat);
        }

        for (RobotVisual enemigo : enemigosEnPantalla.values()){
            if (personajeJugable.getBounds().overlaps(enemigo.getBounds())) {
                boolean jugadorEstaAtacando = personajeJugable.estado.estadoAnimacion == EstadoPlayer.HIT_RIGHT ||
                    personajeJugable.estado.estadoAnimacion == EstadoPlayer.HIT_LEFT ||
                    personajeJugable.estado.estadoAnimacion == EstadoPlayer.KICK_RIGHT ||
                    personajeJugable.estado.estadoAnimacion == EstadoPlayer.KICK_LEFT ||
                    personajeJugable.estado.estadoAnimacion == EstadoPlayer.SPECIAL_LEFT ||
                    personajeJugable.estado.estadoAnimacion == EstadoPlayer.SPECIAL_RIGHT;

                if (jugadorEstaAtacando && enemigo.estado.estadoAnimacion != EnemigoState.EstadoEnemigo.HIT_LEFT && enemigo.estado.estadoAnimacion != EnemigoState.EstadoEnemigo.HIT_RIGHT) {
                    enemigo.setVida(enemigo.getVida() - 1);
                } else if (!jugadorEstaAtacando && (enemigo.estado.estadoAnimacion == EnemigoState.EstadoEnemigo.HIT_LEFT || enemigo.estado.estadoAnimacion == EnemigoState.EstadoEnemigo.HIT_RIGHT)) {
                    personajeJugable.setVida(personajeJugable.getVida() - 1);
                } else if (jugadorEstaAtacando && (enemigo.estado.estadoAnimacion == EnemigoState.EstadoEnemigo.HIT_LEFT || enemigo.estado.estadoAnimacion == EnemigoState.EstadoEnemigo.HIT_RIGHT)) {
                    Random random = new Random();
                    if (random.nextBoolean()) {
                        personajeJugable.setVida(personajeJugable.getVida() - 1);
                    } else {
                        enemigo.setVida(enemigo.getVida() - 1);
                    }
                }
            }
            enemigo.update(deltat);
        }
        Iterator<RobotVisual> iterator = enemigosEnPantalla.values().iterator();
        while (iterator.hasNext()) {
            RobotVisual enemigo = iterator.next();
            if(enemigo.getVida() <= 0){
                enemigo.dispose();
                iterator.remove();
            }
        }

        if (eggman != null){
            if (personajeJugable.getBounds().overlaps(eggman.getBounds())) {
                boolean jugadorEstaAtacando = personajeJugable.estado.estadoAnimacion == EstadoPlayer.HIT_RIGHT ||
                    personajeJugable.estado.estadoAnimacion == EstadoPlayer.HIT_LEFT ||
                    personajeJugable.estado.estadoAnimacion == EstadoPlayer.KICK_RIGHT ||
                    personajeJugable.estado.estadoAnimacion == EstadoPlayer.KICK_LEFT ||
                    personajeJugable.estado.estadoAnimacion == EstadoPlayer.SPECIAL_LEFT ||
                    personajeJugable.estado.estadoAnimacion == EstadoPlayer.SPECIAL_RIGHT;

                if (jugadorEstaAtacando) {
                    eggman.setVida(eggman.getVida() - 3);
                }
            }
            eggman.update(deltat);
        }

        camaraJuego.position.x = personajeJugable.estado.x;
        camaraJuego.position.y = personajeJugable.estado.y;
        manejadorNivel.limitarCamaraAMapa(camaraJuego);
        camaraJuego.update();
        mainStage.act(Math.min(deltat, 1 / 30f));
    }

    private void gestionarHabilidadDeLimpiezaDeSonic() {
        if (personajeJugable instanceof Sonic) {
            Sonic sonic = (Sonic) personajeJugable;

            boolean flashActivoAhora = sonic.getFlashDurationTimer() > 0;

            if (flashActivoAhora && !sonicFlashActivoEnFrameAnterior) {
                Gdx.app.log("PantallaDeJuego", "Habilidad de limpieza de Sonic detectada!");
                // CAMBIO: Renombramos la variable para que sea más genérica.
                ArrayList<Integer> idsItemsLimpiezaARecoger = new ArrayList<>();

                for (ItemVisual item : itemsEnPantalla.values()) {
                    // CAMBIO PRINCIPAL: Ahora comprobamos si el ítem es BASURA o PIEZA_PLASTICO.
                    if (item.estado.tipo == ItemState.ItemType.BASURA || item.estado.tipo == ItemState.ItemType.PIEZA_PLASTICO) {
                        idsItemsLimpiezaARecoger.add(item.estado.id);
                    }
                }

                if (!idsItemsLimpiezaARecoger.isEmpty()) {
                    // CAMBIO: Actualizamos el mensaje de log para ser más preciso.
                    Gdx.app.log("PantallaDeJuego", "Enviando solicitud para recoger " + idsItemsLimpiezaARecoger.size() + " ítems de contaminación.");

                    // CAMBIO: Usamos un nombre de variable más genérico en el bucle.
                    for (Integer idItem : idsItemsLimpiezaARecoger) {
                        Network.PaqueteSolicitudRecogerItem paquete = new Network.PaqueteSolicitudRecogerItem();
                        paquete.idItem = idItem;
                        gameClient.send(paquete);
                    }
                }
            }
            this.sonicFlashActivoEnFrameAnterior = flashActivoAhora;
        }
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

        if (eggman != null){
            if(manejadorNivel.getNombreMapaActual().equals("maps/ZonaJefeN1.tmx") || manejadorNivel.getNombreMapaActual().equals("maps/ZonaJefeN2.tmx") || manejadorNivel.getNombreMapaActual().equals("maps/ZonaJefeN3.tmx"))
                eggman.draw(batch);
        }

        for (Bomba bomba : listaDeBombas) {
            bomba.draw(batch);
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
            gameClient.send(paquete);
        }
    }

    private void lanzarBombaDesdeEggman() {
        if (eggman == null || personajeJugable == null) return;
        EnemigoState estadoBomba = new EnemigoState(0, eggman.estado.x, eggman.estado.y, 1, EnemigoType.ROBOT);
        Vector2 velocidad = new Vector2(
            personajeJugable.estado.x - eggman.estado.x,
            personajeJugable.estado.y - eggman.estado.y
        );
        velocidad.nor().scl(180f);
        estadoBomba.estadoAnimacion = (velocidad.x >= 0) ? EnemigoState.EstadoEnemigo.RUN_RIGHT : EnemigoState.EstadoEnemigo.RUN_LEFT;
        Bomba nuevaBomba = new Bomba(estadoBomba, velocidad, 2.5f);
        listaDeBombas.add(nuevaBomba);
    }

    private void reiniciarTeletransporte() {
        teletransporteCreado = false;
        tiempoTranscurrido = 0f;
    }

    private void renderizarNeblinaConShader() {
        float maxRadius = (float) Math.sqrt(Math.pow(Gdx.graphics.getWidth(), 2) + Math.pow(Gdx.graphics.getHeight(), 2)) / 2f;
        float minRadius = 45.0f;
        float factorLimpieza = 1.0f - (porcentajeContaminacionActual / 100.0f);
        float radioActual = minRadius + (maxRadius - minRadius) * (factorLimpieza * factorLimpieza);

        camaraJuego.project(screenCoords.set(personajeJugable.estado.x, personajeJugable.estado.y, 0));

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shaderNeblina.begin();
        shaderNeblina.setUniformf("u_resolution", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shaderNeblina.setUniformf("u_radius", radioActual);
        shaderNeblina.setUniformf("u_smoothness", 0.3f);
        shaderNeblina.setUniformf("u_fogColor", 0.1f, 0.6f, 0.2f, 0.65f);
        shaderNeblina.setUniformf("u_fogCenter", screenCoords.x, screenCoords.y);
        shaderNeblina.setUniformMatrix("u_projTrans", uiCamera.combined);

        quadMesh.render(shaderNeblina, GL20.GL_TRIANGLE_STRIP, 0, 4);

        shaderNeblina.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public void inicializarJugadorLocal(PlayerState estadoRecibido) {
        this.personajeJugable.estado = estadoRecibido;
        System.out.println("[CLIENT] ID asignado por el servidor: " + this.personajeJugable.estado.id);
    }

    private void crearEnemigoVisual(EnemigoState estadoEnemigo) {
        if (estadoEnemigo.tipo == EnemigoType.ROBOTNIK) {
            if (this.eggman == null) {
                this.eggman = new RobotnikVisual(estadoEnemigo, manejadorNivel);
            }
            return;
        }
        if (!enemigosEnPantalla.containsKey(estadoEnemigo.id)) {
            RobotVisual nuevoRobot = new RobotVisual(estadoEnemigo, manejadorNivel, this.gameClient);
            enemigosEnPantalla.put(estadoEnemigo.id, nuevoRobot);
        }
    }

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

    private void crearItemVisual(ItemState estadoItem) {
        if (!itemsEnPantalla.containsKey(estadoItem.id)) {
            ItemVisual nuevoItem = null;
            switch (estadoItem.tipo) {
                case ANILLO: nuevoItem = new AnillosVisual(estadoItem); break;
                case BASURA: nuevoItem = new BasuraVisual(estadoItem); break;
                case PIEZA_PLASTICO: nuevoItem = new PiezaDePlasticoVisual(estadoItem); break;
                case TELETRANSPORTE: nuevoItem = new TeletransporteVisual(estadoItem); break;
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
            switch (estadoRecibido.characterType) {
                case SONIC:
                    jugadorVisual = new Sonic(estadoRecibido, manejadorNivel);
                    break;
                case TAILS:
                    jugadorVisual = new Tails(estadoRecibido, manejadorNivel);
                    break;
                case KNUCKLES:
                    // *** CORRECCIÓN: Se cambió "uneg.Knuckles" por "new Knuckles" ***
                    jugadorVisual = new Knuckles(estadoRecibido, manejadorNivel);
                    break;
                default:
                    jugadorVisual = new Sonic(estadoRecibido, manejadorNivel);
                    break;
            }
            otrosJugadores.put(estadoRecibido.id, jugadorVisual);
        } else {
            jugadorVisual.estado.x = estadoRecibido.x;
            jugadorVisual.estado.y = estadoRecibido.y;
            jugadorVisual.setEstadoActual(estadoRecibido.estadoAnimacion);
        }
    }

    public void actualizarPosicionOtroJugador(int id, float x, float y, EstadoPlayer estadoAnim) {
        Player jugador = otrosJugadores.get(id);
        if (jugador != null) {
            jugador.estado.x = x;
            jugador.estado.y = y;
            jugador.setEstadoActual(estadoAnim);
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (viewport != null) viewport.update(width, height, true);
        mainStage.getViewport().update(width, height, true);
        if (uiCamera != null) uiCamera.setToOrtho(false, width, height);
    }

    @Override
    public void pause() {
        if (soundManager != null) soundManager.pauseBackgroundMusic();
    }

    @Override
    public void resume() {
        if (soundManager != null) soundManager.resumeBackgroundMusic();
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
        if (eggman != null) eggman.dispose();
        for (Bomba bomba : listaDeBombas) {
            bomba.dispose();
        }
        listaDeBombas.clear();
        for (ItemVisual item : itemsEnPantalla.values()) item.dispose();
        if (contadorAnillos != null) contadorAnillos.dispose();
        if (contadorBasura != null) contadorBasura.dispose();
        if (shaderNeblina != null) shaderNeblina.dispose();
        if (quadMesh != null) quadMesh.dispose();
        if (font != null) font.dispose();
    }
}
