package com.JSonic.uneg.ObjetosDelEntorno;

import com.JSonic.uneg.JSonicJuego;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

/**
 * Clase que representa la interfaz de victoria del juego.
 * Muestra un mensaje de victoria y un subtítulo con instrucciones.
 */
public class VictoriaInterfaz extends Table {

    private final JSonicJuego juegoApp;
    private Label tituloLabel;
    private Label subtituloLabel;

    private BitmapFont fuenteSubtitulo;

    /**
     * Construye la interfaz de victoria e inicializa los componentes.
     * @param juegoApp Instancia principal del juego.
     * @param skin Skin de UI para estilos.
     */
    public VictoriaInterfaz(JSonicJuego juegoApp, Skin skin) {
        super(skin);
        this.juegoApp = juegoApp;
        setupUI(skin);
    }

    /**
     * Configura los elementos de la UI: visibilidad, layout y estilos de labels.
     * @param skin Skin de UI para estilos.
     */
    private void setupUI(Skin skin) {
        this.setVisible(false);
        this.setFillParent(true);
        this.center();

        Label.LabelStyle estiloTitulo = new Label.LabelStyle(skin.getFont("body-font"), Color.GREEN); // Color cambiado a Amarillo para más impacto
        estiloTitulo.font.getData().setScale(2.5f);
        tituloLabel = new Label("VICTORIA", estiloTitulo);


        fuenteSubtitulo = new BitmapFont();
        fuenteSubtitulo.getData().setScale(1.2f);
        Label.LabelStyle estiloSubtitulo = new Label.LabelStyle(fuenteSubtitulo, Color.WHITE);
        subtituloLabel = new Label("Espera para ver las estadisticas...", estiloSubtitulo);

        this.add(tituloLabel).padBottom(15).row();
        this.add(subtituloLabel);
    }


    /**
     * Muestra u oculta la interfaz y lanza la animación de entrada si se hace visible.
     * @param visible true para mostrar, false para ocultar.
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            iniciarAnimacionDeEntrada();
        }
    }


    /**
     * Inicia la animación de aparición para los labels de título y subtítulo.
     */
    public void iniciarAnimacionDeEntrada() {
        tituloLabel.clearActions();
        tituloLabel.getColor().a = 0;
        tituloLabel.setFontScale(2.0f);

        subtituloLabel.getColor().a = 0;

        tituloLabel.addAction(
            Actions.sequence(
                Actions.parallel(
                    Actions.fadeIn(0.7f, Interpolation.pow2Out),
                    Actions.scaleTo(2.5f, 2.5f, 0.7f, Interpolation.pow2Out)
                ),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        subtituloLabel.addAction(Actions.fadeIn(0.5f));
                    }
                })
            )
        );
    }


    /**
     * Libera los recursos asociados a la fuente de subtítulo.
     */
    public void dispose() {
        if (fuenteSubtitulo != null) {
            fuenteSubtitulo.dispose();
        }
    }
}
