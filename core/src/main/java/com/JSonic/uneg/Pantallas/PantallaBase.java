package com.JSonic.uneg.Pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public abstract class PantallaBase implements Screen {
    protected Stage mainStage;
    protected Stage uiStage;
    protected Table uiTable;
    protected Skin skin;
    protected Viewport viewport;
    protected TextureAtlas atlas;

    public PantallaBase(){
        viewport = new FitViewport(1280, 720, new OrthographicCamera());
        mainStage = new Stage(viewport);
        uiStage = new Stage(viewport);

        uiTable = new Table();
        uiTable.setFillParent(true);
        uiStage.addActor(uiTable);
        skin = new Skin(Gdx.files.internal("Skin/ui.json"));

        inicializar();
    }

    public abstract void inicializar();

    public abstract void actualizar(float deltat);

    public void render(float deltat){
        uiStage.act(deltat);
        mainStage.act(deltat);

        actualizar(deltat);
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mainStage.draw();
        uiStage.draw();
    }

    public Skin getSkin() {
        return skin;
    }

    public Viewport getViewport() {
        return viewport;
    }

    //Metodos requeridos por la interface Screen

    @Override
    public void resize(int width, int height) {
        if (mainStage != null) {
            mainStage.getViewport().update(width, height, true); // 'true' para centrar la cámara
        }
        if (uiStage != null) {
            uiStage.getViewport().update(width, height, true);   // 'true' para centrar la cámara
        }
        if (viewport != null) {
            viewport.update(width, height, true);
        }
    };

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        if (mainStage != null) mainStage.dispose();
        if (uiStage != null) uiStage.dispose();
        if (skin != null) skin.dispose();
        if (atlas != null) atlas.dispose();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(uiStage);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }
}
