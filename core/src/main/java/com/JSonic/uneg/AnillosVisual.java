// Archivo: src/com/JSonic/uneg/AnilloVisual.java
package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class AnillosVisual extends ItemVisual {

    public AnillosVisual(ItemState estadoInicial) {
        super(estadoInicial);
    }

    @Override
    public void cargarAnimacion() {
        // 1. Cargar la hoja de sprites
        spriteSheet = new Texture(Gdx.files.internal("Items/anillo.png"));

        // 2. Definir el tamaño de cada frame.
        // Tu imagen tiene 12 anillos. Asumamos que cada uno mide 32x32 píxeles.
        // Si no, ajusta estos valores. AnchoTotal / numFrames = AnchoFrame
        // Ejemplo: si la imagen mide 384px de ancho -> 384 / 12 = 32px por frame.
        int frameWidth = spriteSheet.getWidth() / 12;
        int frameHeight = spriteSheet.getHeight();

        // 3. Dividir la hoja en frames individuales
        TextureRegion[][] tmp = TextureRegion.split(spriteSheet, frameWidth, frameHeight);

        // 4. Crear un array con los frames de la animación

        int frameCount = 4;
        TextureRegion[] framesAnimacion = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            framesAnimacion[i] = tmp[0][i];
        }

        // Creamos la animación con una velocidad agradable (puedes ajustar 0.1f)
        animacion = new Animation<TextureRegion>(0.1f, framesAnimacion);
        animacion.setPlayMode(Animation.PlayMode.LOOP_PINGPONG); // <-- Ping-Pong para que vaya y vuelva suavemente
    }
}
