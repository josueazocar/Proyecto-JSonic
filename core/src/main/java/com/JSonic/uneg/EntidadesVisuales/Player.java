package com.JSonic.uneg.EntidadesVisuales;

import com.JSonic.uneg.LevelManager;
import com.JSonic.uneg.ObjetosDelEntorno.ItemVisual;
import com.JSonic.uneg.ObjetosDelEntorno.ObjetoRomperVisual;
import com.JSonic.uneg.SoundManager;
import com.JSonic.uneg.State.PlayerState;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.utils.Array;
import network.interfaces.IGameClient;

import java.util.EnumMap;

/**
 * Clase base abstracta para jugadores. Gestiona movimiento, estado, colisiones, animaciones y eventos de jugador.
 */
public abstract class Player extends Entity implements Disposable {
    public static final int MAX_VIDA = 100;
    protected EstadoPlayer lastDirection = EstadoPlayer.IDLE_RIGHT;
    protected LevelManager levelManager;
    protected Rectangle bounds; // Rectángulo de colisión del jugador
    protected float collisionWidth;
    protected float collisionHeight;
    protected float collisionOffsetX;
    protected float collisionOffsetY;
    protected boolean isMoving = false;
    protected EstadoPlayer proposedMovementState = null;
    protected boolean actionStateSet = false;
    protected String characterName;
    public PlayerState estado;
    protected String mensajeUI;
    protected float tiempoMensajeVisible;
    protected static final float DURACION_MENSAJE = 3.0f; // Mensaje visible por 3 segundos
    protected transient IGameClient gameClient;
    protected int gemas;
    protected transient SoundManager soundManager;

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

    /**
     * Constructor por defecto de Player.
     */
    public Player() {
        super();
    }

    /**
     * Constructor de Player con estado inicial.
     *
     * @param estadoInicial Estado inicial del jugador.
     */
    public Player(PlayerState estadoInicial) {
        super(estadoInicial);
        setDefaultValues();
        this.gameClient = null;
        this.estado.estadoAnimacion = EstadoPlayer.IDLE_RIGHT; // Establecer un estado por defecto es útil
        // Inicializar el mapa de animaciones
        animations = new EnumMap<>(EstadoPlayer.class);
    }

    /**
     * Constructor de Player con estado inicial y gestor de nivel.
     *
     * @param estadoInicial Estado inicial del jugador.
     * @param levelManager  Gestor de nivel para interacciones con el entorno.
     */
    public Player(PlayerState estadoInicial, LevelManager levelManager) {
        this(estadoInicial);
        this.levelManager = levelManager;
        this.gameClient = null;
        this.estado.estadoAnimacion = EstadoPlayer.IDLE_RIGHT; // Establecer un estado por defecto es útil
        // Inicializar el mapa de animaciones
        animations = new EnumMap<>(EstadoPlayer.class);
    }

    /**
     * Añade una gema al jugador e incrementa su contador.
     */
    public void anadirGema() {
        this.gemas++;
        Gdx.app.log("Sonic", "Gema recogida. Total: " + this.gemas);
    }

    /**
     * Asigna el gestor de nivel al jugador.
     *
     * @param levelManager gestor de nivel.
     */
    public void setLevelManager(LevelManager levelManager) {
        this.levelManager = levelManager;
    }

    /**
     * Obtiene el estado de animación actual del jugador.
     *
     * @return estado de animación.
     */
    public EstadoPlayer getEstadoActual() {
        return this.estado.estadoAnimacion;
    }

    /**
     * Cambia el estado de animación actual del jugador.
     *
     * @param estadoActual nuevo estado de animación.
     */
    public void setEstadoActual(EstadoPlayer estadoActual) {
        this.estado.estadoAnimacion = estadoActual;
    }

    /**
     * Obtiene el estado lógico del jugador.
     *
     * @return objeto PlayerState con la información del jugador.
     */
    public PlayerState getEstado() {
        return estado;
    }

    /**
     * Configura si el jugador está en modo Super (para personajes específicos).
     *
     * @param esSuper true para modo Super, false en caso contrario.
     */
    public void setSuper(boolean esSuper) {
        // Por defecto, no hace nada. Las subclases como Sonic lo sobreescribirán.
    }

    /**
     * Asigna el administrador de sonido al jugador.
     *
     * @param soundManager instancia de SoundManager.
     */
    public void setSoundManager(SoundManager soundManager) {
        this.soundManager = soundManager;
    }

    /**
     * Asigna un nuevo estado lógico al jugador.
     *
     * @param estado nuevo estado PlayerState.
     */
    public void setEstado(PlayerState estado) {
        this.estado = estado;
    }

    /**
     * Inicializa valores por defecto de la entidad jugador.
     */
    @Override
    protected void setDefaultValues() {
        if (this.estado == null) {
            this.estado = new PlayerState();
            this.estado.x = 100;
            this.estado.y = 100;
        }
        speed = 2.8F;
        setEstadoActual(EstadoPlayer.IDLE_RIGHT);
    }

