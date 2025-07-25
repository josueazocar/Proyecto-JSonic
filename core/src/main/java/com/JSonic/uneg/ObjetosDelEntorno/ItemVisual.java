// Archivo: src/com/JSonic/uneg/ItemVisual.java
package com.JSonic.uneg.ObjetosDelEntorno;

import com.JSonic.uneg.EntidadesVisuales.Player;
import com.JSonic.uneg.State.ItemState;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

/**
 * Representa un elemento visual en el juego con animación y colisiones.
 */
public abstract class ItemVisual {

    public ItemState estado;
    protected Texture spriteSheet;
    protected Animation<TextureRegion> animacion;
    protected float tiempoAnimacion;
    protected Rectangle bounds; // Hitbox para colisiones

    /**
     * Crea un ItemVisual con un estado inicial y carga su animación.
     * @param estadoInicial Estado inicial del ítem.
     */
    public ItemVisual(ItemState estadoInicial) {
        this.estado = estadoInicial;
        this.tiempoAnimacion = 0f;
        cargarAnimacion();


        if (animacion != null) {
            TextureRegion frameInicial = animacion.getKeyFrame(0);
            this.bounds = new Rectangle(estado.x, estado.y, frameInicial.getRegionWidth(), frameInicial.getRegionHeight());
        }
    }

    /**
     * Método abstracto que debe implementar cada ítem para cargar su animación.
     */
    public abstract void cargarAnimacion();

    /**
     * Actualiza la lógica del ítem, animación y posición del hitbox.
     * @param deltaTime Tiempo transcurrido desde la última actualización.
     */
    public void update(float deltaTime) {
        tiempoAnimacion += deltaTime;
        // Mantenemos el hitbox sincronizado con la posición del estado
        if (bounds != null) {
            bounds.setPosition(estado.x, estado.y);
        }
    }

    /**
     * Dibuja el frame actual de la animación.
     * @param batch Lote de sprites usado para dibujar el ítem.
     */
    public void draw(SpriteBatch batch) {
        if (animacion != null) {
            TextureRegion frameActual = animacion.getKeyFrame(tiempoAnimacion, true); // true para loop
            batch.draw(frameActual, estado.x, estado.y);
        }
    }

    /**
     * Obtiene el rectángulo de colisiones (hitbox) del ítem.
     * @return Rectángulo de colisiones.
     */
    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * Obtiene la animación del ítem.
     * @return Animación de TextureRegion.
     */
    public Animation<TextureRegion> getAnimacion() {
        return animacion;
    }

    /**
     * Define la acción al recolectar el ítem por parte del jugador.
     * @param player Jugador que colecta el ítem.
     */
    public void onCollect(Player player){

    }

    /**
     * Libera los recursos de la hoja de sprites.
     */
    public void dispose() {
        if (spriteSheet != null) {
            spriteSheet.dispose();
        }
    }

}
