package com.JSonic.uneg.Pantallas;

import com.JSonic.uneg.JSonicJuego;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import network.LocalServer;
import network.Network;

import java.util.HashMap;
import java.util.List;

// Es un Table, diseñado para superponerse en la pantalla de juego.
public class PauseMenuUI extends Table {

    private final JSonicJuego juegoApp;
    private final PantallaDeJuego pantallaJuego; // Referencia para llamar a los métodos de pausa
    private final TextureAtlas textoComoJugarAtlas;
    private Label.LabelStyle estiloReglasPersonalizado;

    // ¡IMPORTANTE! Esta es la clave para corregir el tamaño de la fuente.
    // Es una escala más pequeña para compensar el Viewport del juego.
    // Puede que necesites ajustar este valor (entre 0.3f y 0.5f) hasta que se vea perfecto.
    private static final float PAUSE_FONT_SCALE = 0.5f;

    public PauseMenuUI(JSonicJuego juego, PantallaDeJuego pantalla, Skin skin) {
        super(skin);
        this.juegoApp = juego;
        this.pantallaJuego = pantalla;
        this.textoComoJugarAtlas = new TextureAtlas(Gdx.files.internal("Atlas/comoJugar.atlas"));

        this.setBackground(skin.getDrawable("default-round"));
        this.pad(20);
        this.clip();

        inicializarUI(skin);
    }

    private void inicializarUI(Skin skin) {
        final Table panelDerecho = new Table();
        Label.LabelStyle estiloInicial = new Label.LabelStyle(skin.getFont("body-font"), null);
        estiloInicial.font = new BitmapFont(Gdx.files.internal("Fuentes/juego_fuente2.fnt"));
        estiloInicial.font.getData().setScale(PAUSE_FONT_SCALE );
        panelDerecho.add(new Label("Juego en Pausa", estiloInicial)).expand().center();

        Table panelIzquierdo = crearPanelIzquierdo(skin, panelDerecho);

        this.add(panelIzquierdo).width(320f).fillY();
        this.add(panelDerecho).expand().fill();
    }

