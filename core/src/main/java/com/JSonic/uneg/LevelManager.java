package com.JSonic.uneg;

import com.JSonic.uneg.EntidadesVisuales.AnimalVisual;
import com.JSonic.uneg.EntidadesVisuales.Player;
import com.JSonic.uneg.ObjetosDelEntorno.Arbol_Tails;
import com.JSonic.uneg.ObjetosDelEntorno.ObjetoRomperVisual;
import com.JSonic.uneg.ObjetosDelEntorno.TeletransporteVisual;
import com.JSonic.uneg.State.AnimalState;
import com.JSonic.uneg.State.ItemState;
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
import com.badlogic.gdx.utils.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import com.badlogic.gdx.math.Polygon;


/**
 * Gestor de niveles del juego.
 * Se encarga de cargar y renderizar mapas Tiled, generar entidades,
 * gestionar colisiones dinámicas, portales y sincronización con servidor.
 */
public class LevelManager {

    private Array<Arbol_Tails> arbolesGenerados;
    private Array<Rectangle> colisionesDinamicas;
    private TiledMap mapaActual;
    private OrthogonalTiledMapRenderer renderizadorMapa;
    private final OrthographicCamera camaraJuego; // Referencia a la cámara principal del juego
    private String nombreMapaActual;
    private float anchoMapaPixels;
    private float altoMapaPixels;
    private int tileWidth;
    private int tileHeight;

    private Player player;

    private Texture animalTexture; // Textura para todos los animales
    private ConcurrentHashMap<Integer, AnimalVisual> animalesVisuales; // Mapa para guardar los animales
    private Array<AnimalVisual> animales;
    //para los bloques u objetos irrompibles para otros jugadores menos para knuckles
    private Array<ObjetoRomperVisual> bloquesRompibles;
    private int proximoIdBloque = 30000;


    /**
     * Constructor principal de LevelManager.
     * @param camara cámara ortográfica para renderizado.
     * @param batch  SpriteBatch para dibujado de entidades.
     */
    public LevelManager(OrthographicCamera camara, SpriteBatch batch) {
        this.camaraJuego = camara;
        this.renderizadorMapa = null;
        this.mapaActual = null;

        //Inicilizamos el mapa de animales visuales para evitar errores
        this.animalesVisuales = new ConcurrentHashMap<>();
        this.bloquesRompibles = new Array<>();

        this.arbolesGenerados = new Array<>();
        this.colisionesDinamicas = new Array<>();

    }


    /**
     * Asigna el jugador principal para el nivel.
     * @param player instancia de Player.
     */
    public void setPlayer(Player player) {
        this.player = player;
    }


    /**
     * Obtiene el jugador asignado al nivel.
     * @return instancia de Player o null si no se estableció.
     */
    public Player getPlayer() {
        return player;
    }


    /**
     * Devuelve el ancho del mapa en píxeles virtuales.
     * @return ancho en píxeles.
     */
    public float getAnchoMapaPixels() {
        return anchoMapaPixels;
    }

    /**
     * Devuelve el alto del mapa en píxeles virtuales.
     * @return alto en píxeles.
     */
    public float getAltoMapaPixels() {
        return altoMapaPixels;
    }

    /**
     * Retorna el ancho de un tile en píxeles.
     * @return ancho del tile.
     */
    public int getTileWidth() {
        return tileWidth;
    }

    /**
     * Retorna el alto de un tile en píxeles.
     * @return alto del tile.
     */
    public int getTileHeight() {
        return tileHeight;
    }


    /**
     * Obtiene los objetos de colisión del mapa.
     * @return MapObjects de la capa "Colisiones" o null.
     */
    public MapObjects getCollisionObjects() {
        if (mapaActual == null) {
            return null;
        }

        com.badlogic.gdx.maps.MapLayer collisionLayer = mapaActual.getLayers().get("Colisiones");

        // Si la capa no existe, devolvemos null y lo manejamos en PantallaDeJuego
        if (collisionLayer == null) {
            return null;
        }

        return collisionLayer.getObjects();
    }

