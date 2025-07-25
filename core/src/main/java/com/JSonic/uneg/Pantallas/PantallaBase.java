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

/**
 * Clase base para las pantallas del juego, implementa la interfaz Screen de LibGDX.
 * Proporciona un escenario principal y un escenario de interfaz de usuario, junto con un viewport y una skin.
 */
public abstract class PantallaBase implements Screen {
    protected Stage mainStage;
    protected Stage uiStage;
    protected Table uiTable;
    protected Skin skin;
    protected Viewport viewport;
    protected TextureAtlas atlas;

    /**
     * Constructor por defecto que inicializa el viewport, los escenarios y la skin.
     */
    public PantallaBase() {
        viewport = new FitViewport(1280, 720, new OrthographicCamera());
        mainStage = new Stage(viewport);
        uiStage = new Stage(viewport);

        uiTable = new Table();
        uiTable.setFillParent(true);
        uiStage.addActor(uiTable);
        skin = new Skin(Gdx.files.internal("Skin/ui.json"));
    }

    /**
     * Constructor que permite pasar argumentos adicionales, aunque no se utilicen en este caso.
     *
     * @param args Argumentos adicionales (no utilizados).
     */
    public PantallaBase(String args) {
        viewport = new FitViewport(1280, 720, new OrthographicCamera());
        mainStage = new Stage(viewport);
        uiStage = new Stage(viewport);

        uiTable = new Table();
        uiTable.setFillParent(true);
        uiStage.addActor(uiTable);
        skin = new Skin(Gdx.files.internal("Skin/ui.json"));
    }

    /**
     * Obtiene el escenario principal del juego.
     *
     * @return El escenario principal.
     */
    public Skin getSkin() {
        return skin;
    }

    /**
     * Obtiene el escenario de interfaz de usuario.
     *
     * @return El escenario de interfaz de usuario.
     */
    public Viewport getViewport() {
        return viewport;
    }

    /**
     * Método abstracto que debe ser implementado por las subclases para inicializar la pantalla.
     * Se llama una vez al inicio de la pantalla.
     */
    public abstract void inicializar();

    /**
     * Método abstracto que debe ser implementado por las subclases para actualizar la lógica de la pantalla.
     * Se llama en cada frame con el tiempo transcurrido desde el último frame.
     *
     * @param deltat Tiempo transcurrido desde el último frame.
     */
    public abstract void actualizar(float deltat);

    /**
     * Método que se encarga de renderizar la pantalla.
     * Actualiza los escenarios y dibuja el contenido en cada frame.
     *
     * @param deltat Tiempo transcurrido desde el último frame.
     */
    public void render(float deltat) {
        uiStage.act(deltat);
        mainStage.act(deltat);

        actualizar(deltat);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mainStage.draw();
        uiStage.draw();
    }

    /**
     * Método que se llama al cambiar el tamaño de la ventana.
     * Actualiza los viewports de los escenarios y el viewport principal.
     *
     * @param width  Nuevo ancho de la ventana.
     * @param height Nuevo alto de la ventana.
     */
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
    }

    ;

    /**
     * Método que se llama al pausar la pantalla.
     * En este caso, no se implementa ninguna acción específica.
     */
    @Override
    public void pause() {

    }

    /**
     * Método que se llama al reanudar la pantalla.
     * En este caso, no se implementa ninguna acción específica.
     */
    @Override
    public void resume() {

    }

    /**
     * Método que se llama al cerrar la pantalla.
     * Libera los recursos utilizados por los escenarios, la skin y el atlas.
     */
    @Override
    public void dispose() {
        if (mainStage != null) mainStage.dispose();
        if (uiStage != null) uiStage.dispose();
        if (skin != null) skin.dispose();
        if (atlas != null) atlas.dispose();
    }

    /**
     * Método que se llama al mostrar la pantalla.
     * Establece el procesador de entrada para el escenario de interfaz de usuario.
     */
    @Override
    public void show() {
        Gdx.input.setInputProcessor(uiStage);
    }

    /**
     * Método que se llama al ocultar la pantalla.
     * Establece el procesador de entrada a null, desactivando la entrada del usuario.
     */
    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }
}
