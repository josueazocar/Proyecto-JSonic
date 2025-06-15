package com.JSonic.uneg;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.assets.AssetManager; // Necesario para AssetManager
import network.GameClient;
import network.Network;

import java.util.HashMap;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main implements ApplicationListener {

    private Sonic sonic;
    private PlayerState sonicEstado; // estado del jugador local
    private SpriteBatch batch;
    private GameClient gameClient;
    private AssetManager assetManager; // Para gestionar assets
    private SoundManager soundManager; // Instancia de nuestro SoundManager
    private HashMap<Integer, Player> otrosJugadores = new HashMap<>();;
    private static final String BACKGROUND_MUSIC_PATH1 = "SoundsBackground/Green Hill Zone Theme Sonic (8 Bit Version).mp3";
    private static final String BACKGROUND_MUSIC_PATH2 = "SoundsBackground/Dating Fight.mp3";
    private static final String BACKGROUND_MUSIC_PATH3 = "SoundsBackground/Heartache.mp3";



    @Override
    public void create() {
        batch = new SpriteBatch();
        gameClient = new GameClient(this);
        sonicEstado = new PlayerState();
        sonicEstado.x = 100; // Posición inicial
        sonicEstado.y = 100;
        sonic = new Sonic(sonicEstado);
        assetManager = new AssetManager();

        //Inicializar SoundManager, pasándole el AssetManager que ya está inicializado
        soundManager = new SoundManager(assetManager);

        // Cargar la música con el SoundManager usando el AssetManager
        // Esto solo la pone en la cola de carga
        soundManager.loadMusic(BACKGROUND_MUSIC_PATH2);

        // IMPORTANTE: Esperar a que el AssetManager termine de cargar los assets.
        // En un juego real, esto se haría en una pantalla de carga.
        // Aquí es crucial porque la música no estará disponible hasta que se cargue.
        assetManager.finishLoading(); // Espera a que todos los assets en cola se carguen

        // Reproducir la música de fondo una vez que todo este cargado
        soundManager.playBackgroundMusic(BACKGROUND_MUSIC_PATH2, 0.5f, true); // Volumen al 50%, en bucle
    }

    @Override
    public void resize(int width, int height) {
        if(width <= 0 || height <= 0) return;
    }

    @Override
    public void render() {
        if (gameClient != null) {
            // Mientras haya paquetes en la cola...
            while (!gameClient.paquetesRecibidos.isEmpty()) {
                // Sacamos un paquete para procesarlo
                Object paquete = gameClient.paquetesRecibidos.poll();

                if (paquete instanceof Network.RespuestaAccesoPaquete) {
                    Network.RespuestaAccesoPaquete p = (Network.RespuestaAccesoPaquete) paquete;
                    // Si la respuesta contiene nuestro estado (lo cual debería), lo inicializamos.
                    if(p.tuEstado != null) {
                        inicializarJugadorLocal(p.tuEstado);
                    }
                }
                else if (paquete instanceof Network.PaqueteJugadorConectado) {
                    Network.PaqueteJugadorConectado p = (Network.PaqueteJugadorConectado) paquete;
                    // Nos aseguramos de no añadirnos a nosotros mismos a la lista de "otros" jugadores.
                    if (sonicEstado != null && p.nuevoJugador.id != sonicEstado.id) {
                        agregarOActualizarOtroJugador(p.nuevoJugador);
                    }
                }
                else if (paquete instanceof Network.PaquetePosicionJugador) {
                    Network.PaquetePosicionJugador p = (Network.PaquetePosicionJugador) paquete;
                    // Solo actualizamos la posición si no es nuestro propio paquete de movimiento.
                    if (sonicEstado != null && p.id != sonicEstado.id) {
                        actualizarPosicionOtroJugador(p.id, p.x, p.y, p.estadoAnimacion);
                    }
                }
            }
        }

        Gdx.gl.glClearColor(0.7f, 0.7f, 0.7f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        sonic.KeyHandler();
        sonic.update(Gdx.graphics.getDeltaTime());

        for (Player otroJugador : otrosJugadores.values()) {
            // Le decimos a cada jugador remoto que actualice su lógica de animación.
            otroJugador.update(Gdx.graphics.getDeltaTime());
        }
        batch.begin();
        sonic.draw(batch);

        for (Player otroJugador : otrosJugadores.values()) {
            otroJugador.draw(batch);
        }

        batch.end();

        // Enviar nuestra posición al servidor
        if (gameClient != null && sonic.estado.id != 0) { // Comprobamos el ID en el estado
            Network.PaquetePosicionJugador paquete = new Network.PaquetePosicionJugador();
            paquete.id = sonic.estado.id;
            paquete.x = sonic.estado.x;
            paquete.y = sonic.estado.y;
            // Obtenemos el estado actual de la animación y lo añadimos al paquete
            paquete.estadoAnimacion = sonic.getEstadoActual();
            gameClient.cliente.sendTCP(paquete);
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

    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
        }
        if (assetManager != null) {
            assetManager.dispose(); // Esto liberará la música y cualquier otro asset que hayas cargado con él.
        }
        if (soundManager != null) {
            soundManager.dispose();
        }
    }

    public void agregarOActualizarOtroJugador(PlayerState estadoRecibido) {
        Player jugadorVisual = otrosJugadores.get(estadoRecibido.id);

        if (jugadorVisual == null) {
            System.out.println("Creando nuevo jugador gráfico con ID: " + estadoRecibido.id);
            // Creamos un nuevo objeto SONIC directamente. Ya no necesitamos el adaptador.
            jugadorVisual = new Sonic(estadoRecibido);
            otrosJugadores.put(estadoRecibido.id, jugadorVisual);
        } else {
            // Actualizamos el estado del Sonic ya existente
            jugadorVisual.estado.x = estadoRecibido.x;
            jugadorVisual.estado.y = estadoRecibido.y;
            jugadorVisual.setEstadoActual(estadoRecibido.estadoAnimacion);
        }
    }
    public void actualizarPosicionOtroJugador(int id, float x, float y, Entity.EstadoPlayer estadoAnim) {
        Player jugador = otrosJugadores.get(id);
        if (jugador != null) {
            // Actualizamos directamente el estado del objeto Player/Sonic existente.
            jugador.estado.x = x;
            jugador.estado.y = y;
            jugador.setEstadoActual(estadoAnim);

        }
    }

    public void inicializarJugadorLocal(PlayerState estadoRecibido) {
        // Asignamos el estado recibido (que contiene el ID correcto)
        this.sonicEstado = estadoRecibido;
        this.sonic.estado = this.sonicEstado;
        System.out.println("[CLIENT] ID asignado por el servidor: " + this.sonicEstado.id);
    }
}