    /**
     * Obtiene los límites de colisión del jugador.
     *
     * @return Rectángulo de colisión actualizado.
     */
    //Para las colisiones
    public Rectangle getBounds() {
        if (bounds == null) {
            Gdx.app.log("Player", "ERROR CRÍTICO: bounds es nulo. ¡La inicialización del hitbox debe ocurrir en la subclase Sonic!");
            this.collisionWidth = getTileSize(); // Se mantiene usando getTileSize() (48)
            this.collisionHeight = getTileSize(); // Se mantiene usando getTileSize() (48)
            this.collisionOffsetX = 0;
            this.collisionOffsetY = 0;
            this.bounds = new Rectangle(estado.x, estado.y, collisionWidth, collisionHeight);
        }
        bounds.set(estado.x + collisionOffsetX, estado.y + collisionOffsetY, collisionWidth, collisionHeight);
        return bounds;
    }

    /**
     * Indica si el jugador está ejecutando una acción de ataque.
     *
     * @return true si ataca, false de lo contrario.
     */
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

    /**
     * Comprueba si habría colisión en una posición futura.
     *
     * @param newX coordenada X futura.
     * @param newY coordenada Y futura.
     * @return true si colisiona, false en caso contrario.
     */
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

        // Comprobar colisión con el mapa (bloques, árboles, etc.)
        if (levelManager.colisionaConMapa(futureBounds)) {
            return true; // Hay colisión con el mapa
        }

        // Colisión con bloques rompibles
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

        return false;
    }

    /**
     * Verifica si una acción bloqueante está en curso impidiendo el movimiento.
     *
     * @return true si bloquea movimiento, false en caso contrario.
     */
    public boolean isActionBlockingMovement() {
        if (animacion == null) return false;
        boolean isNormalAction = animacion.getPlayMode() == Animation.PlayMode.NORMAL;
        boolean isSpinning = getEstadoActual() == EstadoPlayer.SPECIAL_LEFT || getEstadoActual() == EstadoPlayer.SPECIAL_RIGHT;

        if (isSpinning && animacion.getPlayMode() == Animation.PlayMode.LOOP) {
            return false;
        }

        return isNormalAction && !animacion.isAnimationFinished(tiempoXFrame);
    }

    /**
     * Maneja la entrada de teclas para el movimiento del jugador.
     */
    @Override
    public void KeyHandler() {

        //Para las colisiones
        float targetX = estado.x;
        float targetY = estado.y;

        isMoving = false;
        proposedMovementState = null; // Reinicia el estado de movimiento propuesto

        // Calcula los valores tentativos sin modificar la posición real
        if (Gdx.input.isKeyPressed(Keys.W)) {
            targetY += speed;
            isMoving = true;
            // El estado de movimiento propuesto para el movimiento vertical depende de la última dirección horizontal
            if (lastDirection == EstadoPlayer.RIGHT || lastDirection == EstadoPlayer.IDLE_RIGHT) {
                proposedMovementState = EstadoPlayer.UP_RIGHT;
            } else {
                proposedMovementState = EstadoPlayer.UP_LEFT;
            }
        }

        if (Gdx.input.isKeyPressed(Keys.S)) {
            targetY -= speed;
            isMoving = true;
            if (lastDirection == EstadoPlayer.RIGHT || lastDirection == EstadoPlayer.IDLE_RIGHT) {
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

        if (!isMoving) {
            //En caso de que no se este presionando ninguna tecla
            // Solo establece IDLE si no se están presionando teclas de movimiento
            if (lastDirection == EstadoPlayer.LEFT) {
                proposedMovementState = EstadoPlayer.IDLE_LEFT;
            } else {
                proposedMovementState = EstadoPlayer.IDLE_RIGHT;
            }
        }

        if (!checkCollision(targetX, estado.y)) {
            estado.x = targetX;
        }
        if (!checkCollision(estado.x, targetY)) {
            estado.y = targetY;
        }

        // Limitar al personaje a los bordes del mapa usando getTileSize()
        if (levelManager != null) {
            float mapWidth = levelManager.getAnchoMapaPixels();
            float mapHeight = levelManager.getAltoMapaPixels();

            estado.x = MathUtils.clamp(estado.x, 0, mapWidth - getTileSize());
            estado.y = MathUtils.clamp(estado.y, 0, mapHeight - getTileSize());
        }

    }


    /**
     * Recolecta objetos del entorno que colisionan con el jugador.
     *
     * @param items lista de objetos ItemVisual.
     */
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

    /**
     * Intenta romper un bloque si el personaje es Knuckles y colisiona.
     */
    public void intentarRomperBloque() {
        // Solo Knuckles puede romper bloques
        if (!"Knuckles".equals(this.characterName)) {
            return;
        }

        if (levelManager == null || levelManager.getBloquesRompibles() == null) {
            return;
        }

        // Usamos los bounds del jugador para ver si se superpone con un bloque
        Rectangle playerBounds = getBounds();

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

    /**
     * Muestra un mensaje UI por pantalla.
     *
     * @param texto texto a mostrar.
     */
    public void mostrarMensaje(String texto) {
    }

    /**
     * Libera los recursos del jugador (por ejemplo, texturas).
     */
    @Override
    public void dispose() {
        if (spriteSheet != null) {
            spriteSheet.dispose();
        }
    }

    /**
     * Asigna el cliente de red al jugador.
     *
     * @param client instancia IGameClient.
     */
    public void setGameClient(IGameClient client) {
        this.gameClient = client;
    }
}
