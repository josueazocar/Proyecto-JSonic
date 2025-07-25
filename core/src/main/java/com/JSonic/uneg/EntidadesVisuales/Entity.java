package com.JSonic.uneg.EntidadesVisuales;

import com.JSonic.uneg.State.EntityState;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Clase base abstracta para todas las entidades del juego.
 * Maneja atributos comunes como estado, tamaño de tile, velocidad y animaciones.
 */
public abstract class Entity {
    //Atributos
    private int tileSize;
    protected float speed;
    public EntityState estado;
    //Atributos para dibujar las animaciones de entidades
    protected Animation<TextureRegion> animacion; // ¡Importante! Aseguramos que 'animacion' siempre use el tipo genérico <TextureRegion>
    protected Texture spriteSheet; //Imagen donde se encuentra todos los sprites en columnas y filas
    protected TextureRegion[] frameIdleRight; //Arreglo para almacenar los sprites cuando el personaje no se mueve
    protected TextureRegion[] frameIdleLeft; //Arreglo para almacenar los sprites cuando el personaje no se mueve
    protected TextureRegion[] frameUpRight; //Arreglo para almacenar los sprites de ir hacia arriba
    protected TextureRegion[] frameUpLeft; //Arreglo para almacenar los sprites de ir hacia arriba
    protected TextureRegion[] frameDownRight; //Arreglo para almacenar los sprites de ir hacia abajo
    protected TextureRegion[] frameDownLeft; //Arreglo para almacenar los sprites de ir hacia abajo
    protected TextureRegion[] frameLeft; //Arreglo para almacenar los sprites de ir hacia izquierda
    protected TextureRegion[] frameRight; //Arreglo para almacenar los sprites de ir hacia derecha
    protected TextureRegion[] frameHitRight; //Arreglo para almacenar los sprites de golpear a la derecha
    protected TextureRegion[] frameHitLeft; //Arreglo para almacenar los sprites de golpear a la izquierda
    protected TextureRegion[] frameKickRight; //Arreglo para almacenar los sprites de patear a la derecha
    protected TextureRegion[] frameKickLeft; //Arreglo para almacenar los sprites de patear a la izquierda
    protected TextureRegion frameActual; // El frame actual a dibujar
    protected float tiempoXFrame; // Tiempo transcurrido para el frame de animación actual


    /**
     * Construye una entidad con el estado inicial especificado.
     * @param estadoInicial Estado inicial de la entidad.
     */
    Entity(EntityState estadoInicial) { //Constructor default
        this.estado = estadoInicial;
        tileSize = 48; //Tamaño de las entidades
        speed = 0;
        animacion = null;
        spriteSheet = null;
        frameIdleRight = null;
        frameIdleLeft = null;
        frameUpRight = null;
        frameUpLeft = null;
        frameDownRight = null;
        frameDownLeft = null;
        frameLeft = null;
        frameRight = null;
        frameHitRight = null;
        frameHitLeft = null;
        frameKickRight = null;
        frameKickLeft = null;
        tiempoXFrame = 0.0f;

    }

    /**
     * Constructor vacío de Entity.
     */
    public Entity() {
    }

    /**
     * Establece la vida de la entidad.
     * @param vida Cantidad de vida a asignar.
     */
    public void setVida(int vida) {
        this.estado.vida = vida;
    }

    /**
     * Obtiene la vida actual de la entidad.
     * @return la vida actual.
     */
    public int getVida() {
        return estado.vida;
    }

    /**
     * Establece el tamaño del tile de la entidad.
     * @param tileSize Tamaño en píxeles del tile.
     */
    public void setTileSize(int tileSize) {
        this.tileSize = tileSize;
    }

    /**
     * Establece la velocidad de movimiento de la entidad.
     * @param speed Velocidad en unidades por segundo.
     */
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    /**
     * Asigna una animación a la entidad.
     * @param animacion Animación a asignar.
     */
    public void setAnimacion(Animation<TextureRegion> animacion) {
        this.animacion = animacion;
    }

    /**
     * Establece el sprite sheet de la entidad.
     * @param spriteSheet Textura que contiene todos los sprites.
     */
    public void setSpriteSheet(Texture spriteSheet) {
        this.spriteSheet = spriteSheet;
    }

    /**
     * Establece el frame actual de la animación.
     * @param frameActual Región de textura actual.
     */
    public void setFrameActual(TextureRegion frameActual) {
        this.frameActual = frameActual;
    }

    /**
     * Obtiene el estado actual de la entidad.
     * @return el estado de la entidad.
     */
    public EntityState getEstado() {
        return estado;
    }

    /**
     * Establece el estado de la entidad.
     * @param estado Nuevo estado a asignar.
     */
    public void setEstado(EntityState estado) {
        this.estado = estado;
    }

    /**
     * Define los frames de animación de idle hacia la derecha.
     * @param frameIdleRight Arreglo de regiones de textura.
     */
    public void setFrameIdleRight(TextureRegion[] frameIdleRight) {
        this.frameIdleRight = frameIdleRight;
    }

    /**
     * Define los frames de animación de idle hacia la izquierda.
     * @param frameIdleLeft Arreglo de regiones de textura.
     */
    public void setFrameIdleLeft(TextureRegion[] frameIdleLeft) {
        this.frameIdleLeft = frameIdleLeft;
    }

    /**
     * Define los frames de animación para moverse a la izquierda.
     * @param frameLeft Arreglo de regiones de textura.
     */
    public void setFrameLeft(TextureRegion[] frameLeft) {
        this.frameLeft = frameLeft;
    }

    /**
     * Define los frames de animación para moverse a la derecha.
     * @param frameRight Arreglo de regiones de textura.
     */
    public void setFrameRight(TextureRegion[] frameRight) {
        this.frameRight = frameRight;
    }

