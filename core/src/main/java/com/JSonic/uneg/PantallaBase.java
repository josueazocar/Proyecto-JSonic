package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;

public abstract class PantallaBase implements Screen {
    protected Stage mainStage;
    protected Stage uiStage;

    public PantallaBase(){
        mainStage = new Stage();
        uiStage = new Stage();

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
    public void resize(int i, int i1) {

    };

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }

    @Override
    public void show() {

    }

    @Override
    public void hide() {

    }
}
