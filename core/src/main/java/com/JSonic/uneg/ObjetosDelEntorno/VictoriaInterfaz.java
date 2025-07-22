package com.JSonic.uneg.ObjetosDelEntorno;

import com.JSonic.uneg.JSonicJuego;
import com.JSonic.uneg.Pantallas.PantallaMenu;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import network.LocalServer;
import network.Network;

public class VictoriaInterfaz extends Table {

    private final JSonicJuego juegoApp;
    private Label tituloLabel;
    private Label subtituloLabel;

    public VictoriaInterfaz(JSonicJuego juegoApp, Skin skin) {
        super(skin);
        this.juegoApp = juegoApp;
        setupUI(skin);
    }
    private void setupUI(Skin skin) {
        this.setVisible(false);
        this.setFillParent(true);
        this.center();

        Label.LabelStyle estiloTitulo = new Label.LabelStyle(skin.getFont("body-font"), Color.GREEN);
        estiloTitulo.font.getData().setScale(2.0f);
        tituloLabel = new Label("VICTORIA", estiloTitulo);

        this.add(tituloLabel).padBottom(50).row();

        BitmapFont fuenteSubtitulo = new BitmapFont(); // Creamos una nueva fuente por defecto
        fuenteSubtitulo.getData().setScale(1.2f); // Hacemos la fuente un poco más grande que la normal

        Label.LabelStyle estiloSubtitulo = new Label.LabelStyle(fuenteSubtitulo, Color.WHITE);
        subtituloLabel = new Label("Espera para ver las estadisticas...", estiloSubtitulo);

        // --- Añadimos los elementos a la tabla ---
        this.add(tituloLabel).padBottom(15).row(); // Reducimos el espacio inferior para que el subtítulo quede más cerca
        this.add(subtituloLabel).padBottom(50).row(); // Añadimos el nuevo label
    }



    public void iniciarAnimacionDeEntrada() {
        // Aquí podrías añadir una acción para que el tituloLabel aparezca con un "fade in"
    }
}
