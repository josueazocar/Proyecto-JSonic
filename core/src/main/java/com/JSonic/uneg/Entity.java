package com.JSonic.uneg;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.EnumMap;

public abstract class Entity {
    //Atributos
    private int tileSize;
    public PlayerState estado;
    protected float speed;

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

    public enum EstadoPlayer {
        IDLE_RIGHT,
        IDLE_LEFT,
        UP_RIGHT,
        UP_LEFT,
        DOWN_RIGHT,
        DOWN_LEFT,
        LEFT,
        RIGHT,
        HIT_RIGHT,
        HIT_LEFT,
        KICK_RIGHT,
        KICK_LEFT,
        SPECIAL_RIGHT,
        SPECIAL_LEFT,
        PUNCH_LEFT,
        PUNCH_RIGHT
    }

    // Mapa para almacenar diferentes animaciones por estado
    protected EnumMap<EstadoPlayer, Animation<TextureRegion>> animations;

    //Constructor
    Entity(PlayerState estadoInicial) { //Constructor default
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
        this.estado.estadoAnimacion = EstadoPlayer.IDLE_RIGHT; // Establecer un estado por defecto es útil
        // Inicializar el mapa de animaciones
        animations = new EnumMap<>(EstadoPlayer.class);
    }

    public Entity(){
    }

    //Setters
    // SETTERS DE MOVIMIENTO
    public void setTileSize(int tileSize) {
        this.tileSize = tileSize;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    // ¡Importante! El setter ahora también usa el tipo genérico <TextureRegion>
    public void setAnimacion(Animation<TextureRegion> animacion) {
        this.animacion = animacion;
    }

    public void setSpriteSheet(Texture spriteSheet) {
        this.spriteSheet = spriteSheet;
    }

    public void setFrameActual(TextureRegion frameActual) {
        this.frameActual = frameActual;
    }

    public PlayerState getEstado() {
        return estado;
    }

    public void setEstado(PlayerState estado) {
        this.estado = estado;
    }

    public void setFrameIdleRight(TextureRegion[] frameIdleRight) {
        this.frameIdleRight = frameIdleRight;
    }

    public void setFrameIdleLeft(TextureRegion[] frameIdleLeft) {
        this.frameIdleLeft = frameIdleLeft;
    }


    public void setFrameLeft(TextureRegion[] frameLeft) {
        this.frameLeft = frameLeft;
    }

    public void setFrameRight(TextureRegion[] frameRight) {
        this.frameRight = frameRight;
    }

    public void setFrameHitRight(TextureRegion[] frameHitRight) {
        this.frameHitRight = frameHitRight;
    }

    public void setFrameHitLeft(TextureRegion[] frameHitLeft) {
        this.frameHitLeft = frameHitLeft;
    }

    public void setFrameKickRight(TextureRegion[] frameKickRight) {
        this.frameKickRight = frameKickRight;
    }

    public void setFrameKickLeft(TextureRegion[] frameKickLeft) {
        this.frameKickLeft = frameKickLeft;
    }

    public void setTiempoXFrame(float tiempoXFrame) {
        this.tiempoXFrame = tiempoXFrame;
    }

    public void setEstadoActual(EstadoPlayer estadoActual) {
        this.estado.estadoAnimacion = estadoActual;
    }

    //Getters


    public int getTileSize() {
        return tileSize;
    }

    public float getSpeed() {
        return speed;
    }

    // ¡Importante! El getter ahora también devuelve el tipo genérico <TextureRegion>
    public Animation<TextureRegion> getAnimacion() {
        return animacion;
    }

    public TextureRegion getFrameActual() {
        return frameActual;
    }

    // Añadidos getters para todos los arreglos de frames para completitud.
    public TextureRegion[] getFrameIdleRight() {
        return frameIdleRight;
    }

    public TextureRegion[] getFrameIdleLeft() {
        return frameIdleLeft;
    }

    public TextureRegion[] getFrameUpRight() {
        return frameUpRight;
    }

    public TextureRegion[] getFrameUpLeft() {
        return frameUpLeft;
    }


    public TextureRegion[] getFrameDownRight() {
        return frameDownRight;
    }

    public TextureRegion[] getFrameDownLeft() {
        return frameDownLeft;
    }

    public TextureRegion[] getFrameLeft() {
        return frameLeft;
    }

    public TextureRegion[] getFrameRight() {
        return frameRight;
    }

    public TextureRegion[] getFrameHitRight() {
        return frameHitRight;
    }

    public TextureRegion[] getFrameHitLeft() {
        return frameHitLeft;
    }

    public TextureRegion[] getFrameKickRight() {
        return frameKickRight;
    }

    public TextureRegion[] getFrameKickLeft() {
        return frameKickLeft;
    }

    public Texture getSpriteSheet() {
        return spriteSheet;
    }

    public float getTiempoXFrame() {
        return tiempoXFrame;
    }

    public EstadoPlayer getEstadoActual() {
        return this.estado.estadoAnimacion;
    }

    //Methods
    protected abstract void setDefaultValues(); // Inicialización de valores lógicos por defecto
    protected abstract void CargarSprites(); // Carga de los sprites de la entidad
    protected abstract void KeyHandler(); // Método abstracto para implementar la lógica de movilidad
    public abstract void draw(SpriteBatch batch); // Método abstracto para implementar la lógica del dibujado por pantalla
    public abstract void update(float deltaTime); // Método abstracto para actualizar lógica y animación
}
