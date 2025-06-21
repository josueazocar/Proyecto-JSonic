package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import network.GameClient;
import network.Network;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject; // Importar MapObject
import com.badlogic.gdx.maps.objects.RectangleMapObject; // Importar RectangleMapObject
import com.badlogic.gdx.math.Rectangle;

import java.util.HashMap;

public class PantallaDeJuego extends PantallaBase {

    // Constantes y cámara (tu compañera lo hizo bien)
    public static final float VIRTUAL_WIDTH = 1280;
    public static final float VIRTUAL_HEIGHT = 720;
    private OrthographicCamera camaraJuego;
    private Viewport viewport;

    // Referencia a la clase principal para acceder a recursos compartidos
    private final JSonicJuego juegoPrincipal;

    // Recursos compartidos
    private final SpriteBatch batch;

    // Objetos y Lógica de esta pantalla
    private LevelManager manejadorNivel;
    private GameClient gameClient;
    private Sonic sonic; // Jugador local
    private PlayerState sonicEstado; // Estado del jugador local
    private final HashMap<Integer, Player> otrosJugadores = new HashMap<>();
    private SoundManager soundManager; // Instancia de nuestro SoundManager
    private static final String BACKGROUND_MUSIC_PATH2 = "SoundsBackground/Dating Fight.mp3";
    private AssetManager assetManager; // Para gestionar assets

   // Mapa de jugadores remotos
//para las colisiones
    private ShapeRenderer shapeRenderer;
    
    // Constructor corregido: solo recibe la referencia a Main
    public PantallaDeJuego(JSonicJuego juego) {
        super();
        this.juegoPrincipal = juego;
        this.batch = juego.batch; // Usamos el SpriteBatch de la clase principal
    }

    @Override
    public void inicializar() {
        // Inicialización de la cámara y el viewport
        camaraJuego = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camaraJuego);

        // Inicialización de los objetos del juego DENTRO de la pantalla
        manejadorNivel = new LevelManager(camaraJuego, batch);
        manejadorNivel.cargarNivel("maps/Zona1N1.tmx");

        // Creamos el estado y el jugador local
        sonicEstado = new PlayerState();
        sonicEstado.x = 100;
        sonicEstado.y = 100;
        sonic = new Sonic(sonicEstado, manejadorNivel); // Le pasamos el manejador de nivel para colisiones
        assetManager = new AssetManager();
        soundManager = new SoundManager(assetManager);

        soundManager.loadMusic(BACKGROUND_MUSIC_PATH2);

        // Creamos el cliente de red
        gameClient = new GameClient(this);

        /*soundManager.playBackgroundMusic(BACKGROUND_MUSIC_PATH2, 0.5f, true); // Volumen al 50%, en bucle
        assetManager.finishLoading(); // Espera a que todos los assets en cola se carguen*/
        soundManager.playBackgroundMusic(BACKGROUND_MUSIC_PATH2, 0.5f, true);
        assetManager.finishLoading();

