package com.JSonic.uneg.ObjetosDelEntorno;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/**
 * Clase visual para representar un árbol usado por el dron de Tails.
 * Incluye el sprite y el hitbox para colisiones.
 */
public class Arbol_Tails {

    private Texture texture;
    private Rectangle bounds;
    private float x, y;

    // El tamaño del sprite del árbol
    private static final float ANCHO = 64;
    private static final float ALTO = 64;

    /**
     * Crea un árbol en la posición especificada y define su hitbox de colisión.
     *
     * @param x coordenada X inicial del árbol.
     * @param y coordenada Y inicial del árbol.
     */
    public Arbol_Tails(float x, float y) {
        this.x = x;
        this.y = y;
        this.texture = new Texture(Gdx.files.internal("Entidades/Dron/Arbol_Dron.png"));

        // Define el rectángulo de colision
        float hitboxAncho = ANCHO * 0.4f;
        float hitboxAlto = ALTO * 0.5f;
        float hitboxOffsetX = (ANCHO - hitboxAncho) / 2;

        this.bounds = new Rectangle(x + hitboxOffsetX, y, hitboxAncho, hitboxAlto);
    }

    /**
     * Dibuja el árbol en pantalla.
     *
     * @param batch SpriteBatch usado para renderizar el sprite.
     */
    public void draw(SpriteBatch batch) {
        batch.draw(texture, x, y, ANCHO, ALTO);
    }

    /**
     * Obtiene el hitbox del árbol para detectar colisiones.
     *
     * @return Rectángulo que representa el área de colisión.
     */
    public Rectangle getBounds() {
        return bounds;
    }

    public void dispose() {
        texture.dispose();
    }
}
