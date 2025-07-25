// Crear nuevo archivo: src/com/JSonic/uneg/ObjetosDelEntorno/EsmeraldaVisual.java

package com.JSonic.uneg.ObjetosDelEntorno;

import com.JSonic.uneg.State.ItemState;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Clase visual para las esmeraldas, gestiona la animación del objeto Esmeralda en pantalla.
 * Extiende ItemVisual para heredar funcionalidades comunes de los objetos del entorno.
 */
public class EsmeraldaVisual extends ItemVisual {

    public EsmeraldaVisual(ItemState estadoInicial) {
        super(estadoInicial);
    }

    /**
     * Carga la animación de las esmeraldas desde su sprite sheet.
     * Define los frames y configura la animación en bucle.
     */
    @Override
    public void cargarAnimacion() {
        spriteSheet = new Texture(Gdx.files.internal("Items/esmeraldas.png"));

        TextureRegion[][] tmp = TextureRegion.split(spriteSheet, spriteSheet.getWidth() / 10, spriteSheet.getHeight());

        // Creamos la animación con los frames
        animacion = new Animation<>(0.15f, tmp[0]); // 0.15f es la velocidad de la animación, ajústala a tu gusto.
        animacion.setPlayMode(Animation.PlayMode.LOOP);

        // Actualiza el hitbox con el tamaño real del frame.
        if (bounds != null) {
            bounds.setSize(tmp[0][0].getRegionWidth(), tmp[0][0].getRegionHeight());
        }
    }
}
