// Archivo: src/com/JSonic/uneg/PantallaDeJuego.java (COMPLETO Y CORREGIDO)
package com.JSonic.uneg;

// Imports...
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import network.Network;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import network.interfaces.IGameClient;
import network.interfaces.IGameServer;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.graphics.Texture;

public class PantallaDeJuego extends PantallaBase {

    // --- VARIABLES ORIGINALES ---
    public static final float VIRTUAL_WIDTH = 900;
    public static final float VIRTUAL_HEIGHT = 505;
    private OrthographicCamera camaraJuego;
    private Viewport viewport;
    private final JSonicJuego juegoPrincipal;
    private final SpriteBatch batch;
    private LevelManager manejadorNivel;
    private final IGameClient gameClient;
    private final IGameServer localServer;
    private Sonic sonic;
    private PlayerState sonicEstado;
    private final HashMap<Integer, Player> otrosJugadores = new HashMap<>();
    private SoundManager soundManager;
    private static final String BACKGROUND_MUSIC_PATH2 = "SoundsBackground/Dating Fight.mp3";
    private AssetManager assetManager;
    private ShapeRenderer shapeRenderer;
    private ContadorUI contadorAnillos;
    private ContadorUI contadorBasura;
    private AnillosVisual anilloVisual;
    private final HashMap<Integer, RobotVisual> enemigosEnPantalla = new HashMap<>();
    private final HashMap<Integer, ItemVisual> itemsEnPantalla = new HashMap<>();

    private int anillosTotal = 0;
    private int basuraTotal = 0;

    //para el teletransporte
    // Al inicio de la clase
    private float tiempoTranscurrido = 0f;
    private boolean teletransporteCreado = false;

    public PantallaDeJuego(JSonicJuego juego, IGameClient client, IGameServer server) {
        super();
        this.juegoPrincipal = juego;
        this.batch = juego.batch;
        this.gameClient = client; // Asignamos el cliente recibido
        this.localServer = server;  // Asignamos el servidor recibido (puede ser null)
    }

    public PantallaDeJuego(JSonicJuego juego) {
        this(juego, null, null); // Llamamos al constructor con nulls si no hay cliente/servidor
    }

    @Override
    /* Tu método inicializar original (SIN CAMBIOS... por ahora)*/
    public void inicializar() {
        camaraJuego = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camaraJuego);
        manejadorNivel = new LevelManager(camaraJuego, batch);
        manejadorNivel.cargarNivel("maps/Zona1N1.tmx");
        sonicEstado = new PlayerState();
        sonicEstado.x = 100;
        sonicEstado.y = 100;
        sonic = new Sonic(sonicEstado, manejadorNivel);
        assetManager = new AssetManager();
        soundManager = new SoundManager(assetManager);
        manejadorNivel.setPlayer(sonic);
        soundManager.loadMusic(BACKGROUND_MUSIC_PATH2);
        soundManager.playBackgroundMusic(BACKGROUND_MUSIC_PATH2, 0.5f, true);
        assetManager.finishLoading();
        shapeRenderer = new ShapeRenderer();

        // --- INICIO: CONFIGURACIÓN UI Anillos ---
        String numerosTexturaPath = "Fondos/numerosContadorAnillos.png";
        contadorAnillos = new ContadorUI(numerosTexturaPath);
        contadorBasura = new ContadorUI(numerosTexturaPath);

        Table tablaUI = new Table();
        tablaUI.top().right();
        tablaUI.setFillParent(true);
        tablaUI.pad(10);

        // Iconos y contadores
        anilloVisual = new AnillosVisual(new ItemState(0, 0, 0, ItemState.ItemType.ANILLO));
        Image anilloIcono = new Image(anilloVisual.animacion.getKeyFrame(0));
        Texture basuraIcono = new Texture("Items/basura.png");

        tablaUI.add(anilloIcono).size(45, 45);
        tablaUI.add(contadorAnillos.getTabla()).padLeft(5);
        tablaUI.add(new Image(basuraIcono)).size(55, 55).padLeft(20);
        tablaUI.add(contadorBasura.getTabla()).padLeft(5);

