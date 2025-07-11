package com.JSonic.uneg;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class ObjetoRomperVisual {
    private int danio = 0;
    private boolean destruido = false;
    private int posicionX, posicionY, ancho, alto;

    private static Texture textura;
    private static TextureRegion[] frames;

    public ObjetoRomperVisual(int posicionX, int posicionY, int ancho, int alto) {
        this.posicionX = posicionX;
        this.posicionY = posicionY;
        this.ancho = ancho;
        this.alto = alto;
        cargarSprites();
    }

    private void cargarSprites() {
        if (textura == null) {
            textura = new Texture("BasuraDestruir.png");
            int anchoFrame = textura.getWidth() / 4; // Suponiendo 4 frames horizontales
            frames = new TextureRegion[4];
            for (int i = 0; i < 4; i++) {
                frames[i] = new TextureRegion(textura, i * anchoFrame, 0, anchoFrame, textura.getHeight());
            }
        }
    }

    // En ObjetoRomperVisual.java
    public Rectangle getBounds() {
        return new Rectangle(posicionX, posicionY, ancho, alto);
    }

    public void dibujar(Batch batch) {
        if (!destruido && frames != null) {
            batch.draw(frames[Math.min(danio, 3)], posicionX, posicionY, ancho, alto);
        }
    }

    public void setDanio(int danio) {
        this.danio = danio;
        if (danio >= 2) {
            this.destruido = true;
        }
    }

    public boolean estaDestruido() { return destruido; }
    public int obtenerFrame() { return danio; }
    public int getPosicionX() { return posicionX; }
    public int getPosicionY() { return posicionY; }
    public int getAncho() { return ancho; }
    public int getAlto() { return alto; }
}
