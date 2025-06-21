// Archivo: src/com/JSonic/uneg/PantallaDeJuego.java (COMPLETO Y CORREGIDO)
package com.JSonic.uneg;

// Imports...
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import java.util.Iterator;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import network.GameClient;
import network.Network;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;

public class PantallaDeJuego extends PantallaBase {

    // --- VARIABLES ORIGINALES ---
    public static final float VIRTUAL_WIDTH = 1280;
    public static final float VIRTUAL_HEIGHT = 720;
    private OrthographicCamera camaraJuego;
    private Viewport viewport;
    private final JSonicJuego juegoPrincipal;
    private final SpriteBatch batch;
    private LevelManager manejadorNivel;
    private GameClient gameClient;
    private Sonic sonic;
    private PlayerState sonicEstado;
    private final HashMap<Integer, Player> otrosJugadores = new HashMap<>();
    private SoundManager soundManager;
    private static final String BACKGROUND_MUSIC_PATH2 = "SoundsBackground/Dating Fight.mp3";
    private AssetManager assetManager;
    private ShapeRenderer shapeRenderer;
    private ArrayList<RobotVisual> enemigosEnPantalla;
    private float tiempoGeneracionEnemigo = 0f;
    private final float INTERVALO_GENERACION_ENEMIGO = 5.0f;
    private int proximoIdEnemigo = 0;

    // --- VARIABLES PARA ÍTEMS (AHORA COMPLETAS) ---
    private ArrayList<ItemVisual> itemsEnPantalla = new ArrayList<>();
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

    public PantallaDeJuego(JSonicJuego juego) {
        super();
        this.juegoPrincipal = juego;
        this.batch = juego.batch;
    }

    @Override
    // Tu método inicializar original (SIN CAMBIOS... por ahora)
    public void inicializar() {
        camaraJuego = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camaraJuego);
        manejadorNivel = new LevelManager(camaraJuego, batch);
        manejadorNivel.cargarNivel("maps/Zona1N1.tmx");
        sonicEstado = new PlayerState();
        sonicEstado.x = 100;
        sonicEstado.y = 100;
        sonic = new Sonic(sonicEstado, manejadorNivel);
        assetManager = new AssetManager();
        soundManager = new SoundManager(assetManager);
        manejadorNivel.setPlayer(sonic);
        enemigosEnPantalla = new ArrayList<>();
        crearNuevoEnemigo(200, 300);
        soundManager.loadMusic(BACKGROUND_MUSIC_PATH2);

        // Si esta línea te da problemas de conexión, coméntala.
        // Si no, déjala como estaba.
        //  gameClient = new GameClient(this);

