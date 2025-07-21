// Crear nuevo archivo: src/com/JSonic/uneg/ObjetosDelEntorno/EsmeraldaVisual.java

package com.JSonic.uneg.ObjetosDelEntorno;

import com.JSonic.uneg.State.ItemState;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class EsmeraldaVisual extends ItemVisual {

    public EsmeraldaVisual(ItemState estadoInicial) {
        super(estadoInicial);
    }

    @Override
    public void cargarAnimacion() {
        // Asegúrate de que la ruta a tu imagen de esmeraldas sea correcta.
        spriteSheet = new Texture(Gdx.files.internal("Items/esmeraldas.png"));

        // Dividimos la hoja de sprites. Asumimos 1 fila y 7 columnas.
        // El ancho de cada frame es el ancho total dividido por 7.
        // La altura es la altura total de la imagen.
        TextureRegion[][] tmp = TextureRegion.split(spriteSheet, spriteSheet.getWidth() / 10, spriteSheet.getHeight());

        // Creamos la animación con los frames.
        animacion = new Animation<>(0.15f, tmp[0]); // 0.15f es la velocidad de la animación, ajústala a tu gusto.
        animacion.setPlayMode(Animation.PlayMode.LOOP);

        // Actualizamos el hitbox con el tamaño real del frame.
        if (bounds != null) {
            bounds.setSize(tmp[0][0].getRegionWidth(), tmp[0][0].getRegionHeight());
        }
    }
}
