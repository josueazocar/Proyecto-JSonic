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
        spriteSheet = new Texture("Items/Telestransporte.png");
        int FRAME_COLS = 4; // Cambia este valor según la cantidad de columnas (frames) en la primera fila
        int FRAME_ROWS = 1; // Solo la primera fila

        TextureRegion[][] tmp = TextureRegion.split(
            spriteSheet,
            spriteSheet.getWidth() / FRAME_COLS,
            spriteSheet.getHeight() / FRAME_ROWS
        );

        TextureRegion[] frames = new TextureRegion[FRAME_COLS];
        for (int i = 0; i < FRAME_COLS; i++) {
            frames[i] = tmp[0][i]; // Solo la primera fila
        }
        animacion = new Animation<>(0.1f, frames);
    }


    public void onCollect(PlayerState player) {
        player.setPosition(destinoX, destinoY);
    }
}
