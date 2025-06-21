// Archivo: src/com/JSonic/uneg/BasuraVisual.java
package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class BasuraVisual extends ItemVisual {

    public BasuraVisual(ItemState estadoInicial) {
        super(estadoInicial);
    }

    @Override
    public void cargarAnimacion() {
        // Para un ítem estático, creamos una animación de un solo frame.
        spriteSheet = new Texture(Gdx.files.internal("Items/basura.png"));
        TextureRegion[] frames = new TextureRegion[1];
        frames[0] = new TextureRegion(spriteSheet);

        // La duración no importa para un solo frame.
        animacion = new Animation<TextureRegion>(1f, frames);
    }
}
