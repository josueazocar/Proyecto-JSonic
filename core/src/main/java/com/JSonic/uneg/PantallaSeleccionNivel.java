package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class PantallaSeleccionNivel extends PantallaBase {

    private final JSonicJuego juegoApp;
    private final Screen pantallaAnterior;
    private Texture backgroundTexture;

    public PantallaSeleccionNivel(JSonicJuego juegoApp, Screen pantallaAnterior) {
        super();
        this.juegoApp = juegoApp;
        this.pantallaAnterior = pantallaAnterior;
        inicializar();
    }

    @Override
    public void inicializar() {
        mainStage = new Stage(new ScreenViewport());

        backgroundTexture = new Texture(Gdx.files.internal("Fondos/Portada_desenfoque.png"));
        Image backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        mainStage.addActor(backgroundImage);

        Table tabla = new Table();
        tabla.setFillParent(true);
        mainStage.addActor(tabla);

        Label titleLabel = new Label("Selecciona un Nivel", getSkin());
        titleLabel.setFontScale(2);

        TextButton nivel1Button = new TextButton("Zona 1 - Nivel 1", getSkin());
        TextButton nivel2Button = new TextButton("Zona 1 - Nivel 2", getSkin());
        TextButton nivel3Button = new TextButton("Zona 1 - Nivel 3", getSkin());
        TextButton jefe1Button = new TextButton("Jefe 1", getSkin());
        TextButton jugarButton = new TextButton("Jugar", getSkin());

        nivel1Button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              //  juegoApp.nivelSeleccionado = "maps/Zona1N1.tmx";
            }
        });

        nivel2Button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                //juegoApp.nivelSeleccionado = "maps/Zona1N2.tmx";
            }
        });

        nivel3Button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                //juegoApp.nivelSeleccionado = "maps/Zona1N3.tmx";
            }
        });

        jefe1Button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                //juegoApp.nivelSeleccionado = "maps/ZonaJefeN1.tmx";
            }
        });

        jugarButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                //juegoApp.iniciarJuegoLocal();
            }
        });

        tabla.add(titleLabel).colspan(2).padBottom(40).row();
        tabla.add(nivel1Button).width(300).height(50).pad(10);
        tabla.add(nivel2Button).width(300).height(50).pad(10).row();
        tabla.add(nivel3Button).width(300).height(50).pad(10);
        tabla.add(jefe1Button).width(300).height(50).pad(10).row();
        tabla.add(jugarButton).colspan(2).padTop(40).width(200).height(50);
    }

    @Override
    public void actualizar(float delta) {
        mainStage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        actualizar(delta);
        mainStage.draw();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(mainStage);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        mainStage.dispose();
        backgroundTexture.dispose();
    }
}

