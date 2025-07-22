package com.JSonic.uneg.Pantallas;

import com.JSonic.uneg.JSonicJuego;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.List;

public class PantallaAyuda extends PantallaBase {
    private final JSonicJuego juegoApp;
    private final Screen pantallaAnterior;
    private TextureAtlas textoComoJugarAtlas;
    private ShapeRenderer shapeRenderer;
    private Label.LabelStyle estiloReglasPersonalizado;

    public PantallaAyuda(JSonicJuego juegoApp, Screen pantallaAnterior) {
        super();
        this.juegoApp = juegoApp;
        this.pantallaAnterior = pantallaAnterior;
        inicializar();
    }

    @Override
    public void inicializar() {
        mainStage = new Stage(new ScreenViewport());
        shapeRenderer = new ShapeRenderer();
        textoComoJugarAtlas = new TextureAtlas(Gdx.files.internal("Atlas/comoJugar.atlas"));
        Table panelContenido = new Table();
        panelContenido.setBackground(getSkin().getDrawable("default-round"));
        panelContenido.pad(20);
        panelContenido.clip();
        Table wrapper = new Table();
        wrapper.setFillParent(true);
        mainStage.addActor(wrapper);
        wrapper.add(panelContenido).width(Gdx.graphics.getWidth() * 0.8f).height(Gdx.graphics.getHeight() * 0.8f);


        TextButton.TextButtonStyle smallStyle = new TextButton.TextButtonStyle(getSkin().get(TextButton.TextButtonStyle.class));
        smallStyle.font = new BitmapFont(Gdx.files.internal("Fuentes/juego_fuente2.fnt"));
        smallStyle.font.getData().setScale(0.7f);
        getSkin().add("small-text", smallStyle);

        Label.LabelStyle smallLabelStyle = new Label.LabelStyle(getSkin().get(Label.LabelStyle.class));
        smallLabelStyle.font = new BitmapFont(Gdx.files.internal("Fuentes/juego_fuente2.fnt"));
        smallLabelStyle.font.getData().setScale(0.75f);
        smallLabelStyle.font.getData().setLineHeight(smallLabelStyle.font.getLineHeight() * 2f);
        getSkin().add("small-label", smallLabelStyle);


        Table tablaOpciones = new Table();
        TextButton btnComoJugar = new TextButton("Como jugar", getSkin(), "small-text");
        TextButton btnEstadisticas = new TextButton("Ver Ult. Stats", getSkin(), "small-text");
        TextButton btnReglas = new TextButton("Reglas", getSkin(), "small-text");
        TextButton btnSalir = new TextButton("Salir", getSkin(), "small-text");

        tablaOpciones.add(btnComoJugar).size(280,50).pad(10).row();
        tablaOpciones.add(btnReglas).size(280,50).pad(10).row();
        tablaOpciones.add(btnEstadisticas).size(280, 50).pad(10).row();
        tablaOpciones.add(btnSalir).size(280,50).pad(10).row();
        tablaOpciones.center().right();



        final Table panelDerecho = new Table();
        final Label contenidoLabel = new Label("Selecciona una opcion a la izquierda.", getSkin());
        contenidoLabel.setWrap(true);
        panelDerecho.add(contenidoLabel).expand().fill().pad(20);


        btnSalir.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                juegoApp.setScreen(pantallaAnterior);
            }
        });

        btnComoJugar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                panelDerecho.clear();
                Table contenidoComoJugar = new Table();

                Label.LabelStyle estiloPersonalizado = new Label.LabelStyle(getSkin().get("default", Label.LabelStyle.class));
                estiloPersonalizado.font.getData().setScale(0.8f);
                Label misionLabel = new Label("Pasa el raton sobre cada imagen para ver la acción correspondiente.", estiloPersonalizado);
                misionLabel.setWrap(true);
                misionLabel.setAlignment(Align.center);
                contenidoComoJugar.add(misionLabel).width(600).pad(20).center().row();

                String[] nombresImagenesPrincipales = {
                    "pantalla_ayuda1", "pantalla_ayuda2", "pantalla_ayuda3",
                    "pantalla_ayuda4", "pantalla_ayuda5", "pantalla_ayuda6"
                };
                java.util.HashMap<String, String> tooltipsMap = new java.util.HashMap<>();
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

                        Table tooltipTable = new Table();

                        if (nombreImagenTooltip.equals("accion_K")) {
                            tooltipTable.add(imagenTooltip).width(150);
                        } else {
                            tooltipTable.add(imagenTooltip).width(400);
                        }

                        Tooltip<Table> tooltip = new Tooltip<>(tooltipTable);
                        tooltip.setInstant(true);
                        imgPrincipal.addListener(tooltip);
                    }

                    if (nombreImagen.equals("pantalla_ayuda1")) {
                        contenidoComoJugar.add(imgPrincipal).pad(10).center().size(550, 110).row();
                    } else {
                        contenidoComoJugar.add(imgPrincipal).pad(10).center().size(450, 90).row();
                    }
                }

                ScrollPane scrollPane = new ScrollPane(contenidoComoJugar, getSkin());
                scrollPane.setFadeScrollBars(false);
                scrollPane.setScrollingDisabled(true, false);
                scrollPane.getStyle().vScroll.setMinWidth(50);
                scrollPane.getStyle().vScrollKnob.setMinWidth(50);
                panelDerecho.add(scrollPane).expand().fill();
            }
        });

        btnEstadisticas.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Obtenemos las estadísticas guardadas desde la clase principal del juego
                List<EstadisticasJugador> stats =  juegoApp.getEstadisticasUltimaPartida();

                // Comprobamos si hay estadísticas guardadas
                if (stats != null && !stats.isEmpty()) {
                    // Si hay, creamos y mostramos la pantalla de estadísticas
                    juegoApp.setScreen(new PantallaEstadisticas(juegoApp, stats));
                } else {
                    // Si no hay (porque no se ha jugado ninguna partida), mostramos un mensaje
                    panelDerecho.clear();
                    Label noStatsLabel = new Label("No hay estadisticas de la ultima partida disponibles.", getSkin());
                    noStatsLabel.setWrap(true);
                    noStatsLabel.setAlignment(Align.center);
                    panelDerecho.add(noStatsLabel).expand().fill().pad(20);
                }
            }
        });

        btnReglas.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // 1. Limpiar el panel derecho
                panelDerecho.clear();

                if (estiloReglasPersonalizado == null) {
                    // A. Creamos un estilo nuevo, copiando el 'default'.
                    estiloReglasPersonalizado = new Label.LabelStyle(getSkin().get("default", Label.LabelStyle.class));

                    // B. Creamos una INSTANCIA DE FUENTE COMPLETAMENTE NUEVA para no modificar la original.
                    estiloReglasPersonalizado.font = new BitmapFont(Gdx.files.internal("Fuentes/juego_fuente2.fnt"));

                    // C. Ahora sí, modificamos esta nueva instancia de fuente de forma segura.
                    estiloReglasPersonalizado.font.getData().setScale(0.7f);
                    estiloReglasPersonalizado.font.getData().setLineHeight(estiloReglasPersonalizado.font.getLineHeight() * 2.5f);
                }

                // 3. 📜 Definir el texto de las reglas (limpio, sin caracteres especiales)
                String textoReglas =
                    "OBJETIVO PRINCIPAL: RESTAURACION ECOLOGICA\n\n" +
                        "Tu mision es limpiar cada nivel de la contaminacion generada por el Dr. Robotnik. " +
                        "Para ganar, debes recolectar basura, derrotar enemigos y restaurar el equilibrio natural de la zona.\n\n" +
                        "EL CICLO DE LIMPIEZA\n\n" +
                        "- Recolectar: Corre por el mapa para recoger toda la basura.\n" +
                        "- Reciclar: Lleva la basura a las plantas de tratamiento para procesarla y ganar puntos.\n" +
                        "- Combatir: Derrota a los robots para detener la fuente de contaminacion.\n\n" +
                        "SISTEMA DE VIDAS Y ANILLOS\n\n" +
                        "Los anillos dorados son tu proteccion. Si un enemigo te golpea teniendo al menos un anillo, solo perderas los anillos. " +
                        "Si te golpean sin anillos, perderas una vida. ¡Gestiona tus anillos con cuidado!\n\n" +
                        "PUNTUACION\n\n" +
                        "Ganas puntos por cada pieza de basura reciclada, cada enemigo derrotado y cada habilidad especial que uses para ayudar al medio ambiente.";

                // 4. Crear el Label
                Label labelReglas = new Label(textoReglas, estiloReglasPersonalizado);
                labelReglas.setWrap(true);
                labelReglas.setAlignment(com.badlogic.gdx.utils.Align.left);

                // 5. Crear la tabla que contendrá el Label
                Table tablaContenidoReglas = new Table();

                // ✨ 2. Añadimos padding en la parte superior y a los lados del texto
                tablaContenidoReglas.add(labelReglas)
                    .width(panelDerecho.getWidth() - 80) // Un poco menos de ancho para más margen lateral
                    .padTop(20f)  // Margen superior
                    .padLeft(10f) // Margen izquierdo
                    .padRight(10f); // Margen derecho

                // 6. Crear el ScrollPane
                ScrollPane scrollPane = new ScrollPane(tablaContenidoReglas, getSkin());
                scrollPane.setFadeScrollBars(false);
                scrollPane.setScrollingDisabled(true, false);
                scrollPane.getStyle().vScroll.setMinWidth(50);
                scrollPane.getStyle().vScrollKnob.setMinWidth(50);

                // 7. Añadir el ScrollPane al panel principal
                panelDerecho.add(scrollPane).expand().fill();
            }
        });

        float totalWidth = Gdx.graphics.getWidth() * 0.8f; // El ancho total del panel es el 80% de la pantalla.
        float leftPanelWidth = 320f; // Ancho fijo para el panel izquierdo de botones.
        float rightPanelWidth = totalWidth - leftPanelWidth; // El resto del espacio para el panel derecho.

        panelContenido.add(tablaOpciones).width(leftPanelWidth).fillY(); // Panel izquierdo con su ancho y llenado vertical.
        panelContenido.add(panelDerecho).width(rightPanelWidth).fill();   // Panel derecho con el ancho restante y llenado completo.
    }



    @Override
    public void actualizar(float delta) {
        mainStage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(mainStage);
    }

   @Override
    public void render(float delta) {
        pantallaAnterior.render(0); // 1. Dibuja la pantalla anterior
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.5f); // Negro con 50% de opacidad
        shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        mainStage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        mainStage.draw();
    }


    @Override
    public void dispose() {
        mainStage.dispose();
        shapeRenderer.dispose();
        textoComoJugarAtlas.dispose();
        getSkin().get("small-text", TextButton.TextButtonStyle.class).font.dispose();
        getSkin().get("small-label", Label.LabelStyle.class).font.dispose();
    }
}
