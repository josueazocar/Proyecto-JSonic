package com.JSonic.uneg;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class AnimalVisual {

    // --- Propiedades del estado del animal (datos) ---
    public int id;
    public float x, y;
    public boolean estaVivo = true;

    // --- Propiedades visuales (solo para el cliente) ---
    private transient Texture spriteSheet; // 'transient' para que no se envíe por red
    private transient Animation<TextureRegion> animacion;
    private transient Animation<TextureRegion> animacionVivo;
    private transient Animation<TextureRegion> animacionMuerto;
    private float tiempoAnimacion = 0f;

    //para colisiones
    private transient Rectangle bounds;

    // Constructor para crear el objeto visual del animal
    public AnimalVisual(int id, float x, float y, Texture spriteSheet) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.spriteSheet = spriteSheet;
        this.bounds = new Rectangle(x, y, spriteSheet.getWidth(), spriteSheet.getHeight() / 2f);
        cargarAnimacion();
    }


    // Carga las animaciones desde la hoja de sprites
    private void cargarAnimacion() {
        // Asumimos que la textura tiene 2 filas: una para vivo, una para muerto
        TextureRegion[][] tmp = TextureRegion.split(spriteSheet, spriteSheet.getWidth(), spriteSheet.getHeight() / 2);

        // Fila 1: Animación del animal vivo
        animacionVivo = new Animation<>(0.25f, tmp[0]);
        animacionVivo.setPlayMode(Animation.PlayMode.LOOP);

        // Fila 2: Animación del animal muerto (un solo frame)
        animacionMuerto = new Animation<>(1f, tmp[1]);

        // La animación por defecto es la de "vivo"
        this.animacion = animacionVivo;
    }

    //método update
    public void update(AnimalState state) {
        this.x = state.x;
        this.y = state.y;
        this.bounds.setPosition(this.x, this.y);

        // Si el estado del servidor es diferente al estado actual
        if (this.estaVivo != state.estaVivo) {
            this.estaVivo = state.estaVivo;
            if (this.estaVivo) {
                // Si el animal revive (en caso de que implementes esa lógica)
                this.animacion = animacionVivo;
            } else {
                // Si el animal muere
                this.animacion = animacionMuerto;
            }
            this.tiempoAnimacion = 0; // Reinicia la animación al cambiar de estado
        }
    }

    // Método para cambiar el estado del animal a "muerto"

   // public void setEstaVivo(boolean vivo) {
      //  this.estaVivo = vivo;
    //}

    // Cambia el estado y la animación a "muerto"
    /*public void morir() {
        if (estaVivo) {
            this.estaVivo = false;
            this.animacion = animacionMuerto; // Cambiamos a la animación de muerto
            this.tiempoAnimacion = 0; // Reiniciamos el tiempo para la nueva animación
        }
    }*/

    // Dibuja el animal en la pantalla
    public void draw(SpriteBatch batch, float delta) {
        if (animacion == null) return;

        tiempoAnimacion += delta;
        TextureRegion currentFrame = animacion.getKeyFrame(tiempoAnimacion, true);
        batch.draw(currentFrame, x, y);
    }

    // Getter para el hitbox

    public int getId() {
        return this.id;
    }

    public boolean estaVivo() {
        return estaVivo;
    }



    /**
     * Cambia el estado de vida del animal.
     * Renombrado de setEstaVivo a setVivo para coincidir con PantallaDeJuego.
     */
    public void setVivo(boolean vivo) {
        this.estaVivo = vivo;
    }

    /**
     * Devuelve el rectángulo de colisión para ser usado en Intersector.overlaps().
     */
    public Rectangle getBounds() {
        return this.bounds;
    }


    public void dispose() {
        /*if (spriteSheet != null) {
            spriteSheet.dispose();
            spriteSheet = null;
        }*/
    }
}
