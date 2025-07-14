// Archivo: src/com/JSonic/uneg/AnimalVisual.java
package com.JSonic.uneg;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class AnimalVisual {

    // --- Referencia directa al estado (¡el patrón a seguir!) ---
    public AnimalState estado; // Puede ser 'public' o 'private' con un getter

    // --- Propiedades visuales (solo para el cliente) ---
    private transient Texture spriteSheet;
    private transient Animation<TextureRegion> animacionVivo;
    private transient Animation<TextureRegion> animacionMuerto;
    private transient Animation<TextureRegion> animacionActual;
    private float tiempoAnimacion = 0f;
    private transient Rectangle bounds;

    // El constructor ahora recibe el objeto de estado completo.
    public AnimalVisual(AnimalState estadoInicial, Texture spriteSheet) {
        this.estado = estadoInicial;
        this.spriteSheet = spriteSheet;
        this.bounds = new Rectangle(estado.x, estado.y, 0, 0); // El tamaño se ajusta al cargar la animación
        cargarAnimacion();
    }

    private void cargarAnimacion() {
        TextureRegion[][] tmp = TextureRegion.split(spriteSheet, spriteSheet.getWidth(), spriteSheet.getHeight() / 2);

        animacionVivo = new Animation<>(0.25f, tmp[0]);
        animacionVivo.setPlayMode(Animation.PlayMode.LOOP);

        animacionMuerto = new Animation<>(1f, tmp[1]);
        animacionMuerto.setPlayMode(Animation.PlayMode.NORMAL);

        // Ajustar el tamaño del hitbox basado en el frame
        this.bounds.setSize(tmp[0][0].getRegionWidth(), tmp[0][0].getRegionHeight());

        // La animación inicial depende del estado inicial.
        this.animacionActual = estado.estaVivo ? animacionVivo : animacionMuerto;
    }

    /**
     * El método update ahora es más simple.
     * Solo se encarga de la lógica visual, como cambiar de animación.
     */
    public void update() {
        // Sincroniza la posición del hitbox con la del estado
        this.bounds.setPosition(estado.x, estado.y);

        // Comprueba si el estado de vida ha cambiado para cambiar la animación
        Animation<TextureRegion> nuevaAnimacion = estado.estaVivo ? animacionVivo : animacionMuerto;
        if (this.animacionActual != nuevaAnimacion) {
            this.animacionActual = nuevaAnimacion;
            this.tiempoAnimacion = 0; // Reinicia la animación
        }
    }

    // El método draw ahora lee directamente del estado.
    public void draw(SpriteBatch batch, float delta) {
        if (animacionActual == null) return;

        tiempoAnimacion += delta;
        TextureRegion currentFrame = animacionActual.getKeyFrame(tiempoAnimacion);

        // Dibuja en la posición dictada por el objeto de estado
        batch.draw(currentFrame, estado.x, estado.y);
    }

    // Getters que delegan la información al objeto de estado
    public int getId() {
        return this.estado.id;
    }

    public boolean estaVivo() {
        return this.estado.estaVivo;
    }

    public Rectangle getBounds() {
        return this.bounds;
    }

    public void dispose() {
        // La gestión de texturas idealmente se hace con un AssetManager
        // para evitar liberar un recurso que otro objeto podría estar usando.
    }
}
