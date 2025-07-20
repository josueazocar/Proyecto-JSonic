package com.JSonic.uneg.Pantallas;

import com.JSonic.uneg.JSonicJuego;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class PantallaMenu extends PantallaBase {

    private final JSonicJuego juegoApp;
    private Texture texturaFondo, textureTextoInicio;
    private TextureAtlas atlasBotones;
    private Image imagenFondo, imagenTextoInicio;
    private Button botonJugar, botonAcercaDe, botonUnJugador, botonOnline , botonLocal,botonMultijugador, botonAtras, botonSalir, botonOpciones, botonAyuda, botonCrear, botonUnirse;
    private final boolean mostrarMenuDirecto;
    private boolean logicaInicializada = false; // Bandera para controlar la inicialización
    private EstadoMenu estadoInicial = EstadoMenu.PRINCIPAL;
    private EstadoMenu estadoActual;

    public enum EstadoMenu {
        PRINCIPAL, JUGAR, OPCIONES, MULTIJUGADOR, CREAR_UNIRSE
    }

    public PantallaMenu(JSonicJuego juegoApp, boolean mostrarMenuDirecto) {
        super(); // Llama a inicializar() internamente
        this.juegoApp = juegoApp;
        this.mostrarMenuDirecto = mostrarMenuDirecto;
        this.estadoActual = EstadoMenu.PRINCIPAL;
    }

    public void setEstadoMenu(EstadoMenu estado) {
        this.estadoInicial = estado;
    }

    public PantallaMenu(JSonicJuego juegoApp) {
        this(juegoApp, false);
    }

    @Override
    public void inicializar() {
        // 1. Cargar todos los assets y crear los actores.
        texturaFondo = new Texture(Gdx.files.internal("Fondos/Portada.png"));
        textureTextoInicio = new Texture(Gdx.files.internal("Fondos/Texto_inicial.png"));
        atlasBotones = new TextureAtlas(Gdx.files.internal("Atlas/BotonesMenu.atlas"));


        imagenFondo = new Image(texturaFondo);
        imagenTextoInicio = new Image(textureTextoInicio);
        botonJugar = crearBotonConEstados("boton_jugar", "boton_jugar_down", "boton_jugar_hover");
        botonAcercaDe = crearBotonConEstados("boton_AcercaDe", "boton_acercade_down", "boton_acercade_hover");
        botonUnJugador = crearBotonConEstados("boton_1jugador", "boton_unjugador_down", "boton_unjugador_hover");
        botonMultijugador = crearBotonConEstados("boton_multijugador", "boton_multijugador_down", "boton_multijugador_hover");
        botonLocal = crearBotonConEstados("boton_local", "boton_local_down", "boton_local_hover");
        botonOnline = crearBotonConEstados("boton_online", "boton_online_down", "boton_online_hover");
        botonCrear = crearBotonConEstados("boton_crear", "boton_crear_down", "boton_crear_hover");
        botonUnirse = crearBotonConEstados("boton_unirse", "boton_unirse_down", "boton_unirse_hover");
        botonAtras = crearBoton("boton_atras");
        botonOpciones = crearBotonConEstados("boton_opciones", "boton_opciones_down", "boton_opciones_hover");
        botonSalir = crearBotonConEstados("boton_salir", "boton_salir_down", "boton_salir_hover");
        botonAyuda = crearBotonConEstados("boton_ayuda", "boton_ayuda_down", "boton_ayuda_hover");
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
            if (estadoInicial == EstadoMenu.OPCIONES) {
                mostrarMenuOpciones();
            } else if (estadoInicial == EstadoMenu.JUGAR) {
                mostrarMenuJugar();
            } else if (estadoInicial == EstadoMenu.MULTIJUGADOR) {
                mostrarMenuMultijugador();
            } else if (estadoInicial == EstadoMenu.CREAR_UNIRSE) {
                mostrarMenuCrearUnirse();
            }
            else {
                mostrarMenuPrincipal();
            }
        } else {
            imagenTextoInicio.setVisible(true);
            botonJugar.setVisible(false);
            botonOpciones.setVisible(false);
            botonSalir.setVisible(false);
            botonAcercaDe.setVisible(false);
            botonUnJugador.setVisible(false);
            botonMultijugador.setVisible(false);
            botonLocal.setVisible(false);
            botonOnline.setVisible(false);
            botonCrear.setVisible(false);
            botonUnirse.setVisible(false);
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
                if (estadoActual == EstadoMenu.CREAR_UNIRSE) {
                    mostrarMenuMultijugador();
                } else if (estadoActual == EstadoMenu.MULTIJUGADOR) {
                    mostrarMenuJugar();
                } else {
                    mostrarMenuPrincipal();
                }
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

                juegoApp.setPantallaActiva(new PantallaAyuda(juegoApp, PantallaMenu.this));
            }
        });

        botonUnJugador.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                JSonicJuego.personajesYaSeleccionados.clear();
                juegoApp.setPantallaActiva(new PantallaSeleccionPersonaje(juegoApp));
            }
        });
        botonMultijugador.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mostrarMenuMultijugador();
            }
        });
        botonOnline.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                juegoApp.direccionIp = "20.112.50.29";
                mostrarMenuCrearUnirse();
            }
        });

        botonLocal.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                juegoApp.direccionIp = "localhost";
                mostrarMenuCrearUnirse();
            }
        });

        botonCrear.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                juegoApp.setPantallaActiva(new PantallaCrearPartida(juegoApp));
            }
        });

        botonUnirse.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                juegoApp.setPantallaActiva(new PantallaUnirsePartida(juegoApp));
            }
        });
    }

    private void mostrarMenuPrincipal() {
        estadoActual = EstadoMenu.PRINCIPAL;
        uiTable.clear();
        uiTable.bottom().padBottom(40);
        uiTable.add(botonJugar).size(250, 125);
        uiTable.row();
        uiTable.add(botonOpciones).size(250, 125);
        uiTable.row();
        uiTable.add(botonSalir).size(250, 125);

        botonJugar.setVisible(true);
        botonOpciones.setVisible(true);
        botonSalir.setVisible(true);
        botonAcercaDe.setVisible(false);
        botonUnJugador.setVisible(false);
        botonLocal.setVisible(false);
        botonMultijugador.setVisible(false);
        botonOnline.setVisible(false);
        botonCrear.setVisible(false);
        botonUnirse.setVisible(false);
        botonAtras.setVisible(false);
        botonAyuda.setVisible(false);
    }

    private void mostrarMenuJugar() {
        estadoActual = EstadoMenu.JUGAR;
        uiTable.clear();
        uiTable.bottom().padBottom(40);
        uiTable.add(botonUnJugador).size(250, 125);
        uiTable.row();
        uiTable.add(botonMultijugador).size(250, 125);

        botonJugar.setVisible(false);
        botonOpciones.setVisible(false);
        botonSalir.setVisible(false);
        botonAcercaDe.setVisible(false);
        botonUnJugador.setVisible(true);
        botonMultijugador.setVisible(true);
        botonLocal.setVisible(false);
        botonOnline.setVisible(false);
        botonCrear.setVisible(false);
        botonUnirse.setVisible(false);
        botonAtras.setVisible(true);
        botonAyuda.setVisible(false);
    }

    private void mostrarMenuMultijugador() {
        estadoActual = EstadoMenu.MULTIJUGADOR;
        uiTable.clear();
        uiTable.bottom().padBottom(40);
        uiTable.add(botonLocal).size(250, 125);
        uiTable.row();
        uiTable.add(botonOnline).size(250, 125);

        botonJugar.setVisible(false);
        botonOpciones.setVisible(false);
        botonSalir.setVisible(false);
        botonAcercaDe.setVisible(false);
        botonUnJugador.setVisible(false);
        botonMultijugador.setVisible(false);
        botonLocal.setVisible(true);
        botonOnline.setVisible(true);
        botonCrear.setVisible(false);
        botonUnirse.setVisible(false);
        botonAtras.setVisible(true);
        botonAyuda.setVisible(false);
    }

    private void mostrarMenuCrearUnirse() {
        estadoActual = EstadoMenu.CREAR_UNIRSE;
        uiTable.clear();
        uiTable.bottom().padBottom(40);
        uiTable.add(botonCrear).size(250, 125);
        uiTable.row();
        uiTable.add(botonUnirse).size(250, 125);

        botonJugar.setVisible(false);
        botonOpciones.setVisible(false);
        botonSalir.setVisible(false);
        botonAcercaDe.setVisible(false);
        botonUnJugador.setVisible(false);
        botonMultijugador.setVisible(false);
        botonLocal.setVisible(false);
        botonOnline.setVisible(false);
        botonCrear.setVisible(true);
        botonUnirse.setVisible(true);
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
        botonLocal.setVisible(false);
        botonOnline.setVisible(false);
        botonAtras.setVisible(true);
        botonAyuda.setVisible(true);
    }

    private Button crearBoton(String nombreRegion) {
        Button.ButtonStyle estilo = new Button.ButtonStyle();
        estilo.up = new TextureRegionDrawable(atlasBotones.findRegion(nombreRegion));
        return new Button(estilo);
    }

    private Button crearBotonConEstados(String up, String down, String over) {
        Button.ButtonStyle estilo = new Button.ButtonStyle();
        estilo.up = new TextureRegionDrawable(atlasBotones.findRegion(up));
        estilo.down = new TextureRegionDrawable(atlasBotones.findRegion(down));
        estilo.over = new TextureRegionDrawable(atlasBotones.findRegion(over));
        return new Button(estilo);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (texturaFondo != null) texturaFondo.dispose();
        if (textureTextoInicio != null) textureTextoInicio.dispose();
        if (atlasBotones != null) atlasBotones.dispose();
    }
}