    /**
     * Retorna la colección de animales visuales activos.
     * @return colección de AnimalVisual.
     */
    public java.util.Collection<AnimalVisual> getAnimalesVisuales() {
        if (animalesVisuales == null) {
            return java.util.Collections.emptyList();
        }
        return animalesVisuales.values();
    }

    /**
     * Genera un árbol en la posición indicada y añade su colisión.
     * @param x coordenada X en píxeles.
     * @param y coordenada Y en píxeles.
     */
    public void generarArbol(float x, float y) {
        // Creamos el nuevo objeto Árbol
        Arbol_Tails nuevoArbol = new Arbol_Tails(x, y);

        // Lo añadimos a la lista para que se dibuje
        arbolesGenerados.add(nuevoArbol);

        // Añadimos su hitbox a nuestra lista de colisiones dinámicas
        colisionesDinamicas.add(nuevoArbol.getBounds());

        Gdx.app.log("LevelManager", "¡Árbol sembrado con éxito en " + x + ", " + y + "!");
    }

    /**
     * Obtiene el mapa Tiled actualmente cargado.
     * @return instancia de TiledMap o null.
     */
    public com.badlogic.gdx.maps.tiled.TiledMap getTiledMap() {
        return mapaActual;
    }

    private List<TeletransporteVisual> portalesVisuales = new ArrayList<>();

    /**
     * Carga un nivel desde el archivo TMX especificado.
     * @param rutaMapa ruta al fichero .tmx.
     */
    public void cargarNivel(String rutaMapa) {
        nombreMapaActual = rutaMapa;
        // Si ya hay un mapa cargado, liberamos sus recursos antes de cargar uno nuevo
        if (mapaActual != null) {
            mapaActual.dispose();
        }
        if (renderizadorMapa != null) {
            renderizadorMapa.dispose();
        }

        // Si la textura ya existe, la liberamos antes de cargar una nueva
        if (animalTexture != null) {
            animalTexture.dispose();
        }
        // Cargamos la textura que usarán todos los animales de este nivel
        animalTexture = new Texture(Gdx.files.internal("Items/Conejo1.png"));
        animalesVisuales.clear(); // Limpiamos los animales del nivel anterior


        for (Arbol_Tails arbol : arbolesGenerados) {
            arbol.dispose();
        }
        arbolesGenerados.clear();
        colisionesDinamicas.clear();

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

        renderizadorMapa = new OrthogonalTiledMapRenderer(mapaActual, 1);
        procesarPortales();

        // Inicializar la lista de bloques rompibles
        bloquesRompibles.clear();
    }

    /**
     * Devuelve la ruta del mapa cargado.
     * @return nombre (ruta) del mapa actual.
     */
    public String getNombreMapaActual() {
        return nombreMapaActual;
    }


    /**
     * Obtiene la lista de bloques rompibles activos.
     * @return Array de ObjetoRomperVisual.
     */
    public Array<ObjetoRomperVisual> getBloquesRompibles() {
        return bloquesRompibles;
    }
    /**
     * Crea bloques rompibles basados en datos recibidos del servidor.
     * @param datosDeBloques mapa de IDs a rectángulos.
     */
    public void crearBloquesDesdeServidor(HashMap<Integer, Rectangle> datosDeBloques) {
        bloquesRompibles.clear(); // Limpiamos cualquier bloque que pudiera existir.
        Gdx.app.log("LevelManager", "Recibiendo datos de bloques desde el servidor. Creando " + datosDeBloques.size() + " bloques.");

        for (java.util.Map.Entry<Integer, Rectangle> entry : datosDeBloques.entrySet()) {
            int id = entry.getKey();
            Rectangle rect = entry.getValue();
            // Creamos el ObjetoRomperVisual con la ID y posición exactas del servidor.
            bloquesRompibles.add(new ObjetoRomperVisual(id, rect.x, rect.y, rect.width));
        }
    }

