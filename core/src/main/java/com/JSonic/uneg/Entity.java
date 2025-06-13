package com.JSonic.uneg;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.EnumMap;

public abstract class Entity{
    //Atributos
    private int tileSize;
    protected int positionX;
    protected int positionY;
    protected int speed;

//Atributos para dibujar las animaciones de entidades

    // ¡Importante! Aseguramos que 'animacion' siempre use el tipo genérico <TextureRegion>
    protected Animation<TextureRegion> animacion;
    protected Texture spriteSheet;//Imagen donde se encuentra todos los sprites en columnas y filas
    protected TextureRegion[] frameIdle;//Arreglo para almacenar los sprites de ir hacia arriba
    protected TextureRegion[] frameUp;//Arreglo para almacenar los sprites de ir hacia arriba
    protected TextureRegion[] frameDown;//Arreglo para almacenar los sprites de ir hacia abajo
    protected TextureRegion[] frameLeft;//Arreglo para almacenar los sprites de ir hacia izquierda
    protected TextureRegion[] frameRight;//Arreglo para almacenar los sprites de ir hacia derecha
    protected TextureRegion[] frameHit;//Arreglo para almacenar los sprites de atacar
    protected TextureRegion frameActual;
    protected float tiempoXFrame;

    public enum EstadoPlayer {
        IDLE,
        UP,
        DOWN,
        LEFT,
        RIGHT,
        HIT,
        SPIN,
        KICK,
    }

    // Mapa para almacenar diferentes animaciones por estado
    protected EnumMap<EstadoPlayer, Animation<TextureRegion>> animations;
    protected EstadoPlayer estadoActual; // El estado actual del jugador

    //Constructor
    Entity(){//Constructor default
        tileSize = 48;//Tamaño de las entidades
        positionX = 0;
        positionY = 0;
        speed = 0;
        // La inicialización a 'null' es aceptable aquí, siempre y cuando se asigne
        // una animación válida en el constructor de las subclases (como hicimos en Sonic).
        animacion = null;
        spriteSheet = null;
        frameIdle = null; // Inicializar todos los arreglos a null en el constructor es una buena práctica
        frameUp = null;
        frameDown = null;
        frameLeft = null;
        frameRight = null;
        frameHit = null;
        tiempoXFrame = 0.0f;
        estadoActual = EstadoPlayer.IDLE; // Establecer un estado por defecto es útil
        // Inicializar el mapa de animaciones si planeas usarlo
        animations = new EnumMap<>(EstadoPlayer.class);
    }

    //Setters
//SETTERS MOVIMIENTO
    public void setTileSize(int tileSize){
        this.tileSize = tileSize;
    }

    public void setPositionX(int positionX) {
        this.positionX += positionX;
    }

    public void setPositionY(int positionY){
        this.positionY += positionY;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    // ¡Importante! El setter ahora también usa el tipo genérico <TextureRegion>
    public void setAnimacion(Animation<TextureRegion> animacion){
        this.animacion = animacion;
    }

    public void setSpriteSheet(Texture spriteSheet){
        this.spriteSheet = spriteSheet;
    }

    public void setFrameActual(TextureRegion frameActual) {
        // La línea de depuración es útil, la mantendremos.
        System.out.println("DEBUG: Tipo de objeto recibido en setFrameActual: " + frameActual.getClass().getName());
        this.frameActual = frameActual;
    }

    public void setFrameIdle(TextureRegion[] frameIdle) {
        this.frameIdle = frameIdle;
    }

    public void setFrameUp(TextureRegion[] frameUp) {
        this.frameUp = frameUp;
    }

    public void setFrameDown(TextureRegion[] frameDown) {
        this.frameDown = frameDown;
    }

    public void setFrameLeft(TextureRegion[] frameLeft) {
        this.frameLeft = frameLeft;
    }

    public void setFrameRight(TextureRegion[] frameRight) {
        this.frameRight = frameRight;
    }

    public void setFrameHit(TextureRegion[] frameHit) {
        this.frameHit = frameHit;
    }

    public void setTiempoXFrame(float tiempoXFrame) {
        this.tiempoXFrame += tiempoXFrame;
    }

    public void setEstadoActual(EstadoPlayer estadoActual){
        this.estadoActual = estadoActual;
    }

    //Getters
    public int getTileSize(){
        return tileSize;
    }

    public int getPositionX(){
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public int getSpeed(){
        return speed;
    }

    // ¡Importante! El getter ahora también devuelve el tipo genérico <TextureRegion>
    public Animation<TextureRegion> getAnimacion() {
        return animacion;
    }

    public TextureRegion getFrameActual() {
        return frameActual;
    }


    public TextureRegion[] getFrameIdle() {
        return frameIdle;
    }

    public TextureRegion[] getFrameUp() {
        return frameUp;
    }

    public TextureRegion[] getFrameHit() {
        return frameHit;
    }

    public TextureRegion[] getFrameLeft() {
        return frameLeft;
    }

    public TextureRegion[] getFrameRight() {
        return frameRight;
    }


    public Texture getSpriteSheet() {
        return spriteSheet;
    }

    public float getTiempoXFrame() {
        return tiempoXFrame;
    }

    public EstadoPlayer getEstadoActual(){
        return estadoActual;
    }

    //Methods
    protected abstract void setDefaultValues();//Inicialización de valores lógicos por defecto
    protected abstract void CargarSprites();//Carga de los sprites de la entidad
    protected abstract void KeyHandler();//Metodo abstracto para implementar la lógica de movilidad
    public abstract void draw(SpriteBatch batch);//Metodo abstracto para implementar la lógica del dibujado por pantalla
    public abstract void update(float deltaTime); //Método abstracto para actualizar lógica y animación
}
