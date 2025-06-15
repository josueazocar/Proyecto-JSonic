package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public abstract class PantallaBase implements Screen {
    protected Stage mainStage;
    protected Stage uiStage;
    protected Table uiTable;

    public PantallaBase(){
        mainStage = new Stage();
        uiStage = new Stage();
        uiTable = new Table();
        uiTable.setFillParent(true);
        uiStage.addActor(uiTable);

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

    //Metodos requeridos por la interface Screen

    @Override
    public void resize(int width, int height) {
        if (mainStage != null) {
            mainStage.getViewport().update(width, height, true); // 'true' para centrar la cámara
        }
        if (uiStage != null) {
            uiStage.getViewport().update(width, height, true);   // 'true' para centrar la cámara
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