    /**
     * Busca y devuelve un bloque rompible por su ID.
     * @param id identificador del bloque.
     * @return ObjetoRomperVisual o null si no existe.
     */
    public ObjetoRomperVisual getBloquePorId(int id) {
        for (ObjetoRomperVisual bloque : bloquesRompibles) {
            // Comparamos la ID de cada bloque con la ID que estamos buscando.
            if (bloque.id == id) {
                // Si encontramos una coincidencia, devolvemos ese objeto 'bloque' inmediatamente y salimos del metodo.
                return bloque;
            }
        }
        // Si el bucle termina y no hemos encontrado ninguna coincidencia,
        // significa que no hay ningún bloque con esa ID en la lista.
        return null;
    }


    /**
     * Dibuja todos los bloques rompibles usando el SpriteBatch.
     * @param batch SpriteBatch de renderizado.
     */
    public void dibujarBloques(SpriteBatch batch) {
        for (ObjetoRomperVisual bloque : bloquesRompibles) {
            bloque.draw(batch);
        }
    }

    /**
     * Procesa un mapa completo de estados de animales, creando nuevos animales visuales
     * o actualizando los existentes.
     * Este método es llamado cuando el cliente recibe un PaqueteActualizacionAnimales.
     *
     * @param estadosAnimales El HashMap que viene del paquete del servidor.
     */
    public void actualizarAnimalesDesdePaquete(java.util.HashMap<Integer, AnimalState> estadosAnimales) {
        if (estadosAnimales == null) return;

        // Itera sobre cada estado de animal recibido en el paquete para añadir/actualizar.
        for (AnimalState estado : estadosAnimales.values()) {
            agregarOActualizarAnimal(estado);
        }

        // Ahora, elimina los animales visuales que ya no existen en el servidor.
        java.util.Iterator<java.util.Map.Entry<Integer, AnimalVisual>> iter = animalesVisuales.entrySet().iterator();
        while (iter.hasNext()) {
            java.util.Map.Entry<Integer, AnimalVisual> entry = iter.next();
            if (!estadosAnimales.containsKey(entry.getKey())) {
                entry.getValue().dispose(); // Liberar recursos si es necesario
                iter.remove();
                Gdx.app.log("LevelManager", "Eliminado animal visual obsoleto con ID: " + entry.getKey());
            }
        }
    }

    /**
     * Añade un nuevo animal visual al juego.
     * Se llama cuando el servidor informa de un nuevo animal.
     */
    public void agregarOActualizarAnimal(AnimalState estadoAnimal) {
        // Busca si ya existe un objeto visual para este ID
        AnimalVisual visual = animalesVisuales.get(estadoAnimal.id);
        if (visual != null) {
            visual.estado = estadoAnimal;
            visual.update();

        } else {

            Gdx.app.log("LevelManager", "Creando nuevo animal visual con ID: " + estadoAnimal.id);
            AnimalVisual nuevoAnimal = new AnimalVisual(estadoAnimal, animalTexture); // ¡Constructor corregido!
            animalesVisuales.put(estadoAnimal.id, nuevoAnimal);
        }
    }


    /**
     * Limpia y libera todos los animales visuales del nivel.
     */
    public void limpiarAnimales() {
        if (animalesVisuales != null) {
            for (AnimalVisual animal : animalesVisuales.values()) {
                animal.dispose();
            }
            animalesVisuales.clear();
        }
    }


    /**
     * Dibuja todos los animales en pantalla.
     * @param batch SpriteBatch de renderizado.
     * @param delta tiempo transcurrido desde el último frame.
     */
    public void dibujarAnimales(SpriteBatch batch, float delta) {
        if (animalesVisuales == null) return;
        for (AnimalVisual animal : animalesVisuales.values()) {
            animal.draw(batch, delta);
        }
    }


