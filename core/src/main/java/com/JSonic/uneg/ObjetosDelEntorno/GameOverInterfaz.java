package com.JSonic.uneg.ObjetosDelEntorno;

import com.JSonic.uneg.JSonicJuego;
import com.JSonic.uneg.Pantallas.PantallaMenu;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
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

    // Se declara la fuente aquí para poder liberarla en el método dispose().
    private BitmapFont fuenteBoton;

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
        estiloTitulo.font.getData().setScale(1.6f);
        tituloLabel = new Label("FIN DEL JUEGO", estiloTitulo);

        // Se crea la fuente para los botones. Guardarla es crucial para evitar fugas de memoria.
        fuenteBoton = new BitmapFont(Gdx.files.internal("Fuentes/juego_fuente2.fnt"));
        TextButton.TextButtonStyle estiloBotonPequeno = new TextButton.TextButtonStyle(skin.get("default", TextButton.TextButtonStyle.class));
        estiloBotonPequeno.font = fuenteBoton;
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
            // Se decide si el botón de reiniciar es visible o no.
            if (JSonicJuego.modoMultijugador) {
                botonReiniciar.setVisible(false);
            } else {
                botonReiniciar.setVisible(true);
            }
            // Se inicia la animación de entrada.
            iniciarAnimacionDeEntrada();
        }
    }

    /**
     * Inicia una animación de entrada secuencial para el título y los botones.
     */
    public void iniciarAnimacionDeEntrada() {
        // 1. Se resetea el estado de los actores a animar.
        tituloLabel.clearActions();
        botonReiniciar.clearActions();
        botonMenu.clearActions();

        // Se hacen invisibles para que la animación de "fadeIn" funcione.
        tituloLabel.getColor().a = 0;
        botonReiniciar.getColor().a = 0;
        botonMenu.getColor().a = 0;

        // 2. Se añade una secuencia de acciones a la tabla misma.
        this.addAction(Actions.sequence(
            // Acción 1: El título aparece con un fundido lento y dramático.
            Actions.run(new Runnable() {
                @Override
                public void run() {
                    tituloLabel.addAction(Actions.fadeIn(1.5f, Interpolation.fade));
                }
            }),
            // Acción 2: Se espera medio segundo para crear una pausa.
            Actions.delay(0.5f),
            // Acción 3: Los botones aparecen simultáneamente.
            Actions.run(new Runnable() {
                @Override
                public void run() {
                    // Se usa "parallel" para que ambos botones se animen a la vez.
                    botonReiniciar.addAction(Actions.fadeIn(0.5f));
                    botonMenu.addAction(Actions.fadeIn(0.5f));
                }
            })
        ));
    }

    /**
     * Este método es ESENCIAL para evitar fugas de memoria.
     * Debe ser llamado desde el método dispose() de la pantalla que contiene esta interfaz.
     */
    public void dispose() {
        if (fuenteBoton != null) {
            fuenteBoton.dispose();
        }
    }
}
