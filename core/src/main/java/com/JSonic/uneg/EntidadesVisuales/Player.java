package com.JSonic.uneg.EntidadesVisuales;

import com.JSonic.uneg.LevelManager;
import com.JSonic.uneg.ObjetosDelEntorno.ItemVisual;
import com.JSonic.uneg.ObjetosDelEntorno.ObjetoRomperVisual;
import com.JSonic.uneg.State.PlayerState;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.math.MathUtils; // Importar para MathUtils.clamp
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.utils.Array;
import network.interfaces.IGameClient;

import java.util.EnumMap;

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
    //para identificar quien esta tocando el bloque
    protected String characterName;
    public PlayerState estado;
    protected String mensajeUI;
    protected float tiempoMensajeVisible;
    protected static final float DURACION_MENSAJE = 3.0f; // Mensaje visible por 3 segundos
    protected transient IGameClient gameClient;
    protected  boolean clean = false;
// Mapa para almacenar diferentes animaciones por estado
protected EnumMap<EstadoPlayer, Animation<TextureRegion>> animations;

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
        CLEAN,
        PUNCH_LEFT,
        PUNCH_RIGHT
    }

    public Player(){
        super();
    }

    public Player(PlayerState estadoInicial) {
        super(estadoInicial);
        setDefaultValues();
        this.gameClient = null;
        this.estado.estadoAnimacion = EstadoPlayer.IDLE_RIGHT; // Establecer un estado por defecto es útil
        // Inicializar el mapa de animaciones
        animations = new EnumMap<>(EstadoPlayer.class);
    }

    public Player(PlayerState estadoInicial, LevelManager levelManager) {
        this(estadoInicial);
        this.levelManager = levelManager;
        this.gameClient = null;
        this.estado.estadoAnimacion = EstadoPlayer.IDLE_RIGHT; // Establecer un estado por defecto es útil
        // Inicializar el mapa de animaciones
        animations = new EnumMap<>(EstadoPlayer.class);
    }

    public void setLevelManager(LevelManager levelManager) {
        this.levelManager = levelManager;
    }

    public EstadoPlayer getEstadoActual() {
        return this.estado.estadoAnimacion;
    }

    public void setEstadoActual(EstadoPlayer estadoActual) {
        this.estado.estadoAnimacion = estadoActual;
    }
    public PlayerState getEstado() {
        return estado;
    }

    public void setEstado(PlayerState estado) {
        this.estado = estado;
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

    public boolean estaAtacando() {
        if (this.estado == null || this.estado.estadoAnimacion == null) {
            return false;
        }

        switch (this.estado.estadoAnimacion) {
            case HIT_RIGHT:
            case HIT_LEFT:
            case KICK_RIGHT:
            case KICK_LEFT:
            case SPECIAL_RIGHT:
            case SPECIAL_LEFT:
            case PUNCH_LEFT:
            case PUNCH_RIGHT:
                return true;
            default:
                return false;
        }
    }
    protected boolean checkCollision(float newX, float newY) {
        if (levelManager == null) {
            return false;
        }
        if (bounds == null) {
            Gdx.app.log("Player", "Error: getBounds() no inicializado antes de checkCollision.");
            return false;
        }
        // Crea el hitbox en la posición futura.
        Rectangle futureBounds = new Rectangle(newX + collisionOffsetX, newY + collisionOffsetY, collisionWidth, collisionHeight);

        // 1. Comprobar colisión con el mapa (bloques, árboles, etc.)
        if (levelManager.colisionaConMapa(futureBounds)) {
            return true; // Hay colisión con el mapa
        }

        // 2. NUEVA COMPROBACIÓN: Colisión con bloques rompibles
        if (levelManager.getBloquesRompibles() != null) {
            Polygon futurePlayerPolygon = new Polygon(new float[]{
                futureBounds.x, futureBounds.y,
                futureBounds.x + futureBounds.width, futureBounds.y,
                futureBounds.x + futureBounds.width, futureBounds.y + futureBounds.height,
                futureBounds.x, futureBounds.y + futureBounds.height
            });
            for (ObjetoRomperVisual bloque : levelManager.getBloquesRompibles()) {
                // Comprobar si el hitbox futuro del jugador se superpone con el de un bloque rompible.
                if (Intersector.overlapConvexPolygons(futurePlayerPolygon, bloque.getBounds())) {
                    return true; // Hay colisión con un bloque rompible
                }
            }
        }

        // Si no hubo colisión con NADA de lo anterior, permite el movimiento.
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
        //esto es para romper bloques
    public void intentarRomperBloque() {
        // Solo Knuckles puede romper bloques
        if (!"Knuckles".equals(this.characterName)) {
            return;
        }

        if (levelManager == null || levelManager.getBloquesRompibles() == null) {
            return;
        }

        // Usamos los bounds del jugador para ver si se superpone con un bloque
        Rectangle playerBounds = getBounds(); // Asumiendo que Player tiene un getBounds()

        Polygon playerPolygon = new Polygon(new float[]{
            playerBounds.x, playerBounds.y,
            playerBounds.x + playerBounds.width, playerBounds.y,
            playerBounds.x + playerBounds.width, playerBounds.y + playerBounds.height,
            playerBounds.x, playerBounds.y + playerBounds.height
        });


        // Usamos un iterador para poder eliminar elementos de forma segura
        java.util.Iterator<ObjetoRomperVisual> iter = levelManager.getBloquesRompibles().iterator();
        while (iter.hasNext()) {
            ObjetoRomperVisual bloque = iter.next();
            if (Intersector.overlapConvexPolygons(playerPolygon, bloque.getBounds())) {
                iter.remove(); // Elimina el bloque de la lista en LevelManager
                Gdx.app.log("Player", "Knuckles rompió un bloque.");
                // Opcional: puedes añadir un sonido o efecto visual aquí
                break; // Rompe solo un bloque a la vez
            }
        }
    }

    public void mostrarMensaje(String texto) {
    }

    @Override
    public void dispose() {
        if (spriteSheet != null) {
            spriteSheet.dispose();
        }
    }

    public void setGameClient(IGameClient client) {
        this.gameClient = client;
        //System.out.println("[PLAYER] setGameClient fue llamado. El cliente es: " + (this.gameClient != null ? "VÁLIDO" : "NULO"));
    }
}
