package com.JSonic.uneg;

import com.JSonic.uneg.Pantallas.PantallaBase;
import com.badlogic.gdx.Game;


public abstract class JuegoBase extends Game{
    private static JuegoBase juego;

    public JuegoBase(){
        juego = this;
    }

    public static void setPantallaActiva(PantallaBase s){
        juego.setScreen(s);
    }
}
