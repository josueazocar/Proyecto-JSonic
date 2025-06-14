package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class Player extends Entity {
    // Attributes
    // Stores the last horizontal direction to know which IDLE state to return to
    // and the orientation of attacks.
    protected EstadoPlayer lastDirection = EstadoPlayer.IDLE_RIGHT; // Default

    // Constructor
    Player() {
        super(); // Call the superclass constructor
        setDefaultValues(); // Set default logical values
    }

    // Setters
    // Getters

    // Methods
    @Override
    protected void setDefaultValues() { // Method to set default values
        positionX = 100; // Initialize initial X position
        positionY = 100; // Initialize initial Y position
        speed = 4; // Set speed
        setEstadoActual(EstadoPlayer.IDLE_RIGHT); // Initialize 'estadoActual' by default.
    }


    @Override
    public void KeyHandler() {
        // 1. Handle non-interruptible actions (hit and kick).
        // If the player is in a hit or kick animation, no other input is processed.
        if (estadoActual == EstadoPlayer.HIT_RIGHT || estadoActual == EstadoPlayer.HIT_LEFT ||
            estadoActual == EstadoPlayer.KICK_RIGHT || estadoActual == EstadoPlayer.KICK_LEFT) {
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
            positionY += speed;
            proposedMovementState = EstadoPlayer.UP;
            isMoving = true;
        }
        if (Gdx.input.isKeyPressed(Keys.S)) {
            positionY -= speed;
            proposedMovementState = EstadoPlayer.DOWN;
            isMoving = true;
        }
        if (Gdx.input.isKeyPressed(Keys.A)) { // Left
            positionX -= speed;
            lastDirection = EstadoPlayer.LEFT; // Update the last HORIZONTAL direction
            proposedMovementState = EstadoPlayer.LEFT;
            isMoving = true;
        }
        if (Gdx.input.isKeyPressed(Keys.D)) { // Right
            positionX += speed;
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
            positionX += speed;
            setEstadoActual(EstadoPlayer.SPIN_LEFT);
            actionStateSet = true;
        }
        else if (Gdx.input.isKeyPressed(Keys.L) && Gdx.input.isKeyPressed(Keys.A) ) {
            positionX -= speed;
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

    // Abstract method to load specific sprites for each character.
    protected abstract void CargarSprites();

    // Abstract methods to update and draw the player.
    public abstract void update(float deltaTime);
    public abstract void draw(SpriteBatch batch);

}
