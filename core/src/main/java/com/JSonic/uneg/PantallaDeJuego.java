// Archivo: src/com/JSonic/uneg/PantallaDeJuego.java (COMPLETO Y CORREGIDO)
package com.JSonic.uneg;

// Imports...
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import network.Network;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import network.interfaces.IGameClient;
import network.interfaces.IGameServer;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.graphics.Texture;

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
    private Sonic sonic;
    private PlayerState sonicEstado;
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

    //para el teletransporte
    // Al inicio de la clase
    private float tiempoTranscurrido = 0f;
    private boolean teletransporteCreado = false;

    public PantallaDeJuego(JSonicJuego juego, IGameClient client, IGameServer server) {
        super();
        this.juegoPrincipal = juego;
        this.batch = juego.batch;
        this.gameClient = client; // Asignamos el cliente recibido
        this.localServer = server;  // Asignamos el servidor recibido (puede ser null)
    }

    public PantallaDeJuego(JSonicJuego juego) {
        this(juego, null, null); // Llamamos al constructor con nulls si no hay cliente/servidor
    }

    @Override
    /* Tu método inicializar original (SIN CAMBIOS... por ahora)*/
    public void inicializar() {
        camaraJuego = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camaraJuego);
        manejadorNivel = new LevelManager(camaraJuego, batch);
        // Java
        manejadorNivel.cargarNivel("maps/Zona1N1.tmx");
        sonicEstado = new PlayerState();

        if (manejadorNivel.getMapaActual().equals("maps/Zona1N1.tmx")) {
            sonicEstado.x = 100;
            sonicEstado.y = 100;
        } else if (manejadorNivel.getMapaActual().equals("maps/ZonaJefeN1.tmx")) {
            sonicEstado.x = 12.01f;
            sonicEstado.y = 156.08f;
        }

        sonic = new Sonic(sonicEstado, manejadorNivel);
        assetManager = new AssetManager();
        soundManager = new SoundManager(assetManager);
        manejadorNivel.setPlayer(sonic);
        soundManager.loadMusic(BACKGROUND_MUSIC_PATH2);
        soundManager.playBackgroundMusic(BACKGROUND_MUSIC_PATH2, 0.5f, true);
        assetManager.finishLoading();
        shapeRenderer = new ShapeRenderer();

        // --- INICIO: CONFIGURACIÓN UI Anillos ---
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
    }

    //para poder crear varios portales se necesita reiniciar el teletransporte
    // Dentro de PantallaDeJuego
    private void reiniciarTeletransporte() {
        teletransporteCreado = false;
        tiempoTranscurrido = 0f;
    }


    @Override
    public void actualizar(float deltat) {
        tiempoTranscurrido += deltat;
        if (!teletransporteCreado && tiempoTranscurrido >= 20f) {
            var layer = manejadorNivel.getMapaActual().getLayers().get("destinox");
            if (layer != null) {
                MapObjects objetos = layer.getObjects();
                int idBase = 999;
                for (com.badlogic.gdx.maps.MapObject obj : objetos) {
                    if (obj instanceof com.badlogic.gdx.maps.objects.RectangleMapObject rectObj) {
                        Rectangle rect = rectObj.getRectangle();
                        ItemState estadoTele = new ItemState(
                            idBase++,
                            rect.x,
                            rect.y,
                            ItemState.ItemType.TELETRANSPORTE
                        );
                        crearItemVisual(estadoTele);
                    }
                }
            } else {
                System.out.println("[CLIENT_DEBUG] Capa 'destinox' no encontrada en el mapa actual.");
            }
            teletransporteCreado = true;
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
                }
                else if (paquete instanceof Network.PaqueteJugadorConectado p) {
                    if (sonicEstado != null && p.nuevoJugador.id != sonicEstado.id) {
                        agregarOActualizarOtroJugador(p.nuevoJugador);
                    }
                } else if (paquete instanceof Network.PaquetePosicionJugador p) {
                    if (sonicEstado != null && p.id != sonicEstado.id) {
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
                        RobotVisual enemigoVisual = enemigosEnPantalla.get(estadoServidor.id);
                        if (enemigoVisual != null) {
                            enemigoVisual.estado.x = estadoServidor.x;
                            enemigoVisual.estado.y = estadoServidor.y;
                            enemigoVisual.setEstadoActual(estadoServidor.estadoAnimacion);
                        }
                    }
                }
            }
        }

        for (ItemVisual item : itemsEnPantalla.values()) {
            item.update(deltat);
        }

        // --- CORRECCIÓN: Manejo seguro del teletransporte ---
        Integer idTeletransporteAEliminar = null;
        Iterator<Map.Entry<Integer, ItemVisual>> iter = itemsEnPantalla.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, ItemVisual> entry = iter.next();
            ItemVisual item = entry.getValue();

            if (sonic.getBounds() != null && item.getBounds() != null && Intersector.overlaps(sonic.getBounds(), item.getBounds())) {

                Network.PaqueteSolicitudRecogerItem paquete = new Network.PaqueteSolicitudRecogerItem();
                paquete.idItem = item.estado.id;
                gameClient.send(paquete);

                System.out.println("[CLIENT_DEBUG] Colisión detectada con item tipo: " + item.estado.tipo);

                boolean itemRecogido = false;
                if (item.estado.tipo == ItemState.ItemType.ANILLO) {
                    anillosTotal++;
                    contadorAnillos.setValor(anillosTotal);
                    itemRecogido = true;
                } else if (item.estado.tipo == ItemState.ItemType.BASURA || item.estado.tipo == ItemState.ItemType.PIEZA_PLASTICO) {
                    basuraTotal++;
                    contadorBasura.setValor(basuraTotal);
                    itemRecogido = true;
                }
                // Teletransporte: solo guardamos el ID y salimos del ciclo
                // Java
                else if (item.estado.tipo == ItemState.ItemType.TELETRANSPORTE) {
                    manejadorNivel.cargarNivel("maps/ZonaJefeN1.tmx");
                    crearRobotsPorNivel("maps/ZonaJefeN1.tmx");
                    reiniciarTeletransporte();

                    // Asigna las coordenadas iniciales según el mapa cargado
                    if (manejadorNivel.getMapaActual().equals("maps/ZonaJefeN1.tmx")) {
                        // Buscar el objeto "Llegada" en la capa de objetos del mapa
                        com.badlogic.gdx.maps.tiled.TiledMap map = manejadorNivel.getTiledMap();
                        boolean llegadaEncontrada = false;
                        if (map != null) {
                            com.badlogic.gdx.maps.MapLayer destinoxLayer = map.getLayers().get("destinox");
                            if (destinoxLayer != null) {
                                for (com.badlogic.gdx.maps.MapObject obj : destinoxLayer.getObjects()) {
                                    String nombre = obj.getName() != null ? obj.getName() : "";
                                    String clase = obj.getProperties().containsKey("class") ? obj.getProperties().get("class", String.class) : "";
                                    if ((nombre.toLowerCase().contains("llegada") || clase.toLowerCase().contains("llegada"))
                                        && obj instanceof com.badlogic.gdx.maps.objects.RectangleMapObject) {
                                        com.badlogic.gdx.maps.objects.RectangleMapObject rectObj = (com.badlogic.gdx.maps.objects.RectangleMapObject) obj;
                                        com.badlogic.gdx.math.Rectangle rect = rectObj.getRectangle();
// Conversión de coordenadas Tiled (origen arriba-izquierda) a LibGDX (origen abajo-izquierda)
                                        float alturaMapa = map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class);
                                        float xFinal = rect.x;
                                        float yFinal = alturaMapa - rect.y - rect.height;
                                        sonic.estado.x = xFinal;
                                        sonic.estado.y = yFinal;
                                        System.out.println("[DEBUG] Llegada encontrada en destinox: rect.x=" + rect.x + ", rect.y=" + rect.y + ", width=" + rect.width + ", height=" + rect.height + ". Sonic en x=" + sonic.estado.x + ", y=" + sonic.estado.y);
                                       // System.out.println("[DEBUG] Llegada encontrada en destinox: rect.x=" + rect.x + ", rect.y=" + rect.y + ", width=" + rect.width + ", height=" + rect.height + ". Sonic en x=" + sonic.estado.x + ", y=" + sonic.estado.y);
                                        llegadaEncontrada = true;
                                        break;
                                    }

                                 }
                            }
                        }
                        // Si no se encontró el objeto Llegada, usar valores por defecto
                        if (!llegadaEncontrada) {
                            sonic.estado.x = 12.01f;
                            sonic.estado.y = 156.08f;
                        }
                             }

                    idTeletransporteAEliminar = item.estado.id;
                    System.out.println("[CLIENT_DEBUG] Teletransporte activado, cambiando de mapa.");
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

        sonic.KeyHandler();
        sonic.update(deltat);

        for (Player otro : otrosJugadores.values()) {
            otro.update(deltat);
        }

        for (RobotVisual enemigo : enemigosEnPantalla.values()) enemigo.update(deltat);

        camaraJuego.position.x = sonic.estado.x;
        camaraJuego.position.y = sonic.estado.y;
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
        sonic.draw(batch);
        for (Player otro : otrosJugadores.values()) otro.draw(batch);
        for (RobotVisual enemigo : enemigosEnPantalla.values()) enemigo.draw(batch);

        for (ItemVisual item : itemsEnPantalla.values()) item.draw(batch);

        batch.end();

        mainStage.getViewport().apply();
        mainStage.draw();

        if (gameClient != null && sonic != null && sonic.estado != null && sonic.estado.id != 0) {
            Network.PaquetePosicionJugador paquete = new Network.PaquetePosicionJugador();
            paquete.id = sonic.estado.id;
            paquete.x = sonic.estado.x;
            paquete.y = sonic.estado.y;
            paquete.estadoAnimacion = sonic.getEstadoActual();
            gameClient.send(paquete); // Usa el método de la interfaz
        }
    }



    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (viewport != null) {
            viewport.update(width, height, true);
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

    public void inicializarJugadorLocal(PlayerState estadoRecibido) {
        this.sonic.estado = estadoRecibido;
        System.out.println("[CLIENT] ID asignado por el servidor: " + this.sonic.estado.id);
    }

    private void crearEnemigoVisual(EnemigoState estadoEnemigo) {
        // Nos aseguramos de no crear un enemigo que ya exista
        if (!enemigosEnPantalla.containsKey(estadoEnemigo.id)) {
            System.out.println("[CLIENT] Recibida orden de crear enemigo con ID: " + estadoEnemigo.id);
            // Creamos el objeto visual usando el estado que nos dio el servidor
            RobotVisual nuevoRobot = new RobotVisual(estadoEnemigo, manejadorNivel, this.gameClient);
            enemigosEnPantalla.put(estadoEnemigo.id, nuevoRobot);
        }
    }

    //para limpriar los enemigos en los otros mapas
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
            jugadorVisual = new Sonic(estadoRecibido);
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

    // Tu método dispose original (CON MODIFICACIONES)
    @Override
    public void dispose() {
        super.dispose();
        manejadorNivel.dispose();
        sonic.dispose();
        if (assetManager != null) assetManager.dispose();
        if (soundManager != null) soundManager.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        for (RobotVisual enemigo : enemigosEnPantalla.values()) enemigo.dispose();

        for (ItemVisual item : itemsEnPantalla.values()) {
            item.dispose();
        }
        if (contadorAnillos != null) contadorAnillos.dispose();
        if (contadorBasura != null) contadorBasura.dispose();
    }

}
