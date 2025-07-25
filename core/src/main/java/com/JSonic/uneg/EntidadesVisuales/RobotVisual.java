package com.JSonic.uneg.EntidadesVisuales;

import com.JSonic.uneg.LevelManager;
import com.JSonic.uneg.State.EnemigoState;
import com.JSonic.uneg.State.EnemigoState.EstadoEnemigo;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import network.Network;
import network.interfaces.IGameClient;

import java.util.EnumMap;

/**
 * Clase visual para el enemigo Robot, gestiona sus animaciones, estados y notificación de finalización de ataque.
 */
public class RobotVisual extends Enemy {

    private LevelManager levelManager;
    private IGameClient gameClient;
    private boolean animacionDeAtaqueTerminada = false;

    /**
     * Constructor de RobotVisual con estado inicial y gestor de nivel.
     * @param estadoInicial Estado inicial del enemigo.
     * @param levelManager Gestor de nivel para colisiones y entorno.
     */
    public RobotVisual(EnemigoState estadoInicial, LevelManager levelManager) {
        this.estado = estadoInicial;
        this.levelManager = levelManager;
        animations = new EnumMap<>(EnemigoState.EstadoEnemigo.class);
        tiempoXFrame = 0.0f;
        CargarSprites();
        setEstadoActual(estado.estadoAnimacion);
    }

    /**
     * Constructor de RobotVisual con estado inicial, gestor de nivel y cliente de red.
     * @param estadoInicial Estado inicial del enemigo.
     * @param levelManager Gestor de nivel.
     * @param gameClient Cliente de red para sincronización de estado del enemigo.
     */
    public RobotVisual(EnemigoState estadoInicial, LevelManager levelManager, IGameClient gameClient) {
        this.estado = estadoInicial;
        this.levelManager = levelManager;
        this.gameClient = gameClient;
        animations = new EnumMap<>(EnemigoState.EstadoEnemigo.class);
        tiempoXFrame = 0.0f;
        CargarSprites();
        setEstadoActual(estado.estadoAnimacion);
    }

    /**
     * Constructor de RobotVisual con estado inicial solo.
     * @param estadoInicial Estado inicial del enemigo.
     */
    public RobotVisual(EnemigoState estadoInicial) {
        this.estado = estadoInicial;
        animations = new EnumMap<>(EnemigoState.EstadoEnemigo.class);
        tiempoXFrame = 0.0f;
        CargarSprites();
        setEstadoActual(estado.estadoAnimacion);
    }

    /**
     * Asigna el gestor de nivel al enemigo después de la construcción.
     * @param levelManager Gestor de nivel.
     */
    public void setLevelManager(LevelManager levelManager) {
        this.levelManager = levelManager;
    }

    /**
     * Proporciona la ruta al sprite sheet del enemigo Robot.
     * @return Cadena con la ruta del archivo de textura.
     */
    protected String getSpriteSheetPath() {
        return "Entidades/Enemy/Robots/robot.png";
    }

    /**
     * Carga los sprites y configura las animaciones del enemigo Robot.
     */
    @Override
    protected void CargarSprites() {
        spriteSheet = new Texture(Gdx.files.internal(getSpriteSheetPath()));
        TextureRegion[][] matrizDeSprites = TextureRegion.split(spriteSheet, spriteSheet.getWidth() / 9, spriteSheet.getHeight() / 26);

        frameIdleRight = new TextureRegion[8];
        frameIdleLeft = new TextureRegion[8];
        frameRunRight = new TextureRegion[6];
        frameRunLeft = new TextureRegion[6];
        frameHitRight = new TextureRegion[8];
        frameHitLeft = new TextureRegion[8];

        for (int i = 0; i < 4; i++) {
            frameIdleLeft[i] = matrizDeSprites[0][i];
        }
        for (int i = 0; i < 4; i++) {
            frameIdleLeft[i + 4] = matrizDeSprites[0][3 - i];
        }
        for (int i = 0; i < 8; i++) {
            frameIdleRight[i] = new TextureRegion(frameIdleLeft[i]);
            frameIdleRight[i].flip(true, false);
        }

        for (int i = 0; i < 6; i++) {
            frameRunLeft[i] = matrizDeSprites[1][i];
        }
        for (int i = 0; i < 6; i++) {
            frameRunRight[i] = new TextureRegion(frameRunLeft[i]);
            frameRunRight[i].flip(true, false);
        }

        for (int i = 0; i < 8; i++) {
            frameHitLeft[i] = matrizDeSprites[6][i];
        }
        for (int i = 0; i < 8; i++) {
            frameHitRight[i] = new TextureRegion(frameHitLeft[i]);
            frameHitRight[i].flip(true, false);
        }

        animations.put(EstadoEnemigo.IDLE_RIGHT, new Animation<TextureRegion>(0.12f, frameIdleRight));
        animations.put(EstadoEnemigo.IDLE_LEFT, new Animation<TextureRegion>(0.12f, frameIdleLeft));
        animations.put(EstadoEnemigo.RUN_RIGHT, new Animation<TextureRegion>(0.1f, frameRunRight));
        animations.put(EstadoEnemigo.RUN_LEFT, new Animation<TextureRegion>(0.1f, frameRunLeft));
        animations.put(EstadoEnemigo.HIT_RIGHT, new Animation<TextureRegion>(0.08f, frameHitRight));
        animations.put(EstadoEnemigo.HIT_LEFT, new Animation<TextureRegion>(0.08f, frameHitLeft));

        animations.get(EstadoEnemigo.IDLE_RIGHT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoEnemigo.IDLE_LEFT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoEnemigo.RUN_RIGHT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoEnemigo.RUN_LEFT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoEnemigo.HIT_RIGHT).setPlayMode(Animation.PlayMode.NORMAL);
        animations.get(EstadoEnemigo.HIT_LEFT).setPlayMode(Animation.PlayMode.NORMAL);

        Animation<TextureRegion> initialAnimation = animations.get(estado.estadoAnimacion);
        if (initialAnimation != null) {
            frameActual = initialAnimation.getKeyFrame(0);
        }
    }

