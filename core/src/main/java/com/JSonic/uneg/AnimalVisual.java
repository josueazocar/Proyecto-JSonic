package com.JSonic.uneg;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class AnimalVisual extends ItemVisual {

    private boolean estaVivo = true;
    private Animation<TextureRegion> animacionVivo;
    private Animation<TextureRegion> animacionMuerto;

    // ---[MODIFICADO]--- Constructor que ahora solo necesita la posición
    public AnimalVisual(float x, float y) {
        super(new ItemState(x, y));
        // Cargamos la textura directamente desde la ruta del archivo
        this.spriteSheet = new Texture("Items/Conejo1.png");
        cargarAnimacion();
    }

    @Override
    public void cargarAnimacion() {
        TextureRegion[][] tmp = TextureRegion.split(spriteSheet, spriteSheet.getWidth(), spriteSheet.getHeight() / 2);

        // Fila 1: Animación del animal vivo
        animacionVivo = new Animation<>(0.25f, tmp[0]); // 0.25f es un ejemplo de velocidad
        animacionVivo.setPlayMode(Animation.PlayMode.LOOP);

        // Fila 2: Animación del animal muerto (puede ser un solo frame)
        animacionMuerto = new Animation<>(1f, tmp[1]);

        // La animación por defecto es la de "vivo"
        this.animacion = animacionVivo;
    }

    // Nuevo método para que el animal muera
    public void morir() {
        if (estaVivo) {
            this.estaVivo = false;
            this.animacion = animacionMuerto; // Cambiamos a la animación de muerto
            this.tiempoAnimacion = 0; // Reiniciamos el tiempo para la nueva animación
        }
    }

    public boolean estaVivo() {
        return estaVivo;
    }

    @Override
    public void draw(SpriteBatch batch) {
        // El método draw del padre ya dibuja la animación correcta que hemos asignado
        super.draw(batch);
    }
}
