package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class PantallaAyuda extends PantallaBase {

    private final JSonicJuego juegoApp;
    private Texture texturaFondo;
    private TextureAtlas botonesMenuAtlas;

    public PantallaAyuda(JSonicJuego juegoApp) {
        super();
        this.juegoApp = juegoApp;
    }

    @Override
    public void inicializar() {
        // Cargar Atlas
        botonesMenuAtlas = new TextureAtlas(Gdx.files.internal("Atlas/botonesMenu.atlas"));

        // --- Fondo ---
        texturaFondo = new Texture(Gdx.files.internal("Fondos/Portada_desenfoque.png"));
        Image fondo = new Image(texturaFondo);
        fondo.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        mainStage.addActor(fondo);



        // --- Botón de Volver ---
        Button.ButtonStyle estiloBotonVolver = new Button.ButtonStyle();
        estiloBotonVolver.up = new TextureRegionDrawable(botonesMenuAtlas.findRegion("boton_atras"));
        Button botonVolver = new Button(estiloBotonVolver);

        botonVolver.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                PantallaMenu pantallaMenu = new PantallaMenu(juegoApp, true);
                pantallaMenu.setEstadoMenu(PantallaMenu.EstadoMenu.OPCIONES);
                juegoApp.setPantallaActiva(pantallaMenu);
            }
        });

        // --- Posicionamiento del botón Volver (en una tabla nueva) ---
        Table tablaVolver = new Table();
        tablaVolver.setFillParent(true);
        uiStage.addActor(tablaVolver);
        tablaVolver.top().left();
        tablaVolver.add(botonVolver).size(125, 125).pad(20);
    }

    @Override
    public void actualizar(float delta) {

    }

    @Override
    public void dispose() {
        super.dispose();
        if (texturaFondo != null) texturaFondo.dispose();
        if (botonesMenuAtlas != null) botonesMenuAtlas.dispose();
    }
}
