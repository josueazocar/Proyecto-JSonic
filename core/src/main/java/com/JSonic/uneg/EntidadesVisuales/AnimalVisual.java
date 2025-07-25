package com.JSonic.uneg.EntidadesVisuales;

import com.JSonic.uneg.State.AnimalState;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

/**
 * Clase que representa la parte visual de un animal en el juego.
 * Maneja las animaciones y el estado visual del animal.
 */
public class AnimalVisual {

    /** El estado del animal, que contiene su posición y estado de vida.
     * Este objeto se actualiza constantemente para reflejar los cambios en el juego.
     */
    public AnimalState estado;

    /**
     * La textura del sprite sheet que contiene las animaciones del animal.
     */
    private transient Texture spriteSheet;
    private transient Animation<TextureRegion> animacionVivo;
    private transient Animation<TextureRegion> animacionMuerto;
    private transient Animation<TextureRegion> animacionActual;
    private float tiempoAnimacion = 0f;
    private transient Rectangle bounds;

    /**
     * Constructor de la clase AnimalVisual.
     *
     * @param estadoInicial El estado inicial del animal, que contiene su posición y estado de vida.
     * @param spriteSheet   La textura del sprite sheet que contiene las animaciones del animal.
     */
    public AnimalVisual(AnimalState estadoInicial, Texture spriteSheet) {
        this.estado = estadoInicial;
        this.spriteSheet = spriteSheet;
        this.bounds = new Rectangle(estado.x, estado.y, 0, 0); // El tamaño se ajusta al cargar la animación
        cargarAnimacion();
    }

    public boolean estaVivo() {
        return this.estado.estaVivo;
    }

    /**
     * Carga las animaciones del animal desde el sprite sheet.
     * Divide el sprite sheet en dos partes: una para el estado vivo y otra para el estado muerto.
     */
    private void cargarAnimacion() {
        TextureRegion[][] tmp = TextureRegion.split(spriteSheet, spriteSheet.getWidth(), spriteSheet.getHeight() / 2);

        animacionVivo = new Animation<>(0.25f, tmp[0]);

        // Ajustar el tamaño del hitbox basado en el frame
        this.bounds.setSize(tmp[0][0].getRegionWidth(), tmp[0][0].getRegionHeight());

        // La animación inicial depende del estado inicial.
        this.animacionActual = estado.estaVivo ? animacionVivo : animacionMuerto;
    }

    /**
     * Actualiza el estado visual del animal.
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

    /**
     * Dibuja la animación actual del animal en la pantalla.
     *
     * @param batch El SpriteBatch utilizado para dibujar la animación.
     * @param delta El tiempo transcurrido desde el último frame, utilizado para actualizar la animación.
     */
    public void draw(SpriteBatch batch, float delta) {
        if (animacionActual == null) return;

        tiempoAnimacion += delta;
        TextureRegion currentFrame = animacionActual.getKeyFrame(tiempoAnimacion);

        // Dibuja en la posición dictada por el objeto de estado
        batch.draw(currentFrame, estado.x, estado.y);
    }

    /**
     * Limpia los recursos utilizados por el animal visual.
     */
    public void dispose() {
    }
}
