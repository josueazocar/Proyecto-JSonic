package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;


// La clase PantallaDeJuego implementa la interfaz Screen de libGDX
public class PantallaDeJuego implements Screen {

    // --- Variables esenciales ---
    private OrthographicCamera camaraJuego; // La cámara que verá el mundo del juego
    private Viewport viewport; //El Viewport gestiona cómo la cámara se ajusta a la pantalla
    private SpriteBatch batchSprites; // Para dibujar todos los sprites (personajes, enemigos, etc.)

    // Dimensiones virtuales del juego. Estas son las "unidades del mundo" que la cámara intentará mostrar.
    // Al configurarlas igual que las dimensiones de la ventana (1040x900), 1 unidad del mundo = 1 píxel.
    public static final float VIRTUAL_WIDTH = 1040f;
    public static final float VIRTUAL_HEIGHT = 900f;

    // --- Manejador del nivel (mapa) ---
    private LevelManager manejadorNivel;

    // --- LÓGICA FUTURA: Instancias de personajes y listas de objetos dinámicos ---
    // private PersonajeJugable jugador;
    // private List<Enemigo> listaEnemigos;
    // private List<Anillo> listaAnillos;

    // Constructor de la clase PantallaDeJuego.
    // Aquí es donde se inicializan los elementos esenciales del juego.
    public PantallaDeJuego() {
        // Inicializa el SpriteBatch.
        batchSprites = new SpriteBatch();

        // Configura la cámara del juego.
        camaraJuego = new OrthographicCamera();
        // `setToOrtho(true, VIRTUAL_WIDTH, VIRTUAL_HEIGHT)` configura la cámara:
        // - `VIRTUAL_WIDTH`, `VIRTUAL_HEIGHT`: La cámara "verá" un área de este tamaño en unidades del mundo.
        camaraJuego.setToOrtho(false, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        // Inicialmente, centra la cámara en el medio de este mundo virtual.
        // Si más tarde sigues a un jugador, esta posición se actualizará.
        camaraJuego.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        camaraJuego.update(); // Siempre actualiza la cámara después de cambiar su posición o parámetros.

        //Inicializa el Viewport.
        // `FitViewport` ajusta el mundo virtual (VIRTUAL_WIDTH x VIRTUAL_HEIGHT) a la ventana real,
        // manteniendo la relación de aspecto y añadiendo barras negras si es necesario.
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camaraJuego);

        //Se Inicializa LevelManager, pasándole la cámara y el batch actualizados.
        manejadorNivel = new LevelManager(camaraJuego, batchSprites);

        // 5. Inicializa las listas para los objetos dinámicos (futuro).
        // listaEnemigos = new ArrayList<>();
        // listaAnillos = new ArrayList<>();
    }

    @Override
    public void show() {
        // Carga el nivel cuando la pantalla se muestra.
        manejadorNivel.cargarNivel("maps/Zona1N1.tmx");
        //Aqui va la logica para que la camara se centre en el jugador

        // Por ahora, la cámara se centra en el punto medio de su viewport virtual.
    }

    @Override
    public void render(float delta) {
        // Limpia la pantalla.
        Gdx.gl.glClearColor(0, 0, 0, 1); // Fondo negro
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // LÓGICA FUTURA: Actualización de todos los elementos del juego (jugador, enemigos, etc.)
        // jugador.actualizar(delta);
        // ...

        // Actualiza la cámara
        camaraJuego.update();

        // Actualiza y dibuja el nivel.
        manejadorNivel.actualizar(delta);
        manejadorNivel.dibujar(); // El renderizador del mapa usa la camaraJuego actualizada.

        // Configura el SpriteBatch para usar la matriz de proyección de la cámara.
        // Esto asegura que todo lo dibujado con batchSprites se posicione y escale
        batchSprites.setProjectionMatrix(camaraJuego.combined);
        batchSprites.begin();
        // LÓGICA FUTURA: Dibujar jugador, enemigos, anillos, HUD (interfaz de usuario)
        // Ejemplo: jugador.dibujar(batchSprites);
        batchSprites.end();
    }

    @Override
    public void resize(int width, int height) {
        // recalcular cómo el mundo virtual (VIRTUAL_WIDTH x VIRTUAL_HEIGHT) se ajusta a la nueva ventana.
        viewport.update(width, height);

        // Después de que el viewport se actualiza, la cámara puede haberse reubicado o ajustado.
        // A menudo, se quiere re-centrar la cámara.
        // Si la cámara sigue a un jugador, esta línea eventualmente será reemplazada por la lógica de seguimiento.
        // Por ahora, la volvemos a centrar en el medio de su mundo virtual.
        camaraJuego.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        camaraJuego.update(); // Actualiza la cámara para aplicar el nuevo centrado.
    }

    @Override
    public void pause() {
        // Código para pausar el juego
    }

    @Override
    public void resume() {
        // Código para reanudar el juego
    }

    @Override
    public void hide() {
        // Código cuando la pantalla se oculta (ej. al cambiar a otra pantalla)
    }

    @Override
    public void dispose() {
        // Libera todos los recursos cuando la pantalla ya no se necesita para evitar fugas de memoria.
        if (manejadorNivel != null) {
            manejadorNivel.dispose();
        }
        if (batchSprites != null) {
            batchSprites.dispose();
        }
        // LÓGICA FUTURA: Disponer de jugador, enemigos, etc.
    }
}
