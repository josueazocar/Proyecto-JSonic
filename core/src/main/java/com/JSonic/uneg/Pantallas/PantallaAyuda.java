// Archivo: src/com/JSonic/uneg/Pantallas/PantallaAyuda.java
package com.JSonic.uneg.Pantallas;

import com.JSonic.uneg.JSonicJuego;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.HashMap;
import java.util.List;

public class PantallaAyuda extends PantallaBase {

    public static final float VIRTUAL_WIDTH = 1920;
    public static final float VIRTUAL_HEIGHT = 1080;

    private final JSonicJuego juegoApp;
    private final Screen pantallaAnterior;
    private TextureAtlas textoComoJugarAtlas;
    private ShapeRenderer shapeRenderer;

    private BitmapFont fuenteBoton, fuenteReglas, fuenteStatsTitulo, fuenteStatsCuerpo, fuenteComoJugar;

    public PantallaAyuda(JSonicJuego juegoApp, Screen pantallaAnterior) {
        super();
        this.juegoApp = juegoApp;
        this.pantallaAnterior = pantallaAnterior;
        inicializar();
    }

    @Override
    public void inicializar() {
        mainStage = new Stage(new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT));
        shapeRenderer = new ShapeRenderer();
        textoComoJugarAtlas = new TextureAtlas(Gdx.files.internal("Atlas/comoJugar.atlas"));

        // --- Creación de Fuentes ---
        fuenteBoton = new BitmapFont(Gdx.files.internal("Fuentes/juego_fuente2.fnt"));
        fuenteReglas = new BitmapFont(Gdx.files.internal("Fuentes/juego_fuente2.fnt"));
        fuenteStatsTitulo = new BitmapFont(Gdx.files.internal("Fuentes/juego_fuente2.fnt"));
        fuenteStatsCuerpo = new BitmapFont(Gdx.files.internal("Fuentes/juego_fuente2.fnt"));
        fuenteComoJugar = new BitmapFont(Gdx.files.internal("Fuentes/juego_fuente2.fnt"));

        // --- Creación de Estilos ---
        TextButton.TextButtonStyle estiloBoton = new TextButton.TextButtonStyle(getSkin().get(TextButton.TextButtonStyle.class));
        estiloBoton.font = fuenteBoton;
        estiloBoton.font.getData().setScale(0.9f);

        final Label.LabelStyle estiloReglas = new Label.LabelStyle(getSkin().get("default", Label.LabelStyle.class));
        estiloReglas.font = fuenteReglas;
        estiloReglas.font.getData().setScale(0.9f);
        estiloReglas.font.getData().setLineHeight(estiloReglas.font.getLineHeight() * 1.5f);

        // --- Contenedor Principal (Wrapper) ---
        Table wrapper = new Table();
        wrapper.setFillParent(true);
        mainStage.addActor(wrapper);

        Table panelContenido = new Table();
        panelContenido.setBackground(getSkin().getDrawable("default-round"));
        panelContenido.pad(40);

        wrapper.add(panelContenido).width(1700).height(950).center();

        // --- Paneles Izquierdo y Derecho ---
        Table tablaOpciones = new Table();
        final Table panelDerecho = new Table();

        panelContenido.add(tablaOpciones).width(450).fillY();
        panelContenido.add(panelDerecho).expand().fill();

        // --- Contenido de Paneles ---
        TextButton btnComoJugar = new TextButton("Como jugar", estiloBoton);
        TextButton btnReglas = new TextButton("Reglas", estiloBoton);
        TextButton btnEstadisticas = new TextButton("Estadisticas", estiloBoton);
        TextButton btnSalir = new TextButton("Salir", estiloBoton);

        tablaOpciones.add(btnComoJugar).fillX().height(80).pad(20).row();
        tablaOpciones.add(btnReglas).fillX().height(80).pad(20).row();
        tablaOpciones.add(btnEstadisticas).fillX().height(80).pad(20).row();
        tablaOpciones.add(btnSalir).fillX().height(80).pad(20).row();

        final Label contenidoInicialLabel = new Label("Selecciona una opcion a la izquierda.", getSkin());
        contenidoInicialLabel.setFontScale(1.3f);
        contenidoInicialLabel.setWrap(true);
        panelDerecho.add(contenidoInicialLabel).expand().fill().pad(20);

