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

/**
 * Pantalla principal del menú que proporciona navegación entre opciones como jugar, opciones y multijugador.
 */
public class PantallaMenu extends PantallaBase {

    private final JSonicJuego juegoApp;
    private Texture texturaFondo, textureTextoInicio, texturaFondoInicio;
    private TextureAtlas atlasBotones;
    private Image imagenFondo, imagenTextoInicio, imagenFondoDesenfoque;
    private Button botonJugar, botonAcercaDe, botonUnJugador, botonOnline, botonLocal, botonMultijugador, botonAtras, botonSalir, botonOpciones, botonAyuda, botonCrear, botonUnirse;
    private final boolean mostrarMenuDirecto;
    private boolean logicaInicializada = false; // Bandera para controlar la inicialización
    private EstadoMenu estadoInicial = EstadoMenu.PRINCIPAL;
    private EstadoMenu estadoActual;

    public enum EstadoMenu {
        PRINCIPAL, JUGAR, OPCIONES, MULTIJUGADOR, CREAR_UNIRSE
    }

    /**
     * Constructor del menú principal.
     * @param juegoApp instancia de JSonicJuego.
     * @param mostrarMenuDirecto si omite la pantalla de inicio y muestra directamente el submenú.
     */
    public PantallaMenu(JSonicJuego juegoApp, boolean mostrarMenuDirecto) {
        super();
        this.juegoApp = juegoApp;
        this.mostrarMenuDirecto = mostrarMenuDirecto;
        this.estadoActual = EstadoMenu.PRINCIPAL;
        inicializar();
    }

    /**
     * Establece el submenú inicial a mostrar al abrir el menú.
     * @param estado el estado de menú a mostrar.
     */
    public void setEstadoMenu(EstadoMenu estado) {
        this.estadoInicial = estado;
    }

    public PantallaMenu(JSonicJuego juegoApp) {
        this(juegoApp, false);
    }

    /**
     * Inicializa recursos gráficos y crea los actores del menú.
     */
    @Override
    public void inicializar() {
        // Carga todos los assets y crea los actores.
        texturaFondo = new Texture(Gdx.files.internal("Fondos/Portada.png"));
        texturaFondoInicio = new Texture(Gdx.files.internal("Fondos/Portada_desenfoque.png"));
        textureTextoInicio = new Texture(Gdx.files.internal("Fondos/Texto_inicial.png"));
        atlasBotones = new TextureAtlas(Gdx.files.internal("Atlas/botonesMenu.atlas"));


        imagenFondo = new Image(texturaFondoInicio);

        imagenTextoInicio = new Image(textureTextoInicio);
        botonJugar = crearBotonConEstados("boton_jugar", "boton_jugar_down", "boton_jugar_hover");
        botonAcercaDe = crearBotonConEstados("boton_AcercaDe", "boton_acercade_down", "boton_acercade_hover");
        botonUnJugador = crearBotonConEstados("boton_1jugador", "boton_unjugador_down", "boton_unjugador_hover");
        botonMultijugador = crearBotonConEstados("boton_multijugador", "boton_multijugador_down", "boton_multijugador_hover");
        botonLocal = crearBotonConEstados("boton_local", "boton_local_down", "boton_local_hover");
        botonOnline = crearBotonConEstados("boton_online", "boton_online_down", "boton_online_hover");
        botonCrear = crearBotonConEstados("boton_crear", "boton_crear_down", "boton_crear_hover");
        botonUnirse = crearBotonConEstados("boton_unirse", "boton_unirse_down", "boton_unirse_hover");
        botonAtras = crearBotonConEstados("boton_atras", "boton_atras_down", "boton_atras_hover");
        botonOpciones = crearBotonConEstados("boton_opciones", "boton_opciones_down", "boton_opciones_hover");
        botonSalir = crearBotonConEstados("boton_salir", "boton_salir_down", "boton_salir_hover");
        botonAyuda = crearBotonConEstados("boton_ayuda", "boton_ayuda_down", "boton_ayuda_hover");
    }