    private Table crearPanelIzquierdo(Skin skin, final Table panelDerecho) {
        Table tablaOpciones = new Table();

        TextButton.TextButtonStyle estiloBoton = new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
        estiloBoton.font = new BitmapFont(Gdx.files.internal("Fuentes/juego_fuente2.fnt"));
        // Aplicamos la escala corregida
        estiloBoton.font.getData().setScale(PAUSE_FONT_SCALE);

        // Creación de botones
        TextButton btnReanudar = new TextButton("Reanudar", estiloBoton);
        TextButton btnComoJugar = new TextButton("Como jugar", estiloBoton);
        TextButton btnReglas = new TextButton("Reglas", estiloBoton);
        TextButton btnEstadisticas = new TextButton("Ver Ult. Stats", estiloBoton);
        TextButton btnSalir = new TextButton("Salir al Menu", estiloBoton);

        // Asignación de listeners
        btnReanudar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                pantallaJuego.togglePause();
            }
        });

        btnSalir.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (juegoApp.getClient() != null) {
                    juegoApp.getClient().send(new Network.PaqueteSalidaDePartida());
                    juegoApp.getClient().disconnect();
                }

                PantallaMenu pantallaMenu = new PantallaMenu(juegoApp, true);
                pantallaMenu.setEstadoMenu(PantallaMenu.EstadoMenu.JUGAR);
                juegoApp.setPantallaActiva(pantallaMenu);

                if (LocalServer.class != null) {
                    LocalServer.decreaseContamination(100);
                }
            }
        });

        // El resto de listeners son idénticos a los de AyudaUI, pero usan la escala de fuente corregida
        btnComoJugar.addListener(createComoJugarListener(panelDerecho));
        btnReglas.addListener(createReglasListener(panelDerecho));
        btnEstadisticas.addListener(createEstadisticasListener(panelDerecho, skin));

        // Añadir botones a la tabla
        tablaOpciones.add(btnReanudar).size(280, 50).pad(10).row();
        tablaOpciones.add(btnComoJugar).size(280, 50).pad(10).row();
        tablaOpciones.add(btnReglas).size(280, 50).pad(10).row();
        tablaOpciones.add(btnEstadisticas).size(280, 50).pad(10).row();
        tablaOpciones.add(btnSalir).size(280, 50).pad(10).row();

        return tablaOpciones;
    }

    // Los métodos para crear listeners y contenido son iguales, pero asegurándose de usar la escala correcta
    private ClickListener createEstadisticasListener(final Table panelDerecho, final Skin skin) {
        return new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Obtenemos las estadísticas guardadas
                List<EstadisticasJugador> stats = juegoApp.getEstadisticasUltimaPartida();

                // Limpiamos el panel derecho para mostrar el nuevo contenido
                panelDerecho.clear();

                if (stats != null && !stats.isEmpty()) {
                    // Si hay estadísticas, llamamos a un nuevo método que construye la tabla
                    // y la añade al panel derecho.
                    panelDerecho.add(construirPanelEstadisticas(stats)).expand().fill();
                } else {
                    Label.LabelStyle estiloMsg = new Label.LabelStyle();
                    estiloMsg.font = new BitmapFont(Gdx.files.internal("Fuentes/juego_fuente2.fnt"));
                    estiloMsg.font.getData().setScale(PAUSE_FONT_SCALE);
                    Label noStatsLabel = new Label("No hay estadisticas de la ultima partida disponibles.", estiloMsg);
                    noStatsLabel.setWrap(true);
                    noStatsLabel.setAlignment(Align.center);
                    panelDerecho.add(noStatsLabel).expand().fill().pad(20);
                }
            }
        };
    }
    // --- Métodos Creadores de Listeners para PauseMenuUI ---

    private ClickListener createComoJugarListener(final Table panelDerecho) {
        return new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                panelDerecho.clear();
                // Llama al método que construye el contenido visual
                panelDerecho.add(construirContenidoComoJugar()).expand().fill();
            }
        };
    }

    private ClickListener createReglasListener(final Table panelDerecho) {
        return new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                panelDerecho.clear();
                // Llama al método que construye el contenido visual
                panelDerecho.add(construirContenidoReglas()).expand().fill();
            }
        };
    }

