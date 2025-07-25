package com.JSonic.uneg.ObjetosDelEntorno;

import com.JSonic.uneg.State.ItemState;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Clase visual para los anillos, gestiona la animación del objeto Anillos en pantalla.
 */
public class AnillosVisual extends ItemVisual {

    /**
     * Constructor de AnillosVisual.
     *
     * @param estadoInicial Estado inicial del objeto Anillos.
     */
    public AnillosVisual(ItemState estadoInicial) {
        super(estadoInicial);
    }

    /**
     * Carga la animación de los anillos desde su sprite sheet.
     * Define los frames y configura la animación en bucle ping-pong.
     */
    @Override
    public void cargarAnimacion() {
        // Carga la hoja de sprites
        spriteSheet = new Texture(Gdx.files.internal("Items/ring.png"));

        // Definir el tamaño de cada frame.
        int frameCount = 4;
        int frameWidth = spriteSheet.getWidth() / frameCount;
        int frameHeight = spriteSheet.getHeight();

        TextureRegion[][] tmp = TextureRegion.split(spriteSheet, frameWidth, frameHeight);

        TextureRegion[] framesAnimacion = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            framesAnimacion[i] = tmp[0][i];
        }

        animacion = new Animation<TextureRegion>(0.1f, framesAnimacion);
        animacion.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
    }
}
