package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.math.MathUtils; // Importar para MathUtils.clamp

public abstract class Player extends Entity implements Disposable {
    // Stores the last horizontal direction to know which IDLE state to return to
    // and the orientation of attacks.
    protected EstadoPlayer lastDirection = EstadoPlayer.IDLE_RIGHT; // Default

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
    protected void setDefaultValues() { // Método para establecer valores por defecto
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
        // 1. Handle non-interruptible actions (hit and kick).
        // If the player is in a hit or kick animation, no other input is processed.
        if (estadoActual == EstadoPlayer.HIT_RIGHT || estadoActual == EstadoPlayer.HIT_LEFT ||
            estadoActual == EstadoPlayer.KICK_RIGHT || estadoActual == EstadoPlayer.KICK_LEFT) {
            return; // No procesar más entrada mientras estas acciones se reproducen
        }

        // Flag para rastrear si se detectó algún movimiento WASD.
        boolean isMoving = false;
        // Almacena el estado de movimiento propuesto (UP, DOWN, LEFT, RIGHT) antes de aplicar acciones.
        EstadoPlayer proposedMovementState = null;

        // 2. Procesar entrada de movimiento (WASD). Esto actualiza la posición del jugador.
        // Las actualizaciones de posición ocurren independientemente de la animación actual,
        // permitiendo el movimiento mientras se gira.
        if (Gdx.input.isKeyPressed(Keys.W)) {
            estado.y += speed;
            proposedMovementState = EstadoPlayer.UP;
            isMoving = true;
        }
        if (Gdx.input.isKeyPressed(Keys.S)) {
            estado.y -= speed;
            proposedMovementState = EstadoPlayer.DOWN;
            isMoving = true;
        }
        if (Gdx.input.isKeyPressed(Keys.A)) { // Izquierda
            estado.x -= speed; // Usa estado.x
            lastDirection = EstadoPlayer.IDLE_LEFT; // Actualiza la última dirección HORIZONTAL para IDLE/ataques
            proposedMovementState = EstadoPlayer.LEFT;
            isMoving = true;
        }
        if (Gdx.input.isKeyPressed(Keys.D)) { // Derecha
            estado.x += speed; // Usa estado.x
            lastDirection = EstadoPlayer.IDLE_RIGHT; // Actualiza la última dirección HORIZONTAL para IDLE/ataques
            proposedMovementState = EstadoPlayer.RIGHT;
            isMoving = true;
        }

        // --- CÓDIGO NUEVO AQUÍ ---
        // Después de calcular la nueva posición, la limitamos a los bordes del mapa.
        if (levelManager != null && levelManager.getMapaActual() != null) {
            // Obtener límites del mapa. Restamos tileSize para que el personaje no se salga del todo.
            float minX = 0;
            float minY = 0;
            float maxX = levelManager.getAnchoMapaPixels() - getTileSize();
            float maxY = levelManager.getAltoMapaPixels() - getTileSize();

            estado.x = MathUtils.clamp(estado.x, minX, maxX); // Usa estado.x
            estado.y = MathUtils.clamp(estado.y, minY, maxY); // Usa estado.y
        }
        // --- FIN CÓDIGO NUEVO ---


        // 3. Process action inputs (SPACE for spin, J for hit, K for kick).
        // These can override the movement animation state.
        if (Gdx.input.isKeyPressed(Keys.SPACE)) {
            if (lastDirection == EstadoPlayer.IDLE_LEFT || lastDirection == EstadoPlayer.LEFT) {
                setEstadoActual(EstadoPlayer.SPIN_LEFT);
            } else {
                setEstadoActual(EstadoPlayer.SPIN_RIGHT);
            }
            isMoving = true; // El spin también se considera un tipo de "movimiento" animado
        } else if (Gdx.input.isKeyJustPressed(Keys.J)) { // Only when pressed for the first time
            if (lastDirection == EstadoPlayer.IDLE_LEFT || lastDirection == EstadoPlayer.LEFT) {
                setEstadoActual(EstadoPlayer.HIT_LEFT);
            } else {
                setEstadoActual(EstadoPlayer.HIT_RIGHT);
            }
            // No set isMoving = true here; hit is a momentary action, not continuous movement.
        } else if (Gdx.input.isKeyJustPressed(Keys.K)) { // Only when pressed for the first time
            if (lastDirection == EstadoPlayer.IDLE_LEFT || lastDirection == EstadoPlayer.LEFT) {
                setEstadoActual(EstadoPlayer.KICK_LEFT);
            } else {
                setEstadoActual(EstadoPlayer.KICK_RIGHT);
            }
            // No set isMoving = true here.
        }
        // 4. If no movement keys are pressed and no action key is active, return to IDLE.
        // This is done after processing all other inputs to ensure action animations take priority.
        else if (!isMoving && !(estadoActual == EstadoPlayer.HIT_RIGHT || estadoActual == EstadoPlayer.HIT_LEFT ||
            estadoActual == EstadoPlayer.KICK_RIGHT || estadoActual == EstadoPlayer.KICK_LEFT ||
            estadoActual == EstadoPlayer.SPIN_RIGHT || estadoActual == EstadoPlayer.SPIN_LEFT)) {
            if (lastDirection == EstadoPlayer.IDLE_LEFT || lastDirection == EstadoPlayer.LEFT) {
                setEstadoActual(EstadoPlayer.IDLE_LEFT);
            } else {
                setEstadoActual(EstadoPlayer.IDLE_RIGHT);
            }
        }
        // If movement was detected but no action, update to the proposed movement state.
        else if (isMoving && proposedMovementState != null &&
            !(estadoActual == EstadoPlayer.SPIN_RIGHT || estadoActual == EstadoPlayer.SPIN_LEFT) && // No sobrescribir Spin
            !(estadoActual == EstadoPlayer.HIT_RIGHT || estadoActual == EstadoPlayer.HIT_LEFT ||
                estadoActual == EstadoPlayer.KICK_RIGHT || estadoActual == EstadoPlayer.KICK_LEFT)) {
            setEstadoActual(proposedMovementState);
        }
    }

    // El método update(float deltaTime) es abstracto en Entity y se implementa en Sonic.
    // El método draw(SpriteBatch batch) es abstracto en Entity y se implementa en Sonic.

    // El método dispose() es de Disposable y se implementa en Entity (asumiendo que Entity lo hace para spriteSheet).
    // Si no, DEBE ser implementado aquí o en Entity para liberar 'spriteSheet'.
    // @Override
    // public void dispose() {
    //     if (spriteSheet != null) {
    //         spriteSheet.dispose();
    //     }
    // }
}
