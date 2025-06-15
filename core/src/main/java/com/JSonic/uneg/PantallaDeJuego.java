package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.audio.Music;

public class PantallaDeJuego implements Screen {

    // Constantes para el tamaño virtual de la pantalla
    public static final float VIRTUAL_WIDTH = 1366; // Ancho virtual del juego
    public static final float VIRTUAL_HEIGHT = 768; // Alto virtual del juego

    // Objetos globales del juego, inyectados desde Main
    private SpriteBatch batchSprites;
    private AssetManager assetManager;
    private SoundManager soundManager;

    // Componentes del juego
    private OrthographicCamera camaraJuego;
    private Viewport viewport;
    private LevelManager manejadorNivel;
    private Sonic sonic; // Instancia del personaje Sonic

    // Música
    private Music currentBackgroundMusic;

    // Constructor que recibe las instancias de Batch, AssetManager y SoundManager
    public PantallaDeJuego(SpriteBatch batchSprites, AssetManager assetManager, SoundManager soundManager) {
        this.batchSprites = batchSprites;
        this.assetManager = assetManager;
        this.soundManager = soundManager;
    }

    @Override
    public void show() {
        camaraJuego = new OrthographicCamera();
        // Inicializa la cámara en el centro de tu mundo virtual
        camaraJuego.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camaraJuego);
        viewport.apply(); // Aplica el viewport al inicio

        manejadorNivel = new LevelManager(camaraJuego, batchSprites); // Pasa la cámara y el batch al LevelManager
        manejadorNivel.cargarNivel("maps/Zona1N1.tmx"); // Carga tu mapa aquí

        // --- CÓDIGO MODIFICADO AQUÍ ---
        sonic = new Sonic(manejadorNivel); // Pasa el LevelManager al constructor de Sonic
        // --- FIN CÓDIGO MODIFICADO ---

        // Configurar y reproducir música
        soundManager.loadMusic("SoundsBackground/Dating Fight.mp3"); // Asegúrate de que el AssetManager la cargue
        assetManager.finishLoadingAsset("SoundsBackground/Dating Fight.mp3"); // Cargar de inmediato
        currentBackgroundMusic = assetManager.get("SoundsBackground/Dating Fight.mp3", Music.class);
        if (currentBackgroundMusic != null) {
            soundManager.playBackgroundMusic("SoundsBackground/Dating Fight.mp3", 0.5f, true);
        } else {
            Gdx.app.log("PantallaDeJuego", "No se pudo cargar la música Dating Fight.mp3");
        }
    }

    @Override
    public void render(float delta) {
        // Limpia la pantalla
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Actualización de todos los elementos del juego
        if (sonic != null) {
            sonic.update(delta); // Actualiza la lógica de Sonic (movimiento, etc.)

            // --- CÓDIGO MODIFICADO AQUÍ ---
            // Centrar la cámara en la posición de Sonic (centrada en el medio del sprite).
            camaraJuego.position.x = sonic.getPositionX() + sonic.getTileSize() / 2;
            camaraJuego.position.y = sonic.getPositionY() + sonic.getTileSize() / 2;

            // Limita la cámara a los bordes del mapa.
            manejadorNivel.limitarCamaraAMapa(camaraJuego);
            // --- FIN CÓDIGO MODIFICADO ---
        }

        camaraJuego.update(); // Siempre actualiza la cámara después de modificar su posición

        // Dibuja el nivel primero
        manejadorNivel.actualizar(delta);
        manejadorNivel.dibujar();

        // Dibuja tus sprites (personaje, enemigos, etc.)
        batchSprites.setProjectionMatrix(camaraJuego.combined); // Usa la matriz de proyección de la cámara
        batchSprites.begin();
        if (sonic != null) {
            sonic.draw(batchSprites); // Sonic ahora solo dibuja, no inicia/finaliza el batch
        }
        batchSprites.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        // La cámara ya no necesita centrarse aquí, lo hace en render() siguiendo a Sonic.
    }

    @Override
    public void pause() {
        soundManager.pauseBackgroundMusic();
    }

    @Override
    public void resume() {
        soundManager.resumeBackgroundMusic();
    }

    @Override
    public void hide() {
        // Nada específico que hacer aquí por ahora
    }

    @Override
    public void dispose() {
        // Libera los recursos que son propiedad de esta pantalla
        // Los recursos compartidos (batchSprites, assetManager, soundManager)
        // se liberan en la clase Main.
        if (manejadorNivel != null) {
            manejadorNivel.dispose();
        }
        if (sonic != null) {
            sonic.dispose(); // Llama al dispose de Sonic (que libera su spriteSheet)
        }
        // No liberar batchSprites, assetManager, soundManager aquí
    }
}