// --- Métodos Constructores de Contenido para PauseMenuUI ---

    private ScrollPane construirContenidoComoJugar() {
        Table contenidoComoJugar = new Table();

        // Estilo y creación del label de instrucciones CON LA ESCALA CORREGIDA
        Label.LabelStyle estiloPersonalizado = new Label.LabelStyle(getSkin().get("default", Label.LabelStyle.class));
        // ¡IMPORTANTE! Se crea una nueva instancia de la fuente para no afectar a otros labels
        estiloPersonalizado.font = new BitmapFont(Gdx.files.internal("Fuentes/juego_fuente2.fnt"));
        estiloPersonalizado.font.getData().setScale(PAUSE_FONT_SCALE * 1.1f); // Un poco más grande que los botones

        Label misionLabel = new Label("Pasa el raton sobre cada imagen para ver la accion correspondiente.", estiloPersonalizado);
        misionLabel.setWrap(true);
        misionLabel.setAlignment(Align.center);
        contenidoComoJugar.add(misionLabel).width(600).pad(20).center().row();

        // Nombres de las imágenes a mostrar
        String[] nombresImagenesPrincipales = {
            "pantalla_ayuda1", "pantalla_ayuda2", "pantalla_ayuda3",
            "pantalla_ayuda4", "pantalla_ayuda5", "pantalla_ayuda6"
        };

        // Mapa para asociar imágenes con sus tooltips
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

        return scrollPane;
    }

    private ScrollPane construirContenidoReglas() {
        // Si el estilo no ha sido creado, lo creamos con la escala corregida
        if (estiloReglasPersonalizado == null) {
            estiloReglasPersonalizado = new Label.LabelStyle(getSkin().get("default", Label.LabelStyle.class));
            estiloReglasPersonalizado.font = new BitmapFont(Gdx.files.internal("Fuentes/juego_fuente2.fnt"));
            // Aplicamos la escala corregida
            estiloReglasPersonalizado.font.getData().setScale(PAUSE_FONT_SCALE);
            estiloReglasPersonalizado.font.getData().setLineHeight(estiloReglasPersonalizado.font.getLineHeight() * 2.5f);
        }

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

        Label labelReglas = new Label(textoReglas, estiloReglasPersonalizado);
        labelReglas.setWrap(true);
        labelReglas.setAlignment(Align.left);

        Table tablaContenido = new Table();
        tablaContenido.add(labelReglas).expandX().fillX().pad(20f).padLeft(10f).padRight(10f);

        ScrollPane scrollPane = new ScrollPane(tablaContenido, getSkin());
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.getStyle().vScroll.setMinWidth(50);
        scrollPane.getStyle().vScrollKnob.setMinWidth(50);

        return scrollPane;
    }

    private ScrollPane construirPanelEstadisticas(List<EstadisticasJugador> stats) {

        // 1. Ordenamos las estadísticas de mayor a menor puntuación
        stats.sort(java.util.Comparator.comparingInt(EstadisticasJugador::getPuntuacionTotal).reversed());

        Table tablaStats = new Table();

        // 2. Creamos los estilos para las fuentes, similar a PantallaEstadisticas
        //    Es importante crear nuevas instancias para no afectar otros estilos del skin.
        BitmapFont fuenteTitulo = new BitmapFont(Gdx.files.internal("Fuentes/juego_fuente2.fnt"));
        fuenteTitulo.getData().setScale(0.8f); // Un poco más pequeño para que quepa bien
        Label.LabelStyle estiloTitulo = new Label.LabelStyle(fuenteTitulo, com.badlogic.gdx.graphics.Color.WHITE);

        BitmapFont fuenteCuerpo = new BitmapFont(Gdx.files.internal("Fuentes/juego_fuente2.fnt"));
        fuenteCuerpo.getData().setScale(0.7f);
        Label.LabelStyle estiloEncabezado = new Label.LabelStyle(fuenteCuerpo, com.badlogic.gdx.graphics.Color.WHITE);
        Label.LabelStyle estiloDatos = new Label.LabelStyle(fuenteCuerpo, com.badlogic.gdx.graphics.Color.LIGHT_GRAY);

        // 3. Añadimos el Título
        tablaStats.add(new Label("Resultados Ultima Partida", estiloTitulo)).colspan(2).center().padBottom(30);
        tablaStats.row();

        // 4. Añadimos los Encabezados
        tablaStats.add(new Label("Jugador", estiloEncabezado)).padBottom(10).left();
        tablaStats.add(new Label("Puntuacion", estiloEncabezado)).padBottom(10).right();
        tablaStats.row();

        // 5. Añadimos una línea separadora (opcional pero estético)
        Image separador = new Image(getSkin().getDrawable("default-round-down")); // Usamos un drawable del skin
        tablaStats.add(separador).colspan(2).height(2).fillX().padBottom(10);
        tablaStats.row();

        // 6. Recorremos las estadísticas y añadimos una fila por cada jugador
        for (EstadisticasJugador stat : stats) {
            tablaStats.add(new Label(stat.getNombreJugador(), estiloDatos)).pad(5).left();
            tablaStats.add(new Label(String.valueOf(stat.getPuntuacionTotal()), estiloDatos)).pad(5).right();
            tablaStats.row();
        }

        // 7. Envolvemos la tabla en un ScrollPane por si la lista es muy larga
        ScrollPane scrollPane = new ScrollPane(tablaStats, getSkin());
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        return scrollPane;
    }


    public void dispose() {
        textoComoJugarAtlas.dispose();
    }
}
