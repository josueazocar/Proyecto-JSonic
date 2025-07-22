// Archivo: src/com/JSonic/uneg/PantallaSeleccionNivel.java
package com.JSonic.uneg.Pantallas;

import com.JSonic.uneg.JSonicJuego;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class PantallaSeleccionNivel extends PantallaBase {

    private final JSonicJuego juegoApp;
    private TextureAtlas atlasNiveles, atlasTextures;
    private Button playButton;
    private Button botonNivel1, botonNivel2, botonNivel3;
    private ButtonGroup<Button> grupoBotonesNivel;
    private final boolean esMultijugador;
    private boolean logicaInicializada = false;

    public PantallaSeleccionNivel(JSonicJuego juegoApp) {
        super();
        this.juegoApp = juegoApp;
        esMultijugador = false;
        inicializar();
    }

    public PantallaSeleccionNivel(JSonicJuego juegoApp, boolean esMultijugador) {
        super();
        this.juegoApp = juegoApp;
        this.esMultijugador = esMultijugador;
    }

    @Override
    public void inicializar() {
        atlasNiveles = new TextureAtlas(Gdx.files.internal("Atlas/seleccionNiveles.atlas"));
        atlasTextures = new TextureAtlas(Gdx.files.internal("Atlas/textures.atlas"));

        // --- Creación de Botones de Nivel ---
        botonNivel1 = crearBotonNivel("nivel1_seleccion", "nivel1_seleccionado", "nivel1_disabled");
        botonNivel2 = crearBotonNivel("nivel2_seleccion", "nivel2_seleccionado", "nivel2_disabled");
        botonNivel3 = crearBotonNivel("nivel3_seleccion", "nivel3_seleccionado", "nivel3_disabled");

        grupoBotonesNivel = new ButtonGroup<>(botonNivel1, botonNivel2, botonNivel3);
        grupoBotonesNivel.setMaxCheckCount(1);
        grupoBotonesNivel.setMinCheckCount(1);

        // --- Creación del Botón Jugar ---
        Button.ButtonStyle playStyle = new Button.ButtonStyle();
        playStyle.up = new TextureRegionDrawable(atlasNiveles.findRegion("boton_jugar"));

        playStyle.down = new TextureRegionDrawable(atlasNiveles.findRegion("boton_jugar_down"));
        playStyle.over = new TextureRegionDrawable(atlasNiveles.findRegion("boton_jugar_hover"));
        playButton = new Button(playStyle);

        //botonNivel1.setChecked(true);
        botonNivel1.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (botonNivel1.isDisabled()) {
                    return;
                }
                System.out.println("Botón Nivel 1 clickeado");
                ConfiguracionJuego.mapaSeleccionado = "maps/Zona1N1.tmx";
            }
        });
        botonNivel2.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (botonNivel2.isDisabled()) {
                    return;
                }
                System.out.println("Botón Nivel 2 clickeado");
                ConfiguracionJuego.mapaSeleccionado = "maps/Zona1N2.tmx";
            }
        });
        botonNivel3.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (botonNivel3.isDisabled()) {
                    return;
                }
                System.out.println("Botón Nivel 3 clickeado");
                ConfiguracionJuego.mapaSeleccionado = "maps/Zona1N3.tmx";
            }
        });



        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (ConfiguracionJuego.mapaSeleccionado != null) {
                    if (esMultijugador) {
                        // --- FLUJO MULTIJUGADOR (ANFITRIÓN) ---
                        // Después de seleccionar el nivel, el anfitrión va al LOBBY.
                        System.out.println("Anfitrión seleccionó el nivel. -> PantallaLobby");
                        // Le pasamos 'true' porque es el anfitrión.
                        juegoApp.setPantallaActiva(new PantallaLobby(juegoApp, true));

                    } else {
                        // --- FLUJO UN JUGADOR ---
                        // El jugador local inicia el juego directamente.
                        System.out.println("Un Jugador seleccionó el nivel. -> Iniciando Juego Local");
                        juegoApp.iniciarJuegoLocal();
                    }

                }
            }
        });

    }

    private void configurarInterfazCompleta(){
        mainStage.addActor(new Image(new Texture("Fondos/Portada_desenfoque.png")));

        TextureRegionDrawable fondoDrawable = new TextureRegionDrawable(atlasTextures.findRegion("shade"));

        // Usaremos una única tabla para todo el layout, como en la pantalla de personajes.
        uiTable.setFillParent(true);
        mainStage.addActor(uiTable);

        // Título usando una región del atlas
        TextureRegionDrawable tituloDrawable = null;
        tituloDrawable = new TextureRegionDrawable(atlasNiveles.findRegion("seleccionatunivel"));
        uiTable.add(new Image(tituloDrawable)).colspan(3).padBottom(20).row();


        // Agregar textos descriptivos ARRIBA de cada botón
        Label.LabelStyle smallLabelStyle = new Label.LabelStyle(getSkin().get(Label.LabelStyle.class));
        smallLabelStyle.font.getData().setScale(0.8f);
        Label labelNivel1 = new Label("Green Hill", smallLabelStyle);
        Label labelNivel2 = new Label("Chemical Plant", smallLabelStyle);
        Label labelNivel3 = new Label("Ice Cap", smallLabelStyle);

        //1
        Label tooltipLabel1 = new Label("Zona clasica de pasto y colinas.", getSkin(), "default");
        tooltipLabel1.setWrap(true);
        Table tooltipTable1 = new Table(getSkin());
        tooltipTable1.setBackground(fondoDrawable);
        tooltipTable1.add(tooltipLabel1)
            .width(300)
            .pad(20);
        Tooltip<Table> tooltip1 = new Tooltip<>(tooltipTable1);
        tooltip1.setInstant(true);
        botonNivel1.addListener(tooltip1);


        Label tooltipLabel2 = new Label("Fabrica llena de productos quimicos.", getSkin(), "default");
        tooltipLabel2.setWrap(true);
        Table tooltipTable2 = new Table(getSkin());
        tooltipTable2.setBackground(fondoDrawable);
        tooltipTable2.add(tooltipLabel2).width(300).pad(20);
        Tooltip<Table> tooltip2 = new Tooltip<>(tooltipTable2);
        tooltip2.setInstant(true);
        botonNivel2.addListener(tooltip2);

        Label tooltipLabel3 = new Label("Zona helada y resbaladiza.", getSkin(), "default");
        tooltipLabel3.setWrap(true);
        Table tooltipTable3 = new Table(getSkin());
        tooltipTable3.setBackground(fondoDrawable);
        tooltipTable3.add(tooltipLabel3).width(300).pad(20);
        Tooltip<Table> tooltip3 = new Tooltip<>(tooltipTable3);
        tooltip3.setInstant(true);
        botonNivel3.addListener(tooltip3);

        uiTable.add(labelNivel1).padBottom(10);
        uiTable.add(labelNivel2).padBottom(10);
        uiTable.add(labelNivel3).padBottom(10).row();

        uiTable.add(botonNivel1).size(300, 225).pad(20);
        uiTable.add(botonNivel2).size(300, 225).pad(20);
        uiTable.add(botonNivel3).size(300, 225).pad(20).row();

        uiTable.add(playButton).colspan(3).padTop(40).size(250, 80);

        actualizarEstadoNiveles();
    }

    private Button crearBotonNivel(String up, String checked, String disabled) {
        Button.ButtonStyle estilo = new Button.ButtonStyle();
        estilo.up = new TextureRegionDrawable(atlasNiveles.findRegion(up));
        estilo.checked = new TextureRegionDrawable(atlasNiveles.findRegion(checked));
        estilo.disabled = new TextureRegionDrawable(atlasNiveles.findRegion(disabled));
        return new Button(estilo);
    }

    private void actualizarEstadoNiveles() {
        // Lógica para habilitar/deshabilitar según el modo de juego
        if (esMultijugador) {
            botonNivel1.setDisabled(false);
            botonNivel2.setDisabled(false);
            botonNivel3.setDisabled(false);
        } else {
            botonNivel1.setDisabled(false);
            botonNivel2.setDisabled(true);
            botonNivel3.setDisabled(true);
        }

        // Lógica para la selección inicial (como en la pantalla de personajes)
        grupoBotonesNivel.uncheckAll(); // Limpia cualquier selección previa
        ConfiguracionJuego.mapaSeleccionado = null; // Reinicia el mapa seleccionado

        if (!botonNivel1.isDisabled()) {
            botonNivel1.setChecked(true);
            ConfiguracionJuego.mapaSeleccionado = "maps/Zona1N1.tmx";
        } else if (!botonNivel2.isDisabled()) {
            botonNivel2.setChecked(true);
            ConfiguracionJuego.mapaSeleccionado = "maps/Zona1N2.tmx";
        } else if (!botonNivel3.isDisabled()) {
            botonNivel3.setChecked(true);
            ConfiguracionJuego.mapaSeleccionado = "maps/Zona1N3.tmx";
        }
    }



    @Override
    public void actualizar(float delta) {
        mainStage.act(delta);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        actualizar(delta);
        mainStage.draw();
    }

    @Override
    public void show() {
        if (!logicaInicializada) {
            configurarInterfazCompleta();
            logicaInicializada = true;
        }
        Gdx.input.setInputProcessor(mainStage);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        super.dispose();
        atlasNiveles.dispose();
        atlasTextures.dispose();
    }
}

