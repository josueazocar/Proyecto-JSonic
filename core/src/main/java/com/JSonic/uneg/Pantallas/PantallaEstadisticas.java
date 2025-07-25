package com.JSonic.uneg.Pantallas;

import com.JSonic.uneg.JSonicJuego;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import network.LocalServer;
import network.Network;

import java.util.Comparator;
import java.util.List;

/**
 * Pantalla para mostrar las estadísticas de los jugadores en orden descendente de puntuación.
 */
public class PantallaEstadisticas extends PantallaBase {
    private List<EstadisticasJugador> listaEstadisticas;
    private Texture texturaFondo;
    private JSonicJuego juegoApp;

    /**
     * Constructor de PantallaEstadisticas.
     *
     * @param juego        instancia del juego JSonic.
     * @param estadisticas lista de estadísticas de los jugadores a mostrar.
     */
    public PantallaEstadisticas(JSonicJuego juego, List<EstadisticasJugador> estadisticas) {
        super();
        this.juegoApp = juego;
        this.listaEstadisticas = estadisticas;
        this.listaEstadisticas.sort(
            Comparator.comparingInt(EstadisticasJugador::getPuntuacionTotal).reversed()
        );
    }

    /**
     * Configura y muestra los elementos visuales de la pantalla de estadísticas.
     */
    @Override
    public void show() {
        super.show();
        Gdx.input.setInputProcessor(mainStage);
        mainStage.clear();
        uiTable.clear();

        // --- Diseño con Fondo y Panel ---
        texturaFondo = new Texture(Gdx.files.internal("Fondos/Portada_desenfoque.png"));
        Image imagenFondoPrincipal = new Image(texturaFondo);
        imagenFondoPrincipal.setSize(mainStage.getWidth(), mainStage.getHeight());
        mainStage.addActor(imagenFondoPrincipal);

        // --- PANEL DE ESTADÍSTICAS ---
        Table panelContenido = new Table();
        panelContenido.setBackground(skin.getDrawable("default-round"));
        Table tablaStats = new Table();

        // Estilos de fuente...
        BitmapFont fuenteTitulo = new BitmapFont(Gdx.files.internal("Fuentes/juego_fuente2.fnt"));
        fuenteTitulo.getData().setScale(1.1f);
        Label.LabelStyle estiloTitulo = new Label.LabelStyle(fuenteTitulo, Color.WHITE);
        Label.LabelStyle estiloEncabezado = new Label.LabelStyle(skin.getFont("body-font"), Color.WHITE);
        estiloEncabezado.font.getData().setScale(0.85f);
        Label.LabelStyle estiloDatos = new Label.LabelStyle(skin.getFont("body-font"), Color.GOLD);
        estiloDatos.font.getData().setScale(0.8f);

        // Contenido de la tabla de estadísticas...
        tablaStats.add(new Label("Estadisticas", estiloTitulo)).colspan(2).center().padTop(-300);
        tablaStats.row();
        tablaStats.add(new Label("Jugador", estiloEncabezado)).padTop(-160).padBottom(20);
        tablaStats.add(new Label("Puntuacion Final", estiloEncabezado)).padTop(-160).padBottom(20).padLeft(80);
        tablaStats.row();
        for (EstadisticasJugador stats : listaEstadisticas) {
            tablaStats.add(new Label(stats.getNombreJugador(), estiloDatos)).center();
            tablaStats.add(new Label(String.valueOf(stats.getPuntuacionTotal()), estiloDatos)).padLeft(100);
            tablaStats.getCells().peek().padBottom(20); // Celda de Puntuación
            tablaStats.getCells().get(tablaStats.getCells().size - 2).padBottom(20);
            tablaStats.row();
        }

        // --- BOTÓN DE VOLVER ---
        TextButton.TextButtonStyle estiloBotonPequeno = new TextButton.TextButtonStyle(skin.get("default", TextButton.TextButtonStyle.class));
        estiloBotonPequeno.font = new BitmapFont(Gdx.files.internal("Fuentes/juego_fuente2.fnt"));
        estiloBotonPequeno.font.getData().setScale(0.55f);
        TextButton botonVolver = new TextButton("Volver al Menu", estiloBotonPequeno);
        botonVolver.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (juegoApp.getClient() != null) {
                    juegoApp.getClient().send(new Network.PaqueteSalidaDePartida());
                    juegoApp.getClient().disconnect();
                }
                PantallaMenu pantallaMenu = new PantallaMenu(juegoApp, true);
                pantallaMenu.setEstadoMenu(PantallaMenu.EstadoMenu.PRINCIPAL);
                juegoApp.setPantallaActiva(pantallaMenu);
                if (LocalServer.class != null) {
                    LocalServer.decreaseContamination(100);
                }
            }
        });

        // Añadimos la tabla de stats al panel contenedor
        panelContenido.add(tablaStats).expand().fill().pad(20);


        // --- Posición del Panel de Estadísticas ---
        float anchoDelPanel = 950;
        float altoDelPanel = 500;
        panelContenido.setSize(anchoDelPanel, altoDelPanel);
        float panelPosX = (mainStage.getWidth() / 2) - (anchoDelPanel / 2);
        float panelPosY = (mainStage.getHeight() / 2) - (altoDelPanel / 2);
        panelContenido.setPosition(panelPosX, panelPosY);
        mainStage.addActor(panelContenido);

        // --- Posición del Botón Volver ---
        float anchoBoton = 300;
        float altoBoton = 60;
        botonVolver.setSize(anchoBoton, altoBoton);
        float botonPosX = (mainStage.getWidth() / 2) - (anchoBoton / 2);
        float botonPosY = 150; // Margen de 50px desde abajo
        botonVolver.setPosition(botonPosX, botonPosY);
        mainStage.addActor(botonVolver);
    }

    /**
     * Libera los recursos utilizados por esta pantalla, como la textura de fondo.
     */
    @Override
    public void dispose() {
        super.dispose();
        if (texturaFondo != null) {
            texturaFondo.dispose();
        }
    }

    /**
     * Inicializa los elementos de la pantalla (vacío en esta implementación).
     */
    @Override
    public void inicializar() {
        // Se mantiene vacío
    }

    /**
     * Actualiza la lógica de la pantalla cada frame (vacío en esta implementación).
     *
     * @param deltat tiempo transcurrido desde el último frame en segundos.
     */
    @Override
    public void actualizar(float deltat) {
        // Se mantiene vacío
    }
}
