package com.JSonic.uneg.Pantallas;

import com.JSonic.uneg.JSonicJuego;
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
import network.Network;

import static com.JSonic.uneg.Pantallas.PantallaCrearPartida.getTuNombre;

/**
 * Pantalla de espera (lobby) que gestiona la lista de jugadores antes de iniciar la partida online.
 */
public class PantallaLobby extends PantallaBase {
    private static final int MAX_PLAYERS = 3;
    private final JSonicJuego juegoApp;
    private Texture texturaFondo;
    private TextureAtlas texturesAtlas;
    private final Array<String> nombresJugadores = new Array<>();
    private final Label[] playerLabels = new Label[MAX_PLAYERS];
    private Table listaContainer;
    private final boolean esAnfitrion;

    /**
     * Constructor de PantallaLobby.
     * @param juegoApp instancia del juego JSonic.
     * @param esAnfitrion indica si el jugador actual es el anfitrión de la partida.
     */
    public PantallaLobby(JSonicJuego juegoApp, boolean esAnfitrion) {
        super();
        this.juegoApp = juegoApp;
        this.esAnfitrion = esAnfitrion;
        inicializar();
    }

    /**
     * Inicializa recursos gráficos como el fondo y el atlas de texturas.
     */
    @Override
    public void inicializar() {
        texturaFondo = new Texture(Gdx.files.internal("Fondos/Portada_desenfoque.png"));
        Image imagenFondo = new Image(texturaFondo);
        imagenFondo.setSize(mainStage.getWidth(), mainStage.getHeight());
        mainStage.addActor(imagenFondo);
        texturesAtlas = new TextureAtlas(Gdx.files.internal("Atlas/textures.atlas"));
    }

    /**
     * Configura y muestra los elementos de la interfaz del lobby y envía el nombre del jugador al servidor.
     */
    @Override
    public void show() {
        super.show();
        if (uiStage.getActors().size > 1) {
            return;
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
                @Override
                public void clicked(InputEvent e, float x, float y) {
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
        if (juegoApp.getGameClient() != null) {
            Network.PaqueteEnviarNombre paqueteNombre = new Network.PaqueteEnviarNombre();
            paqueteNombre.nombre = getTuNombre();
            juegoApp.getGameClient().send(paqueteNombre);
        }
    }

    /**
     * Añade un jugador a la sala y actualiza la UI.
     * Este método debería ser llamado por la lógica de red.
     *
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
     *
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

    /**
     * Procesa paquetes recibidos del servidor y actualiza la lógica del lobby cada frame.
     * @param delta tiempo transcurrido desde el último frame en segundos.
     */
    @Override
    public void actualizar(float delta) {
        if (juegoApp.getGameClient() == null) {
            return;
        }

        java.util.Queue<Object> paquetesRecibidos = juegoApp.getGameClient().getPaquetesRecibidos();
        java.util.ArrayList<Object> paquetesParaDespues = new java.util.ArrayList<>();

        while (!paquetesRecibidos.isEmpty()) {
            Object paquete = paquetesRecibidos.poll();

            if (paquete instanceof Network.PaqueteActualizarLobby paqueteLobby) {
                // Es para actualizar la lista de nombres. Lo procesamos.
                System.out.println("[LOBBY] Procesando actualización de nombres.");
                this.nombresJugadores.clear();
                for (String nombre : paqueteLobby.nombres) {
                    this.nombresJugadores.add(nombre);
                }
                actualizarListaJugadoresUI();

            } else if (paquete instanceof Network.PaqueteIniciarPartida) {
                // Orden para iniciar el juego, La procesamos.
                System.out.println("[LOBBY] Recibida orden del servidor para iniciar la partida.");
                // Condición de seguridad para no iniciar dos veces.
                if (!(juegoApp.getScreen() instanceof PantallaDeJuego)) {
                    juegoApp.iniciarJuegoOnline();
                }
                // No necesitamos devolver este paquete a la cola.

            } else {
                // No es para el lobby. Lo guardamos para la siguiente pantalla.
                paquetesParaDespues.add(paquete);
            }
        }

        // Devolvemos los paquetes que no usamos a la cola principal.
        if (!paquetesParaDespues.isEmpty()) {
            paquetesRecibidos.addAll(paquetesParaDespues);
        }
    }

    /**
     * Renderiza la pantalla del lobby.
     * @param delta tiempo transcurrido desde el último frame en segundos.
     */
    @Override
    public void render(float delta) {
        super.render(delta);
    }

    /**
     * Libera los recursos gráficos del lobby, como texturas y atlas.
     */
    @Override
    public void dispose() {
        super.dispose();
        if (texturaFondo != null) texturaFondo.dispose();
        if (texturesAtlas != null) texturesAtlas.dispose();
    }
}