        // NUEVO: Inicializar ShapeRenderer
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void actualizar(float deltat) {
        // --- 1. PROCESAR PAQUETES DE RED (Lógica recuperada) ---
        if (gameClient != null) {
            while (!gameClient.paquetesRecibidos.isEmpty()) {
                Object paquete = gameClient.paquetesRecibidos.poll();

                if (paquete instanceof Network.RespuestaAccesoPaquete p) {
                    if (p.tuEstado != null) inicializarJugadorLocal(p.tuEstado);
                } else if (paquete instanceof Network.PaqueteJugadorConectado p) {
                    if (sonicEstado != null && p.nuevoJugador.id != sonicEstado.id) {
                        agregarOActualizarOtroJugador(p.nuevoJugador);
                    }
                } else if (paquete instanceof Network.PaquetePosicionJugador p) {
                    if (sonicEstado != null && p.id != sonicEstado.id) {
                        actualizarPosicionOtroJugador(p.id, p.x, p.y, p.estadoAnimacion);
                    }
                }
            }
        }

        // --- 2. ACTUALIZAR LÓGICA (Lógica recuperada y corregida) ---
        sonic.KeyHandler(); // SOLO el jugador local procesa el teclado
        sonic.update(deltat); // TODOS actualizan su animación
        for (Player otro : otrosJugadores.values()) {
            otro.update(deltat);
        }

        // --- 3. ACTUALIZAR CÁMARA ---
        camaraJuego.position.x = sonic.estado.x;
        camaraJuego.position.y = sonic.estado.y;
        manejadorNivel.limitarCamaraAMapa(camaraJuego);
        camaraJuego.update();

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

        // 1. Actualizar Stages (heredados de PantallaBase)
        // Los stages son 'protected', así que podemos acceder a ellos.
        if (uiStage != null) uiStage.act(delta);
        if (mainStage != null) mainStage.act(delta);

        // 2. Llamar a la lógica de actualización de esta pantalla
        this.actualizar(delta); // Llama al método actualizar() de esta clase


        // 3. Dibujar el contenido específico del juego (mapa, sonic, etc.)
        if (viewport != null) { // Asegurarse que el viewport del juego esté aplicado
            viewport.apply();
        }

        batch.setProjectionMatrix(camaraJuego.combined);


        manejadorNivel.dibujar(); // Asumiendo que LevelManager gestiona su dibujado
        batch.begin();
        sonic.draw(batch);

        for (Player otro : otrosJugadores.values()) {
            otro.draw(batch);
        }
        batch.end();
    //Cambio para las coliciones
        // INICIO DIBUJO DE HITBOXES PARA DEPURACIÓN (ShapeRenderer)
        shapeRenderer.setProjectionMatrix(camaraJuego.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line); // Dibuja solo el contorno
        shapeRenderer.setColor(1, 0, 0, 1); // Rojo para el hitbox del jugador local

        // Dibuja el hitbox del jugador local
        Rectangle playerBounds = sonic.getBounds();
        shapeRenderer.rect(playerBounds.x, playerBounds.y, playerBounds.width, playerBounds.height);

        // Dibuja los objetos de colisión del mapa
        shapeRenderer.setColor(0, 1, 0, 1); // Verde para los objetos del mapa
        if (manejadorNivel != null && manejadorNivel.getCollisionObjects() != null) {
            for (MapObject object : manejadorNivel.getCollisionObjects()) {
                // Aquí solo dibujamos RectangleMapObject, ya que tu lógica de colisión actual solo los maneja.
                // Si decides implementar polígonos/elipses, deberías dibujarlos aquí también.
                if (object instanceof RectangleMapObject) {
                    Rectangle rectObject = ((RectangleMapObject) object).getRectangle();
                    shapeRenderer.rect(rectObject.x, rectObject.y, rectObject.width, rectObject.height);
                }
            }
        }
        shapeRenderer.end();
//-------------------------------------------------------------------------
        // 4. Dibujar los Stages de PantallaBase (UI encima del juego)
        if (mainStage != null) mainStage.draw(); // Dibuja actores en el stage principal (si los hay)
        if (uiStage != null) uiStage.draw();   // Dibuja actores en el stage de UI (botones, etc.)

        // 5. Enviar datos de red
        if (gameClient != null && sonic != null && sonic.estado != null && sonic.estado.id != 0) {
            Network.PaquetePosicionJugador paquete = new Network.PaquetePosicionJugador();
            paquete.id = sonic.estado.id;
            paquete.x = sonic.estado.x;
            paquete.y = sonic.estado.y;
            paquete.estadoAnimacion = sonic.getEstadoActual();
            if (gameClient.cliente != null && gameClient.cliente.isConnected()) {
                gameClient.cliente.sendTCP(paquete);
            }
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
    // --- MÉTODOS HELPER (Ahora pertenecen a esta pantalla) ---
    public void inicializarJugadorLocal(PlayerState estadoRecibido) {
        this.sonic.estado = estadoRecibido;
        System.out.println("[CLIENT] ID asignado por el servidor: " + this.sonic.estado.id);
    }

    public void agregarOActualizarOtroJugador(PlayerState estadoRecibido) {
        Player jugadorVisual = otrosJugadores.get(estadoRecibido.id);
        if (jugadorVisual == null) {
            System.out.println("Creando nuevo jugador gráfico con ID: " + estadoRecibido.id);
            jugadorVisual = new Sonic(estadoRecibido); // Remotos no necesitan el LevelManager
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

    @Override public void resize(int width, int height) { super.resize(width, height); // Actualiza los viewports de mainStage y uiStage en PantallaBase
        if (viewport != null) {
            viewport.update(width, height, true); // Actualiza el viewport específico del juego
        } }
    @Override public void dispose() {
        super.dispose();
        manejadorNivel.dispose();
        sonic.dispose();
        if (assetManager != null) {
            assetManager.dispose(); // Esto liberará la música y cualquier otro asset que hayas cargado con él.
        }
        if (soundManager != null) {
            soundManager.dispose();
        }
    }

}
