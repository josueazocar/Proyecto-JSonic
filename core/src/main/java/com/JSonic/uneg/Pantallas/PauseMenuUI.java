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

/**
 * Menú de pausa que permite reanudar, ver cómo jugar, reglas, estadísticas y salir al menú principal.
 * Se extiende de Table para organizar los componentes en dos paneles.
 */
public class PauseMenuUI extends Table {

    private final JSonicJuego juegoApp;
    private final PantallaDeJuego pantallaJuego; // Referencia para llamar a los métodos de pausa
    private final TextureAtlas textoComoJugarAtlas;
    private Label.LabelStyle estiloReglasPersonalizado;
    private static final float PAUSE_FONT_SCALE = 0.5f;

    /**
     * Constructor del menú de pausa.
     * @param juego instancia de JSonicJuego para controlar sonidos y navegación.
     * @param pantalla instancia de PantallaDeJuego para controlar la pausa.
     * @param skin skin para estilos de UI.
     */
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

    /**
     * Inicializa la interfaz de usuario del menú de pausa, creando paneles y estableciendo estilos.
     * @param skin skin para estilos de UI.
     */
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

    /**
     * Crea el panel izquierdo con los botones de opciones, asignando listeners para cada acción.
     * @param skin skin para estilos de botones.
     * @param panelDerecho panel derecho donde se mostrarán contenidos dinámicos.
     * @return tabla con los botones de opciones.
     */
    private Table crearPanelIzquierdo(Skin skin, final Table panelDerecho) {
        Table tablaOpciones = new Table();

        TextButton.TextButtonStyle estiloBoton = new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
        estiloBoton.font = new BitmapFont(Gdx.files.internal("Fuentes/juego_fuente2.fnt"));
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
                juegoApp.reproducirSonidoClick();
                pantallaJuego.togglePause();
            }
        });

        btnSalir.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                juegoApp.reproducirSonidoClick();
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

    /**
     * Crea un listener para mostrar estadísticas de la última partida en el panel derecho.
     * @param panelDerecho panel donde se mostrará el contenido.
     * @param skin skin para estilos de UI.
     * @return listener que maneja el clic en el botón de estadísticas.
     */
    private ClickListener createEstadisticasListener(final Table panelDerecho, final Skin skin) {
        return new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                juegoApp.reproducirSonidoClick();
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

    /**
     * Crea un listener para mostrar el contenido de 'Cómo jugar' en el panel derecho.
     * @param panelDerecho panel donde se mostrará el contenido.
     * @return listener que maneja el clic en el botón de 'Cómo jugar'.
     */
    private ClickListener createComoJugarListener(final Table panelDerecho) {
        return new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                juegoApp.reproducirSonidoClick();
                panelDerecho.clear();
                // Llama al método que construye el contenido visual
                panelDerecho.add(construirContenidoComoJugar()).expand().fill();
            }
        };
    }

    /**
     * Crea un listener para mostrar el contenido de reglas en el panel derecho.
     * @param panelDerecho panel donde se mostrará el contenido.
     * @return listener que maneja el clic en el botón de reglas.
     */
    private ClickListener createReglasListener(final Table panelDerecho) {
        return new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                juegoApp.reproducirSonidoClick();
                panelDerecho.clear();
                // Llama al método que construye el contenido visual
                panelDerecho.add(construirContenidoReglas()).expand().fill();
            }
        };
    }

