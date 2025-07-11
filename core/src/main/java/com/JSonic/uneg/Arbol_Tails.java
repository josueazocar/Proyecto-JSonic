package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Arbol_Tails {

    private Texture texture;
    private Rectangle bounds;
    private float x, y;

    // El tamaño del sprite del árbol
    private static final float ANCHO = 64; // Ajusta el ancho según tu sprite
    private static final float ALTO = 64; // Ajusta el alto según tu sprite

    public Arbol_Tails(float x, float y) {
        this.x = x;
        this.y = y;
        this.texture = new Texture(Gdx.files.internal("Entidades/Dron/Arbol_Dron.png")); // Asegúrate de tener esta imagen

        // Definimos el rectángulo de colisión.
        // Puede ser más pequeño que el sprite, por ejemplo, solo el tronco.
        float hitboxAncho = ANCHO * 0.4f; // 40% del ancho
        float hitboxAlto = ALTO * 0.5f;  // 50% del alto
        float hitboxOffsetX = (ANCHO - hitboxAncho) / 2; // Para centrar el hitbox

        this.bounds = new Rectangle(x + hitboxOffsetX, y, hitboxAncho, hitboxAlto);
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, x, y, ANCHO, ALTO);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void dispose() {
        texture.dispose();
    }
}
