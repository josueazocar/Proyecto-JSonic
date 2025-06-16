package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.math.MathUtils; // Importar para MathUtils.clamp

public abstract class Player extends Entity implements Disposable {
    // Stores the last horizontal direction to know which IDLE state to return to
    // and the orientation of attacks.
    protected EstadoPlayer lastDirection = EstadoPlayer.IDLE_RIGHT; // Default
    protected float lastPosX, lastPosY;
    protected LevelManager levelManager; // Referencia al LevelManager para obtener límites del mapa

    // Constructor para jugadores remotos (o local inicial)
    public Player(PlayerState estadoInicial) {
        super(estadoInicial); // Llama al constructor de la superclase Entity con PlayerState
        setDefaultValues(); // Establece valores lógicos por defecto
    }

    // Constructor para el jugador local, que necesita acceso al LevelManager
    // Aunque Sonic tiene su propio constructor que llama a este, lo mantenemos aquí si Player
    // fuera a ser instanciado directamente con un LevelManager.
    public Player(PlayerState estadoInicial, LevelManager levelManager) {
        this(estadoInicial); // Llama al constructor de arriba
        this.levelManager = levelManager; // Asigna el LevelManager
    }

    // Setter para LevelManager (necesario si Sonic se crea en Main sin LevelManager)
    public void setLevelManager(LevelManager levelManager) {
        this.levelManager = levelManager;
    }


    // Métodos (se implementan en subclases o se definen aquí)
    @Override
    protected void setDefaultValues() { // Metodo para establecer valores por defecto
        // Asegúrate de que this.estado ya ha sido inicializado por el constructor de Entity
        // Si el estado inicial viene de la red, sus x,y pueden ser diferentes de 100,100
        if (this.estado == null) { // Solo si Entity no lo inicializó ya
            this.estado = new PlayerState(); // Crea un nuevo PlayerState si es nulo
            this.estado.x = 100; // Posición inicial por defecto
            this.estado.y = 100;
        }
        speed = 4;
        setEstadoActual(EstadoPlayer.IDLE_RIGHT);
    }

    @Override
    public void KeyHandler() {
        // Estas variables ahora son solo para registrar la intención de este frame
        boolean quiereMoverse = false;

        // --- LÓGICA DE POSICIÓN ---
        // Solo actualizamos las coordenadas X e Y y la última dirección.
        if (Gdx.input.isKeyPressed(Keys.W)) {
            estado.y += speed;
            quiereMoverse = true;
        }
        if (Gdx.input.isKeyPressed(Keys.S)) {
            estado.y -= speed;
            quiereMoverse = true;
        }
        if (Gdx.input.isKeyPressed(Keys.A)) {
            estado.x -= speed;
            lastDirection = EstadoPlayer.IDLE_LEFT;
            quiereMoverse = true;
        }
        if (Gdx.input.isKeyPressed(Keys.D)) {
            estado.x += speed;
            lastDirection = EstadoPlayer.IDLE_RIGHT;
            quiereMoverse = true;
        }

        // Lógica de clamp (límites del mapa)
        if (levelManager != null && levelManager.getMapaActual() != null) {
            float minX = 0, minY = 0;
            float maxX = levelManager.getAnchoMapaPixels() - getTileSize();
            float maxY = levelManager.getAltoMapaPixels() - getTileSize();
            estado.x = MathUtils.clamp(estado.x, minX, maxX);
            estado.y = MathUtils.clamp(estado.y, minY, maxY);
        }
    }


     @Override
     public void dispose() {
         if (spriteSheet != null) {
             spriteSheet.dispose();
       }
    }
}
