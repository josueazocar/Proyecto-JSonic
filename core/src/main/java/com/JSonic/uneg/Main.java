package com.JSonic.uneg;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.assets.AssetManager;
// import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20; // Para Gdx.gl.glClearColor

// Importaciones de networking y elementos del juego
import network.GameClient;
import network.Network;
//import network.PlayerState;

import java.util.HashMap;

/** {@link com.badlogic.gdx.Game} implementation compartida por todas las plataformas. */
public class Main extends Game {

    // --- Variables globales que Main gestionará y pasará a las pantallas ---
    private AssetManager assetManager;
    private SoundManager soundManager;
    private SpriteBatch batch;
    private GameClient gameClient;

    // Estado del jugador local (Sonic)
    // Se inicializará con un ID del servidor
    private PlayerState sonicEstado;
    private Sonic sonic; // El objeto Sonic del jugador local

    // Mapa para otros jugadores conectados
    private HashMap<Integer, Player> otrosJugadores;

    // Rutas de música
    private static final String BACKGROUND_MUSIC_PATH1 = "SoundsBackground/Green Hill Zone Theme Sonic (8 Bit Version).mp3";
    private static final String BACKGROUND_MUSIC_PATH2 = "SoundsBackground/Dating Fight.mp3";
    private static final String BACKGROUND_MUSIC_PATH3 = "SoundsBackground/Heartache.mp3";


    @Override
    public void create() {
        // Inicializa SpriteBatch
        batch = new SpriteBatch();

        // Inicializa AssetManager y SoundManager
        assetManager = new AssetManager();
        soundManager = new SoundManager(assetManager);

        // Carga la música (solo la pone en la cola de carga del AssetManager)
        soundManager.loadMusic(BACKGROUND_MUSIC_PATH2);
        soundManager.loadMusic(BACKGROUND_MUSIC_PATH1);
        soundManager.loadMusic(BACKGROUND_MUSIC_PATH3);

        // --- Lógica de networking y creación del jugador local ---
        gameClient = new GameClient(this); // 'this' se pasa para que GameClient pueda llamar a métodos de Main
        sonicEstado = new PlayerState(); // Estado inicial por defecto, se actualizará con el ID del servidor
        sonicEstado.x = 100; // Posición inicial
        sonicEstado.y = 100;
        // Crea el objeto Sonic local con el estado inicial.
        // El LevelManager se le asignará en PantallaDeJuego cuando se inicialice.
        sonic = new Sonic(sonicEstado);

        otrosJugadores = new HashMap<>(); // Inicializa el mapa de otros jugadores

        // IMPORTANTE: Esperar a que el AssetManager termine de cargar los assets.
        // Esto se podría hacer en una pantalla de carga real para no bloquear la UI.
        assetManager.finishLoading(); // Espera a que todos los assets en cola se carguen

        // Reproducir la música de fondo una vez que todo esté cargado
        soundManager.playBackgroundMusic(BACKGROUND_MUSIC_PATH2, 0.5f, true); // Volumen al 50%, en bucle

        // Establece la pantalla inicial y le pasa todos los recursos que necesitará
        setScreen(new PantallaDeJuego(batch, assetManager, soundManager, gameClient, sonic, otrosJugadores));
    }

    // Los métodos resize, render, pause, resume en Main ahora simplemente
    // delegan a la pantalla actual (que es PantallaDeJuego).
    // Toda la lógica de juego se ha movido a PantallaDeJuego.
    @Override
    public void resize(int width, int height) {
        super.resize(width, height); // Llama al resize de la pantalla actual
    }

    @Override
    public void render() {
        super.render(); // Llama al render de la pantalla actual
    }

    @Override
    public void pause() {
        super.pause(); // Llama al pause de la pantalla actual
        if (soundManager != null) { // Adicionalmente, puedes pausar la música global aquí
            soundManager.pauseBackgroundMusic();
        }
    }

    @Override
    public void resume() {
        super.resume(); // Llama al resume de la pantalla actual
        if (soundManager != null) { // Adicionalmente, puedes reanudar la música global aquí
            soundManager.resumeBackgroundMusic();
        }
    }

    @Override
    public void dispose() {
        super.dispose(); // Llama al dispose de la clase Game (importante)

        // Libera los recursos globales que Main ha inicializado
        if (batch != null) {
            batch.dispose();
        }
        if (assetManager != null) {
            assetManager.dispose(); // Esto libera toda la música y assets cargados
        }
        if (soundManager != null) {
            soundManager.dispose(); // Si SoundManager tiene recursos propios, los libera
        }
       /* if (gameClient != null) {
            gameClient.dispose(); // Asegúrate de cerrar la conexión del cliente
        }
        if (sonic != null) {
            sonic.dispose(); // Dispone el spriteSheet de Sonic
        }*/
        // otrosJugadores no necesitan dispose aquí, ya que los objetos Player individuales
        // serán gestionados si es necesario (ej. si se desconectan) o se dispondrán
        // si se recrea toda la pantalla/juego.
    }

    // --- Métodos llamados por GameClient para actualizar el estado global del juego ---

    /**
     * Agrega un nuevo jugador o actualiza el estado de uno existente
     * basado en la información recibida de la red.
     * Es llamado por GameClient.
     *
     * @param estadoRecibido El PlayerState con la información del jugador.
     */
    public void agregarOActualizarOtroJugador(PlayerState estadoRecibido) {
        Player jugadorVisual = otrosJugadores.get(estadoRecibido.id);

        if (jugadorVisual == null) {
            System.out.println("Creando nuevo jugador gráfico con ID: " + estadoRecibido.id);
            // Creamos un nuevo objeto SONIC directamente.
            // Para jugadores remotos, NO se les pasa LevelManager, ya que no manejan input local
            jugadorVisual = new Sonic(estadoRecibido);
            otrosJugadores.put(estadoRecibido.id, jugadorVisual);
        } else {
            // Actualizamos el estado del Sonic ya existente (para que PantallaDeJuego lo dibuje)
            jugadorVisual.estado.x = estadoRecibido.x;
            jugadorVisual.estado.y = estadoRecibido.y;
            jugadorVisual.setEstadoActual(estadoRecibido.estadoAnimacion);
        }
    }

    /**
     * Actualiza la posición y estado de animación de un jugador remoto.
     * Es llamado por GameClient.
     *
     * @param id El ID del jugador.
     * @param x La nueva posición X.
     * @param y La nueva posición Y.
     * @param estadoAnim El estado de animación actual.
     */
    public void actualizarPosicionOtroJugador(int id, float x, float y, Entity.EstadoPlayer estadoAnim) {
        Player jugador = otrosJugadores.get(id);
        if (jugador != null) {
            // Actualizamos directamente el estado del objeto Player/Sonic existente.
            jugador.estado.x = x;
            jugador.estado.y = y;
            jugador.setEstadoActual(estadoAnim);
        }
    }

    /**
     * Inicializa el estado del jugador local con el ID asignado por el servidor.
     * Es llamado por GameClient.
     *
     * @param estadoRecibido El PlayerState con el ID asignado.
     */
    public void inicializarJugadorLocal(PlayerState estadoRecibido) {
        // Asignamos el estado recibido (que contiene el ID correcto) al PlayerState del Sonic local
        this.sonicEstado = estadoRecibido;
        this.sonic.estado = this.sonicEstado; // Asegúrate de que Sonic.estado apunta a esta referencia
        System.out.println("[CLIENT] ID asignado por el servidor: " + this.sonicEstado.id);
    }
}