    /**
     * Actualiza la lógica de animación y navegación del menú.
     * @param delta tiempo transcurrido desde el último frame en segundos.
     */
    @Override
    public void actualizar(float delta) {
        // La lógica de configuración se ejecuta solo una vez en el primer fotograma.
        if (!logicaInicializada) {
            configurarEscena();
            logicaInicializada = true;
        }

        // Lógica de actualización continua
        if (imagenTextoInicio.isVisible() && Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {

            juegoApp.getSoundManager().stopBackgroundMusic();

            juegoApp.setPantallaActiva(new PantallaHistoria(juegoApp, juegoApp.getSoundManager()));

        }
    }

    /**
     * Configura la escena inicial del menú, colocando el fondo y los botones según el estado.
     */
    private void configurarEscena() {
        imagenFondo.setSize(mainStage.getWidth(), mainStage.getHeight());
        mainStage.addActor(imagenFondo);

        float textoAncho = 600f;
        float textoAlto = textoAncho * textureTextoInicio.getHeight() / textureTextoInicio.getWidth();
        imagenTextoInicio.setSize(textoAncho, textoAlto);
        imagenTextoInicio.setPosition((mainStage.getWidth() - textoAncho) / 2, 70f);
        mainStage.addActor(imagenTextoInicio);

        // Configurar visibilidad inicial
        if (mostrarMenuDirecto) {
            imagenFondo.setDrawable(new TextureRegionDrawable(texturaFondo));
            imagenTextoInicio.setVisible(false);
            if (estadoInicial == EstadoMenu.OPCIONES) {
                mostrarMenuOpciones();
            } else if (estadoInicial == EstadoMenu.JUGAR) {
                mostrarMenuJugar();
            } else if (estadoInicial == EstadoMenu.MULTIJUGADOR) {
                mostrarMenuMultijugador();
            } else if (estadoInicial == EstadoMenu.CREAR_UNIRSE) {
                mostrarMenuCrearUnirse();
            } else {
                mostrarMenuPrincipal();
            }
        } else {
            imagenFondo.setDrawable(new TextureRegionDrawable(texturaFondoInicio));
            imagenTextoInicio.setVisible(true);
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

        imagenFondo.setSize(mainStage.getWidth(), mainStage.getHeight());
        mainStage.addActor(imagenFondo);
        configurarListeners();

        Table tablaAtras = new Table();
        tablaAtras.setFillParent(true);
        uiStage.addActor(tablaAtras);
        tablaAtras.top().left();
        tablaAtras.add(botonAtras).size(105, 105).pad(20);
    }

    /**
     * Configura los listeners de eventos para los botones del menú.
     */
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

        botonAyuda.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                juegoApp.setPantallaActiva(new PantallaAyuda(juegoApp, PantallaMenu.this));
            }
        });

        botonUnJugador.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                JSonicJuego.personajesYaSeleccionados.clear();
                JSonicJuego.modoMultijugador = false;
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
                JSonicJuego.modoMultijugador = true;
                mostrarMenuCrearUnirse();
            }
        });

        botonLocal.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                JSonicJuego.modoMultijugador = true;
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

    /**
     * Muestra el menú principal con los botones de Jugar, Opciones y Salir.
     */
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

    /**
     * Muestra el submenú Jugar con opciones de un jugador o multijugador.
     */
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

    /**
     * Muestra el submenú Multijugador con opciones local u online.
     */
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

    /**
     * Muestra el submenú para crear o unirse a una partida online.
     */
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


    /**
     * Muestra el submenú Opciones con las opciones Acerca De y Ayuda.
     */
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


    /**
     * Crea un botón con diferentes estados gráficos y reproduce un sonido al hacer click.
     * @param up región para estado normal.
     * @param down región para estado presionado.
     * @param over región para estado hover.
     * @return el botón configurado.
     */
    private Button crearBotonConEstados(String up, String down, String over) {
        Button.ButtonStyle estilo = new Button.ButtonStyle();
        estilo.up = new TextureRegionDrawable(atlasBotones.findRegion(up));
        estilo.down = new TextureRegionDrawable(atlasBotones.findRegion(down));
        estilo.over = new TextureRegionDrawable(atlasBotones.findRegion(over));
        Button boton = new Button(estilo);

        boton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                juegoApp.reproducirSonidoClick();
            }
        });

        return boton;
    }

    /**
     * Libera los recursos gráficos del menú, como texturas y atlas.
     */
    @Override
    public void dispose() {
        super.dispose();
        if (texturaFondo != null) texturaFondo.dispose();
        if (textureTextoInicio != null) textureTextoInicio.dispose();
        if (texturaFondoInicio != null) texturaFondoInicio.dispose();
        if (atlasBotones != null) atlasBotones.dispose();
    }
}
