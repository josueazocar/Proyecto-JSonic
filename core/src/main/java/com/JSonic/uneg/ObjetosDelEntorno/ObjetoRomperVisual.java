package com.JSonic.uneg.ObjetosDelEntorno;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polygon;

/**
 * Representa un objeto en el juego que puede recibir golpes,
 * cambiar de estado y destruirse con animación.
 */
public class ObjetoRomperVisual {

    private TextureRegion[] frames;
    private Polygon bounds;
    private int estadoActual; // 0: intacto, 1: dañado, 2: destruyéndose
    public final int id;
    public float x, y;
    private float size;
    private boolean debeSerEliminado = false;

    // Variables para la animación de destrucción
    private float tiempoAnimacionDestruccion = 0f;
    private final float tiempoPorFrameDestruccion = 0.1f;
    private int frameActualDestruccion = 2;// La animación de destrucción empieza en el frame 2


    /**
     * Crea un objeto rompible con posición, tamaño e identificador.
     *
     * @param id       Identificador único del objeto.
     * @param x        Posición en el eje X.
     * @param y        Posición en el eje Y.
     * @param tileSize Tamaño del objeto.
     */
    public ObjetoRomperVisual(int id, float x, float y, float tileSize) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.size = tileSize;
        this.estadoActual = 0;

        // Define los vértices de la forma de tu hitbox.

        float[] vertices = new float[]{
            size * 0.95f, 0,
            size * 0.05f, 0,
            size * 0.85f, size * 0.4f,
            size * 0.5f, size * 0.55f,
            size * 0.15f, size * 0.4f
        };


        this.bounds = new Polygon(vertices);
        this.bounds.setPosition(x, y); // Mueve el polígono a su posición correcta en el mapa.

        Texture spriteSheet = new Texture("Items/BasuraDestruir.png");
        TextureRegion[][] tmp = TextureRegion.split(spriteSheet, spriteSheet.getWidth() / 2, spriteSheet.getHeight() / 2);

        frames = new TextureRegion[4];
        frames[0] = tmp[0][0]; // Estado 0: Intacto
        frames[1] = tmp[0][1]; // Estado 1: Dañado
        frames[2] = tmp[1][0]; // Estado 2: Animación de destrucción frame 1
        frames[3] = tmp[1][1]; // Estado 2: Animación de destrucción frame 2
    }

    /**
     * Cambia el estado del objeto al recibir un golpe hasta iniciar la destrucción.
     */
    public void recibirGolpe() {
        if (estadoActual < 2) {
            estadoActual++;
            Gdx.app.log("ObjetoRomperVisual", "Bloque golpeado. Nuevo estado: " + estadoActual);
        }
    }

    /**
     * Actualiza la animación de destrucción si el objeto está en ese estado.
     *
     * @param deltaTime Tiempo transcurrido desde la última actualización.
     */
    public void update(float deltaTime) {
        // Si el estado es 2, comienza la animación de destrucción.
        if (estadoActual == 2 && !debeSerEliminado) {
            tiempoAnimacionDestruccion += deltaTime;
            if (tiempoAnimacionDestruccion > tiempoPorFrameDestruccion) {
                tiempoAnimacionDestruccion = 0;
                frameActualDestruccion++;
                if (frameActualDestruccion >= frames.length) {
                    // La animación terminó. Marcar para eliminar.
                    this.debeSerEliminado = true;
                    Gdx.app.log("ObjetoRomperVisual", "Animación de destrucción completa. Marcado para eliminar.");
                }
            }
        }
    }

    /**
     * Dibuja el objeto según su estado actual.
     *
     * @param batch Lote de sprites usado para renderizar.
     */
    public void draw(SpriteBatch batch) {
        // No dibujar si ya debe ser eliminado
        if (debeSerEliminado) {
            return;
        }

        TextureRegion currentFrame = null;
        switch (estadoActual) {
            case 0: // Intacto
                currentFrame = frames[0];
                break;
            case 1: // Dañado
                currentFrame = frames[1];
                break;
            case 2: // Destruyéndose
                if (frameActualDestruccion < frames.length) {
                    currentFrame = frames[frameActualDestruccion];
                }
                break;
        }
        if (currentFrame != null) {
            batch.draw(currentFrame, x, y, size, size);
        }
    }

    /**
     * Inicia manualmente la destrucción del objeto si aún no lo está.
     */
    public void destruir() {
        // Solo inicia la destrucción si no está ya destruyéndose o marcado para eliminar.
        if (estadoActual < 2 && !debeSerEliminado) {
            estadoActual = 2; // Pasa directamente al estado de destrucción.
        }
    }

    /**
     * Indica si el objeto ha completado su animación de destrucción
     * y debe ser eliminado del juego.
     *
     * @return true si debe ser eliminado.
     */
    public boolean debeSerEliminado() {
        return debeSerEliminado;
    }

    /**
     * Obtiene el polígono de colisión asociado al objeto.
     *
     * @return Hitbox como un objeto Polygon.
     */
    public Polygon getBounds() {
        return bounds;
    }
}
