package com.JSonic.uneg.ObjetosDelEntorno;

import com.JSonic.uneg.JSonicJuego;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class VictoriaInterfaz extends Table {

    private final JSonicJuego juegoApp;
    private Label tituloLabel;
    private Label subtituloLabel;

    private BitmapFont fuenteSubtitulo;

    public VictoriaInterfaz(JSonicJuego juegoApp, Skin skin) {
        super(skin);
        this.juegoApp = juegoApp;
        setupUI(skin);
    }

    private void setupUI(Skin skin) {
        this.setVisible(false);
        this.setFillParent(true);
        this.center();

        Label.LabelStyle estiloTitulo = new Label.LabelStyle(skin.getFont("body-font"), Color.GREEN); // Color cambiado a Amarillo para más impacto
        estiloTitulo.font.getData().setScale(2.5f); // Un poco más grande
        tituloLabel = new Label("VICTORIA", estiloTitulo);


        fuenteSubtitulo = new BitmapFont();
        fuenteSubtitulo.getData().setScale(1.2f);
        Label.LabelStyle estiloSubtitulo = new Label.LabelStyle(fuenteSubtitulo, Color.WHITE);
        subtituloLabel = new Label("Espera para ver las estadisticas...", estiloSubtitulo);

        this.add(tituloLabel).padBottom(15).row();
        this.add(subtituloLabel);
    }


    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            iniciarAnimacionDeEntrada();
        }
    }


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


    public void dispose() {
        if (fuenteSubtitulo != null) {
            fuenteSubtitulo.dispose();
        }
    }
}
