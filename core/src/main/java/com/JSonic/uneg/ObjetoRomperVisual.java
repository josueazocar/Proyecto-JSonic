package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class ObjetoRomperVisual {

    private TextureRegion[] frames;
    private int estadoActual; // 0: intacto, 1: dañado, 2: destruyéndose
    public Rectangle bounds;
    public float x, y;
    private float size;
    private boolean debeSerEliminado = false;

    // Variables para la animación de destrucción
    private float tiempoAnimacionDestruccion = 0f;
    private final float tiempoPorFrameDestruccion = 0.1f;
    private int frameActualDestruccion = 2; // La animación de destrucción empieza en el frame 2


    public ObjetoRomperVisual(float x, float y, float tileSize) {
        this.x = x;
        this.y = y;
        this.size = tileSize;
        this.estadoActual = 0;
        this.bounds = new Rectangle(x, y, size, size);

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

    public boolean debeSerEliminado() {
        return debeSerEliminado;
    }

    public Rectangle getBounds() {
        return bounds;
    }
}
