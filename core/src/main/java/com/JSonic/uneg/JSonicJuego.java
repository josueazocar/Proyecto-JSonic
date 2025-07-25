package com.JSonic.uneg;

import com.JSonic.uneg.Pantallas.EstadisticasJugador;
import com.JSonic.uneg.Pantallas.PantallaDeJuego;
import com.JSonic.uneg.Pantallas.PantallaMenu;
import com.JSonic.uneg.State.PlayerState;
import com.badlogic.gdx.assets.AssetManager;
import network.LocalServer;
import network.GameClient;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import network.interfaces.IGameClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase principal del juego JSonic.
 * Maneja la inicialización, recursos y pantallas del juego.
 * Permite iniciar el juego en modo local o en modo multijugador online.
 */
public class JSonicJuego extends JuegoBase {

    public SpriteBatch batch;

    //Para la musica y efectos de sonido.
    public AssetManager assetManager;
    public SoundManager soundManager;

    public static String direccionIp = "localhost"; // Dirección IP del servidor al que se conecta el cliente.
    public static ArrayList<PlayerState.CharacterType> personajesYaSeleccionados = new ArrayList<>();
    private IGameClient client;
    private List<EstadisticasJugador> estadisticasUltimaPartida = null;

    public static boolean modoMultijugador = false;

    /**
     * Constructor por defecto de JSonicJuego.
     * Inicializa la lógica base del juego.
     */
    public JSonicJuego() {
        super();
    }

    /**
     * Obtiene el cliente de juego actual.
     *
     * @return la instancia de IGameClient o null si no está asignada.
     */
    public IGameClient getClient() {
        return client;
    }

    /**
     * Devuelve el cliente si es una instancia de GameClient.
     *
     * @return GameClient o null en caso contrario.
     */
    public IGameClient getGameClient() {
        if (client instanceof GameClient) {
            // Si es un GameClient, lo devolvemos directamente.
            return client;
        }
        return null; // Si no es un GameClient, devolvemos null.
    }

    /**
     * Asigna las estadísticas de la última partida.
     *
     * @param estadisticas lista de estadísticas del jugador.
     */
    public void setEstadisticasUltimaPartida(List<EstadisticasJugador> estadisticas) {
        this.estadisticasUltimaPartida = estadisticas;
    }

    /**
     * Obtiene las estadísticas de la última partida jugada.
     *
     * @return lista de estadísticas del jugador.
     */
    public List<EstadisticasJugador> getEstadisticasUltimaPartida() {
        return this.estadisticasUltimaPartida;
    }


    /**
     * Inicializa recursos gráficos y de sonido, y carga la pantalla de menú.
     */
    @Override
    public void create() {
        batch = new SpriteBatch();

        assetManager = new AssetManager();
        soundManager = new SoundManager(assetManager);

        soundManager.loadClickSound("SoundsBackground/Boton_sonido.wav");
        soundManager.registerSound("recolectar_anillo", "SoundsBackground/RecoleccionAnillos.wav");
        soundManager.registerSound("recolectar_esmeralda", "SoundsBackground/EsmeraldasSonido.wav");
        soundManager.registerSound("explosion_bomba", "SoundsBackground/misil.wav");
        soundManager.registerSound("golpe", "SoundsBackground/golpeGeneral.wav");
        soundManager.registerSound("habilidad_knuckles_punch", "SoundsBackground/golpeknuckle.mp3");
        soundManager.registerSound("habilidad_Tails_punch", "SoundsBackground/robotsTails.wav");
        soundManager.registerSound("habilidad_Sonic_punch", "SoundsBackground/habilidadEspecialSonic.mp3");
        soundManager.registerSound("recolectar_basura", "SoundsBackground/recolectarBasura.mp3");
        soundManager.registerSound("spin", "SoundsBackground/spinSonic.mp3");
        assetManager.finishLoading();

        // Inicializar la pantalla de menú al iniciar el juego
        setPantallaActiva(new PantallaMenu(this));

    }

    /**
     * Obtiene el gestor de sonidos.
     *
     * @return la instancia de SoundManager.
     */
    public SoundManager getSoundManager() {
        return this.soundManager;
    }

    /**
     * Inicia el juego en modo de un solo jugador (offline).
     * Crea un servidor y un cliente locales que se ejecutan en memoria.
     */
    public void iniciarJuegoLocal() {
        System.out.println(">>> INICIANDO EN MODO LOCAL (UN JUGADOR)");
        // Crear el servidor local que manejará la lógica del juego.
        LocalServer server = new LocalServer();
        server.start();

        // Obtener el cliente asociado a ese servidor local.
        client = server.getClient();

        // Crear la pantalla de juego, inyectando el cliente y servidor locales.
        setPantallaActiva(new PantallaDeJuego(this, server));
    }

    /**
     * Inicia el juego en modo multijugador (online).
     * Se conecta a un servidor externo.
     */
    public void iniciarJuegoOnline() {
        System.out.println(">>> INICIANDO EN MODO ONLINE (MULTIJUGADOR)");
        // Crear la pantalla de juego, inyectando el cliente online y NINGÚN servidor local.
        setPantallaActiva(new PantallaDeJuego(this, null));
    }

    /**
     * Conecta al cliente al servidor especificado por la dirección IP.
     * Esta función se utiliza para iniciar el juego en modo multijugador online.
     */
    public void conectarAlServidor() {
        this.client = new GameClient();
        this.client.connect(direccionIp);
    }


    /**
     * Libera recursos y limpia datos al cerrar el juego.
     */
    @Override
    public void dispose() {
        if (personajesYaSeleccionados != null) {
            personajesYaSeleccionados.clear();
        }
        batch.dispose();
        assetManager.dispose();
        super.dispose();
    }

    /**
     * Reproduce el sonido de clic si el gestor de sonido está inicializado.
     */
    public void reproducirSonidoClick() {
        if (soundManager != null) {
            soundManager.playClickSound();
        }
    }


}