        soundManager.playBackgroundMusic(BACKGROUND_MUSIC_PATH2, 0.5f, true);
        assetManager.finishLoading();
        shapeRenderer = new ShapeRenderer();
    }

    private void spawnItem(ItemState.ItemType tipo) {
        if (manejadorNivel == null || manejadorNivel.getAnchoMapaPixels() == 0) return;
        float randomX = MathUtils.random(0, manejadorNivel.getAnchoMapaPixels());
        float randomY = MathUtils.random(0, manejadorNivel.getAltoMapaPixels());
        ItemState nuevoEstado = new ItemState(proximoIdItem++, randomX, randomY, tipo);
        ItemVisual nuevoItem = null;

        switch (tipo) {
            case ANILLO:
                nuevoItem = new AnillosVisual(nuevoEstado);
                break;
            case BASURA:
                nuevoItem = new BasuraVisual(nuevoEstado);
                break;
            case PIEZA_PLASTICO:
                nuevoItem = new PiezaDePlasticoVisual(nuevoEstado);
                break;
        }

        if (nuevoItem != null) {
            itemsEnPantalla.add(nuevoItem);
        }
    }

    @Override
    public void actualizar(float deltat) {
        // --- LÓGICA DE SPAWNING (AHORA COMPLETA) ---
        int anillos = 0, basura = 0, plastico = 0;
        for(ItemVisual item : itemsEnPantalla) {
            if(item.estado.tipo == ItemState.ItemType.ANILLO) anillos++;
            if(item.estado.tipo == ItemState.ItemType.BASURA) basura++;
            if(item.estado.tipo == ItemState.ItemType.PIEZA_PLASTICO) plastico++;
        }

        tiempoSpawnAnillo += deltat;
        if (tiempoSpawnAnillo >= INTERVALO_SPAWN_ANILLO && anillos < MAX_ANILLOS) {
            spawnItem(ItemState.ItemType.ANILLO);
            tiempoSpawnAnillo = 0f;
        }

        tiempoSpawnBasura += deltat;
        if (tiempoSpawnBasura >= INTERVALO_SPAWN_BASURA && basura < MAX_BASURA) {
            spawnItem(ItemState.ItemType.BASURA);
            tiempoSpawnBasura = 0f;
        }

        tiempoSpawnPlastico += deltat;
        if (tiempoSpawnPlastico >= INTERVALO_SPAWN_PLASTICO && plastico < MAX_PLASTICO) {
            spawnItem(ItemState.ItemType.PIEZA_PLASTICO);
            tiempoSpawnPlastico = 0f;
        }

        // Actualizar ítems y comprobar colisiones con jugador
        Iterator<ItemVisual> iter = itemsEnPantalla.iterator();
        while (iter.hasNext()) {
            ItemVisual item = iter.next();
            item.update(deltat);
            if (sonic.getBounds() != null && item.getBounds() != null && Intersector.overlaps(sonic.getBounds(), item.getBounds())) {
                iter.remove();
                item.dispose();
            }
        }

        // --- LÓGICA DE JUGADOR Y ENEMIGOS ---
        sonic.KeyHandler(); // Esto ahora maneja el movimiento Y la colisión con el mapa
        sonic.update(deltat);

        for (RobotVisual enemigo : enemigosEnPantalla) {
            enemigo.update(deltat);
        }

        // Actualizar cámara
        camaraJuego.position.x = sonic.estado.x;
        camaraJuego.position.y = sonic.estado.y;
        manejadorNivel.limitarCamaraAMapa(camaraJuego);
        camaraJuego.update();
    }

    // El resto de la clase (render, dispose, etc.) se queda igual que la última versión que te dí.
    // Tu método render original (CON MODIFICACIONES)
    @Override
    public void render(float delta) {
        super.render(delta);
        if (viewport != null) viewport.apply();

        batch.setProjectionMatrix(camaraJuego.combined);
        manejadorNivel.dibujar();

        batch.begin();
        sonic.draw(batch);
        for (Player otro : otrosJugadores.values()) otro.draw(batch);
        for (RobotVisual enemigo : enemigosEnPantalla) enemigo.draw(batch);

        // --- INICIO: DIBUJAR ÍTEMS ---
        for (ItemVisual item : itemsEnPantalla) {
            item.draw(batch);
        }
        // --- FIN: DIBUJAR ÍTEMS ---
        batch.end();

        // --- DIBUJO DE DEPURACIÓN (MUY RECOMENDADO) ---
        if (shapeRenderer != null) {
            shapeRenderer.setProjectionMatrix(camaraJuego.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            // Dibuja hitbox de sonic
            shapeRenderer.setColor(1, 0, 0, 1);
            shapeRenderer.rect(sonic.getBounds().x, sonic.getBounds().y, sonic.getBounds().width, sonic.getBounds().height);
            // Dibuja hitbox de ítems
            shapeRenderer.setColor(0, 0, 1, 1);
            for (ItemVisual item : itemsEnPantalla) {
                if (item.getBounds() != null) shapeRenderer.rect(item.getBounds().x, item.getBounds().y, item.getBounds().width, item.getBounds().height);
            }
            shapeRenderer.end();
        }
        // --- FIN DIBUJO DEPURACIÓN ---

        // --- Lógica original ---
        if (gameClient != null && sonic != null && sonic.estado != null) { /* ... */ }
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
        for (RobotVisual enemigo : enemigosEnPantalla) enemigo.dispose();

        // --- INICIO: LIMPIAR ÍTEMS ---
        for (ItemVisual item : itemsEnPantalla) {
            item.dispose();
        }
        // --- FIN: LIMPIAR ÍTEMS ---
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

    private void crearNuevoEnemigo(float x, float y) {
        EnemigoState nuevoEstadoEnemigo = new EnemigoState(proximoIdEnemigo++, x, y, 100, EnemigoState.EnemigoType.ROBOT);
        RobotVisual nuevoRobot = new RobotVisual(nuevoEstadoEnemigo, manejadorNivel);
        enemigosEnPantalla.add(nuevoRobot);
    }

    public void inicializarJugadorLocal(PlayerState estadoRecibido) {
        this.sonic.estado = estadoRecibido;
        System.out.println("[CLIENT] ID asignado por el servidor: " + this.sonic.estado.id);
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
}
