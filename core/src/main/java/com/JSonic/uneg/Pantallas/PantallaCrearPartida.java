package com.JSonic.uneg.Pantallas;

import com.JSonic.uneg.JSonicJuego;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * Pantalla para crear una nueva partida multijugador.
 * Permite introducir el nombre de la partida y el nombre del usuario.
 */
public class PantallaCrearPartida extends PantallaBase {
    private Texture texturaFondo;
    private Stage stage;
    private TextureAtlas texturesAtlas;
    static String tuNombre = "";

    /**
     * Constructor que inicializa la pantalla de creación de partida.
     * @param juegoApp Instancia principal del juego.
     */
    public PantallaCrearPartida(final JSonicJuego juegoApp) {
        super();
        stage = new Stage(getViewport());

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // --- Columna Izquierda: Controles de la partida ---
        Table controlesIzquierda = new Table();

        Label nombrePartidaLabel = new Label("Nombre de la Partida:", getSkin());
        controlesIzquierda.add(nombrePartidaLabel).padBottom(10).row();

        final TextField nombrePartidaField = new TextField("", getSkin());
        nombrePartidaField.setMaxLength(15);


        controlesIzquierda.add(nombrePartidaField).size(300, 50).padBottom(20).row();

        Label tuNombreLabel = new Label("Tu Nombre:", getSkin());
        controlesIzquierda.add(tuNombreLabel).padBottom(10).row();

        final TextField tuNombreField = new TextField("", getSkin());
        tuNombreField.setMaxLength(15);
        controlesIzquierda.add(tuNombreField).size(300, 50).padBottom(20).row();

        Label serverIpLabel = new Label("Tu IP: " , getSkin());
        controlesIzquierda.add(serverIpLabel).padBottom(20).row();

        // --- Columna Derecha: Botones de acción ---
        Table botonesDerecha = new Table();

        TextButton.TextButtonStyle estiloBotonConFondo = new TextButton.TextButtonStyle(getSkin().get(TextButton.TextButtonStyle.class));

        TextButton iniciarPartidaButton = new TextButton("Seguir", estiloBotonConFondo);
        botonesDerecha.add(iniciarPartidaButton).size(250, 75).padBottom(10).row();

        TextButton atrasButton = new TextButton("Atras", estiloBotonConFondo);
        botonesDerecha.add(atrasButton).size(250, 75);

        // --- Añadir columnas a la tabla principal ---
        table.add(controlesIzquierda).pad(20);
        table.add(botonesDerecha).pad(20);

        iniciarPartidaButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                juegoApp.reproducirSonidoClick();
                String nombrePartida = nombrePartidaField.getText();
                tuNombre = tuNombreField.getText();
                System.out.println("Iniciando partida con nombre: " + nombrePartida + " | Anfitrión: " + tuNombre);
                /// --- SIMULACIÓN ---
                // Al crear una partida, la lista de personajes ocupados está vacía.
                JSonicJuego.personajesYaSeleccionados.clear();
                juegoApp.setPantallaActiva(new PantallaSeleccionNivel(juegoApp, true)); // true para modo multijugador


                juegoApp.setPantallaActiva(new PantallaSeleccionPersonaje(juegoApp,true));
            }
        });

        atrasButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                juegoApp.reproducirSonidoClick();
                PantallaMenu pantallaMenu = new PantallaMenu(juegoApp, true);
                pantallaMenu.setEstadoMenu(PantallaMenu.EstadoMenu.CREAR_UNIRSE);
                juegoApp.setPantallaActiva(pantallaMenu);
            }
        });
        inicializar();
    }

    /**
     * Devuelve el nombre del usuario que creó la partida.
     * @return Nombre del usuario.
     */
    static public String getTuNombre() {
        return tuNombre;
    }

    /**
     * Configura el procesador de entrada para la pantalla.
     */
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    /**
     * Renderiza la pantalla y actualiza el escenario.
     * @param delta Tiempo en segundos desde el último frame.
     */
    @Override
    public void render(float delta) {
        super.render(delta);
        stage.act(delta);
        stage.draw();
    }

    /**
     * Ajusta el viewport al cambiar el tamaño de la ventana.
     * @param width Nuevo ancho.
     * @param height Nuevo alto.
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    /**
     * Libera los recursos de la pantalla.
     */
    @Override
    public void dispose() {
        stage.dispose();
        if (texturaFondo != null) texturaFondo.dispose();
        if (texturesAtlas != null) texturesAtlas.dispose();
    }

    /**
     * Inicializa los elementos de la pantalla, como el fondo y el atlas de texturas.
     */
    @Override
    public void inicializar() {
        texturaFondo = new Texture(Gdx.files.internal("Fondos/Portada_desenfoque.png"));
        Image imagenFondo = new Image(texturaFondo);
        imagenFondo.setSize(mainStage.getWidth(), mainStage.getHeight());
        mainStage.addActor(imagenFondo);

        // Cargar el atlas que contiene la imagen para los botones
        texturesAtlas = new TextureAtlas(Gdx.files.internal("Atlas/textures.atlas"));
    }

    /**
     * Actualiza la lógica de la pantalla.
     * @param delta Tiempo en segundos desde el último update.
     */
    @Override
    public void actualizar(float delta) {

    }
}
