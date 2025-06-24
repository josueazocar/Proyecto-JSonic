package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

/**
 * Pantalla que muestra la información "Acerca De" del juego.
 * Muestra detalles sobre el desarrollo, las herramientas y la versión.
 */
public class PantallaAcercaDe extends PantallaBase {

    private final JSonicJuego juegoApp;
    private Texture texturaFondo, texturaBotonVolver;
    private BitmapFont font;

    public PantallaAcercaDe(JSonicJuego juegoApp) {
        super();
        this.juegoApp = juegoApp;
    }

    @Override
    public void inicializar() {
        // --- Fondo ---
        texturaFondo = new Texture(Gdx.files.internal("Fondos/Sonic-Tails-Knuckles.png"));
        Image imagenFondo = new Image(texturaFondo);
        imagenFondo.setSize(mainStage.getWidth(), mainStage.getHeight());
        mainStage.addActor(imagenFondo);

        // --- Texto de "Acerca de" ---
        font = new BitmapFont();
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        String textoInfo = "JSonicJuego\n\n" +
                           "Lenguaje de Programacion: Java\n" +
                           "Librerias Externas: LibGDX\n\n" +
                           "Desarrolladores:\n" +
                           "Victor M., Jesus L., Reinaldo M.\n\n" +
                           "Version: 1.0";
        Label infoLabel = new Label(textoInfo, labelStyle);
        infoLabel.setWrap(true);
        infoLabel.setAlignment(Align.center);

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

        // --- Posicionamiento ---
        uiTable.center();
        uiTable.add(infoLabel).width(mainStage.getWidth() * 0.8f).padBottom(40);

        // --- Posicionamiento del botón Volver (en una tabla nueva) ---
        Table tablaVolver = new Table();
        tablaVolver.setFillParent(true);
        uiStage.addActor(tablaVolver); // Se añade al stage de la UI
        tablaVolver.top().left(); // Se alinea arriba a la izquierda
        tablaVolver.add(botonVolver).size(125, 125).pad(20);
    }

    @Override
    public void actualizar(float delta) {
        // No se necesita lógica aquí
    }

    @Override
    public void dispose() {
        super.dispose();
        if (texturaFondo != null) texturaFondo.dispose();
        if (texturaBotonVolver != null) texturaBotonVolver.dispose();
        if (font != null) font.dispose();
    }
}
