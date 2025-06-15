package com.JSonic.uneg;

import com.badlogic.gdx.Game;
import com.JSonic.uneg.PantallaDeJuego; // <-- IMPORTA TU CLASE PANTALLADEJUEGO
// Asegúrate de que la ruta sea correcta según donde la creaste.
// Si la creaste en un paquete 'screens', sería:
// import com.JSonic.uneg.screens.PantallaDeJuego;


/** {@link com.badlogic.gdx.Game} implementation shared by all platforms. */

public class Main extends Game {

    @Override
    public void create() {
        // En el método create simplemente establecemos la pantalla inicial de nuestro juego.
        setScreen(new PantallaDeJuego());
    }

    // Comente esto proque ya La clase Game los maneja y los delega a PantallaDeJuego.
    // @Override
    // public void resize(int width, int height) {
    //     if(width <= 0 || height <= 0) return;
    // }

    // @Override
    // public void render() {
    // }

    // @Override
    // public void pause() {
    // }

    // @Override
    // public void resume() {
    // }

    // @Override
    // public void dispose() {
    // }
}
