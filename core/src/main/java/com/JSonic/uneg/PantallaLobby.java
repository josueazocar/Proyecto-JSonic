package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

public class PantallaLobby extends PantallaBase {
    private static final int MAX_PLAYERS = 3;
    private final JSonicJuego juegoApp;
    private Texture texturaFondo;
    private TextureAtlas texturesAtlas;
    private final Array<String> nombresJugadores = new Array<>();
    private final Label[] playerLabels = new Label[MAX_PLAYERS];
    private Table listaContainer;
    private final boolean esAnfitrion;

    public PantallaLobby(JSonicJuego juegoApp, boolean esAnfitrion) {
        super();
        this.juegoApp = juegoApp;
        this.esAnfitrion = esAnfitrion;
    }

    @Override
    public void inicializar() {
        texturaFondo = new Texture(Gdx.files.internal("Fondos/Portada_desenfoque.png"));
        Image imagenFondo = new Image(texturaFondo);
        imagenFondo.setSize(mainStage.getWidth(), mainStage.getHeight());
        mainStage.addActor(imagenFondo);
        texturesAtlas = new TextureAtlas(Gdx.files.internal("Atlas/textures.atlas"));
    }

    @Override
    public void show() {
        super.show();
        if (uiStage.getActors().size > 1) {
            return; // Ya inicializado
        }

        Table root = new Table();
        root.setFillParent(true);
        root.center(); // Centra toda la tabla en la pantalla
        uiStage.addActor(root);

        Label title = new Label("Sala de Espera", getSkin(), "title");
        root.add(title).colspan(2).center().padBottom(20).row();

        listaContainer = new Table(getSkin());
        root.add(listaContainer).width(400).height(200).colspan(2).row();

        for (int i = 0; i < MAX_PLAYERS; i++) {
            Label lbl = new Label("", getSkin(), "body");
            lbl.setAlignment(Align.center); // Centra el texto de los jugadores
            listaContainer.add(lbl).growX().pad(4).row();
            playerLabels[i] = lbl;
        }

        TextButton.TextButtonStyle estiloBotonConFondo = new TextButton.TextButtonStyle(getSkin().get(TextButton.TextButtonStyle.class));
        if (esAnfitrion) {
            TextButton iniciarPartida = new TextButton("Iniciar Partida", estiloBotonConFondo);
            iniciarPartida.getLabel().setFontScale(0.75f);
            iniciarPartida.getLabel().setWrap(true);
            iniciarPartida.getLabel().setAlignment(Align.center);
            iniciarPartida.addListener(new ClickListener() {
                @Override public void clicked(InputEvent e, float x, float y) {
                    // TODO: La lógica de red debería iniciar la partida para todos.
                    juegoApp.iniciarJuegoOnline();
                }
            });
            root.add(iniciarPartida).size(250, 75).pad(10);
        }

        TextButton atras = new TextButton("Salir", estiloBotonConFondo);
        atras.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                PantallaMenu pantallaMenu = new PantallaMenu(juegoApp, true);
                pantallaMenu.setEstadoMenu(PantallaMenu.EstadoMenu.CREAR_UNIRSE);
                juegoApp.setPantallaActiva(pantallaMenu);
            }
        });
        root.add(atras).size(250, 75).pad(10);

        if (!esAnfitrion) {
            root.getCell(atras).colspan(2).center(); // Centra el botón cuando está solo
        }

        // Inicializa la UI vacía
        actualizarListaJugadoresUI();
    }

    /**
     * Añade un jugador a la sala y actualiza la UI.
     * Este método debería ser llamado por la lógica de red.
     * @param nombre El nombre del jugador que se une.
     */
    public void agregarJugador(String nombre) {
        if (nombresJugadores.size < MAX_PLAYERS) {
            nombresJugadores.add(nombre);
            actualizarListaJugadoresUI();
        }
    }

    /**
     * Elimina un jugador de la sala y actualiza la UI.
     * Este método debería ser llamado por la lógica de red.
     * @param nombre El nombre del jugador que se va.
     */
    public void eliminarJugador(String nombre) {
        nombresJugadores.removeValue(nombre, false);
        actualizarListaJugadoresUI();
    }

    /**
     * Redibuja la lista de jugadores en la interfaz gráfica.
     */
    private void actualizarListaJugadoresUI() {
        for (int i = 0; i < MAX_PLAYERS; i++) {
            if (i < nombresJugadores.size) {
                playerLabels[i].setText(nombresJugadores.get(i));
            } else {
                playerLabels[i].setText("Esperando jugador...");
            }
        }
    }

    @Override
    public void actualizar(float delta){
        // Se gestiona a través de agregarJugador/eliminarJugador.
    }

    @Override
    public void render(float delta) {
        super.render(delta);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (texturaFondo != null) texturaFondo.dispose();
        if (texturesAtlas != null) texturesAtlas.dispose();
    }
}