    /**
     * Define los frames de animación de golpeo hacia la derecha.
     * @param frameHitRight Arreglo de regiones de textura.
     */
    public void setFrameHitRight(TextureRegion[] frameHitRight) {
        this.frameHitRight = frameHitRight;
    }

    /**
     * Define los frames de animación de golpeo hacia la izquierda.
     * @param frameHitLeft Arreglo de regiones de textura.
     */
    public void setFrameHitLeft(TextureRegion[] frameHitLeft) {
        this.frameHitLeft = frameHitLeft;
    }

    /**
     * Define los frames de animación de patada hacia la derecha.
     * @param frameKickRight Arreglo de regiones de textura.
     */
    public void setFrameKickRight(TextureRegion[] frameKickRight) {
        this.frameKickRight = frameKickRight;
    }

    /**
     * Define los frames de animación de patada hacia la izquierda.
     * @param frameKickLeft Arreglo de regiones de textura.
     */
    public void setFrameKickLeft(TextureRegion[] frameKickLeft) {
        this.frameKickLeft = frameKickLeft;
    }

    /**
     * Establece el tiempo transcurrido en el frame de animación.
     * @param tiempoXFrame Tiempo en segundos del frame.
     */
    public void setTiempoXFrame(float tiempoXFrame) {
        this.tiempoXFrame = tiempoXFrame;
    }

    /**
     * Obtiene el tamaño en píxeles del tile de la entidad.
     * @return el tamaño del tile.
     */
    public int getTileSize() {
        return tileSize;
    }

    /**
     * Obtiene la velocidad de movimiento de la entidad.
     * @return la velocidad en unidades por segundo.
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * Obtiene la animación activa de la entidad.
     * @return la animación actual.
     */
    public Animation<TextureRegion> getAnimacion() {
        return animacion;
    }

    /**
     * Obtiene el frame actual de la animación.
     * @return la región de textura actual.
     */
    public TextureRegion getFrameActual() {
        return frameActual;
    }

    /**
     * Obtiene los frames de animación de idle hacia la derecha.
     * @return arreglo de regiones de textura.
     */
    public TextureRegion[] getFrameIdleRight() {
        return frameIdleRight;
    }

    /**
     * Obtiene los frames de animación de idle hacia la izquierda.
     * @return arreglo de regiones de textura.
     */
    public TextureRegion[] getFrameIdleLeft() {
        return frameIdleLeft;
    }

    /**
     * Obtiene los frames de animación para moverse hacia arriba-derecha.
     * @return arreglo de regiones de textura.
     */
    public TextureRegion[] getFrameUpRight() {
        return frameUpRight;
    }

    /**
     * Obtiene los frames de animación para moverse hacia arriba-izquierda.
     * @return arreglo de regiones de textura.
     */
    public TextureRegion[] getFrameUpLeft() {
        return frameUpLeft;
    }

    /**
     * Obtiene los frames de animación para moverse hacia abajo-derecha.
     * @return arreglo de regiones de textura.
     */
    public TextureRegion[] getFrameDownRight() {
        return frameDownRight;
    }

    /**
     * Obtiene los frames de animación para moverse hacia abajo-izquierda.
     * @return arreglo de regiones de textura.
     */
    public TextureRegion[] getFrameDownLeft() {
        return frameDownLeft;
    }

    /**
     * Obtiene los frames de animación para moverse hacia la izquierda.
     * @return arreglo de regiones de textura.
     */
    public TextureRegion[] getFrameLeft() {
        return frameLeft;
    }

    /**
     * Obtiene los frames de animación para moverse hacia la derecha.
     * @return arreglo de regiones de textura.
     */
    public TextureRegion[] getFrameRight() {
        return frameRight;
    }

    /**
     * Obtiene los frames de animación de golpeo hacia la derecha.
     * @return arreglo de regiones de textura.
     */
    public TextureRegion[] getFrameHitRight() {
        return frameHitRight;
    }

    /**
     * Obtiene los frames de animación de golpeo hacia la izquierda.
     * @return arreglo de regiones de textura.
     */
    public TextureRegion[] getFrameHitLeft() {
        return frameHitLeft;
    }

    /**
     * Obtiene los frames de animación de patada hacia la derecha.
     * @return arreglo de regiones de textura.
     */
    public TextureRegion[] getFrameKickRight() {
        return frameKickRight;
    }

    /**
     * Obtiene los frames de animación de patada hacia la izquierda.
     * @return arreglo de regiones de textura.
     */
    public TextureRegion[] getFrameKickLeft() {
        return frameKickLeft;
    }

    /**
     * Obtiene el sprite sheet de la entidad.
     * @return la textura del sprite sheet.
     */
    public Texture getSpriteSheet() {
        return spriteSheet;
    }

    /**
     * Obtiene el tiempo transcurrido para el frame de animación.
     * @return tiempo en segundos.
     */
    public float getTiempoXFrame() {
        return tiempoXFrame;
    }

    /**
     * Inicializa valores lógicos por defecto de la entidad.
     */
    protected abstract void setDefaultValues();

    /**
     * Carga los sprites y animaciones de la entidad.
     */
    protected abstract void CargarSprites();

    /**
     * Maneja la entrada de teclas o controles para la entidad.
     */
    protected abstract void KeyHandler();

    /**
     * Dibuja la entidad usando el SpriteBatch proporcionado.
     * @param batch el lote de sprites para renderizar.
     */
    public abstract void draw(SpriteBatch batch);

    /**
     * Actualiza la lógica y animación de la entidad.
     * @param deltaTime tiempo transcurrido desde el último frame en segundos.
     */
    public abstract void update(float deltaTime);
}
