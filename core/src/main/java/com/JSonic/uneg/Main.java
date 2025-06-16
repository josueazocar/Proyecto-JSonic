package com.JSonic.uneg;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Main extends Game {

    // Main solo gestiona los recursos que podrían compartirse entre pantallas.

    JSonicJuego sonicJuego;

    @Override
    public void create() {

        sonicJuego = new JSonicJuego();
        sonicJuego.create();
    }

    // render() simplemente delega a la pantalla activa. Perfecto.
    @Override
    public void render() {
        super.render();
        if (sonicJuego != null) {
            sonicJuego.render();
        }
    }

    @Override
    public void pause() {
        if(sonicJuego != null) {
            sonicJuego.pause();
        }
    }

    @Override
    public void resume() {
        if(sonicJuego != null) {
            sonicJuego.resume();
        }
    }

    @Override
    public void resize(int width, int height) {
        if(sonicJuego != null) {
            sonicJuego.resize(width, height);
        }
    }

    // dispose() libera los recursos compartidos cuando el juego se cierra.
    @Override
    public void dispose() {
        super.dispose();

        if(sonicJuego != null) {
            sonicJuego.dispose();
        }
    }
}