        mainStage.addActor(tablaUI);
    }

    @Override
    public void actualizar(float deltat) {
        // Al inicio del método actualizar
        tiempoTranscurrido += deltat;
        if (!teletransporteCreado && tiempoTranscurrido >= 20f) {
            // Coordenadas esquina superior derecha (ajusta según tu mapa)
            float destinoX = VIRTUAL_WIDTH - 50;
            float destinoY = VIRTUAL_HEIGHT - 50;
            ItemState estadoTele = new ItemState(
                999, // ID único
                destinoX,
                destinoY,
                ItemState.ItemType.TELETRANSPORTE
            );
            // Puedes agregar destinoX y destinoY de destino en el estado si lo necesitas
            crearItemVisual(estadoTele);
            teletransporteCreado = true;
        }
        if (localServer != null) {
            localServer.update(deltat, this.manejadorNivel);
        }

        if (gameClient != null) {
            while (!gameClient.getPaquetesRecibidos().isEmpty()) {
                Object paquete = gameClient.getPaquetesRecibidos().poll();
                // LÍNEA DE DEPURACIÓN ===
                System.out.println("[CLIENT_DEBUG] Procesando paquete de tipo: " + paquete.getClass().getSimpleName());

                if (paquete instanceof Network.RespuestaAccesoPaquete p) {
                    if (p.tuEstado != null) {
                        inicializarJugadorLocal(p.tuEstado);

                        System.out.println("[CLIENT] Conexión aceptada. Extrayendo y enviando plano del mapa...");

                        java.util.ArrayList<Rectangle> paredes = new java.util.ArrayList<>();
                        MapObjects objetosColision = manejadorNivel.getCollisionObjects();

                        if (objetosColision != null) {
                            for (com.badlogic.gdx.maps.MapObject obj : objetosColision) {
                                if (obj instanceof com.badlogic.gdx.maps.objects.RectangleMapObject) {
                                    paredes.add(((com.badlogic.gdx.maps.objects.RectangleMapObject) obj).getRectangle());
                                }
                            }
                        }

                        Network.PaqueteInformacionMapa paqueteMapa = new Network.PaqueteInformacionMapa();
                        paqueteMapa.paredes = paredes;
                        gameClient.send(paqueteMapa);
                        System.out.println("[CLIENT] Plano del mapa con " + paredes.size() + " paredes enviado.");
                    }
                }
                else if (paquete instanceof Network.PaqueteJugadorConectado p) {
                    if (sonicEstado != null && p.nuevoJugador.id != sonicEstado.id) {
                        agregarOActualizarOtroJugador(p.nuevoJugador);
                    }
                } else if (paquete instanceof Network.PaquetePosicionJugador p) {
                    if (sonicEstado != null && p.id != sonicEstado.id) {
                        actualizarPosicionOtroJugador(p.id, p.x, p.y, p.estadoAnimacion);
                    }
                } else if (paquete instanceof Network.PaqueteEnemigoNuevo p) {
                    // El servidor nos ordena crear un enemigo
                    crearEnemigoVisual(p.estadoEnemigo);
                } else if (paquete instanceof Network.PaqueteItemNuevo p) {
                    // El servidor nos ordena crear un ítem
                    crearItemVisual(p.estadoItem);
                } else if (paquete instanceof Network.PaqueteItemEliminado p) {
                    // El servidor nos ordena eliminar un ítem
                    ItemVisual itemEliminado = itemsEnPantalla.remove(p.idItem);
                    if (itemEliminado != null) {
                        System.out.println("[CLIENT] Obedeciendo orden de eliminar ítem con ID: " + p.idItem);
                        itemEliminado.dispose();
                    }
                } else if (paquete instanceof Network.PaqueteActualizacionEnemigos p) {
                    // Recibimos la lista completa de estados de enemigos del servidor
                    for (EnemigoState estadoServidor : p.estadosEnemigos.values()) {
                        RobotVisual enemigoVisual = enemigosEnPantalla.get(estadoServidor.id);
                        if (enemigoVisual != null) {
                            // Actualizamos la posición y el estado del enemigo visual
                            // para que coincida con lo que dice el servidor.
                            enemigoVisual.estado.x = estadoServidor.x;
                            enemigoVisual.estado.y = estadoServidor.y;
                            enemigoVisual.setEstadoActual(estadoServidor.estadoAnimacion); // Asumiendo que RobotVisual tiene este método
                        }
                    }
                }
            }
        }

        // Primero, actualizamos todos los ítems
        for (ItemVisual item : itemsEnPantalla.values()) {
            item.update(deltat);
        }


        // Usamos un iterador para poder eliminar elementos de forma segura mientras recorremos la colección.
        Iterator<Map.Entry<Integer, ItemVisual>> iter = itemsEnPantalla.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, ItemVisual> entry = iter.next();
            ItemVisual item = entry.getValue();

            if (sonic.getBounds() != null && item.getBounds() != null && Intersector.overlaps(sonic.getBounds(), item.getBounds())) {

                Network.PaqueteSolicitudRecogerItem paquete = new Network.PaqueteSolicitudRecogerItem();
                paquete.idItem = item.estado.id;
                gameClient.send(paquete);

                System.out.println("[CLIENT_DEBUG] Colisión detectada con item tipo: " + item.estado.tipo);

                boolean itemRecogido = false;
                if (item.estado.tipo == ItemState.ItemType.ANILLO) {
                    anillosTotal++;
                    contadorAnillos.setValor(anillosTotal);
                    itemRecogido = true;
                } else if (item.estado.tipo == ItemState.ItemType.BASURA || item.estado.tipo == ItemState.ItemType.PIEZA_PLASTICO) {
                    basuraTotal++;
                    contadorBasura.setValor(basuraTotal);
                    itemRecogido = true;
                }
                //para el Teletransporte
                else if (item.estado.tipo == ItemState.ItemType.TELETRANSPORTE) {
                    manejadorNivel.cargarNivel("maps/ZonaJefeN1.tmx");
                    sonic.estado.x = 100;
                    sonic.estado.y = 100;
                    iter.remove();
                    item.dispose();
                    System.out.println("[CLIENT_DEBUG] Teletransporte activado, cambiando de mapa.");
                    break;
                }

                // 3. Lógica de prueba: Eliminar el ítem visualmente.
                // En la versión final, esto solo debe ocurrir cuando el servidor lo ordene.
                if (itemRecogido) {
                    iter.remove(); // Elimina el ítem del HashMap de forma segura.
                    item.dispose(); // Libera los recursos del ítem.
                    System.out.println("[CLIENT_DEBUG] Ítem " + item.estado.id + " eliminado visualmente para la prueba.");
                    break; // Rompemos el bucle para procesar solo una recogida por fotograma.
                }
            }
        }

        // --- LÓGICA DE JUGADOR Y ENEMIGOS ---
        sonic.KeyHandler(); // Esto ahora maneja el movimiento Y la colisión con el mapa
        sonic.update(deltat);

        for (Player otro : otrosJugadores.values()) {
            otro.update(deltat);
        }

        for (RobotVisual enemigo : enemigosEnPantalla.values()) enemigo.update(deltat);

        // Actualizar cámara
        camaraJuego.position.x = sonic.estado.x;
        camaraJuego.position.y = sonic.estado.y;
        manejadorNivel.limitarCamaraAMapa(camaraJuego);
        camaraJuego.update();
        mainStage.act(Math.min(deltat, 1 / 30f));
    }


    @Override
    public void render(float delta) {
        super.render(delta);
        if (viewport != null) viewport.apply();

        batch.setProjectionMatrix(camaraJuego.combined);
        manejadorNivel.dibujar();

        batch.begin();
        sonic.draw(batch);
        for (Player otro : otrosJugadores.values()) otro.draw(batch);
        for (RobotVisual enemigo : enemigosEnPantalla.values()) enemigo.draw(batch);

        for (ItemVisual item : itemsEnPantalla.values()) item.draw(batch);

        batch.end();

        mainStage.getViewport().apply();
        mainStage.draw();

        if (gameClient != null && sonic != null && sonic.estado != null && sonic.estado.id != 0) {
            Network.PaquetePosicionJugador paquete = new Network.PaquetePosicionJugador();
            paquete.id = sonic.estado.id;
            paquete.x = sonic.estado.x;
            paquete.y = sonic.estado.y;
            paquete.estadoAnimacion = sonic.getEstadoActual();
            gameClient.send(paquete); // Usa el método de la interfaz
        }
    }



    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (viewport != null) {
            viewport.update(width, height, true);
        }
    }

    @Override
    public void pause() {
        if (soundManager != null) {
            soundManager.pauseBackgroundMusic();
        }
    }



    @Override
    public void resume() {
        if (soundManager != null) {
            soundManager.resumeBackgroundMusic();
        }
    }

    public void inicializarJugadorLocal(PlayerState estadoRecibido) {
        this.sonic.estado = estadoRecibido;
        System.out.println("[CLIENT] ID asignado por el servidor: " + this.sonic.estado.id);
    }

    private void crearEnemigoVisual(EnemigoState estadoEnemigo) {
        // Nos aseguramos de no crear un enemigo que ya exista
        if (!enemigosEnPantalla.containsKey(estadoEnemigo.id)) {
            System.out.println("[CLIENT] Recibida orden de crear enemigo con ID: " + estadoEnemigo.id);
            // Creamos el objeto visual usando el estado que nos dio el servidor
            RobotVisual nuevoRobot = new RobotVisual(estadoEnemigo, manejadorNivel, this.gameClient);
            enemigosEnPantalla.put(estadoEnemigo.id, nuevoRobot);
        }
    }

    private void crearItemVisual(ItemState estadoItem) {
        if (!itemsEnPantalla.containsKey(estadoItem.id)) {
            System.out.println("[CLIENT] Recibida orden de crear ítem tipo " + estadoItem.tipo + " con ID: " + estadoItem.id);
            ItemVisual nuevoItem = null;
            // Creamos el tipo de ítem visual correcto según la información del servidor
            switch (estadoItem.tipo) {
                case ANILLO:
                    nuevoItem = new AnillosVisual(estadoItem);
                    break;
                case BASURA:
                    nuevoItem = new BasuraVisual(estadoItem);
                    break;
                case PIEZA_PLASTICO:
                    nuevoItem = new PiezaDePlasticoVisual(estadoItem);
                    break;
                case TELETRANSPORTE:
                    nuevoItem = new TeletransporteVisual(estadoItem);
                    break;
            }

            if (nuevoItem != null) {
                itemsEnPantalla.put(estadoItem.id, nuevoItem);
            }
        }
    }
    public void agregarOActualizarOtroJugador(PlayerState estadoRecibido) {
        Player jugadorVisual = otrosJugadores.get(estadoRecibido.id);
        if (jugadorVisual == null) {
            System.out.println("Creando nuevo jugador gráfico con ID: " + estadoRecibido.id);
            jugadorVisual = new Sonic(estadoRecibido);
            otrosJugadores.put(estadoRecibido.id, jugadorVisual);
        } else {
            jugadorVisual.estado.x = estadoRecibido.x;
            jugadorVisual.estado.y = estadoRecibido.y;
            jugadorVisual.setEstadoActual(estadoRecibido.estadoAnimacion);
        }
    }

    public void actualizarPosicionOtroJugador(int id, float x, float y, Entity.EstadoPlayer estadoAnim) {
        Player jugador = otrosJugadores.get(id);
        if (jugador != null) {
            jugador.estado.x = x;
            jugador.estado.y = y;
            jugador.setEstadoActual(estadoAnim);
        }
    }

    // Tu método dispose original (CON MODIFICACIONES)
    @Override
    public void dispose() {
        super.dispose();
        manejadorNivel.dispose();
        sonic.dispose();
        if (assetManager != null) assetManager.dispose();
        if (soundManager != null) soundManager.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        for (RobotVisual enemigo : enemigosEnPantalla.values()) enemigo.dispose();

        for (ItemVisual item : itemsEnPantalla.values()) {
            item.dispose();
        }
        if (contadorAnillos != null) contadorAnillos.dispose();
        if (contadorBasura != null) contadorBasura.dispose();
    }

}
