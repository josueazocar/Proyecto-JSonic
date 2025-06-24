package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class PantallaAyuda extends PantallaBase{
    private final JSonicJuego juegoApp;
    private Texture texturaBotonVolver;
    public PantallaAyuda(JSonicJuego juegoApp) {
        super();
        this.juegoApp = juegoApp;
    }

    @Override
    public void inicializar() {
        // Aquí puedes inicializar los elementos de la pantalla de ayuda, como botones, fondos, etc.
        System.out.println("Inicializando pantalla de ayuda");

        Texture fondoTex = new Texture(Gdx.files.internal("Fondos/Sonic-Tails-Knuckles.png"));
        Image fondo = new Image(fondoTex);
        fondo.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        mainStage.addActor(fondo);

        // --- Botón de Volver ---
        texturaBotonVolver = new Texture(Gdx.files.internal("Botones/boton_atras.png"));
        Button.ButtonStyle estiloBotonVolver = new Button.ButtonStyle();
        estiloBotonVolver.up = new TextureRegionDrawable(new TextureRegion(texturaBotonVolver));
        Button botonVolver = new Button(estiloBotonVolver);

        botonVolver.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Volvemos al menú principal, mostrando los botones directamente
                juegoApp.setPantallaActiva(new PantallaMenu(juegoApp, true));
            }
        });

        // --- Posicionamiento del botón Volver (en una tabla nueva) ---
        Table tablaVolver = new Table();
        tablaVolver.setFillParent(true);
        uiStage.addActor(tablaVolver); // Se añade al stage de la UI
        tablaVolver.top().left(); // Se alinea arriba a la izquierda
        tablaVolver.add(botonVolver).size(125, 125).pad(20);
    }

    @Override
    public void actualizar(float delta) {
        // Aquí puedes manejar la lógica de actualización específica de la pantalla de ayuda
        // Por ejemplo, detectar eventos de entrada o animaciones
    }

    @Override
    public void dispose() {
        super.dispose();
        if (texturaBotonVolver != null) texturaBotonVolver.dispose();
    }
}
