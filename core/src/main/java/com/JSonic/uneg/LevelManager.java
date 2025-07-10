package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch; // Necesario para el constructor, aunque no se usa directamente en dibujar()
import com.badlogic.gdx.math.MathUtils; // Importar para MathUtils.clamp
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LevelManager {

    private TiledMap mapaActual;
    private OrthogonalTiledMapRenderer renderizadorMapa;
    private final OrthographicCamera camaraJuego; // Referencia a la cámara principal del juego
    private String nombreMapaActual;
    private float anchoMapaPixels;
    private float altoMapaPixels;
    private int tileWidth;
    private int tileHeight;

    private Player player;


    // --- AÑADIDO PARA GESTIÓN DE ANIMALES ---
    private Texture animalTexture; // Textura para todos los animales
    private ConcurrentHashMap<Integer, AnimalVisual> animalesVisuales; // Mapa para guardar los animales


    // Constructor que recibe la cámara y el SpriteBatch (aunque el batch no se use directamente aquí para dibujar el mapa)
    public LevelManager(OrthographicCamera camara, SpriteBatch batch) {
        this.camaraJuego = camara;
        this.renderizadorMapa = null;
        this.mapaActual = null;
        //Inicilizamos el mapa de animales visuales para evitar errores
        this.animalesVisuales = new ConcurrentHashMap<>();
    }

    // --- NUEVO MÉTODO PARA ESTABLECER EL JUGADOR ---
    public void setPlayer(Player player) {
        this.player = player;
    }


    public Player getPlayer() {
        return player;
    }


    public com.badlogic.gdx.maps.tiled.TiledMap getTiledMap() {
        return mapaActual;
    }

    //para el teletransporte
    private List<TeletransporteVisual> portalesVisuales = new ArrayList<>();

    // Método para cargar un nivel (mapa Tiled)
    public void cargarNivel(String rutaMapa) {
        nombreMapaActual = rutaMapa;
        // Si ya hay un mapa cargado, liberamos sus recursos antes de cargar uno nuevo
        if (mapaActual != null) {
            mapaActual.dispose();
        }
        if (renderizadorMapa != null) {
            renderizadorMapa.dispose();
        }

        // --- AÑADIDO: Carga de recursos para el nivel ---
        // Si la textura ya existe, la liberamos antes de cargar una nueva
        if (animalTexture != null) {
            animalTexture.dispose();
        }
        // Cargamos la textura que usarán todos los animales de este nivel
        animalTexture = new Texture(Gdx.files.internal("Items/Conejo1.png"));
        animalesVisuales.clear(); // Limpiamos los animales del nivel anterior

        mapaActual = new TmxMapLoader().load(rutaMapa);


        // Obtener dimensiones del mapa en píxeles virtuales.
        // Asumiendo que todos los tiles del mapa tienen el mismo tamaño.
        tileWidth = mapaActual.getProperties().get("tilewidth", Integer.class);
        tileHeight = mapaActual.getProperties().get("tileheight", Integer.class);
        int mapWidthInTiles = mapaActual.getProperties().get("width", Integer.class);
        int mapHeightInTiles = mapaActual.getProperties().get("height", Integer.class);

        anchoMapaPixels = (float) mapWidthInTiles * tileWidth;
        altoMapaPixels = (float) mapHeightInTiles * tileHeight;
        Gdx.app.log("LevelManager", "Mapa cargado. Dimensiones: " + anchoMapaPixels + "x" + altoMapaPixels + " pixels.");

        // El '1' es la escala, si tu mapa ya está a la resolución que quieres, déjalo en 1.
        renderizadorMapa = new OrthogonalTiledMapRenderer(mapaActual, 1);
        procesarPortales();

    }

    public String getNombreMapaActual() {
        return nombreMapaActual;
    }


    // --- AÑADIDO: Métodos para gestionar los animales ---

    /**
     * Añade un nuevo animal visual al juego.
     * Se llama cuando el servidor informa de un nuevo animal.
     */
    public void agregarOActualizarAnimal(AnimalState estadoAnimal) {
        AnimalVisual visual = animalesVisuales.get(estadoAnimal.id);
        if (visual != null) {
            // Si el animal ya existe, solo actualizamos su estado
            visual.update(estadoAnimal);
        } else {
            // Si es un animal nuevo, lo creamos y lo añadimos al mapa
            Gdx.app.log("LevelManager", "Creando nuevo animal visual con ID: " + estadoAnimal.id);
            AnimalVisual nuevoAnimal = new AnimalVisual(estadoAnimal.id, estadoAnimal.x, estadoAnimal.y, animalTexture);
            animalesVisuales.put(estadoAnimal.id, nuevoAnimal);
        }
    }

    /**
     * Dibuja todos los animales en la pantalla.
     * Este método debe ser llamado desde la pantalla de juego principal.
     */
    public void dibujarAnimales(SpriteBatch batch, float delta) {
        if (animalesVisuales == null) return;
        for (AnimalVisual animal : animalesVisuales.values()) {
            animal.draw(batch, delta);
        }
    }

    //para la texture de los animales
    public Texture getAnimalTexture() {
        return this.animalTexture;
    }

    // Dentro de LevelManager.java

    private void procesarPortales() {
        portalesVisuales.clear();
        com.badlogic.gdx.maps.MapLayer capaDestinox = mapaActual.getLayers().get("destinox");
        if (capaDestinox == null) return;

        MapObjects objetos = capaDestinox.getObjects();
        for (com.badlogic.gdx.maps.MapObject obj : objetos) {
            if ("Portal".equals(obj.getName())) {
                float x = ((com.badlogic.gdx.maps.objects.RectangleMapObject)obj).getRectangle().x;
                float y = ((com.badlogic.gdx.maps.objects.RectangleMapObject)obj).getRectangle().y;
                float destinoX = obj.getProperties().get("destinoX", Float.class);
                float destinoY = obj.getProperties().get("destinoY", Float.class);
                String destinoMapa = obj.getProperties().get("destinoMapa", String.class);

                ItemState estado = new ItemState(x, y, ItemState.ItemType.TELETRANSPORTE, destinoX, destinoY, destinoMapa);
                TeletransporteVisual portalVisual = new TeletransporteVisual(estado);
                portalVisual.cargarAnimacion();
                portalesVisuales.add(portalVisual);
            }
        }
    }

    // Para dibujar los portales visuales
    public void dibujarPortales(SpriteBatch batch, float delta) {
        for (TeletransporteVisual portal : portalesVisuales) {
            portal.render(batch, delta);
        }
    }

    // Método para actualizar la lógica del nivel (si es necesario)
    public void actualizar(float deltaTime) {
        // En este caso, la cámara se actualiza y se limita en PantallaDeJuego,
        // no es necesario actualizar la cámara aquí.
    }

    // Método para dibujar el nivel en pantalla
    public void dibujar() {
        if (mapaActual == null || renderizadorMapa == null) {
            return;
        }
        // Configura el renderizador del mapa para usar la cámara actual
        renderizadorMapa.setView(camaraJuego);
        // Dibuja todas las capas del mapa
        renderizadorMapa.render();
    }

    //  MÉTODO PARA LIMITAR LA CÁMARA A LOS BORDES DEL MAPA ---
    public void limitarCamaraAMapa(OrthographicCamera camara) {
        // Si el mapa no está cargado o sus dimensiones no son válidas, no hacemos nada.
        if (mapaActual == null || anchoMapaPixels == 0 || altoMapaPixels == 0) {
            return;
        }

        // Calcula la mitad del ancho y alto visible por la cámara
        float camHalfWidth = camara.viewportWidth / 2f;
        float camHalfHeight = camara.viewportHeight / 2f;

        // Calcula los límites mínimos y máximos de la posición X e Y de la cámara
        // La cámara no puede ir más allá de los bordes del mapa.
        // Asegúrate de que el "centro" de la cámara esté siempre dentro de los límites.
        float minCamX = camHalfWidth;
        float maxCamX = anchoMapaPixels - camHalfWidth;
        float minCamY = camHalfHeight;
        float maxCamY = altoMapaPixels - camHalfHeight;

        // Aplica MathUtils.clamp para limitar la posición de la cámara
        camara.position.x = MathUtils.clamp(camara.position.x, minCamX, maxCamX);
        camara.position.y = MathUtils.clamp(camara.position.y, minCamY, maxCamY);

    }


    public TiledMap getMapaActual() {
        return mapaActual;
    }

    // Devuelve la posición del objeto Llegada en la capa "destinox"
    public com.badlogic.gdx.math.Vector2 obtenerPosicionLlegada() {
        com.badlogic.gdx.maps.MapLayer capaDestinox = mapaActual.getLayers().get("destinox");
        if (capaDestinox != null) {
            for (com.badlogic.gdx.maps.MapObject obj : capaDestinox.getObjects()) {
                if ("Llegada".equals(obj.getName()) && obj instanceof com.badlogic.gdx.maps.objects.RectangleMapObject rectObj) {
                    Rectangle rect = rectObj.getRectangle();
                    return new com.badlogic.gdx.math.Vector2(rect.x, rect.y);
                }
            }
        }
        // Valor por defecto si no se encuentra
        return new com.badlogic.gdx.math.Vector2(70f, 250f);
    }

    public static class PortalInfo {
        public float x, y;
        public float destinoX, destinoY;
        public String destinoMapa;
        public PortalInfo(float x, float y, float destinoX, float destinoY, String destinoMapa) {
            this.x = x;
            this.y = y;
            this.destinoX = destinoX;
            this.destinoY = destinoY;
            this.destinoMapa = destinoMapa;
        }
    }

    public List<PortalInfo> obtenerPortales() {
        List<PortalInfo> portales = new ArrayList<>();
        com.badlogic.gdx.maps.MapLayer capaDestinox = mapaActual.getLayers().get("destinox");
        if (capaDestinox != null) {
            for (com.badlogic.gdx.maps.MapObject obj : capaDestinox.getObjects()) {
                if ("Portal".equals(obj.getName()) && obj instanceof com.badlogic.gdx.maps.objects.RectangleMapObject rectObj) {
                    Rectangle rect = rectObj.getRectangle();
                    float destinoX = obj.getProperties().get("destinoX", Float.class);
                    float destinoY = obj.getProperties().get("destinoY", Float.class);
                    String destinoMapa = obj.getProperties().get("destinoMapa", String.class);
                    portales.add(new PortalInfo(rect.x, rect.y, destinoX, destinoY, destinoMapa));
                }
            }
        }
        return portales;
    }

    // --- Nuevos getters para las dimensiones del mapa (útiles para colisiones) ---
    public float getAnchoMapaPixels() {
        return anchoMapaPixels;
    }

    public float getAltoMapaPixels() {
        return altoMapaPixels;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }


public MapObjects getCollisionObjects() {
    if (mapaActual == null) {
        return null;
    }
    // Asumiendo que tu capa de colisiones se llama "Colisiones"
    com.badlogic.gdx.maps.MapLayer collisionLayer = mapaActual.getLayers().get("Colisiones");

    // Si la capa no existe, devolvemos null y lo manejamos en PantallaDeJuego
    if (collisionLayer == null) {
        return null;
    }

    return collisionLayer.getObjects();
}
    public boolean colisionaConMapa(Rectangle bounds) {
        MapObjects objetosColision = getCollisionObjects();
        if (objetosColision == null) return false;
        for (com.badlogic.gdx.maps.MapObject obj : objetosColision) {
            if (obj instanceof com.badlogic.gdx.maps.objects.RectangleMapObject) {
                Rectangle rect = ((com.badlogic.gdx.maps.objects.RectangleMapObject) obj).getRectangle();
                if (rect.overlaps(bounds)) {
                    return true; // hay colisión
                }
            }
        }
        return false; // no hay colisión
    }

    public Vector2 encontrarPosicionValida() {
        MapObjects objetosColision = getCollisionObjects();
        float mapWidth = getAnchoMapaPixels();
        float mapHeight = getAltoMapaPixels();

        boolean posicionValida = false;
        float x = 0, y = 0;
        int intentos = 0; // Para evitar un bucle infinito si el mapa está muy lleno

        while (!posicionValida && intentos < 100) {
            intentos++;
            // Genera una posición aleatoria dentro del mapa
            x = (float) (Math.random() * mapWidth);
            y = (float) (Math.random() * mapHeight);

            // Crea un rectángulo para la posición del animal (asumimos 32x32)
            Rectangle animalBounds = new Rectangle(x, y, 32, 32);

            // Revisa si choca con alguna pared
            boolean chocaConPared = false;
            if (objetosColision != null) {
                for (com.badlogic.gdx.maps.MapObject obj : objetosColision) {
                    if (obj instanceof RectangleMapObject) {
                        if (Intersector.overlaps(((RectangleMapObject) obj).getRectangle(), animalBounds)) {
                            chocaConPared = true;
                            break;
                        }
                    }
                }
            }

            if (!chocaConPared) {
                posicionValida = true; // ¡Lugar encontrado!
            }
        }

        // Si después de 100 intentos no encuentra lugar, al menos devuelve la última posición intentada.
        return new Vector2(x, y);
    }

    // Método para liberar los recursos del nivel
    public void dispose() {
        if (mapaActual != null) {
            mapaActual.dispose();
            mapaActual = null;
        }
        if (renderizadorMapa != null) {
            renderizadorMapa.dispose();
            renderizadorMapa = null;
        }
        // --- AÑADIDO: Liberar la textura del animal ---
        if (animalTexture != null) {
            animalTexture.dispose();
            animalTexture = null;
        }
    }

}
