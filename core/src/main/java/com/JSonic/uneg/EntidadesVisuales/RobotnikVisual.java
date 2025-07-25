package com.JSonic.uneg.EntidadesVisuales;

import com.JSonic.uneg.LevelManager;
import com.JSonic.uneg.State.EnemigoState;
import com.JSonic.uneg.State.EnemigoState.EstadoEnemigo;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import network.interfaces.IGameClient;

import java.util.EnumMap; // Se eliminó 'java.awt.*' ya que no se usaba.

/**
 * Clase visual para el enemigo Robotnik, gestiona sus animaciones, estados y renderizado.
 */
public class RobotnikVisual extends Enemy {

    public static final float MAX_VIDA = 100;
    private LevelManager levelManager;
    private IGameClient gameClient;


    /**
     * Constructor de RobotnikVisual con estado inicial y gestor de nivel.
     *
     * @param estadoInicial Estado inicial del enemigo.
     * @param levelManager  Gestor de nivel para colisiones y entorno.
     */
    public RobotnikVisual(EnemigoState estadoInicial, LevelManager levelManager) {
        this.estado = estadoInicial;
        this.levelManager = levelManager;
        animations = new EnumMap<>(EnemigoState.EstadoEnemigo.class);
        tiempoXFrame = 0.0f;
        CargarSprites();
        setEstadoActual(estado.estadoAnimacion);
    }

    /**
     * Constructor de RobotnikVisual con estado inicial, gestor de nivel y cliente de red.
     *
     * @param estadoInicial Estado inicial del enemigo.
     * @param levelManager  Gestor de nivel.
     * @param gameClient    Cliente de red para sincronización de estado.
     */
    public RobotnikVisual(EnemigoState estadoInicial, LevelManager levelManager, IGameClient gameClient) {
        this.estado = estadoInicial;
        this.levelManager = levelManager;
        this.gameClient = gameClient;
        animations = new EnumMap<>(EnemigoState.EstadoEnemigo.class);
        tiempoXFrame = 0.0f;
        CargarSprites();
        setEstadoActual(estado.estadoAnimacion);
    }

    /**
     * Constructor de RobotnikVisual con estado inicial.
     *
     * @param estadoInicial Estado inicial del enemigo.
     */
    public RobotnikVisual(EnemigoState estadoInicial) {
        this.estado = estadoInicial;
        animations = new EnumMap<>(EnemigoState.EstadoEnemigo.class);
        tiempoXFrame = 0.0f;
        CargarSprites();
        setEstadoActual(estado.estadoAnimacion);
    }

    /**
     * Asigna el gestor de nivel al enemigo.
     *
     * @param levelManager Gestor de nivel.
     */
    public void setLevelManager(LevelManager levelManager) {
        this.levelManager = levelManager;
    }

    /**
     * Proporciona la ruta al sprite sheet de Robotnik.
     *
     * @return Ruta del archivo de textura.
     */
    protected String getSpriteSheetPath() {
        return "Entidades/Enemy/Robotnik/drR.png";
    }

    /**
     * Carga los sprites y anima al Robotnik usando su sprite sheet.
     */
    @Override
    protected void CargarSprites() {
        String spriteSheetPath = getSpriteSheetPath();
        if (!Gdx.files.internal(spriteSheetPath).exists()) {
            Gdx.app.error("RobotnikVisual", "Error: Archivo de hoja de sprites no encontrado: " + spriteSheetPath);
            // Puedes lanzar una RuntimeException aquí si el archivo es crítico para la ejecución.
            throw new RuntimeException("Sprite sheet missing: " + spriteSheetPath);
        }

        spriteSheet = new Texture(Gdx.files.internal(spriteSheetPath));
        TextureRegion[][] matrizDeSprites = TextureRegion.split(spriteSheet, spriteSheet.getWidth() / 4, spriteSheet.getHeight() / 1);

        frameIdleRight = new TextureRegion[1];
        frameIdleLeft = new TextureRegion[1];
        frameRunRight = new TextureRegion[2];
        frameRunLeft = new TextureRegion[2];
        frameIdleLeft[0] = matrizDeSprites[0][0]; // Usa el primer frame de la primera fila
        frameIdleRight[0] = new TextureRegion(frameIdleLeft[0]);
        frameIdleRight[0].flip(true, false);

        frameRunLeft[0] = matrizDeSprites[0][0];
        frameRunLeft[1] = matrizDeSprites[0][1];

        for (int i = 0; i < frameRunLeft.length; i++) { // Itera sobre el tamaño real de frameRunLeft
            frameRunRight[i] = new TextureRegion(frameRunLeft[i]);
            frameRunRight[i].flip(true, false);
        }

        animations.put(EstadoEnemigo.IDLE_RIGHT, new Animation<TextureRegion>(0.12f, frameIdleRight));
        animations.put(EstadoEnemigo.IDLE_LEFT, new Animation<TextureRegion>(0.12f, frameIdleLeft));
        animations.put(EstadoEnemigo.RUN_RIGHT, new Animation<TextureRegion>(0.1f, frameRunRight));
        animations.put(EstadoEnemigo.RUN_LEFT, new Animation<TextureRegion>(0.1f, frameRunLeft));

        animations.get(EstadoEnemigo.IDLE_RIGHT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoEnemigo.IDLE_LEFT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoEnemigo.RUN_RIGHT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoEnemigo.RUN_LEFT).setPlayMode(Animation.PlayMode.LOOP);

        Animation<TextureRegion> initialAnimation = animations.get(estado.estadoAnimacion);
        if (initialAnimation != null) {
            frameActual = initialAnimation.getKeyFrame(0);
        }
    }

    /**
     * Obtiene el área de colisión del enemigo Robotnik.
     *
     * @return Rectángulo de colisión.
     */
    public com.badlogic.gdx.math.Rectangle getBounds() {
        return new com.badlogic.gdx.math.Rectangle(estado.x, estado.y, 48, 48);
    }

    /**
     * Actualiza el estado y la animación de Robotnik cada frame.
     *
     * @param deltaTime Tiempo transcurrido desde el último frame en segundos.
     */
    @Override
    public void update(float deltaTime) {
        tiempoXFrame += deltaTime;
        if (tiempoDesdeUltimoGolpe > 0) {
            tiempoDesdeUltimoGolpe -= deltaTime;
        }
        //Actualiza el frame visual que se debe dibujar.
        Animation<TextureRegion> currentAnimation = animations.get(estado.estadoAnimacion);
        if (currentAnimation != null) {
            frameActual = currentAnimation.getKeyFrame(tiempoXFrame, true); // Añadido 'true' para bucle por defecto
        } else {
            Gdx.app.log("RobotnikVisual", "Advertencia: 'currentAnimation' es nula para estado: " + estado.estadoAnimacion);
            frameActual = null;
        }
    }

    /**
     * Dibuja al Robotnik en pantalla.
     *
     * @param batch SpriteBatch utilizado para renderizar.
     */
    @Override
    public void draw(SpriteBatch batch) {
        if (frameActual != null) {
            // Dibuja el frame actual en la posición del estado.
            batch.draw(frameActual, estado.x, estado.y, 48, 48);
        } else {
            Gdx.app.log("RobotnikVisual", "Advertencia: 'frameActual' es nulo en draw(). No se puede dibujar el robot.");
        }
    }

    /**
     * Cambia el estado de animación actual del Robotnik.
     *
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
     * Libera los recursos de la hoja de sprites de Robotnik.
     */
    public void dispose() {
        if (spriteSheet != null) {
            spriteSheet.dispose();
        }
    }
}
