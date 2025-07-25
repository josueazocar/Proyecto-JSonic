package com.JSonic.uneg.ObjetosDelEntorno;

import com.JSonic.uneg.State.ItemState;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Clase visual para el objeto Basura, que representa un ítem estático en el juego.
 * Esta clase extiende ItemVisual y maneja la animación del ítem Basura.
 */
public class BasuraVisual extends ItemVisual {

    public BasuraVisual(ItemState estadoInicial) {
        super(estadoInicial);
    }

    /**
     * Carga la animación de la basura desde su sprite sheet.
     * En este caso, la basura es un ítem estático, por lo que solo se carga un frame.
     */
    @Override
    public void cargarAnimacion() {
        // Para un ítem estático, creamos una animación de un solo frame.
        spriteSheet = new Texture(Gdx.files.internal("Items/basura.png"));
        TextureRegion[] frames = new TextureRegion[1];
        frames[0] = new TextureRegion(spriteSheet);


        animacion = new Animation<TextureRegion>(1f, frames);
    }
}
