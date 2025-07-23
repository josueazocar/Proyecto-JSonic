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

public class JSonicJuego extends JuegoBase {

    public SpriteBatch batch;
    //Para la musica y efectos de sonido.
    public AssetManager assetManager;
    public SoundManager soundManager;
    public static String direccionIp = "localhost"; // Dirección IP del servidor al que se conecta el cliente.
    public static ArrayList<PlayerState.CharacterType> personajesYaSeleccionados = new ArrayList<>();
    private IGameClient client;
    private List<EstadisticasJugador> estadisticasUltimaPartida = null;

    // true  -> Inicia en modo multijugador online.
    // false -> Inicia en modo de un jugador offline.
    public static boolean modoOnline = false;
    public static boolean modoMultijugador = false;

    public JSonicJuego() {
        super();
    }

    @Override
    public void create() {
        batch = new SpriteBatch();

        assetManager = new AssetManager();
        soundManager = new SoundManager(assetManager);

        soundManager.loadClickSound("SoundsBackground/Boton_sonido.wav");
        soundManager.registerSound("recolectar_anillo", "SoundsBackground/RecoleccionAnillos.wav");
        soundManager.registerSound("recolectar_esmeralda", "SoundsBackground/EsmeraldasSonido.wav");
        soundManager.registerSound("explosion_bomba", "SoundsBackground/misil.wav");
        soundManager.registerSound("golpe", "SoundsBackground/golpeGeneral.mp3");
        soundManager.registerSound("habilidad_knuckles_punch", "SoundsBackground/golpeknuckle.mp3");
        soundManager.registerSound("habilidad_Tails_punch", "SoundsBackground/robotsTails.mp3");
        soundManager.registerSound("habilidad_Sonic_punch", "SoundsBackground/habilidadEspecialSonic.mp3");

        assetManager.finishLoading();

        // Inicializar la pantalla de menú al iniciar el juego
        setPantallaActiva(new PantallaMenu(this));

    }

    // --- ¡AÑADE ESTE MÉTODO! ESTE ES EL QUE FALTABA ---
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

    public void conectarAlServidor(){
    this.client = new GameClient();
    this.client.connect(direccionIp);
    }

    @Override
    public void dispose() {
        if(personajesYaSeleccionados != null) {
            personajesYaSeleccionados.clear();
        }
        batch.dispose();
        assetManager.dispose();
        super.dispose();
    }

    public IGameClient getGameClient() {
        if (client instanceof GameClient) {
            // Si es un GameClient, lo devolvemos directamente.
            return client;
        }
        return null; // Si no es un GameClient, devolvemos null.
    }

    public void setEstadisticasUltimaPartida(List<EstadisticasJugador> estadisticas) {
        this.estadisticasUltimaPartida = estadisticas;
    }

    public List<EstadisticasJugador> getEstadisticasUltimaPartida() {
        return this.estadisticasUltimaPartida;
    }

    public void reproducirSonidoClick() {
        if (soundManager != null) {
            soundManager.playClickSound();
        }
    }

    public IGameClient getClient() {
        return client;
    }
}
