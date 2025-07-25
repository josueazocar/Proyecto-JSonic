package com.JSonic.uneg;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Clase principal del juego que extiende Game de libGDX.
 * Se encarga de inicializar y delegar el ciclo de vida al juego JSonicJuego,
 * así como de gestionar recursos compartidos entre pantallas.
 */
public class Main extends Game {

    JSonicJuego sonicJuego; // Instancia del juego JSonicJuego que maneja la lógica del juego.

    /**
     * Crea la instancia de JSonicJuego y llama a su método create().
     */
    @Override
    public void create() {
        sonicJuego = new JSonicJuego();
        sonicJuego.create();
    }

    /**
     * Renderiza la pantalla activa y actualiza el juego.
     */
    @Override
    public void render() {
        super.render();
        if (sonicJuego != null) {
            sonicJuego.render();
        }
    }

    /**
     * Pausa el juego delegando al método pause de JSonicJuego.
     */
    @Override
    public void pause() {
        if(sonicJuego != null) {
            sonicJuego.pause();
        }
    }

    /**
     * Reanuda el juego delegando al método resume de JSonicJuego.
     */
    @Override
    public void resume() {
        if(sonicJuego != null) {
            sonicJuego.resume();
        }
    }

    /**
     * Ajusta el tamaño de la ventana y notifica a JSonicJuego.
     *
     * @param width  nuevo ancho de la ventana.
     * @param height nuevo alto de la ventana.
     */
    @Override
    public void resize(int width, int height) {
        if(sonicJuego != null) {
            sonicJuego.resize(width, height);
        }
    }

    /**
     * Libera los recursos compartidos y finaliza JSonicJuego.
     */
    @Override
    public void dispose() {
        super.dispose();

        if(sonicJuego != null) {
            sonicJuego.dispose();
        }
    }
}
