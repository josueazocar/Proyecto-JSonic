package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.math.MathUtils; // Importar para MathUtils.clamp

public abstract class Player extends Entity implements Disposable {
    protected EstadoPlayer lastDirection = EstadoPlayer.IDLE_RIGHT;
    protected float lastPosX, lastPosY;
    protected LevelManager levelManager;

    public Player(PlayerState estadoInicial) {
        super(estadoInicial);
        setDefaultValues();
    }

    public Player(PlayerState estadoInicial, LevelManager levelManager) {
        this(estadoInicial);
        this.levelManager = levelManager;
    }

    public void setLevelManager(LevelManager levelManager) {
        this.levelManager = levelManager;
    }

    @Override
    protected void setDefaultValues() {
        if (this.estado == null) {
            this.estado = new PlayerState();
            this.estado.x = 100;
            this.estado.y = 100;
        }
        speed = 4; // Asegúrate de que 'speed' esté declarado en Entity o aquí
        setEstadoActual(EstadoPlayer.IDLE_RIGHT);
    }

    // NUEVO MÉTODO para verificar si una acción bloqueante está en curso
    /**
     * Verifica si el jugador está actualmente en una animación de acción
     * (como HIT o KICK) que debería impedir el movimiento.
     * Las animaciones de SPIN (si son LOOP) no se consideran bloqueantes aquí.
     * @return true si una acción bloqueante está en curso, false de lo contrario.
     */
    public boolean isActionBlockingMovement() {
        if (animacion == null) return false;
        // Considera acciones bloqueantes aquellas que son PlayMode.NORMAL y no han terminado.
        // SPIN, si es LOOP, no entra aquí. Si SPIN fuera NORMAL y quieres que bloquee,
        // no necesitarías una condición especial para ello aquí.
        boolean isNormalAction = animacion.getPlayMode() == Animation.PlayMode.NORMAL;
        boolean isSpinning = getEstadoActual() == EstadoPlayer.SPIN_LEFT || getEstadoActual() == EstadoPlayer.SPIN_RIGHT;

        if (isSpinning && animacion.getPlayMode() == Animation.PlayMode.LOOP) {
            return false; // El Spin en modo LOOP no bloquea el *procesamiento* de teclas de movimiento,
            // aunque la lógica de estado en Sonic.update() podría anular el movimiento.
            // Lo importante es que KeyHandler SÍ registre el intento de moverse.
        }

        return isNormalAction && !animacion.isAnimationFinished(tiempoXFrame);
    }


    @Override
    public void KeyHandler() {
        // 1. Handle non-interruptible actions (hit and kick).
        // If the player is in a hit or kick animation, no other input is processed.
        if (estado.estadoAnimacion == EstadoPlayer.HIT_RIGHT || estado.estadoAnimacion == EstadoPlayer.HIT_LEFT ||
            estado.estadoAnimacion == EstadoPlayer.KICK_RIGHT || estado.estadoAnimacion == EstadoPlayer.KICK_LEFT) {
            return; // Do not process further input while these actions play
        }

        // Flag to track if any WASD movement was detected.
        boolean isMoving = false;
        // Stores the proposed movement state (UP, DOWN, LEFT, RIGHT) before applying actions.
        EstadoPlayer proposedMovementState = null;

        // 2. Process movement input (WASD). This updates the player's position.
        // Position updates occur independently of the current animation,
        // allowing movement while spinning.
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
        if (Gdx.input.isKeyPressed(Keys.A)) { // Left
            estado.x -= speed;
            lastDirection = EstadoPlayer.LEFT; // Update the last HORIZONTAL direction
            proposedMovementState = EstadoPlayer.LEFT;
            isMoving = true;
        }
        if (Gdx.input.isKeyPressed(Keys.D)) { // Right
            estado.x += speed;
            lastDirection = EstadoPlayer.RIGHT; // Update the last HORIZONTAL direction
            proposedMovementState = EstadoPlayer.RIGHT;
            isMoving = true;
        }

        // 3. Process action inputs (J, K, L). These determine the animation state.
        // Actions take precedence over simple movement animations (walk/run).
        boolean actionStateSet = false; // Flag to know if an action state has been set.

        // Handle hit (punch - J)
        // Hit has priority if just pressed.
        if (Gdx.input.isKeyJustPressed(Keys.J)) {
            // The direction of the hit is based on the last horizontal direction.
            if (lastDirection == EstadoPlayer.LEFT) {
                setEstadoActual(EstadoPlayer.HIT_LEFT);
            } else {
                setEstadoActual(EstadoPlayer.HIT_RIGHT);
            }
            tiempoXFrame = 0; // Reset time so the hit animation starts from the beginning
            actionStateSet = true;
        }
        // Handle kick (K)
        // Kick has priority if just pressed.
        else if (Gdx.input.isKeyJustPressed(Keys.K)) {
            // The direction of the kick is based on the last horizontal direction.
            if (lastDirection == EstadoPlayer.LEFT) {
                setEstadoActual(EstadoPlayer.KICK_LEFT);
            } else {
                setEstadoActual(EstadoPlayer.KICK_RIGHT);
            }
            tiempoXFrame = 0; // Reset time for kick animation
            actionStateSet = true;
        }
        // Handle SPIN (L)
        // Spin can be held down and combines with WASD movement.
        else if (Gdx.input.isKeyPressed(Keys.L) && Gdx.input.isKeyPressed(Keys.D) ) {
            estado.x += speed;
            setEstadoActual(EstadoPlayer.SPIN_LEFT);
            actionStateSet = true;
        }
        else if (Gdx.input.isKeyPressed(Keys.L) && Gdx.input.isKeyPressed(Keys.A) ) {
            estado.x -= speed;
            setEstadoActual(EstadoPlayer.SPIN_RIGHT);
            actionStateSet = true;
        }


        // 4. If no action state was set (J, K, L), determine the state based on movement or IDLE.
        if (!actionStateSet) {
            if (isMoving) {
                // If there's movement (WASD), set the current state to the detected movement state.
                setEstadoActual(proposedMovementState);
            } else {
                // If there's no movement and no action key was pressed, return to the IDLE state.
                // The IDLE direction is based on the last saved HORIZONTAL direction.
                if (lastDirection == EstadoPlayer.LEFT) {
                    setEstadoActual(EstadoPlayer.IDLE_LEFT);
                } else {
                    setEstadoActual(EstadoPlayer.IDLE_RIGHT);
                }
            }
        }
    }

    @Override
    public void dispose() {
        if (spriteSheet != null) {
            spriteSheet.dispose();
        }
    }
}
