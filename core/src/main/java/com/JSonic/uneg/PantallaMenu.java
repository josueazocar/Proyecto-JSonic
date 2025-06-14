package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class PantallaMenu extends PantallaBase{

    public void inicializar() {
        // Aquí puedes inicializar los elementos de la pantalla de menú, como botones, fondos, etc.
        System.out.println("Inicializando pantalla de menú");
        System.out.println("PantallaMenu: inicializar() llamada. Menú listo.");
        System.out.println("Presiona 'S' para simular ir al juego.");
        System.out.println("Presiona 'ESCAPE' para salir.");
    }

    public void actualizar(float deltat) {
        // Aquí puedes manejar la lógica de actualización de la pantalla de menú, como la navegación entre opciones.
       if (Gdx.input.isKeyJustPressed(Input.Keys.S)){
           System.out.println("Tecla S presionada, cambiando a la pantalla de juego");
           //JuegoBase.setPantallaActiva(new PantallaJuego());
      } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
          System.out.println("Tecla ESCAPE presionada, saliendo del juego");
           Gdx.app.exit();
       }
    }

    @Override
    public void show() {
        inicializar();
    }

    @Override
    public void render(float deltat) {
        actualizar(deltat);
    }
}
