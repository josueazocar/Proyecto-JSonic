package com.JSonic.uneg;

import com.badlogic.gdx.ApplicationListener;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main implements ApplicationListener {

    JSonicJuego sonicJuego;

    @Override
    public void create() {
        sonicJuego = new JSonicJuego();
        sonicJuego.create();
    }

    @Override
    public void resize(int width, int height) {
        if(width <= 0 || height <= 0) return;

        if(sonicJuego != null) {
            sonicJuego.resize(width, height);
        }
    }

    @Override
    public void render() {
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
    public void dispose() {
        if(sonicJuego != null) {
            sonicJuego.dispose();
        }
    }
}