        // --- Listeners ---
        btnSalir.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                juegoApp.reproducirSonidoClick();
                juegoApp.setScreen(pantallaAnterior);
            }
        });

        btnComoJugar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                juegoApp.reproducirSonidoClick();
                panelDerecho.clear();
                panelDerecho.add(construirPanelComoJugar()).expand().fill();
            }
        });

        btnEstadisticas.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                juegoApp.reproducirSonidoClick();
                panelDerecho.clear();
                List<EstadisticasJugador> stats = juegoApp.getEstadisticasUltimaPartida();
                if (stats != null && !stats.isEmpty()) {
                    panelDerecho.add(construirPanelEstadisticas(stats)).expand().fill();
                } else {
                    Label noStatsLabel = new Label("No hay estadisticas de la ultima partida disponibles.", getSkin());
                    noStatsLabel.setFontScale(1.2f);
                    noStatsLabel.setWrap(true);
                    noStatsLabel.setAlignment(Align.center);
                    panelDerecho.add(noStatsLabel).expand().fill().pad(20);
                }
            }
        });

        btnReglas.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                juegoApp.reproducirSonidoClick();
                panelDerecho.clear();
                String textoReglas = """
                    OBJETIVO PRINCIPAL: RESTAURACIÓN ECOLÓGICA

                    Tu misión es limpiar cada nivel de la contaminación generada por el Dr. Robotnik. Para ganar, debes recolectar basura, derrotar a sus robots y restaurar el equilibrio natural de cada zona.

                    EL CICLO DE LIMPIEZA

                    - Recolectar: Corre por el mapa para recoger toda la basura y piezas de plástico que encuentres.
                    - Combatir: Derrota a los robots para detener la fuente de contaminación y proteger a los animales de la zona.
                    - Progresar: Vence al jefe de cada zona para desbloquear el siguiente nivel y continuar tu aventura.

                    SISTEMA DE ANILLOS Y DAÑO

                    Los anillos son tu recurso más valioso para sobrevivir a los ataques.
                    - Daño con Anillos: Si un enemigo te golpea, recibirás un daño mínimo (1-2 puntos de vida).
                    - Daño SIN Anillos: Si te golpean con 0 anillos, ¡el golpe será crítico y perderás 15 puntos de vida!
                    - Vida Extra: Al recolectar 100 anillos, tu vida se restaurará por completo.

                    HABILIDADES DE PERSONAJES

                    Cada héroe contribuye de una manera única a la misión:

                    - Sonic: Su objetivo es encontrar las 7 Esmeraldas del Caos, ocultas una en cada mapa. Reunirlas le permitirá transformarse en Súper Sonic para la batalla final, volviéndose invencible y restaurando su vida. Además, puede usar una habilidad especial para limpiar toda la contaminación de golpe.

                    - Tails: Es el especialista en reciclaje. Siendo Tails, al llevar 5 o más piezas de basura a una planta de tratamiento, ¡todos los jugadores recuperarán 10 puntos de vida! También puede usar 20 piezas de basura para invocar un dron de apoyo o plantar un árbol que ayude al ecosistema.

                    - Knuckles: Gracias a su fuerza, puede destruir ciertos bloques y obstáculos, limpiando el camino y obteniendo los materiales de reciclaje que contienen.

                    PUNTUACIÓN

                    Ganas puntos por cada acción que ayuda al medio ambiente.
                    - Limpiar una Zona (Habilidad/Bloque): 100 Puntos
                    - Reciclar un Lote de Basura: 50 Puntos
                    - Derrotar un Enemigo: 20 Puntos
                    """;
                Label labelReglas = new Label(textoReglas, estiloReglas);
                labelReglas.setWrap(true);
                labelReglas.setAlignment(Align.left);
                Table tablaContenidoReglas = new Table();
                tablaContenidoReglas.add(labelReglas).expandX().fillX().pad(40);
                ScrollPane scrollPane = new ScrollPane(tablaContenidoReglas, getSkin());
                scrollPane.setFadeScrollBars(false);
                scrollPane.setScrollingDisabled(true, false);
                panelDerecho.add(scrollPane).expand().fill();
            }
        });
    }

    private ScrollPane construirPanelComoJugar() {
        Table contenidoComoJugar = new Table();
        Label.LabelStyle estiloPersonalizado = new Label.LabelStyle(getSkin().get("default", Label.LabelStyle.class));
        estiloPersonalizado.font = fuenteComoJugar;
        estiloPersonalizado.font.getData().setScale(1.3f);
        Label misionLabel = new Label("Pasa el raton sobre cada imagen para ver la accion correspondiente.", estiloPersonalizado);
        misionLabel.setWrap(true);
        misionLabel.setAlignment(Align.center);
        contenidoComoJugar.add(misionLabel).width(750).padBottom(20).center().row();

        String[] nombresImagenesPrincipales = {
            "pantalla_ayuda1", "pantalla_ayuda2", "pantalla_ayuda3",
            "pantalla_ayuda4", "pantalla_ayuda5", "pantalla_ayuda6"
        };
        HashMap<String, String> tooltipsMap = new HashMap<>();
        tooltipsMap.put("pantalla_ayuda3", "accion_K");
        tooltipsMap.put("pantalla_ayuda2", "accion_J");
        tooltipsMap.put("pantalla_ayuda5", "accion_L");
        tooltipsMap.put("pantalla_ayuda4", "accion_espacio");

        for (String nombreImagen : nombresImagenesPrincipales) {
            Image imgPrincipal = new Image(textoComoJugarAtlas.findRegion(nombreImagen));
            imgPrincipal.setScaling(Scaling.fit);

            if (tooltipsMap.containsKey(nombreImagen)) {
                String nombreImagenTooltip = tooltipsMap.get(nombreImagen);
                Image imagenTooltip = new Image(textoComoJugarAtlas.findRegion(nombreImagenTooltip));
                imagenTooltip.setScaling(Scaling.fit);

                // Envolvemos la imagen del tooltip en una tabla para controlar su tamaño
                Table tooltipTable = new Table();

                // Aplicamos los tamaños que tenías en tu código original
                if (nombreImagenTooltip.equals("accion_K")) {
                    tooltipTable.add(imagenTooltip).width(225);
                } else {
                    tooltipTable.add(imagenTooltip).width(600);
                }

                Tooltip<Table> tooltip = new Tooltip<>(tooltipTable);
                tooltip.setInstant(true);
                imgPrincipal.addListener(tooltip);
            }

            if (nombreImagen.equals("pantalla_ayuda1")) {
                contenidoComoJugar.add(imgPrincipal).pad(10).center().size(825, 165).row();
            } else {
                contenidoComoJugar.add(imgPrincipal).pad(10).center().size(675, 135).row();
            }
        }

        ScrollPane scrollPane = new ScrollPane(contenidoComoJugar, getSkin());
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        return scrollPane;
    }

    private ScrollPane construirPanelEstadisticas(List<EstadisticasJugador> stats) {
        stats.sort(java.util.Comparator.comparingInt(EstadisticasJugador::getPuntuacionTotal).reversed());
        Table tablaStats = new Table();

        Label.LabelStyle estiloTitulo = new Label.LabelStyle(fuenteStatsTitulo, Color.WHITE);
        fuenteStatsTitulo.getData().setScale(1.2f);
        Label.LabelStyle estiloEncabezado = new Label.LabelStyle(fuenteStatsCuerpo, Color.WHITE);
        fuenteStatsCuerpo.getData().setScale(1.0f);
        Label.LabelStyle estiloDatos = new Label.LabelStyle(fuenteStatsCuerpo, Color.LIGHT_GRAY);

        tablaStats.add(new Label("Resultados Ultima Partida", estiloTitulo)).colspan(2).center().padBottom(40).row();
        tablaStats.add(new Label("Jugador", estiloEncabezado)).padBottom(15).left();
        tablaStats.add(new Label("Puntuacion", estiloEncabezado)).padBottom(15).right().row();

        Image separador = new Image(getSkin().getDrawable("default-round-down")); // Usamos un drawable del skin
        tablaStats.add(separador).colspan(2).height(2).fillX().padBottom(10);
        tablaStats.row();

        for (EstadisticasJugador stat : stats) {
            tablaStats.add(new Label(stat.getNombreJugador(), estiloDatos)).pad(10).left();
            tablaStats.add(new Label(String.valueOf(stat.getPuntuacionTotal()), estiloDatos)).pad(10).right().row();
        }

        return new ScrollPane(tablaStats, getSkin());
    }

    @Override
    public void actualizar(float delta) {
        // La lógica de actualización del Stage ahora se maneja en el método render.
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(mainStage);
    }

    @Override
    public void resize(int width, int height) {
        mainStage.getViewport().update(width, height, true);
    }

    @Override
    public void render(float delta) {
        pantallaAnterior.render(0);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(mainStage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.5f);
        shapeRenderer.rect(0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        mainStage.act(Math.min(delta, 1 / 30f));
        mainStage.draw();
    }

    @Override
    public void dispose() {
        if (mainStage != null) mainStage.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (textoComoJugarAtlas != null) textoComoJugarAtlas.dispose();

        if (fuenteBoton != null) fuenteBoton.dispose();
        if (fuenteReglas != null) fuenteReglas.dispose();
        if (fuenteStatsTitulo != null) fuenteStatsTitulo.dispose();
        if (fuenteStatsCuerpo != null) fuenteStatsCuerpo.dispose();
        if (fuenteComoJugar != null) fuenteComoJugar.dispose();
    }
}
