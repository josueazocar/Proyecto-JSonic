package com.JSonic.uneg.ObjetosDelEntorno;

import com.JSonic.uneg.State.ItemState;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Clase visual para la pieza de plástico en el juego.
 * Extiende ItemVisual para manejar la animación y el estado visual de la pieza.
 */
public class PiezaDePlasticoVisual extends ItemVisual {

    /**
     * Constructor de la clase PiezaDePlasticoVisual.
     * Inicializa el estado del objeto y carga la animación.
     *
     * @param estadoInicial El estado inicial del objeto, que contiene su posición y estado visual.
     */
    public PiezaDePlasticoVisual(ItemState estadoInicial) {
        super(estadoInicial);
    }

    /**
     * Carga la animación de la pieza de plástico.
     * Utiliza un sprite sheet para crear una animación con un solo frame.
     */
    @Override
    public void cargarAnimacion() {

        spriteSheet = new Texture(Gdx.files.internal("Items/Pieza_plastico.png"));
        TextureRegion[] frames = new TextureRegion[1];
        frames[0] = new TextureRegion(spriteSheet);

        animacion = new Animation<TextureRegion>(1f, frames);
    }
}
