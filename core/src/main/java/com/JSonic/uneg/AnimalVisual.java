package com.JSonic.uneg;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

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

    // Constructor para crear el objeto visual del animal
    public AnimalVisual(int id, float x, float y, Texture spriteSheet) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.spriteSheet = spriteSheet;
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

    // Actualiza el estado del animal (llamado desde la lógica del juego)
    public void update(AnimalState state) {
        this.x = state.x;
        this.y = state.y;
        if (this.estaVivo && !state.estaVivo) {
            morir(); // Llama a morir si el estado cambió a no vivo
        }
        this.estaVivo = state.estaVivo;
    }

    // Cambia el estado y la animación a "muerto"
    public void morir() {
        if (estaVivo) {
            this.estaVivo = false;
            this.animacion = animacionMuerto; // Cambiamos a la animación de muerto
            this.tiempoAnimacion = 0; // Reiniciamos el tiempo para la nueva animación
        }
    }

    // Dibuja el animal en la pantalla
    public void draw(SpriteBatch batch, float delta) {
        if (animacion == null) return;

        tiempoAnimacion += delta;
        TextureRegion currentFrame = animacion.getKeyFrame(tiempoAnimacion, true);
        batch.draw(currentFrame, x, y);
    }

    public boolean estaVivo() {
        return estaVivo;
    }
}
