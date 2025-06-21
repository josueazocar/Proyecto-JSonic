// Archivo: src/com/JSonic/uneg/ItemVisual.java
package com.JSonic.uneg;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public abstract class ItemVisual {

    public ItemState estado;
    protected Texture spriteSheet; // La hoja de sprites completa
    protected Animation<TextureRegion> animacion;
    protected float tiempoAnimacion;
    protected Rectangle bounds; // Hitbox para colisiones

    public ItemVisual(ItemState estadoInicial) {
        this.estado = estadoInicial;
        this.tiempoAnimacion = 0f;
        cargarAnimacion(); // Ahora cargamos una animación

        // Inicializamos el hitbox. Asumimos un tamaño, puedes ajustarlo.
        // Usaremos el tamaño del primer frame de la animación.
        if (animacion != null) {
            TextureRegion frameInicial = animacion.getKeyFrame(0);
            this.bounds = new Rectangle(estado.x, estado.y, frameInicial.getRegionWidth(), frameInicial.getRegionHeight());
        }
    }

    // Método abstracto para que cada clase hija cree su animación.
    public abstract void cargarAnimacion();

    // Actualiza la lógica del ítem (animación y posición del hitbox)
    public void update(float deltaTime) {
        tiempoAnimacion += deltaTime;
        // Mantenemos el hitbox sincronizado con la posición del estado
        if (bounds != null) {
            bounds.setPosition(estado.x, estado.y);
        }
    }

    // Dibuja el frame actual de la animación.
    public void draw(SpriteBatch batch) {
        if (animacion != null) {
            TextureRegion frameActual = animacion.getKeyFrame(tiempoAnimacion, true); // true para loop
            batch.draw(frameActual, estado.x, estado.y);
        }
    }

    // Getter para el hitbox
    public Rectangle getBounds() {
        return bounds;
    }

    // Libera la memoria de la hoja de sprites.
    public void dispose() {
        if (spriteSheet != null) {
            spriteSheet.dispose();
        }
    }
}
