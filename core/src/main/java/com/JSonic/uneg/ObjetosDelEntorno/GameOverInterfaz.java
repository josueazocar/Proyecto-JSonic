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

public class GameOverInterfaz extends Table {

    private final JSonicJuego juegoApp;
    private Label tituloLabel;
    private TextButton botonReiniciar;
    private TextButton botonMenu;

    public GameOverInterfaz(JSonicJuego juegoApp, Skin skin) {
        super(skin);
        this.juegoApp = juegoApp;
        setupUI(skin);
    }

    private void setupUI(Skin skin) {
        this.setVisible(false);
        this.setFillParent(true);
        this.center();

        Label.LabelStyle estiloTitulo = new Label.LabelStyle(skin.getFont("body-font"), Color.RED);
        estiloTitulo.font.getData().setScale(2.0f);
        tituloLabel = new Label("FIN DEL JUEGO", estiloTitulo);

        TextButton.TextButtonStyle estiloBotonPequeno = new TextButton.TextButtonStyle(skin.get("default", TextButton.TextButtonStyle.class));

        estiloBotonPequeno.font = new BitmapFont(Gdx.files.internal("Fuentes/juego_fuente2.fnt"));
        estiloBotonPequeno.font.getData().setScale(0.55f);

        botonReiniciar = new TextButton("Reiniciar", estiloBotonPequeno);
        botonMenu = new TextButton("Volver al Menu", estiloBotonPequeno);


        botonReiniciar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                juegoApp.iniciarJuegoLocal();
            }
        });

        botonMenu.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("[CLIENT] El jugador ha decidido salir de la partida desde Game Over.");

                if (juegoApp.getClient() != null) {
                    juegoApp.getClient().send(new Network.PaqueteSalidaDePartida());
                    juegoApp.getClient().disconnect();
                }

                PantallaMenu pantallaMenu = new PantallaMenu(juegoApp, true);
                pantallaMenu.setEstadoMenu(PantallaMenu.EstadoMenu.JUGAR);
                juegoApp.setPantallaActiva(pantallaMenu);

                if (LocalServer.class != null) {
                    LocalServer.decreaseContamination(100);
                }
            }
        });

        this.add(tituloLabel).padBottom(50).row();
        this.add(botonReiniciar).size(300, 80).pad(10).row();
        this.add(botonMenu).size(300, 80).pad(10);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            if (JSonicJuego.modoMultijugador) {
                botonReiniciar.setVisible(false);
            } else {
                botonReiniciar.setVisible(true);
            }
        }
    }

    public void iniciarAnimacionDeEntrada() {
        // Aquí podrías añadir una acción para que el tituloLabel aparezca con un "fade in"
    }
}
