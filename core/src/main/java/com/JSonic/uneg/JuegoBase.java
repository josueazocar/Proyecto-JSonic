package com.JSonic.uneg;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

public abstract class JuegoBase extends Game{
    private static JuegoBase juego;

    public JuegoBase(){
        juego = this;
    }

    public static void setPantallaActiva(PantallaBase s){
        juego.setScreen(s);
    }
}
