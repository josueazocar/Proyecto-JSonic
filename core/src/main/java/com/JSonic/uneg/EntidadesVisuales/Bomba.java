// Archivo: src/com/JSonic/uneg/Bomba.java
package com.JSonic.uneg.EntidadesVisuales;

import com.JSonic.uneg.SoundManager;
import com.JSonic.uneg.State.EnemigoState;
import com.JSonic.uneg.State.EnemigoState.EstadoEnemigo;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.EnumMap;

public class Bomba {

    // --- VARIABLES ORIGINALES ---
    private TextureRegion[] frameUp;
    private TextureRegion[] frameDown;
    private TextureRegion[] frameLeft;
    private TextureRegion[] frameRight;
    private TextureRegion[] frameExplosion;
    protected Texture spriteSheet;
    private TextureRegion frameActual;
    private float tiempoXFrame;
    public EnemigoState estado;
    private EnumMap<EnemigoState.EstadoEnemigo, Animation<TextureRegion>> animations;

    // --- Lógica interna de la bomba ---
    private final Vector2 velocidad;
    private float tiempoDeVida;
    private boolean explotando = false;
    private boolean paraEliminar = false;
    private boolean yaHizoDanio = false;

    // --- CONSTANTES ---
    private static final float DURACION_EXPLOSION = 0.08f * 6;
    private float tiempoExplosionRestante = DURACION_EXPLOSION;

    // [AGREGADO] Radio de proximidad para que la bomba explote cerca del jugador.
    private static final float RADIO_PROXIMIDAD = 50f; // Puedes ajustar este valor
    private transient SoundManager soundManager;

    public Bomba(EnemigoState estadoInicial, Vector2 velocidad, float tiempoDeVida, SoundManager soundManager) {
        this.estado = estadoInicial;
        this.velocidad = velocidad;
        this.tiempoDeVida = tiempoDeVida;
        this.soundManager = soundManager;
        this.tiempoXFrame = 0.0f;
        this.animations = new EnumMap<>(EnemigoState.EstadoEnemigo.class);

        CargarSprites();
    }

    protected String getSpriteSheetPath() {
        return "Entidades/Enemy/Robotnik/Bomba/drEggmanEfects.png";
    }

    protected void CargarSprites() {
        String spriteSheetPath = getSpriteSheetPath();
        if (!Gdx.files.internal(spriteSheetPath).exists()) {
            Gdx.app.error("Bomba", "Error: Archivo de hoja de sprites no encontrado: " + spriteSheetPath);
            throw new RuntimeException("Sprite sheet missing: " + spriteSheetPath);
        }

        spriteSheet = new Texture(Gdx.files.internal(spriteSheetPath));
        TextureRegion[][] matrizDeSprites = TextureRegion.split(spriteSheet, spriteSheet.getWidth() / 14, spriteSheet.getHeight() / 2);

        frameUp = new TextureRegion[1];
        frameDown = new TextureRegion[1];
        frameLeft = new TextureRegion[1];
        frameRight = new TextureRegion[1];
        frameExplosion = new TextureRegion[6];

        frameUp[0] = matrizDeSprites[0][1];
        frameDown[0] = matrizDeSprites[0][5];
        frameLeft[0] = matrizDeSprites[0][7];
        frameRight[0] = matrizDeSprites[0][3];

        for (int i = 0; i < 6; i++) {
            frameExplosion[i] = matrizDeSprites[0][8 + i];
        }

        animations.put(EstadoEnemigo.RUN_RIGHT, new Animation<>(0.2f, frameRight));
        animations.put(EstadoEnemigo.RUN_LEFT, new Animation<>(0.2f, frameLeft));
        animations.put(EstadoEnemigo.HIT_RIGHT, new Animation<>(0.08f, frameExplosion));
        animations.put(EstadoEnemigo.HIT_LEFT, new Animation<>(0.08f, frameExplosion));

        animations.get(EstadoEnemigo.RUN_RIGHT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoEnemigo.RUN_LEFT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoEnemigo.HIT_RIGHT).setPlayMode(Animation.PlayMode.NORMAL);
        animations.get(EstadoEnemigo.HIT_LEFT).setPlayMode(Animation.PlayMode.NORMAL);

        if (estado != null && estado.estadoAnimacion != null) {
            Animation<TextureRegion> initialAnimation = animations.get(estado.estadoAnimacion);
            if (initialAnimation != null) {
                frameActual = initialAnimation.getKeyFrame(0);
            } else {
                frameActual = matrizDeSprites[0][0];
            }
        } else {
            frameActual = matrizDeSprites[0][0];
        }
    }

