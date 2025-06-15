package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.assets.AssetManager; // Necesario para AssetManager
import com.badlogic.gdx.audio.Music; // Necesario para la música

public class PantallaJuego extends PantallaBase {

    private Sonic sonic;
    private SpriteBatch batch;
    private AssetManager assetManager; // Para gestionar assets
    private SoundManager soundManager; // Instancia de nuestro SoundManager

    private static final String BACKGROUND_MUSIC_PATH1 = "SoundsBackground/Green Hill Zone Theme Sonic (8 Bit Version).mp3";
    private static final String BACKGROUND_MUSIC_PATH2 = "SoundsBackground/Dating Fight.mp3";
    private static final String BACKGROUND_MUSIC_PATH3 = "SoundsBackground/Heartache.mp3";


    @Override
    public void inicializar() {
        batch = new SpriteBatch();
        sonic = new Sonic(); // Sonic carga su textura internamente con Gdx.files.internal()

        // 1. Inicializar AssetManager PRIMERO
        assetManager = new AssetManager(); // ¡AQUÍ ES DONDE SE INICIALIZA!

        // 2. Inicializar SoundManager, pasándole el AssetManager que ya está inicializado
        soundManager = new SoundManager(assetManager);

        // 3. Cargar la música con el SoundManager usando el AssetManager
        // Esto solo la pone en la cola de carga
        soundManager.loadMusic(BACKGROUND_MUSIC_PATH2);

        // 4. IMPORTANTE: Esperar a que el AssetManager termine de cargar los assets.
        // En un juego real, esto se haría en una pantalla de carga.
        // Aquí es crucial porque la música no estará disponible hasta que se cargue.
        assetManager.finishLoading(); // Espera a que todos los assets en cola se carguen

        // 5. Reproducir la música de fondo una vez que todo esté cargado
        soundManager.playBackgroundMusic(BACKGROUND_MUSIC_PATH2, 0.5f, true); // Volumen al 50%, en bucle

    }

    @Override
    public void actualizar(float deltat) {
        // Aquí puedes actualizar la lógica del juego, como el movimiento de personajes, colisiones, etc.
        System.out.println("Actualizando lógica del juego");
        sonic.update(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void render(float deltat) {
        super.render(deltat);
        Gdx.gl.glClearColor(0.7f, 0.7f, 0.7f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        sonic.draw(batch);
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

    @Override
    public void dispose() {
        super.dispose();

        if (batch != null) {
            batch.dispose();
        }
        if (assetManager != null) {
            assetManager.dispose(); // Esto liberará la música y cualquier otro asset que hayas cargado con él.
        }
        if (soundManager != null) {
            // No es estrictamente necesario si assetManager.dispose() ya se llamó,
            // pero si SoundManager tiene más lógica de dispose, se mantiene.
            soundManager.dispose();
        }
    }
}
