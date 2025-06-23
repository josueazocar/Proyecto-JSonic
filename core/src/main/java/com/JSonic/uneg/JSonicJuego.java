package com.JSonic.uneg;
import network.LocalServer;
import network.GameClient;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import network.interfaces.IGameClient;

public class JSonicJuego extends JuegoBase {

    public AssetManager assetManager;
    public SoundManager soundManager;
    public SpriteBatch batch;

    // true  -> Inicia en modo multijugador online.
    // false -> Inicia en modo de un jugador offline.
    public static boolean modoOnline = true;

    public JSonicJuego() {
        super();
    }

    @Override
    public void create() {

        batch = new SpriteBatch();
        assetManager = new AssetManager();
        soundManager = new SoundManager(assetManager);

        // Inicializar la pantalla de menú al iniciar el juego
        setPantallaActiva(new PantallaMenu(this));

    }

    /**
     * Inicia el juego en modo de un solo jugador (offline).
     * Crea un servidor y un cliente locales que se ejecutan en memoria.
     */
    public void iniciarJuegoLocal() {
        System.out.println(">>> INICIANDO EN MODO LOCAL (UN JUGADOR)");
        // 1. Crear el servidor local que manejará la lógica del juego.
        LocalServer server = new LocalServer();
        server.start();

        // 2. Obtener el cliente asociado a ese servidor local.
        IGameClient client = server.getClient();

        // 3. Crear la pantalla de juego, inyectando el cliente y servidor locales.
        setPantallaActiva(new PantallaDeJuego(this, client, server));
    }

    /**
     * Inicia el juego en modo multijugador (online).
     * Se conecta a un servidor externo.
     */
    public void iniciarJuegoOnline() {
        System.out.println(">>> INICIANDO EN MODO ONLINE (MULTIJUGADOR)");
        // 1. Crear el cliente de red real.
        // Nota: Le pasamos 'null' a PantallaDeJuego, así que la referencia interna no se usará.
        GameClient client = new GameClient(null);
        client.connect("localhost"); // Conecta al servidor en la misma máquina. Cambia "localhost" por una IP si es necesario.

        // 2. Crear la pantalla de juego, inyectando el cliente online y NINGÚN servidor local.
        setPantallaActiva(new PantallaDeJuego(this, client, null));
    }

    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
        assetManager.dispose();
        soundManager.dispose();
    }
}
