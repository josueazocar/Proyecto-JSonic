package com.JSonic.uneg.Pantallas;

import com.JSonic.uneg.JSonicJuego;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
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
    private TextureAtlas botonesMenuAtlas;
    private Texture texturaFondo;

    public PantallaAcercaDe(JSonicJuego juegoApp) {
        super();
        this.juegoApp = juegoApp;
    }

    @Override
    public void inicializar() {
        // --- Cargar Atlas ---
        botonesMenuAtlas = new TextureAtlas(Gdx.files.internal("Atlas/botonesMenu.atlas"));

        // --- Fondo ---
        texturaFondo = new Texture(Gdx.files.internal("Fondos/Portada_desenfoque.png"));
        Image imagenFondoPrincipal = new Image(texturaFondo);
        imagenFondoPrincipal.setSize(mainStage.getWidth(), mainStage.getHeight());
        mainStage.addActor(imagenFondoPrincipal);

        // --- Panel de Contenido con Fondo Redondeado (¡LA NUEVA LÓGICA!) ---
        Table panelContenido = new Table();
        panelContenido.setBackground(getSkin().getDrawable("default-round"));
        float anchoDelPanel = 950; // Ancho en píxeles
        float altoDelPanel = 500;  // Alto en píxeles
        panelContenido.setSize(anchoDelPanel, altoDelPanel);
        panelContenido.setPosition(mainStage.getWidth() / 2, mainStage.getHeight() / 2, Align.center);
        mainStage.addActor(panelContenido);

        // --- Contenido de "Acerca de" ---
        Label titleLabel = new Label("ACERCA DE", getSkin(), "title");
        titleLabel.setPosition(mainStage.getWidth() / 2, mainStage.getHeight() - 200, Align.center);

        Label devLabel = new Label("Desarrollado en Java con libgdx", getSkin(), "credits");
        devLabel.setPosition(mainStage.getWidth() / 2 + 65, mainStage.getHeight() / 2 + 100, Align.center);
        devLabel.setFontScale(0.85f);

        Label creadoresLabel = new Label("Creado por: Josmer Azocar\n Josue Azocar\n Franner Bermudez\n Santiago Roman\n Gloria Saimans", getSkin(), "body");
        creadoresLabel.setWrap(true);
        creadoresLabel.setAlignment(Align.center);
        creadoresLabel.setWidth(mainStage.getWidth() * 1f);
        creadoresLabel.setPosition(mainStage.getWidth() / 2, mainStage.getHeight() / 2 - 20, Align.center);

        Label versionLabel = new Label("Version: 1.0", getSkin(), "credits");
        versionLabel.setPosition(mainStage.getWidth() / 2, mainStage.getHeight() / 2 - 140, Align.center);

        mainStage.addActor(titleLabel);
        mainStage.addActor(devLabel);
        mainStage.addActor(creadoresLabel);
        mainStage.addActor(versionLabel);

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
        if (botonesMenuAtlas != null) botonesMenuAtlas.dispose();
        if (texturaFondo != null) texturaFondo.dispose();
    }
}
