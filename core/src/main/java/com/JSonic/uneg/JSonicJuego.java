package com.JSonic.uneg;

public class JSonicJuego extends JuegoBase{

    public JSonicJuego() {
        super();
    }

    @Override
    public void create() {
        // Inicializar la pantalla de menú al iniciar el juego
        setPantallaActiva(new PantallaMenu());
    }

}
