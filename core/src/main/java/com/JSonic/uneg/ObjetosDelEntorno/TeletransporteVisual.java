package com.JSonic.uneg.ObjetosDelEntorno;

import com.JSonic.uneg.State.ItemState;
import com.JSonic.uneg.State.PlayerState;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TeletransporteVisual extends ItemVisual {
    private float destinoX, destinoY;
    private float stateTime = 0f;
    public TeletransporteVisual(ItemState estado) {
        super(estado);
        this.destinoX = estado.destinoX;
        this.destinoY = estado.destinoY;

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

    //
    public void render(com.badlogic.gdx.graphics.g2d.SpriteBatch batch, float delta) {
        stateTime += delta;
        com.badlogic.gdx.graphics.g2d.TextureRegion frame = animacion.getKeyFrame(stateTime, true);
        batch.draw(frame, estado.x, estado.y);
    }

    public void onCollect(PlayerState player) {
        player.setPosition(destinoX, destinoY);
    }
}