// --- Métodos Constructores de Contenido para PauseMenuUI ---

    /**
     * Construye el contenido de 'Cómo jugar' con imágenes y tooltips, envuelto en un ScrollPane.
     * @return ScrollPane con el contenido de ayuda.
     */
    private ScrollPane construirContenidoComoJugar() {
        Table contenidoComoJugar = new Table();

        Label.LabelStyle estiloPersonalizado = new Label.LabelStyle(getSkin().get("default", Label.LabelStyle.class));

        estiloPersonalizado.font = new BitmapFont(Gdx.files.internal("Fuentes/juego_fuente2.fnt"));
        estiloPersonalizado.font.getData().setScale(PAUSE_FONT_SCALE * 1.1f);

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

    /**
     * Construye el contenido de reglas con texto explicativo, envuelto en un ScrollPane.
     * @return ScrollPane con las reglas del juego.
     */
    private ScrollPane construirContenidoReglas() {
        if (estiloReglasPersonalizado == null) {
            estiloReglasPersonalizado = new Label.LabelStyle(getSkin().get("default", Label.LabelStyle.class));
            estiloReglasPersonalizado.font = new BitmapFont(Gdx.files.internal("Fuentes/juego_fuente2.fnt"));
            estiloReglasPersonalizado.font.getData().setScale(PAUSE_FONT_SCALE);
            estiloReglasPersonalizado.font.getData().setLineHeight(estiloReglasPersonalizado.font.getLineHeight() * 2.5f);
        }

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

    /**
     * Construye el panel de estadísticas de la última partida, ordenado por puntuación, envuelto en un ScrollPane.
     * @param stats lista de estadísticas de jugadores.
     * @return ScrollPane con la tabla de estadísticas.
     */
    private ScrollPane construirPanelEstadisticas(List<EstadisticasJugador> stats) {

        //Ordenamos las estadísticas de mayor a menor puntuación
        stats.sort(java.util.Comparator.comparingInt(EstadisticasJugador::getPuntuacionTotal).reversed());

        Table tablaStats = new Table();

        // Creamos los estilos para las fuentes, similar a PantallaEstadisticas
        //  Es importante crear nuevas instancias para no afectar otros estilos del skin.
        BitmapFont fuenteTitulo = new BitmapFont(Gdx.files.internal("Fuentes/juego_fuente2.fnt"));
        fuenteTitulo.getData().setScale(0.8f);
        Label.LabelStyle estiloTitulo = new Label.LabelStyle(fuenteTitulo, com.badlogic.gdx.graphics.Color.WHITE);

        BitmapFont fuenteCuerpo = new BitmapFont(Gdx.files.internal("Fuentes/juego_fuente2.fnt"));
        fuenteCuerpo.getData().setScale(0.7f);
        Label.LabelStyle estiloEncabezado = new Label.LabelStyle(fuenteCuerpo, com.badlogic.gdx.graphics.Color.WHITE);
        Label.LabelStyle estiloDatos = new Label.LabelStyle(fuenteCuerpo, com.badlogic.gdx.graphics.Color.LIGHT_GRAY);

        // Añadimos el Título
        tablaStats.add(new Label("Resultados Ultima Partida", estiloTitulo)).colspan(2).center().padBottom(30);
        tablaStats.row();

        // Añadimos los Encabezados
        tablaStats.add(new Label("Jugador", estiloEncabezado)).padBottom(10).left();
        tablaStats.add(new Label("Puntuacion", estiloEncabezado)).padBottom(10).right();
        tablaStats.row();

        // Añadimos una línea separadora
        Image separador = new Image(getSkin().getDrawable("default-round-down")); // Usamos un drawable del skin
        tablaStats.add(separador).colspan(2).height(2).fillX().padBottom(10);
        tablaStats.row();

        // Recorremos las estadísticas y añadimos una fila por cada jugador
        for (EstadisticasJugador stat : stats) {
            tablaStats.add(new Label(stat.getNombreJugador(), estiloDatos)).pad(5).left();
            tablaStats.add(new Label(String.valueOf(stat.getPuntuacionTotal()), estiloDatos)).pad(5).right();
            tablaStats.row();
        }

        // Envolvemos la tabla en un ScrollPane por si la lista es muy larga
        ScrollPane scrollPane = new ScrollPane(tablaStats, getSkin());
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        return scrollPane;
    }

    /**
     * Libera los recursos utilizados por el menú de pausa, como atlas de texto de ayuda.
     */
    public void dispose() {
        textoComoJugarAtlas.dispose();
    }
}
