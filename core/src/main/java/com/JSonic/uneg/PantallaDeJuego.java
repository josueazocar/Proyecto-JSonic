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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


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


    // --- NUEVAS VARIABLES PARA EL MANEJO DE ENEMIGOS ---
    private ArrayList<RobotVisual> enemigosEnPantalla;
    private float tiempoGeneracionEnemigo = 0f;
    private final float INTERVALO_GENERACION_ENEMIGO = 5.0f;
    private int proximoIdEnemigo = 0;
    // --- FIN NUEVAS VARIABLES ---



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

        // --- IMPORTANTE: PASAR LA INSTANCIA DE SONIC AL LEVELMANAGER ---
        manejadorNivel.setPlayer(sonic); // <-- Add this line
        // --- FIN IMPORTANTE ---

        // --- INICIALIZACIÓN DE ENEMIGOS ---
        enemigosEnPantalla = new ArrayList<>();
        crearNuevoEnemigo(200, 300); // Create an initial enemy
        // --- FIN INICIALIZACIÓN DE ENEMIGOS ---

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

    // --- MÉTODO PARA CREAR ENEMIGOS ---
    private void crearNuevoEnemigo(float x, float y) {
        EnemigoState nuevoEstadoEnemigo = new EnemigoState(proximoIdEnemigo++, x, y, 100, EnemigoState.EnemigoType.ROBOT);
        // Asegúrate de pasar el manejadorNivel al constructor de RobotVisual
        RobotVisual nuevoRobot = new RobotVisual(nuevoEstadoEnemigo, manejadorNivel);
        enemigosEnPantalla.add(nuevoRobot);
        Gdx.app.log("PantallaDeJuego", "Enemigo #" + nuevoEstadoEnemigo.id + " creado en (" + x + ", " + y + ")");
    }
    // --- FIN MÉTODO ---

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


        // --- ACTUALIZAR Y GENERAR ENEMIGOS ---
        tiempoGeneracionEnemigo += deltat;
        if (tiempoGeneracionEnemigo >= INTERVALO_GENERACION_ENEMIGO) {
            // Spawn enemies relative to Sonic's position
            float spawnX = sonic.estado.x + (Math.random() > 0.5 ? 400 : -400); // 400 pixels left/right
            float spawnY = sonic.estado.y + (Math.random() > 0.5 ? 200 : -200); // 200 pixels up/down
            crearNuevoEnemigo(spawnX, spawnY);
            tiempoGeneracionEnemigo = 0;
        }

        Iterator<RobotVisual> iterator = enemigosEnPantalla.iterator();
        while (iterator.hasNext()) {
            RobotVisual enemigo = iterator.next();
            enemigo.update(deltat);
            // Example: Remove enemy if its health is zero (implement health in EnemigoState if not already)
            // if (enemigo.estado.vida <= 0) {
            //     enemigo.dispose(); // Dispose enemy resources
            //     iterator.remove();
            // }
        }
        // --- FIN ACTUALIZAR Y GENERAR ENEMIGOS ---



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


// NUEVO RENDER Dentro de PantallaDeJuego.java
@Override
public void render(float delta) {
    super.render(delta);

    if (viewport != null) {
        viewport.apply();
    }

    batch.setProjectionMatrix(camaraJuego.combined);
    manejadorNivel.dibujar();

    batch.begin();
    sonic.draw(batch);
    for (Player otro : otrosJugadores.values()) {
        otro.draw(batch);
    }
    for (RobotVisual enemigo : enemigosEnPantalla) {
        enemigo.draw(batch);
    }
    batch.end();

    // --- INICIO DIBUJO DE HITBOXES PARA DEPURACIÓN (ShapeRenderer) ---
    // Asegúrate de que shapeRenderer se inicialice en inicializar() y se haga dispose() en dispose().
    if (shapeRenderer != null) {
        shapeRenderer.setProjectionMatrix(camaraJuego.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 0, 0, 1); // Rojo para el hitbox del jugador local

        // Dibuja el hitbox del jugador local
        // Asumiendo que sonic.getBounds() existe y devuelve un Rectangle
        Rectangle playerBounds = sonic.getBounds();
        shapeRenderer.rect(playerBounds.x, playerBounds.y, playerBounds.width, playerBounds.height);

        // Dibuja los objetos de colisión del mapa
        shapeRenderer.setColor(0, 1, 0, 1); // Verde para los objetos del mapa
        if (manejadorNivel != null && manejadorNivel.getCollisionObjects() != null) {
            for (MapObject object : manejadorNivel.getCollisionObjects()) {
                if (object instanceof RectangleMapObject) {
                    Rectangle rectObject = ((RectangleMapObject) object).getRectangle();
                    shapeRenderer.rect(rectObject.x, rectObject.y, rectObject.width, rectObject.height);
                }
            }
        }
        shapeRenderer.end();
    }
    // --- FIN DIBUJO DE HITBOXES ---

    // Lógica para enviar datos de red
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




/*Antiguo Render:
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

        // --- DIBUJAR ENEMIGOS ---
        for (RobotVisual enemigo : enemigosEnPantalla) {
            enemigo.draw(batch);
        }
        // --- FIN DIBUJAR ENEMIGOS ---


    }
*/

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
    @Override
    public void dispose() {
        super.dispose();
        manejadorNivel.dispose();
        sonic.dispose();
        if (assetManager != null) {
            assetManager.dispose();
        }
        if (soundManager != null) {
            soundManager.dispose();
        }
        // Asegúrate de liberar el ShapeRenderer
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        for (RobotVisual enemigo : enemigosEnPantalla) {
            enemigo.dispose();
        }
    }

}