    /**
     * [MODIFICADO] El rectángulo de colisión ahora es más grande durante la explosión.
     */
    public Rectangle getBounds() {
        if (explotando) {
            // Un área de colisión más grande que coincide con el sprite de la explosión
            float size = 96;
            float offset = size / 4; // Para centrar el rectángulo de colisión
            return new Rectangle(estado.x - offset, estado.y - offset, size, size);
        } else {
            // El área de colisión normal de la bomba
            return new Rectangle(estado.x, estado.y, 48, 48);
        }
    }

    /**
     * [MODIFICADO] La actualización ahora recibe al jugador para comprobar la proximidad.
     * @param deltaTime El tiempo transcurrido desde el último fotograma.
     * @param personajeJugable El objeto del jugador para saber su posición.
     */
    public void update(float deltaTime, Player personajeJugable) {
        tiempoXFrame += deltaTime;

        if (!explotando) {
            // Fase de movimiento
            tiempoDeVida -= deltaTime;
            estado.x += velocidad.x * deltaTime;
            estado.y += velocidad.y * deltaTime;

            // [AGREGADO] Chequeo de proximidad con el jugador
            float distanciaAlJugador = new Vector2(estado.x, estado.y).dst(personajeJugable.estado.x, personajeJugable.estado.y);

            // La bomba explota si se acaba el tiempo O si está lo suficientemente cerca del jugador.
            if (tiempoDeVida <= 0 || distanciaAlJugador < RADIO_PROXIMIDAD) {
                if (soundManager != null) {
                    soundManager.play("explosion_bomba");
                }
                // Iniciar explosión
                explotando = true;
                tiempoXFrame = 0; // Reiniciar timer de animación para la explosión
                setEstadoActual(velocidad.x > 0 ? EstadoEnemigo.HIT_RIGHT : EstadoEnemigo.HIT_LEFT);

            }
        } else {
            // Fase de explosión
            tiempoExplosionRestante -= deltaTime;
            if (tiempoExplosionRestante <= 0) {
                paraEliminar = true; // Marcar para eliminación
            }
        }

        Animation<TextureRegion> currentAnimation = animations.get(estado.estadoAnimacion);
        if (currentAnimation != null) {
            frameActual = currentAnimation.getKeyFrame(tiempoXFrame, !explotando); // No hacer loop en la explosión
        }
    }

    public void draw(SpriteBatch batch) {
        if (frameActual != null) {
            float size = explotando ? 96 : 48;
            float offset = explotando ? -24 : 0; // Centrar la explosión más grande
            batch.draw(frameActual, estado.x + offset, estado.y + offset, size, size);
        }
    }

    public void setEstadoActual(EstadoEnemigo nuevoEstado) {
        if (estado.estadoAnimacion != nuevoEstado) {
            tiempoXFrame = 0;
            estado.estadoAnimacion = nuevoEstado;
        }
    }

    // --- Métodos de ayuda (sin cambios) ---

    public boolean isExplotando() {
        return explotando;
    }

    public boolean isParaEliminar() {
        return paraEliminar;
    }

    public boolean yaHaHechoDanio() {
        return yaHizoDanio;
    }

    public void marcarComoDanioHecho() {
        this.yaHizoDanio = true;
    }

    public void dispose() {
        if (spriteSheet != null) {
            spriteSheet.dispose();
        }
    }
}
