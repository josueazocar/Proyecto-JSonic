package com.JSonic.uneg;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.JSonic.uneg.PantallaDeJuego; // Asegúrate de que esta ruta sea correcta
import com.JSonic.uneg.SoundManager; // Asegúrate de que esta ruta sea correcta
// Si usas Gdx.gl.glClearColor, Gdx.graphics.getDeltaTime, etc. en Main, necesitas Gdx.
// Sin embargo, si esa lógica se mueve a PantallaDeJuego, no necesitarías Gdx directamente en Main.
// Por ahora, no lo incluyo si la idea es delegar casi todo a PantallaDeJuego.



/** {@link com.badlogic.gdx.Game} implementation shared by all platforms. */
public class Main extends Game { // ¡Esta es la base que queremos!

    // Declaración de variables globales que Main gestionará y pasará a las pantallas
    private AssetManager assetManager;
    private SoundManager soundManager;
    private SpriteBatch batch;

    // Rutas de música (pueden quedarse aquí o ir al SoundManager si lo prefieres)
    private static final String BACKGROUND_MUSIC_PATH1 = "SoundsBackground/Green Hill Zone Theme Sonic (8 Bit Version).mp3";
    private static final String BACKGROUND_MUSIC_PATH2 = "SoundsBackground/Dating Fight.mp3";
    private static final String BACKGROUND_MUSIC_PATH3 = "SoundsBackground/Heartache.mp3";


    @Override
    public void create() {
        // Inicializa SpriteBatch
        batch = new SpriteBatch();

        // Inicializa AssetManager y SoundManager (lógica de 'develop')
        assetManager = new AssetManager();
        soundManager = new SoundManager(assetManager);

        // Carga la música (lógica de 'develop')
        soundManager.loadMusic(BACKGROUND_MUSIC_PATH2);
        soundManager.loadMusic(BACKGROUND_MUSIC_PATH1);
        soundManager.loadMusic(BACKGROUND_MUSIC_PATH3);

        // Espera a que los assets se carguen (lógica de 'develop')
        assetManager.finishLoading();

        // Reproduce la música de fondo (lógica de 'develop')
        soundManager.playBackgroundMusic(BACKGROUND_MUSIC_PATH2, 0.5f, true);


        // Establece la pantalla inicial y le pasa los recursos globales
        setScreen(new PantallaDeJuego(batch, assetManager, soundManager));
    }
    // Comenta o elimina los métodos resize, render, pause, resume, dispose
    // que se encuentran comentados en tu HEAD, porque la clase Game los maneja
    // y los delega a PantallaDeJuego. Solo mantendremos el dispose general de Main.

    @Override
    public void dispose() {
        super.dispose(); // Llama al dispose de la clase Game (importante)

        // Libera los recursos globales que Main ha inicializado
        if (batch != null) {
            batch.dispose();
        }
        if (assetManager != null) {
            assetManager.dispose(); // Esto libera toda la música y assets cargados
        }
        if (soundManager != null) {
            soundManager.dispose(); // Si SoundManager tiene recursos propios, los libera
        }
    }
}