    /**
     * Procesa la capa de portales del mapa y crea sus representaciones.
     */
    private void procesarPortales() {
        portalesVisuales.clear();
        com.badlogic.gdx.maps.MapLayer capaDestinox = mapaActual.getLayers().get("destinox");
        if (capaDestinox == null) return;

        MapObjects objetos = capaDestinox.getObjects();
        for (com.badlogic.gdx.maps.MapObject obj : objetos) {
            if ("Portal".equals(obj.getName())) {
                float x = ((com.badlogic.gdx.maps.objects.RectangleMapObject) obj).getRectangle().x;
                float y = ((com.badlogic.gdx.maps.objects.RectangleMapObject) obj).getRectangle().y;
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


    /**
     * Actualiza la lógica interna del nivel (bloques rompibles).
     * @param deltaTime tiempo desde el último frame.
     */
    public void actualizar(float deltaTime) {
        // En este caso, la cámara se actualiza y se limita en PantallaDeJuego,
        //Para actualizar y eliminar los bloques rompibles
        for (int i = bloquesRompibles.size - 1; i >= 0; i--) {
            ObjetoRomperVisual bloque = bloquesRompibles.get(i);
            bloque.update(deltaTime);
            if (bloque.debeSerEliminado()) {
                bloquesRompibles.removeIndex(i);
                Gdx.app.log("LevelManager", "Bloque rompible eliminado permanentemente.");
            }
        }
    }


    /**
     * Renderiza el mapa usando la cámara actual.
     */
    public void dibujar() {
        if (mapaActual == null || renderizadorMapa == null) {
            return;
        }
        // Configura el renderizador del mapa para usar la cámara actual
        renderizadorMapa.setView(camaraJuego);
        // Dibuja todas las capas del mapa
        renderizadorMapa.render();
    }


    /**
     * Restringe la posición de la cámara a los límites del mapa.
     * @param camara cámara ortográfica a restringir.
     */
    public void limitarCamaraAMapa(OrthographicCamera camara) {

        if (mapaActual == null || anchoMapaPixels == 0 || altoMapaPixels == 0) {
            return;
        }

        // Calcula la mitad del ancho y alto visible por la cámara
        float camHalfWidth = camara.viewportWidth / 2f;
        float camHalfHeight = camara.viewportHeight / 2f;

        // Calcula los límites mínimos y máximos de la posición X e Y de la cámara
        // La cámara no puede ir más allá de los bordes del mapa.
        float minCamX = camHalfWidth;
        float maxCamX = anchoMapaPixels - camHalfWidth;
        float minCamY = camHalfHeight;
        float maxCamY = altoMapaPixels - camHalfHeight;

        camara.position.x = MathUtils.clamp(camara.position.x, minCamX, maxCamX);
        camara.position.y = MathUtils.clamp(camara.position.y, minCamY, maxCamY);

    }

    /**
     * Obtiene la posición de llegada desde la capa "destinox".
     * @return Vector2 con las coordenadas de llegada.
     */
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

    /**
     * Información de un portal para menor acoplamiento.
     */
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

    /**
     * Recupera todos los portales definidos en el mapa.
     * @return lista de PortalInfo.
     */
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

    /**
     * Comprueba colisiones con el mapa, árboles y bloques.
     * @param bounds rectángulo de colisión de la entidad.
     * @return true si hay colisión, false en caso contrario.
     */
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

        for (Rectangle rect : colisionesDinamicas) {
            if (rect.overlaps(bounds)) {
                return true; // Colisión con un árbol
            }
        }
        //colisiones con robots con BLOQUES ROMPIBLES
        if (!bloquesRompibles.isEmpty()) {
            // Polígono temporal para representar los límites de la entidad que se mueve (enemigo/jugador)
            Polygon boundsPolygon = new Polygon();
            // Array para los vértices del polígono temporal, declarado fuera del bucle para eficiencia.
            float[] vertices = new float[8];

            for (ObjetoRomperVisual bloque : bloquesRompibles) {
                // Define los vértices del rectángulo de la entidad
                vertices[0] = bounds.x;
                vertices[1] = bounds.y;
                vertices[2] = bounds.x + bounds.width;
                vertices[3] = bounds.y;
                vertices[4] = bounds.x + bounds.width;
                vertices[5] = bounds.y + bounds.height;
                vertices[6] = bounds.x;
                vertices[7] = bounds.y + bounds.height;
                boundsPolygon.setVertices(vertices);

                // Comprueba si el polígono de la entidad se superpone con el polígono del bloque.
                if (Intersector.overlapConvexPolygons(bloque.getBounds(), boundsPolygon)) {
                    return true; // Colisión precisa con un bloque rompible
                }
            }
        }

        return false; // no hay colisión

    }

    /**
     * Dibuja todos los árboles generados en el nivel.
     * @param batch SpriteBatch para renderizado.
     */
    public void dibujarArboles(SpriteBatch batch) {
        for (Arbol_Tails arbol : arbolesGenerados) {
            arbol.draw(batch);
        }
    }


    /**
     * Busca en el mapa actual una capa de objeto que corresponda a una planta de tratamiento
     * y devuelve el rectángulo de su primer objeto.
     *
     * @return El Rectangle de la planta de tratamiento, o null si no se encuentra en el mapa actual.
     */
    public com.badlogic.gdx.math.Rectangle obtenerPlantaDeTratamiento() {
        // Nombres de las capas de objetos que estamos buscando.
        String[] nombresDeCapas = {"PlantaDeTratamientoN1", "PlantaDeTratamientoN2", "PlantaDeTratamientoN3"};

        if (mapaActual == null) {
            return null; // Seguridad por si se llama al método antes de cargar un mapa.
        }

        for (String nombreCapa : nombresDeCapas) {
            // Obtenemos la capa de objetos directamente por su nombre.
            com.badlogic.gdx.maps.MapLayer layer = mapaActual.getLayers().get(nombreCapa);

            // Comprobación de seguridad: nos aseguramos de que la capa exista y tenga al menos un objeto.
            if (layer != null && layer.getObjects().getCount() > 0) {
                // Asumimos que cada capa de planta tiene un solo objeto grande que la define.
                com.badlogic.gdx.maps.MapObject mapObject = layer.getObjects().get(0);

                if (mapObject instanceof com.badlogic.gdx.maps.objects.RectangleMapObject) {
                    // Si encontramos la planta, devolvemos su hitbox y terminamos la búsqueda.
                    return ((com.badlogic.gdx.maps.objects.RectangleMapObject) mapObject).getRectangle();
                }
            }
        }

        // Si después de buscar en todas las capas no encontramos ninguna, devolvemos null.
        return null;
    }

    /**
     * Busca en la capa de objetos "Esmeraldas" y devuelve la posición del primer
     * objeto que encuentre.
     * @return Un Vector2 con las coordenadas (x, y) de la esmeralda, o null si no se encuentra.
     */
    public Vector2 obtenerPosicionEsmeralda() {
        if (mapaActual == null) return null;

        com.badlogic.gdx.maps.MapLayer capaEsmeraldas = mapaActual.getLayers().get("Esmeraldas");
        if (capaEsmeraldas != null && capaEsmeraldas.getObjects().getCount() > 0) {
            // Asumimos que solo hay un objeto esmeralda por mapa
            com.badlogic.gdx.maps.MapObject obj = capaEsmeraldas.getObjects().get(0);
            if (obj instanceof com.badlogic.gdx.maps.objects.RectangleMapObject) {
                com.badlogic.gdx.math.Rectangle rect = ((com.badlogic.gdx.maps.objects.RectangleMapObject) obj).getRectangle();
                return new Vector2(rect.x, rect.y);
            }
        }
        // Si no se encuentra la capa o no hay objetos en ella, no hay esmeralda en este mapa.
        return null;
    }


    /**
     * Libera todos los recursos utilizados por el LevelManager.
     * Esto incluye el mapa actual, el renderizador, los animales visuales,
     * bloques rompibles y árboles generados.
     */
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
        limpiarAnimales();
        if (bloquesRompibles != null) {
            // Si los bloques tienen recursos propios (como Texturas), hay que liberarlos aquí.
            // Por ahora, solo limpiamos la lista.
            bloquesRompibles.clear();
        }
        if (arbolesGenerados != null) {
            for (Arbol_Tails arbol : arbolesGenerados) {
                arbol.dispose();
            }
            arbolesGenerados.clear();
        }
    }
}
