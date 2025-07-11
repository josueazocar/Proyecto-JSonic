package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.math.MathUtils; // Importar para MathUtils.clamp
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.utils.Array;

public abstract class Player extends Entity implements Disposable {
    protected EstadoPlayer lastDirection = EstadoPlayer.IDLE_RIGHT;
    protected float lastPosX, lastPosY;
    protected LevelManager levelManager;
    //Nuevas varialbles para las colisiones
    protected Rectangle bounds; // Rectángulo de colisión del jugador
    protected float collisionWidth;
    protected float collisionHeight;
    protected float collisionOffsetX;
    protected float collisionOffsetY;
    // Flag to track if any WASD movement was detected.
    protected boolean isMoving = false;
    // Stores the proposed movement state (UP, DOWN, LEFT, RIGHT) before applying actions.
    protected EstadoPlayer proposedMovementState = null;
    protected boolean actionStateSet = false; // Flag to know if an action state has been set.
//hasta aqui

    public Player(){
        super();
    }

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
        speed = 2.8F; // Asegúrate de que 'speed' esté declarado en Entity o aquí
        setEstadoActual(EstadoPlayer.IDLE_RIGHT);
    }
    //Para las colisiones
    public Rectangle getBounds() {
        if (bounds == null) {
            Gdx.app.log("Player", "ERROR CRÍTICO: bounds es nulo. ¡La inicialización del hitbox debe ocurrir en la subclase Sonic!");
            // Fallback: Usar el tileSize de la entidad (48) para el hitbox si no se inicializó.
            this.collisionWidth = getTileSize(); // Se mantiene usando getTileSize() (48)
            this.collisionHeight = getTileSize(); // Se mantiene usando getTileSize() (48)
            this.collisionOffsetX = 0;
            this.collisionOffsetY = 0;
            this.bounds = new Rectangle(estado.x, estado.y, collisionWidth, collisionHeight);
        }
        bounds.set(estado.x + collisionOffsetX, estado.y + collisionOffsetY, collisionWidth, collisionHeight);
        return bounds;
    }
//colisiones

    /**
     * CAMBIO: Nuevo método privado para verificar colisiones con los objetos del mapa.
     * @param newX La coordenada X tentativa para el movimiento.
     * @param newY La coordenada Y tentativa para el movimiento.
     * @return true si hay colisión, false de lo contrario.
     */
    private boolean checkCollision(float newX, float newY) {
        if (levelManager == null || levelManager.getMapaActual() == null) {
            return false;
        }
        if (bounds == null) {
            Gdx.app.log("Player", "Error: getBounds() no inicializado antes de checkCollision.");
            return false;
        }

        // CAMBIO: Crea un rectángulo de colisión en la posición tentativa.
        Rectangle futureBounds = new Rectangle(newX + collisionOffsetX, newY + collisionOffsetY, collisionWidth, collisionHeight);

        // CAMBIO: Itera sobre los objetos de la capa de colisiones del mapa.
        if (levelManager.getCollisionObjects() != null) {
            for (MapObject object : levelManager.getCollisionObjects()) {
                if (object instanceof RectangleMapObject) {
                    Rectangle rectObject = ((RectangleMapObject) object).getRectangle();

                    // CAMBIO: Si el hitbox futuro del jugador se superpone con un objeto del mapa, hay colisión.
                    if (futureBounds.overlaps(rectObject)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
//colisiones fin

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
        boolean isSpinning = getEstadoActual() == EstadoPlayer.SPECIAL_LEFT || getEstadoActual() == EstadoPlayer.SPECIAL_RIGHT;

        if (isSpinning && animacion.getPlayMode() == Animation.PlayMode.LOOP) {
            return false; // El Spin en modo LOOP no bloquea el *procesamiento* de teclas de movimiento,
            // aunque la lógica de estado en Sonic.update() podría anular el movimiento.
            // Lo importante es que KeyHandler SÍ registre el intento de moverse.
        }

        return isNormalAction && !animacion.isAnimationFinished(tiempoXFrame);
    }


    @Override
    public void KeyHandler() {

        //Para las colisiones
        // CAMBIO: Declarar e inicializar oldX, oldY, targetX, targetY al inicio
        float oldX = estado.x;
        float oldY = estado.y;
        float targetX = estado.x;
        float targetY = estado.y;

        isMoving = false;
        proposedMovementState = null; // Reinicia el estado de movimiento propuesto

        // Calcula los valores tentativos sin modificar la posición real
        if (Gdx.input.isKeyPressed(Keys.W)) {
            targetY += speed;
            isMoving = true;
            // El estado de movimiento propuesto para el movimiento vertical depende de la última dirección horizontal
            if(lastDirection == EstadoPlayer.RIGHT || lastDirection == EstadoPlayer.IDLE_RIGHT) {
                proposedMovementState = EstadoPlayer.UP_RIGHT;
            } else {
                proposedMovementState = EstadoPlayer.UP_LEFT;
            }
        }

        if (Gdx.input.isKeyPressed(Keys.S)) {
            targetY -= speed;
            isMoving = true;
            if(lastDirection == EstadoPlayer.RIGHT || lastDirection == EstadoPlayer.IDLE_RIGHT) {
                proposedMovementState = EstadoPlayer.DOWN_RIGHT;
            } else {
                proposedMovementState = EstadoPlayer.DOWN_LEFT;
            }
        }
        if (Gdx.input.isKeyPressed(Keys.A)) {
            targetX -= speed;
            lastDirection = EstadoPlayer.LEFT; // Actualiza la última dirección horizontal
            isMoving = true;
            proposedMovementState = EstadoPlayer.LEFT;
        }

        if (Gdx.input.isKeyPressed(Keys.D)) {
            targetX += speed;
            lastDirection = EstadoPlayer.RIGHT; // Actualiza la última dirección horizontal
            isMoving = true;
            proposedMovementState = EstadoPlayer.RIGHT;
        }


        if(!isMoving) {
            //En caso de que no se este presionando ninguna tecla
            // Solo establece IDLE si no se están presionando teclas de movimiento
            if (lastDirection == EstadoPlayer.LEFT) {
                proposedMovementState = EstadoPlayer.IDLE_LEFT;
            } else {
                proposedMovementState = EstadoPlayer.IDLE_RIGHT;
            }
        }

// Aplica el movimiento solo si no hay colisión
        if (!checkCollision(targetX, estado.y)) {
            estado.x = targetX;
        }
        if (!checkCollision(estado.x, targetY)) {
            estado.y = targetY;
        }

        // CAMBIO: Limitar al personaje a los bordes del mapa usando getTileSize() (que es 48).
        if (levelManager != null) {
            float mapWidth = levelManager.getAnchoMapaPixels();
            float mapHeight = levelManager.getAltoMapaPixels();

            estado.x = MathUtils.clamp(estado.x, 0, mapWidth - getTileSize());
            estado.y = MathUtils.clamp(estado.y, 0, mapHeight - getTileSize());
        }

    }

    //Para que todos los personajes puedan recolectar anillos, basura..

    public void recolectarItems(Array<ItemVisual> items) {
        Rectangle playerBounds = getBounds();
        for (int i = items.size - 1; i >= 0; i--) {
            ItemVisual item = items.get(i);
            if (playerBounds.overlaps(item.getBounds())) {
                item.onCollect(this); // Método a definir en ItemVisual
                items.removeIndex(i);
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
