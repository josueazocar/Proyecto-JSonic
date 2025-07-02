package com.JSonic.uneg;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TeletransporteVisual extends ItemVisual {
    private float destinoX, destinoY;
    public TeletransporteVisual(ItemState estado) {
        super(estado);
        this.destinoX = destinoX;
        this.destinoY = destinoY;
    }

    @Override
    public void cargarAnimacion() {
        spriteSheet = new Texture("Items/Teletransporte.png");
        int FRAME_COLS = 8; // Cambia este valor según la cantidad de columnas (frames) en la primera fila


        TextureRegion[][] tmp = TextureRegion.split(
            spriteSheet,
            spriteSheet.getWidth() / FRAME_COLS,
            spriteSheet.getHeight()
        );

        TextureRegion[] frames = new TextureRegion[FRAME_COLS];
        for (int i = 0; i < FRAME_COLS; i++) {
            frames[i] = tmp[0][i];
        }
        animacion = new Animation<>(0.1f, frames);
    }


    public void onCollect(PlayerState player) {
        player.setPosition(destinoX, destinoY);
    }
}
