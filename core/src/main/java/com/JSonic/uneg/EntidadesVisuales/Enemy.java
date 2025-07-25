package com.JSonic.uneg.EntidadesVisuales;

import com.JSonic.uneg.State.EnemigoState;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.EnumMap;

/**
 * Clase base para los enemigos del juego.
 * Esta clase extiende Entity y maneja el estado y las animaciones de los enemigos.
 */
public class Enemy extends Entity {

    // Atributos
    public EnemigoState estado;
    protected Texture spriteSheet;
    protected TextureRegion frameActual;
    protected float tiempoXFrame;
    protected static final float COOLDOWN_ENTRE_GOLPES = 0.5f; // Medio segundo entre golpes
    protected float tiempoDesdeUltimoGolpe = 0f;

    // Animaciones
    protected EnumMap<EnemigoState.EstadoEnemigo, Animation<TextureRegion>> animations;
    protected TextureRegion[] frameIdleRight;
    protected TextureRegion[] frameIdleLeft;
    protected TextureRegion[] frameRunRight;
    protected TextureRegion[] frameRunLeft;
    protected TextureRegion[] frameHitRight;
    protected TextureRegion[] frameHitLeft;


    /**
     * Obtiene la vida actual del enemigo.
     * @return la vida actual.
     */
    @Override
    public int getVida() {
        return estado.vida;
    }

    /**
     * Establece los puntos de vida del enemigo.
     * @param vida puntos de vida a asignar.
     */
    @Override
    public void setVida(int vida) {
        this.estado.vida = vida;
    }

    /**
     * Inicializa los valores por defecto del enemigo.
     */
    @Override
    protected void setDefaultValues() {
    }

    /**
     * Marca al enemigo como golpeado, reiniciando el tiempo de cooldown entre golpes.
     */
    public void marcarComoGolpeado() {
        this.tiempoDesdeUltimoGolpe = COOLDOWN_ENTRE_GOLPES;
    }

    /**
     * Verifica si el enemigo ha sido golpeado recientemente.
     * @return true si está en el tiempo de cooldown de golpe, false en caso contrario.
     */
    public boolean haSidoGolpeadoRecientemente() {
        return tiempoDesdeUltimoGolpe > 0;
    }

    /**
     * Carga las texturas y animaciones del enemigo.
     */
    @Override
    protected void CargarSprites() {

    }

    /**
     * Maneja la entrada de teclas o controles para el enemigo.
     */
    @Override
    protected void KeyHandler() {
    }

    /**
     * Dibuja el enemigo usando el SpriteBatch proporcionado.
     * @param batch el lote de sprites para renderizar.
     */
    @Override
    public void draw(SpriteBatch batch) {

    }

    /**
     * Actualiza el estado interno del enemigo.
     * @param deltaTime tiempo transcurrido desde el último frame en segundos.
     */
    @Override
    public void update(float deltaTime) {
    }
}
