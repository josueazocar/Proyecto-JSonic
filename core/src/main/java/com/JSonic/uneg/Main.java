package com.JSonic.uneg;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.assets.AssetManager; // Necesario para AssetManager
import com.badlogic.gdx.audio.Music; // Necesario para la música

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main implements ApplicationListener {

    private Sonic sonic;
    private SpriteBatch batch;
    private AssetManager assetManager; // Para gestionar assets
    private SoundManager soundManager; // Instancia de nuestro SoundManager

    private static final String BACKGROUND_MUSIC_PATH = "SoundsBackground/Green Hill Zone Theme Sonic (8 Bit Version).mp3";

    @Override
    public void create() {
        batch = new SpriteBatch();
        sonic = new Sonic(); // Sonic carga su textura internamente con Gdx.files.internal()

        // 1. Inicializar AssetManager PRIMERO
        assetManager = new AssetManager(); // ¡AQUÍ ES DONDE SE INICIALIZA!

        // 2. Inicializar SoundManager, pasándole el AssetManager que ya está inicializado
        soundManager = new SoundManager(assetManager);

        // 3. Cargar la música con el SoundManager usando el AssetManager
        // Esto solo la pone en la cola de carga
        soundManager.loadMusic(BACKGROUND_MUSIC_PATH);

        // 4. IMPORTANTE: Esperar a que el AssetManager termine de cargar los assets.
        // En un juego real, esto se haría en una pantalla de carga.
        // Aquí es crucial porque la música no estará disponible hasta que se cargue.
        assetManager.finishLoading(); // Espera a que todos los assets en cola se carguen

        // 5. Reproducir la música de fondo una vez que todo esté cargado
        soundManager.playBackgroundMusic(BACKGROUND_MUSIC_PATH, 0.5f, true); // Volumen al 50%, en bucle
    }

    @Override
    public void resize(int width, int height) {
        if(width <= 0 || height <= 0) return;
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.7f, 0.7f, 0.7f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        sonic.update(Gdx.graphics.getDeltaTime());
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