    /**
     * Obtiene el rectángulo de colisión basado en la posición y tamaño fijo.
     * @return Rectángulo de colisión del enemigo.
     */
    public com.badlogic.gdx.math.Rectangle getBounds() {
        return new com.badlogic.gdx.math.Rectangle(estado.x, estado.y, 48, 48);
    }

    /**
     * Actualiza la lógica de animación del enemigo cada frame.
     * @param deltaTime Tiempo transcurrido desde el último frame en segundos.
     */
    @Override
    public void update(float deltaTime) {
        tiempoXFrame += deltaTime;

        if (tiempoDesdeUltimoGolpe > 0) {
            tiempoDesdeUltimoGolpe -= deltaTime;
        }
        //Revisa si una animación de ataque ha terminado.
        boolean estaAtacando = estado.estadoAnimacion == EstadoEnemigo.HIT_LEFT || estado.estadoAnimacion == EstadoEnemigo.HIT_RIGHT;
        if (estaAtacando) {
            Animation<TextureRegion> currentAnim = animations.get(estado.estadoAnimacion);
            if (currentAnim != null && currentAnim.isAnimationFinished(tiempoXFrame)) {

                // Si terminó, envía el paquete de notificación al servidor.
                if (gameClient != null) {
                    Network.PaqueteAnimacionEnemigoTerminada paquete = new Network.PaqueteAnimacionEnemigoTerminada();
                    paquete.idEnemigo = estado.id;
                    gameClient.send(paquete);
                }

                tiempoXFrame = 0;
            }
        }

        //Actualiza el frame visual que se debe dibujar.
        Animation<TextureRegion> currentAnimation = animations.get(estado.estadoAnimacion);
        if (currentAnimation != null) {
            frameActual = currentAnimation.getKeyFrame(tiempoXFrame);
        } else {
            Gdx.app.log("RobotVisual", "Advertencia: 'currentAnimation' es nula para estado: " + estado.estadoAnimacion);
            frameActual = null;
        }
    }

    /**
     * Dibuja el enemigo en pantalla usando el SpriteBatch proporcionado.
     * @param batch SpriteBatch para renderizar el frame actual.
     */
    @Override
    public void draw(SpriteBatch batch) {
        if (frameActual != null) {
            batch.draw(frameActual, estado.x, estado.y, 48, 48);
        } else {
            Gdx.app.log("RobotVisual", "Advertencia: 'frameActual' es nulo en draw(). No se puede dibujar el robot.");
        }
    }

    /**
     * Cambia el estado de animación del enemigo y reinicia el tiempo de animación.
     * @param nuevoEstado Nuevo estado de animación.
     */
    public void setEstadoActual(EstadoEnemigo nuevoEstado) {
        // Solo cambia el estado si es diferente para evitar reiniciar animaciones innecesariamente
        if (estado.estadoAnimacion != nuevoEstado) {
            tiempoXFrame = 0; // Reinicia el tiempo si el estado cambia para que la nueva animación empiece de 0
            estado.estadoAnimacion = nuevoEstado;
        }
    }

    /**
     * Libera los recursos de la hoja de sprites del enemigo.
     */
    public void dispose() {
        if (spriteSheet != null) {
            spriteSheet.dispose();
        }
    }

    /**
     * Indica si la animación de ataque ha terminado desde la última comprobación.
     * @return true si la animación de ataque terminó, false en caso contrario.
     */
    public boolean haTerminadoAnimacionDeAtaque() {
        return animacionDeAtaqueTerminada;
    }

    /**
     * Reinicia la bandera que indica la finalización de la animación de ataque.
     */
    public void reiniciarBanderaDeAnimacion() {
        this.animacionDeAtaqueTerminada = false;
    }
}
