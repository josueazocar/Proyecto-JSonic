package com.JSonic.uneg.ObjetosDelEntorno;

import com.JSonic.uneg.State.ItemState;
import com.JSonic.uneg.State.PlayerState;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;


/**
 * Representa un punto de teletransporte visual que, al recolectarse,
 * teletransporta al jugador a una posición destino.
 */
public class TeletransporteVisual extends ItemVisual {
    private float destinoX, destinoY;
    private float stateTime = 0f;
    /**
     * Crea una instancia de TeletransporteVisual con estado y coordenadas destino.
     * @param estado Estado del ítem que contiene posición inicial y destino.
     */
    public TeletransporteVisual(ItemState estado) {
        super(estado);
        this.destinoX = estado.destinoX;
        this.destinoY = estado.destinoY;

    }

    /**
     * Carga la animación de teletransporte desde la hoja de sprites.
     */
    @Override
    public void cargarAnimacion() {
        spriteSheet = new Texture("Items/Teletransporte.png");
        int FRAME_COLS = 8;


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

    /**
     * Actualiza el tiempo de animación y dibuja el frame actual.
     * @param batch Lote de sprites utilizado para dibujar.
     * @param delta Tiempo transcurrido desde la última llamada.
     */
    public void render(com.badlogic.gdx.graphics.g2d.SpriteBatch batch, float delta) {
        stateTime += delta;
        com.badlogic.gdx.graphics.g2d.TextureRegion frame = animacion.getKeyFrame(stateTime, true);
        batch.draw(frame, estado.x, estado.y);
    }

    /**
     * Acción que ocurre al recolectar el ítem: teletransporta al jugador.
     * @param player Estado del jugador que será teletransportado.
     */
    public void onCollect(PlayerState player) {
        player.setPosition(destinoX, destinoY);
    }
}
