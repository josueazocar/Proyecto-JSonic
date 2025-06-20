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

        soundManager.playBackgroundMusic(BACKGROUND_MUSIC_PATH2, 0.5f, true); // Volumen al 50%, en bucle
        assetManager.finishLoading(); // Espera a que todos los assets en cola se carguen
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
        // Llama al render de la clase base (PantallaBase), que se encarga de:
        // 1. Actualizar los stages (uiStage, mainStage) con deltat.
        // 2. Llamar a this.actualizar(delta) de PantallaDeJuego.
        // 3. Limpiar la pantalla (Gdx.gl.glClearColor, Gdx.gl.glClear).
        // 4. Dibujar los stages (mainStage.draw(), uiStage.draw()).
        // IMPORTANTE: Si PantallaBase.render ya limpia la pantalla, no es necesario limpiarla de nuevo aquí.
        super.render(delta); // Mantiene la lógica de actualización y dibujado de UI/stages de PantallaBase

        // Aplica el viewport de la cámara del juego.
        // Esto asegura que lo que dibujemos se vea correctamente escalado y centrado.
        if (viewport != null) {
            viewport.apply();
        }

        // Configura la matriz de proyección del batch para que use la cámara del juego.
        // Esto es vital para que todo lo que se dibuje con este batch (Sonic, enemigos)
        // se posicione y escale según la vista de la cámara.
        batch.setProjectionMatrix(camaraJuego.combined);

        // Dibuja el nivel del mapa.
        // OrthogonalTiledMapRenderer (usado por LevelManager) gestiona su propio "batching" interno.
        // Por lo tanto, NO necesita estar dentro de TU bloque batch.begin()/end().
        // Se dibuja primero para que los sprites (Sonic, enemigos) queden encima.
        manejadorNivel.dibujar();

        // --- INICIO DEL BLOQUE DE DIBUJADO DE SPRITES ---
        // Este 'begin()' es crucial y debe ir antes de CUALQUIER llamada a batch.draw()
        batch.begin();

        // Dibuja a Sonic
        sonic.draw(batch);

        // Dibuja a otros jugadores remotos
        for (Player otro : otrosJugadores.values()) {
            otro.draw(batch);
        }

        // Dibuja a los enemigos (RobotVisual)
        for (RobotVisual enemigo : enemigosEnPantalla) {
            enemigo.draw(batch);
        }

        // Este 'end()' es crucial y debe ir después de TODAS las llamadas a batch.draw()
        batch.end();
        // --- FIN DEL BLOQUE DE DIBUJADO DE SPRITES ---

        // Lógica para enviar datos de red (no es una operación de dibujado)
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
        // --- IMPORTANTE: DISPOSE DE ENEMIGOS ---
        for (RobotVisual enemigo : enemigosEnPantalla) {
            enemigo.dispose();
        }
        // --- FIN IMPORTANTE ---
    }

}
