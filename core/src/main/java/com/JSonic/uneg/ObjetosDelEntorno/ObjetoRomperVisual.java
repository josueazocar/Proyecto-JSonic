package com.JSonic.uneg.ObjetosDelEntorno;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polygon;

// Esta clase representa un objeto que puede ser destruido visualmente en el juego.
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


    public ObjetoRomperVisual(int id,float x, float y, float tileSize) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.size = tileSize;
        this.estadoActual = 0;

        // Define los vértices de la forma de tu hitbox.

        float[] vertices = new float[]{
                size * 0.05f, 0,                // Base izquierda (se mantiene igual)
                size * 0.95f, 0,                // Base derecha (se mantiene igual)
                size * 0.85f, size * 0.4f,      // Lado derecho, ahora a un 40% de la altura
                size * 0.5f,  size * 0.55f,     // Punta del montículo, ahora a un 55% de la altura (antes 80%)
                size * 0.15f, size * 0.4f       // Lado izquierdo, ahora a un 40% de la altura
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

    // Knuckles llama a este método. Solo cambia el estado.
    public void recibirGolpe() {
        if (estadoActual < 2) {
            estadoActual++;
            Gdx.app.log("ObjetoRomperVisual", "Bloque golpeado. Nuevo estado: " + estadoActual);
        }
    }

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

    //Nuevo metodo para destruir
    public void destruir() {
        // Solo inicia la destrucción si no está ya destruyéndose o marcado para eliminar.
        if (estadoActual < 2 && !debeSerEliminado) {
            estadoActual = 2; // Pasa directamente al estado de destrucción.
            Gdx.app.log("ObjetoRomperVisual", "¡Orden de destrucción recibida! Nuevo estado: " + estadoActual);
        }
    }

    public boolean debeSerEliminado() {
        return debeSerEliminado;
    }

    public Polygon getBounds() {
        return bounds;
    }
}
