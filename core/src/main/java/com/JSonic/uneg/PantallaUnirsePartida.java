package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import java.util.ArrayList;
import java.util.List;

public class PantallaUnirsePartida extends PantallaBase {

    private final JSonicJuego juegoApp;
    private Texture texturaFondo;
    private Table partidasTable; // La tabla que contendrá los botones de las partidas
    private TextureAtlas texturesAtlas; // Atlas para los fondos de los botones

    public PantallaUnirsePartida(final JSonicJuego juegoApp) {
        this.juegoApp = juegoApp;
    }

    @Override
    public void inicializar() {
        texturaFondo = new Texture(Gdx.files.internal("Fondos/Portada_desenfoque.png"));
        Image imagenFondo = new Image(texturaFondo);
        imagenFondo.setSize(mainStage.getWidth(), mainStage.getHeight());
        mainStage.addActor(imagenFondo);

        // Cargar el atlas que contiene la imagen para los botones
        texturesAtlas = new TextureAtlas(Gdx.files.internal("Atlas/textures.atlas"));

        Table layout = new Table();
        layout.setFillParent(true);
        uiStage.addActor(layout);

        Label titulo = new Label("Partidas Disponibles", skin, "title");
        layout.add(titulo).padBottom(20).colspan(2).center();
        layout.row();

        // --- ScrollPane para la lista de partidas ---
        partidasTable = new Table(getSkin());
        ScrollPane scrollPane = new ScrollPane(partidasTable, getSkin());
        scrollPane.setScrollingDisabled(true, false); // Desactiva el scroll horizontal
        scrollPane.setFadeScrollBars(false);

        // Hacer la barra de scroll más gruesa
        scrollPane.getStyle().vScroll.setMinWidth(50);
        scrollPane.getStyle().vScrollKnob.setMinWidth(50);

        // --- Tabla para los botones de la derecha ---
        Table botonesLaterales = new Table();

        // Crear un estilo para los botones con la imagen de fondo del atlas cargado
        TextButton.TextButtonStyle estiloBotonConFondo = new TextButton.TextButtonStyle(getSkin().get(TextButton.TextButtonStyle.class));

        TextButton actualizarButton = new TextButton("Actualizar", estiloBotonConFondo);
        botonesLaterales.add(actualizarButton).size(350, 75).padBottom(10).row();

        TextButton atrasButton = new TextButton("Atras", estiloBotonConFondo);
        botonesLaterales.add(atrasButton).size(350, 76);

        // --- Añadir ScrollPane y botones a la tabla principal ---
        layout.add(scrollPane).width(425).height(300).pad(20);
        layout.add(botonesLaterales).pad(20);
        layout.row();

        // --- Listeners de los botones ---
        actualizarButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                descubrirPartidas();
            }
        });

        atrasButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                PantallaMenu menu = new PantallaMenu(juegoApp, true);
                menu.setEstadoMenu(PantallaMenu.EstadoMenu.CREAR_UNIRSE);
                juegoApp.setPantallaActiva(menu);
            }
        });

        descubrirPartidas(); // Descubre partidas al entrar en la pantalla
    }

    private void descubrirPartidas() {
        partidasTable.clear(); // Limpia solo la tabla de partidas

        // --- Lógica de ejemplo ---
        // Aquí es donde se integrará la búsqueda de servidores.
        // Por ahora, es una lista de ejemplo para el frontend.

        List<String> hosts = new ArrayList<>();
        hosts.add("Partida de Juan");
        hosts.add("Servidor Mega Pro");
        hosts.add("Aventura Sonic");
        hosts.add("Partida (Simulacion)");
        // Añadir más para probar el scroll
        hosts.add("Servidor 5");
        hosts.add("Servidor 6");
        hosts.add("Servidor 7");
        hosts.add("Servidor 8");
        hosts.add("Servidor 9");
        hosts.add("Servidor 10");

        if (hosts.isEmpty()) {
            partidasTable.add(new Label("No se encontraron partidas.", getSkin())).center();
        } else {
            for (final String host : hosts) {
                TextButton unirseButton = new TextButton(host, getSkin());
                unirseButton.getLabel().setFontScale(0.6f);
                unirseButton.getLabel().setWrap(true);
                unirseButton.getLabel().setAlignment(Align.center);
                unirseButton.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (host.contains("(Simulacion)")) {
                            new Dialog("Error al unirse", getSkin(), "dialog")
                                    .text("No se pudo unir, la partida ya ha comenzado.")
                                    .button("Aceptar")
                                    .show(uiStage);
                        } else {
                            // Crear el campo de texto para el nombre
                            final TextField nombreJugadorField = new TextField("", getSkin());
                            nombreJugadorField.setMessageText("Tu nombre...");
                            nombreJugadorField.setMaxLength(15);

                            // Crear el diálogo para pedir el nombre
                            Dialog dialog = new Dialog("", getSkin(), "dialog") {
                                @Override
                                protected void result(Object object) {
                                    if (Boolean.TRUE.equals(object)) {
                                        String nombreJugador = nombreJugadorField.getText();
                                        // Opcional: Validar que el nombre no esté vacío
                                        if (!nombreJugador.trim().isEmpty()) {
                                            System.out.println("Jugador '" + nombreJugador + "' intentando unirse a: " + host);
                                            juegoApp.setPantallaActiva(new PantallaLobby(juegoApp, false));
                                        }
                                    }
                                }
                            };
                            dialog.text("Introduce tu nombre para unirte:");
                            dialog.getContentTable().row();
                            dialog.getContentTable().add(nombreJugadorField).size(400, 50).pad(20);

                            TextButton aceptarButton = new TextButton("Aceptar", getSkin());
                            aceptarButton.getLabelCell().pad(15, 40, 15, 40);

                            TextButton cancelarButton = new TextButton("Cancelar", getSkin());
                            cancelarButton.getLabelCell().pad(15, 40, 15, 40);

                            dialog.button(aceptarButton, true);
                            dialog.button(cancelarButton, false);

                            dialog.pad(30);
                            dialog.show(uiStage);
                        }
                    }
                });
                partidasTable.add(unirseButton).pad(10).size(350, 60).center().row();
            }
        }
    }

    @Override
    public void actualizar(float delta) {
        // No se necesita lógica de actualización específica aquí por ahora
    }

    @Override
    public void dispose() {
        super.dispose();
        if (texturaFondo != null) {
            texturaFondo.dispose();
        }
        if (texturesAtlas != null) {
            texturesAtlas.dispose(); // Liberar el atlas de la memoria
        }
    }
}
