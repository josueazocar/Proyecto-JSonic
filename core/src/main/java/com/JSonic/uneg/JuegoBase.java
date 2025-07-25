package com.JSonic.uneg;

import com.JSonic.uneg.Pantallas.PantallaBase;
import com.badlogic.gdx.Game;

/**
 * Clase base para el juego, extiende Game de libGDX y mantiene una referencia
 * estática a la instancia activa para el manejo de pantallas.
 */
public abstract class JuegoBase extends Game {
    private static JuegoBase juego;

    public JuegoBase() {
        juego = this;
    }

    /**
     * Establece la pantalla activa en el juego.
     * @param s instancia de PantallaBase que se mostrará.
     */
    public static void setPantallaActiva(PantallaBase s) {
        juego.setScreen(s);
    }
}
