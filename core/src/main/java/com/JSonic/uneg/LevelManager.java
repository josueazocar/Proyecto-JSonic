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


    // --- AÑADIDO PARA GESTIÓN DE ANIMALES ---
    private Texture animalTexture; // Textura para todos los animales
    private ConcurrentHashMap<Integer, AnimalVisual> animalesVisuales; // Mapa para guardar los animales
    private Array<AnimalVisual> animales;
    //para los bloques u objetos irrompibles para otros jugadores menos para knuckles
    private Array<ObjetoRomperVisual> bloquesRompibles;
    private int proximoIdBloque = 30000;

    // Constructor que recibe la cámara y el SpriteBatch (aunque el batch no se use directamente aquí para dibujar el mapa)
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

    // --- NUEVO MÉTODO PARA ESTABLECER EL JUGADOR ---
    public void setPlayer(Player player) {
        this.player = player;
    }


    public Player getPlayer() {
        return player;
    }

    //para obtener los animales visuales
    public java.util.Collection<AnimalVisual> getAnimalesVisuales() {
        if (animalesVisuales == null) {
            // Devuelve una colección vacía para evitar NullPointerException
            return java.util.Collections.emptyList();
        }
        return animalesVisuales.values();
    }

    //-----------------------------------
    public void generarArbol(float x, float y) {
        // 1. Creamos el nuevo objeto Árbol
        Arbol_Tails nuevoArbol = new Arbol_Tails(x, y);

        // 2. Lo añadimos a la lista para que se dibuje
        arbolesGenerados.add(nuevoArbol);

        // 3. Añadimos su hitbox a nuestra lista de colisiones dinámicas
        colisionesDinamicas.add(nuevoArbol.getBounds());

        Gdx.app.log("LevelManager", "¡Árbol sembrado con éxito en " + x + ", " + y + "!");
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

        // El '1' es la escala, si tu mapa ya está a la resolución que quieres, déjalo en 1.
        renderizadorMapa = new OrthogonalTiledMapRenderer(mapaActual, 1);
        procesarPortales();

        // Inicializar la lista de bloques rompibles ---
        bloquesRompibles.clear();
        //generarBloquesRompibles(5);
    }

    public String getNombreMapaActual() {
        return nombreMapaActual;
    }

    // Metodo para generar bloques rompibles ---
    // Nuevo método para generar bloques en lugares válidos
    private void generarBloquesRompibles(int cantidad) {
        // Obtenemos la capa de colisiones como una capa genérica, sin forzar el tipo.
        com.badlogic.gdx.maps.MapLayer capaDeColision = mapaActual.getLayers().get("Colisiones");
        if (capaDeColision == null) {
            Gdx.app.error("LevelManager", "La capa de colisiones 'Colisiones' no se encontró en el mapa.");
            return;
        }

        // Obtenemos los objetos de colisión de esa capa.
        MapObjects objetosColision = capaDeColision.getObjects();
        Random random = new Random();
        float anchoBloque = 100f; // Usamos el tamaño del tile como tamaño del bloque. getTileWidth()
        float altoBloque = 100f; // .getTileHeight()

        for (int i = 0; i < cantidad; i++) {
            int intentos = 0;
            boolean posicionValida = false;
            float x = 0, y = 0;

            // Busca una posición que no colisione.
            do {
                // Genera una posición aleatoria en píxeles dentro del mapa.
                x = random.nextFloat() * (anchoMapaPixels - anchoBloque);
                y = random.nextFloat() * (altoMapaPixels - altoBloque);

                Rectangle boundsBloque = new Rectangle(x, y, anchoBloque, altoBloque);
                boolean chocaConAlgo = false;

                // Revisa si el nuevo bloque choca con algún objeto de colisión existente.
                for (com.badlogic.gdx.maps.MapObject obj : objetosColision) {
                    if (obj instanceof RectangleMapObject) {
                        Rectangle rectColision = ((RectangleMapObject) obj).getRectangle();
                        if (rectColision.overlaps(boundsBloque)) {
                            chocaConAlgo = true;
                            break; // Si choca con uno, no hace falta seguir revisando.
                        }
                    }
                }

                if (!chocaConAlgo) {
                    posicionValida = true; // ¡Encontramos una posición válida!
                }
                intentos++;
            } while (!posicionValida && intentos < 100); // Limita los intentos para evitar bucles infinitos.

            if (posicionValida) {
                // Asigna el ID al crear el bloque
                bloquesRompibles.add(new ObjetoRomperVisual(proximoIdBloque++, x, y, anchoBloque));
            }
        }
    }

    // --- NUEVO: Metodo para que Knuckles acceda a los bloques ---
    public Array<ObjetoRomperVisual> getBloquesRompibles() {
        return bloquesRompibles;
    }
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
    // ================== INICIO DEL CÓDIGO A AÑADIR ==================
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
    // =================== FIN DEL CÓDIGO A AÑADIR ===================

    // --- NUEVO: Método para dibujar los bloques rompibles ---
    public void dibujarBloques(SpriteBatch batch) {
        for (ObjetoRomperVisual bloque : bloquesRompibles) {
            bloque.draw(batch);
        }
    }
    //----------------------------------------------------------

    // --- AÑADIDO: Métodos para gestionar los animales ---

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
            // --- CASO 1: El animal visual YA EXISTE ---

            // Su estado interno se actualiza automáticamente porque 'visual.estado'
            // y 'estadoAnimal' apuntan a la misma información en tu mapa de estados.
            // Solo necesitamos llamar a su update() para que la parte VISUAL (animación) reaccione al cambio.
            visual.estado = estadoAnimal;
            visual.update();

        } else {
            // --- CASO 2: Es un ANIMAL NUEVO ---
            // Creamos un nuevo AnimalVisual usando el constructor corregido,
            // pasándole el objeto de estado completo.
            Gdx.app.log("LevelManager", "Creando nuevo animal visual con ID: " + estadoAnimal.id);
            AnimalVisual nuevoAnimal = new AnimalVisual(estadoAnimal, animalTexture); // ¡Constructor corregido!
            animalesVisuales.put(estadoAnimal.id, nuevoAnimal);
        }
    }

    /**
     * ---[CAMBIO]--- Nuevo método para limpiar los animales al cambiar de mapa.
     */
    public void limpiarAnimales() {
        if (animalesVisuales != null) {
            for (AnimalVisual animal : animalesVisuales.values()) {
                animal.dispose();
            }
            animalesVisuales.clear();
        }
    }
    //-------------------------------

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
        //----------------------------------------------------

        return false; // no hay colisión

    }

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
