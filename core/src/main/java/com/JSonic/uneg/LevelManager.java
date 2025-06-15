package com.JSonic.uneg;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch; //Para que si despues se dibujan cosas en el mapa

// Clase para manejar la carga y renderizado de los niveles (mapas)
public class LevelManager {

    private TiledMap mapaActual; // El mapa Tiled que está cargado actualmente
    private OrthogonalTiledMapRenderer renderizadorMapa; // El encargado de dibujar el mapa
    private OrthographicCamera camaraJuego; // La cámara que verá el mapa

    // Constructor de la clase. Recibe la cámara que usará el nivel y el batch para dibujar.
    public LevelManager(OrthographicCamera camara, SpriteBatch batch) {
        this.camaraJuego = camara;
        // this.batchSprites = batch; // Puedes pasar el batch si el nivel va a dibujar sus propios objetos

        // Inicializamos el renderizador con un mapa nulo por ahora, se asignará al cargar
        this.renderizadorMapa = null;
        this.mapaActual = null;
    }

    /**
     * Carga un mapa Tiled específico.
     * @param rutaMapa La ruta al archivo .tmx dentro de la carpeta assets/ (ej. "maps/Zona1N1.tmx")
     */
    public void cargarNivel(String rutaMapa) {
        // Si ya hay un mapa cargado, liberamos sus recursos antes de cargar uno nuevo
        if (mapaActual != null) {
            mapaActual.dispose();
        }
        if (renderizadorMapa != null) {
            renderizadorMapa.dispose();
        }

        // Carga el nuevo mapa desde la ruta especificada
        mapaActual = new TmxMapLoader().load(rutaMapa);

        // Inicializa el renderizador del mapa con el mapa recién cargado y la escala de los tiles
        renderizadorMapa = new OrthogonalTiledMapRenderer(mapaActual,1);

        // LÓGICA FUTURA: Aquí podrías inicializar el jugador, enemigos y objetos específicos del nivel
        // dependiendo de las propiedades del mapa Tiled o de una capa de objetos en Tiled.
        // Por ejemplo, leer puntos de aparición para el jugador o posiciones de enemigos.
        // map.getLayers().get("ObjectsLayer").getObjects();
    }

    /**
     * Actualiza la lógica del nivel.
     * @param deltaTime El tiempo transcurrido desde el último frame (para movimientos suaves)
     */
    public void actualizar(float deltaTime) {
        // Por ahora, solo actualiza la cámara para asegurar que esté lista.
        // En el futuro, aquí iría la lógica de actualización para todos los elementos del nivel:
        // - Movimiento de enemigos
        // - Lógica de objetos interactivos
        // - Lógica de colisiones (aunque parte de esto puede ir en otras clases como PersonajeJugable)

        camaraJuego.update();
    }

    /**
     * Dibuja el mapa en pantalla usando la cámara actual.
     */
    public void dibujar() {
        if (mapaActual == null || renderizadorMapa == null) {

            return;
        }
        renderizadorMapa.setView(camaraJuego); // Le dice al renderizador qué es lo que la cámara está viendo
        renderizadorMapa.render(); // Dibuja el mapa

        // LÓGICA FUTURA: Aquí se dibujarían los elementos dinámicos del nivel (personajes, enemigos, etc.)
        // Esto generalmente se hace en la clase que orquesta el juego (GameScreen o Main)
        // para tener control sobre el SpriteBatch.
        // Si ManejadorNivel va a dibujar los elementos, necesitaría el SpriteBatch
        /*
        batchSprites.setProjectionMatrix(camaraJuego.combined);
        batchSprites.begin();
        // Dibujar jugador: jugador.dibujar(batchSprites);
        // Dibujar enemigos: for (Enemigo e : listaEnemigos) e.dibujar(batchSprites);
        batchSprites.end();
        */
    }

    /**
     * Libera los recursos del mapa y el renderizador cuando ya no se necesitan.
     * Es crucial para evitar fugas de memoria.
     */
    public void dispose() {
        if (mapaActual != null) {
            mapaActual.dispose();
        }
        if (renderizadorMapa != null) {
            renderizadorMapa.dispose();
        }
        // TODO: LÓGICA FUTURA: Liberar recursos de enemigos, objetos, etc., cargados por este nivel.
    }

    // --- Métodos adicionales que podrías necesitar ---

    /**
     * Obtiene el mapa Tiled actual. Útil para que otras clases accedan a sus capas y propiedades para colisiones.
     * @return El TiledMap actual.
     */
    public TiledMap getMapaActual() {
        return mapaActual;
    }

    /**
     * Ajusta la cámara para que se centre en una posición específica.
     * Esto lo llamaría tu clase principal (GameScreen) para centrar la cámara en el jugador.
     */
    public void centrarCamaraEn(float x, float y) {
        camaraJuego.position.set(x, y, 0);
       // camaraJuego.update();
    }
}
