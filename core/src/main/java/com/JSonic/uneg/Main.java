package com.JSonic.uneg;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Main extends Game {

    // Main solo gestiona los recursos que podrían compartirse entre pantallas.
    public AssetManager assetManager;
    public SoundManager soundManager;
    public SpriteBatch batch;

    @Override
    public void create() {
        // Inicializa los recursos una sola vez al inicio del juego.
        batch = new SpriteBatch();
        assetManager = new AssetManager();
        soundManager = new SoundManager(assetManager);

        // Establece la pantalla inicial y le pasa los recursos que necesita.
        // Ya no le pasamos jugadores ni el cliente de red.
        this.setScreen(new PantallaDeJuego(this));
    }

    // render() simplemente delega a la pantalla activa. Perfecto.
    @Override
    public void render() {
        super.render();
    }

    // dispose() libera los recursos compartidos cuando el juego se cierra.
    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
        assetManager.dispose();
        soundManager.dispose();
    }
}
