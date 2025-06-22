package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class PantallaMenu extends PantallaBase {

    private final JSonicJuego juegoApp;
    private Texture texturaFondo, textureTextoInicio;
    private Texture texturaBotonJugar, texturaBotonJugarHover, texturaBotonJugarDown,
                  texturaBotonAcercaDe, texturaBotonAcercaDeHover, texturaBotonAcercaDeDown,
                  texturaBotonUnJugador, texturaBotonUnJugadorHover, texturaBotonUnJugadorDown,
                  texturaBotonMultijugador, texturaBotonMultijugadorHover, texturaBotonMultijugadorDown,
                  texturaBotonAtras,
                  texturaBotonSalir, texturaBotonSalirHover, texturaBotonSalirDown,
                  texturaBotonOpciones, texturaBotonOpcionesHover, texturaBotonOpcionesDown,
                  texturaBotonAyuda, texturaBotonAyudaHover, texturaBotonAyudaDown;
    private Image imagenFondo, imagenTextoInicio;
    private Button botonJugar, botonAcercaDe, botonUnJugador, botonMultijugador, botonAtras, botonSalir, botonOpciones, botonAyuda;
    private final boolean mostrarMenuDirecto;
    private boolean logicaInicializada = false; // Bandera para controlar la inicialización

    public PantallaMenu(JSonicJuego juegoApp, boolean mostrarMenuDirecto) {
        super(); // Llama a inicializar() internamente
        this.juegoApp = juegoApp;
        this.mostrarMenuDirecto = mostrarMenuDirecto;
    }

    public PantallaMenu(JSonicJuego juegoApp) {
        this(juegoApp, false);
    }

    @Override
    public void inicializar() {
        // 1. Cargar todos los assets y crear los actores.
        texturaFondo = new Texture(Gdx.files.internal("assets/Fondos/Sonic-Tails-Knuckles.png"));
        textureTextoInicio = new Texture(Gdx.files.internal("assets/Fondos/Texto_inicial.png"));

        texturaBotonJugar = new Texture(Gdx.files.internal("assets/Botones/boton_Jugar.png"));
        texturaBotonJugarHover = new Texture(Gdx.files.internal("assets/Botones/boton_jugar_hover.png"));
        texturaBotonJugarDown = new Texture(Gdx.files.internal("assets/Botones/boton_jugar_down.png"));

        texturaBotonAcercaDe = new Texture(Gdx.files.internal("assets/Botones/boton_AcercaDe.png"));
        texturaBotonAcercaDeHover = new Texture(Gdx.files.internal("assets/Botones/boton_acercade_hover.png"));
        texturaBotonAcercaDeDown = new Texture(Gdx.files.internal("assets/Botones/boton_acercade_down.png"));

        texturaBotonUnJugador = new Texture(Gdx.files.internal("assets/Botones/boton_1jugador.png"));
        texturaBotonUnJugadorHover = new Texture(Gdx.files.internal("assets/Botones/boton_unjugador_hover.png"));
        texturaBotonUnJugadorDown = new Texture(Gdx.files.internal("assets/Botones/boton_unjugador_down.png"));

        texturaBotonMultijugador = new Texture(Gdx.files.internal("assets/Botones/boton_multijugador.png"));
        texturaBotonMultijugadorHover = new Texture(Gdx.files.internal("assets/Botones/boton_multijugador_hover.png"));
        texturaBotonMultijugadorDown = new Texture(Gdx.files.internal("assets/Botones/boton_multijugador_down.png"));

        texturaBotonAtras = new Texture(Gdx.files.internal("assets/Botones/boton_atras.png"));

        texturaBotonSalir = new Texture(Gdx.files.internal("assets/Botones/boton_salir.png"));
        texturaBotonSalirHover = new Texture(Gdx.files.internal("assets/Botones/boton_salir_hover.png"));
        texturaBotonSalirDown = new Texture(Gdx.files.internal("assets/Botones/boton_salir_down.png"));

        texturaBotonOpciones = new Texture(Gdx.files.internal("assets/Botones/boton_opciones.png"));
        texturaBotonOpcionesHover = new Texture(Gdx.files.internal("assets/Botones/boton_opciones_hover.png"));
        texturaBotonOpcionesDown = new Texture(Gdx.files.internal("assets/Botones/boton_opciones_down.png"));

        texturaBotonAyuda = new Texture(Gdx.files.internal("assets/Botones/boton_ayuda.png"));
        texturaBotonAyudaHover = new Texture(Gdx.files.internal("assets/Botones/boton_ayuda_hover.png"));
        texturaBotonAyudaDown = new Texture(Gdx.files.internal("assets/Botones/boton_ayuda_down.png"));


        imagenFondo = new Image(texturaFondo);
        imagenTextoInicio = new Image(textureTextoInicio);
        botonJugar = crearBotonConEstados(texturaBotonJugar, texturaBotonJugarDown, texturaBotonJugarHover);
        botonAcercaDe = crearBotonConEstados(texturaBotonAcercaDe, texturaBotonAcercaDeDown, texturaBotonAcercaDeHover);
        botonUnJugador = crearBotonConEstados(texturaBotonUnJugador, texturaBotonUnJugadorDown, texturaBotonUnJugadorHover);
        botonMultijugador = crearBotonConEstados(texturaBotonMultijugador, texturaBotonMultijugadorDown, texturaBotonMultijugadorHover);
        botonAtras = crearBoton(texturaBotonAtras);
        botonOpciones = crearBotonConEstados(texturaBotonOpciones, texturaBotonOpcionesDown, texturaBotonOpcionesHover);
        botonSalir = crearBotonConEstados(texturaBotonSalir, texturaBotonSalirDown, texturaBotonSalirHover);
        botonAyuda = crearBotonConEstados(texturaBotonAyuda, texturaBotonAyudaDown, texturaBotonAyudaHover);
    }

    @Override
    public void actualizar(float delta) {
        // 2. La lógica de configuración se ejecuta solo una vez en el primer fotograma.
        if (!logicaInicializada) {
            configurarEscena();
            logicaInicializada = true;
        }

        // Lógica de actualización continua
        if (imagenTextoInicio.isVisible() && Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {
            imagenTextoInicio.setVisible(false);
            mostrarMenuPrincipal();
        }
    }

    private void configurarEscena() {
        // En este punto, 'mostrarMenuDirecto' ya tiene el valor correcto.
        imagenFondo.setSize(mainStage.getWidth(), mainStage.getHeight());
        mainStage.addActor(imagenFondo);

        float textoAncho = 600f;
        float textoAlto = textoAncho * textureTextoInicio.getHeight() / textureTextoInicio.getWidth();
        imagenTextoInicio.setSize(textoAncho, textoAlto);
        imagenTextoInicio.setPosition((mainStage.getWidth() - textoAncho) / 2, 70f);
        mainStage.addActor(imagenTextoInicio);

        // Configurar visibilidad inicial
        if (mostrarMenuDirecto) {
            imagenTextoInicio.setVisible(false);
            mostrarMenuPrincipal();
        } else {
            imagenTextoInicio.setVisible(true);
            botonJugar.setVisible(false);
            botonOpciones.setVisible(false);
            botonSalir.setVisible(false);
            botonAcercaDe.setVisible(false);
            botonUnJugador.setVisible(false);
            botonMultijugador.setVisible(false);
            botonAtras.setVisible(false);
            botonAyuda.setVisible(false);
        }

        configurarListeners();

        Table tablaAtras = new Table();
        tablaAtras.setFillParent(true);
        uiStage.addActor(tablaAtras);
        tablaAtras.top().left();
        tablaAtras.add(botonAtras).size(125, 125).pad(20);
    }

    private void configurarListeners() {
        botonJugar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mostrarMenuJugar();
            }
        });
        botonAtras.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mostrarMenuPrincipal();
            }
        });

        botonOpciones.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mostrarMenuOpciones();
            }
        });

        botonSalir.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        botonAcercaDe.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                juegoApp.setPantallaActiva(new PantallaAcercaDe(juegoApp));
            }
        });

        botonAyuda.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                juegoApp.setPantallaActiva(new PantallaAyuda(juegoApp));
            }
        });

        botonUnJugador.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                juegoApp.setPantallaActiva(new PantallaDeJuego(juegoApp));
            }
        });
        botonMultijugador.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                juegoApp.setPantallaActiva(new PantallaDeJuego(juegoApp));
            }
        });
    }

    private void mostrarMenuPrincipal() {
        uiTable.clear();
        uiTable.bottom().padBottom(40);
        uiTable.add(botonJugar).size(250, 125).padBottom(10);
        uiTable.row();
        uiTable.add(botonOpciones).size(250, 125);
        uiTable.row();
        uiTable.add(botonSalir).size(250, 125);

        botonJugar.setVisible(true);
        botonOpciones.setVisible(true);
        botonSalir.setVisible(true);
        botonAcercaDe.setVisible(false);
        botonUnJugador.setVisible(false);
        botonMultijugador.setVisible(false);
        botonAtras.setVisible(false);
        botonAyuda.setVisible(false);
    }

    private void mostrarMenuJugar() {
        uiTable.clear();
        uiTable.bottom().padBottom(40);
        uiTable.add(botonUnJugador).size(250, 125).padBottom(10);
        uiTable.row();
        uiTable.add(botonMultijugador).size(250, 125);

        botonJugar.setVisible(false);
        botonOpciones.setVisible(false);
        botonSalir.setVisible(false);
        botonAcercaDe.setVisible(false);
        botonUnJugador.setVisible(true);
        botonMultijugador.setVisible(true);
        botonAtras.setVisible(true);
        botonAyuda.setVisible(false);
    }

    private void mostrarMenuOpciones() {
        uiTable.clear();
        uiTable.bottom().padBottom(40);
        uiTable.add(botonAcercaDe).size(250, 125);
        uiTable.row();
        uiTable.add(botonAyuda).size(250, 125);

        botonJugar.setVisible(false);
        botonOpciones.setVisible(false);
        botonSalir.setVisible(false);
        botonAcercaDe.setVisible(true);
        botonUnJugador.setVisible(false);
        botonMultijugador.setVisible(false);
        botonAtras.setVisible(true);
        botonAyuda.setVisible(true);
    }

    private Button crearBoton(Texture textura) {
        Button.ButtonStyle estilo = new Button.ButtonStyle();
        estilo.up = new TextureRegionDrawable(new TextureRegion(textura));
        return new Button(estilo);
    }

    private Button crearBotonConEstados(Texture up, Texture down, Texture over) {
        Button.ButtonStyle estilo = new Button.ButtonStyle();
        estilo.up = new TextureRegionDrawable(new TextureRegion(up));
        estilo.down = new TextureRegionDrawable(new TextureRegion(down));
        estilo.over = new TextureRegionDrawable(new TextureRegion(over));
        return new Button(estilo);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (texturaFondo != null) texturaFondo.dispose();
        if (textureTextoInicio != null) textureTextoInicio.dispose();

        if (texturaBotonJugar != null) texturaBotonJugar.dispose();
        if (texturaBotonJugarHover != null) texturaBotonJugarHover.dispose();
        if (texturaBotonJugarDown != null) texturaBotonJugarDown.dispose();

        if (texturaBotonAcercaDe != null) texturaBotonAcercaDe.dispose();
        if (texturaBotonAcercaDeHover != null) texturaBotonAcercaDeHover.dispose();
        if (texturaBotonAcercaDeDown != null) texturaBotonAcercaDeDown.dispose();

        if (texturaBotonUnJugador != null) texturaBotonUnJugador.dispose();
        if (texturaBotonUnJugadorHover != null) texturaBotonUnJugadorHover.dispose();
        if (texturaBotonUnJugadorDown != null) texturaBotonUnJugadorDown.dispose();

        if (texturaBotonMultijugador != null) texturaBotonMultijugador.dispose();
        if (texturaBotonMultijugadorHover != null) texturaBotonMultijugadorHover.dispose();
        if (texturaBotonMultijugadorDown != null) texturaBotonMultijugadorDown.dispose();

        if (texturaBotonAtras != null) texturaBotonAtras.dispose();

        if (texturaBotonSalir != null) texturaBotonSalir.dispose();
        if (texturaBotonSalirHover != null) texturaBotonSalirHover.dispose();
        if (texturaBotonSalirDown != null) texturaBotonSalirDown.dispose();

        if (texturaBotonOpciones != null) texturaBotonOpciones.dispose();
        if (texturaBotonOpcionesHover != null) texturaBotonOpcionesHover.dispose();
        if (texturaBotonOpcionesDown != null) texturaBotonOpcionesDown.dispose();

        if(texturaBotonAyuda != null) texturaBotonAyuda.dispose();
        if(texturaBotonAyudaHover != null) texturaBotonAyudaHover.dispose();
        if(texturaBotonAyudaDown != null) texturaBotonAyudaDown.dispose();

    }
}
