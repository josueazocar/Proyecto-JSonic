package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;


public class PantallaMenu extends PantallaBase{

    private Texture texturaBotonJugar;
    private Texture texturaFondo;


    @Override
    public void inicializar() {
        // Aquí puedes inicializar los elementos de la pantalla de menú, como botones, fondos, etc.
        System.out.println("Inicializando pantalla de menú");

        //Logica Imagen de Fondo
        texturaFondo = new Texture(Gdx.files.internal("assets/Fondos/Sonic-Tails-Knuckles.png"));

        Image imagenFondo = new Image(texturaFondo);
        imagenFondo.setSize(mainStage.getWidth(), mainStage.getHeight());
        imagenFondo.setPosition(0, 0);
        mainStage.addActor(imagenFondo);

        //Logica del Boton para Jugar
        ButtonStyle botonEstilo = new ButtonStyle();
        texturaBotonJugar = new Texture(Gdx.files.internal("assets/Botones/boton_jugar.png"));

        TextureRegion botonRegion = new TextureRegion(texturaBotonJugar);
        botonEstilo.up = new TextureRegionDrawable(botonRegion);

        Button botonJugar = new Button(botonEstilo);

        //Funcionalidad al Boton
        botonJugar.addListener(new EventListener() {
            public boolean handle(Event event) {
                if (event instanceof InputEvent) {
                    InputEvent inputEvent = (InputEvent) event;
                    if (inputEvent.getType().equals(InputEvent.Type.touchDown)) {
                        System.out.println("Botón Jugar presionado");
                        // Aquí puedes cambiar a la pantalla del juego
                        JuegoBase.setPantallaActiva(new PantallaJuego());
                        return true; // Indica que el evento fue manejado
                    }
                }
                return false; // Indica que el evento no fue manejado
            }
        });

        this.uiTable.center();
        // Añadir el botón a la tabla. Usará su tamaño preferido derivado de la imagen.
        this.uiTable.add(botonJugar).size(200f, 110f).padTop(350f);
    }

    public void actualizar(float deltat) {
        // Aquí puedes manejar la lógica de actualización de la pantalla de menú, como la navegación entre opciones.
    }


    @Override
    public void dispose() {
        System.out.println("PantallaMenu: dispose() llamado.");
        super.dispose();
        if (texturaBotonJugar != null) {
            texturaBotonJugar.dispose();
        }
    }
}
