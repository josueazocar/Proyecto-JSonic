package com.JSonic.uneg;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class JSonicJuego extends JuegoBase {

    public AssetManager assetManager;
    public SoundManager soundManager;
    public SpriteBatch batch;

    public JSonicJuego() {
        super();
    }

    @Override
    public void create() {

        batch = new SpriteBatch();
        assetManager = new AssetManager();
        soundManager = new SoundManager(assetManager);

        // Inicializar la pantalla de menú al iniciar el juego
        setPantallaActiva(new PantallaMenu(this));
    }

    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
        assetManager.dispose();
        soundManager.dispose();
    }
}
