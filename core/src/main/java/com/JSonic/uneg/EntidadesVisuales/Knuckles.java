package com.JSonic.uneg.EntidadesVisuales;

import com.JSonic.uneg.LevelManager;
import com.JSonic.uneg.State.PlayerState;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;


/**
 * Clase para el personaje Knuckles, extiende Player con animaciones y controles específicos.
 */
public class Knuckles extends Player {

    protected TextureRegion[] frameSpinRight;
    protected TextureRegion[] frameSpinLeft;
    protected TextureRegion[] framePunchRight;
    protected TextureRegion[] framePunchLeft;

    private float tiempoDesdeUltimoGolpe = 0f; // Tiempo para detectar doble toque
    private final float cooldownGolpe = 0.4f; // Tiempo máximo entre toques para considerar un doble toque
    private boolean haIniciadoGolpeEsteFrame = false;

    /**
     * Constructor de Knuckles con estado inicial.
     *
     * @param estadoInicial Estado inicial del jugador.
     */
    public Knuckles(PlayerState estadoInicial) {
        super(estadoInicial);
        this.characterName = "Knuckles";
        CargarSprites();
        inicializarHitbox();
        EstadoPlayer estadoInicialAnimacion = (getEstadoActual() != null && animations.containsKey(getEstadoActual())) ? getEstadoActual() : EstadoPlayer.IDLE_RIGHT;
        animacion = animations.get(estadoInicialAnimacion);
        if (animacion == null) {
            Gdx.app.error("Knuckles", "ERROR: Animación inicial nula para el estado: " + estadoInicialAnimacion + ". Verifique CargarSprites.");
        }
    }

    /**
     * Constructor de Knuckles con estado inicial y administrador de nivel.
     *
     * @param estadoInicial Estado inicial del jugador.
     * @param levelManager  Administrador de nivel para interactuar con el entorno.
     */
    public Knuckles(PlayerState estadoInicial, LevelManager levelManager) {
        super(estadoInicial, levelManager);
        this.characterName = "Knuckles";
        CargarSprites();
        inicializarHitbox();
        EstadoPlayer estadoInicialAnimacion = (getEstadoActual() != null && animations.containsKey(getEstadoActual())) ? getEstadoActual() : EstadoPlayer.IDLE_RIGHT;
        animacion = animations.get(estadoInicialAnimacion);
        if (animacion == null) {
            Gdx.app.error("Knuckles", "ERROR: Animación inicial nula para el estado: " + estadoInicialAnimacion + ". Verifique CargarSprites.");
        }
    }

    /**
     * Maneja la entrada de teclas específica de Knuckles, incluyendo ataques y acciones especiales.
     */
    @Override
    public void KeyHandler() {
        // Primero, reiniciamos la bandera de acción de este frame.
        haIniciadoGolpeEsteFrame = false;
        // Guarda la posición actual antes de que el KeyHandler del padre la modifique
        float currentX = estado.x;
        float currentY = estado.y;

        super.KeyHandler();

        actionStateSet = false;

        // --- Lógica para MANEJAR TECLAS DE ACCIÓN ---
        // Prioriza las acciones bloqueantes (HIT, KICK)

        if (isActionBlockingMovement()) {
            // Si está en una acción bloqueante, RESTAURA la posición a la que estaba antes del super.KeyHandler()
            // para evitar cualquier desplazamiento no deseado.
            estado.x = currentX;
            estado.y = currentY;
            return; // Termina la ejecución del KeyHandler para este frame
        }

        // Maneja las teclas de acción inmediata (J, K) que deben anular el movimiento continuo por un corto período.
        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            if (soundManager != null) soundManager.play("golpe");
            if (lastDirection == EstadoPlayer.LEFT || lastDirection == EstadoPlayer.IDLE_LEFT) {
                setEstadoActual(EstadoPlayer.HIT_LEFT);
            } else {
                setEstadoActual(EstadoPlayer.HIT_RIGHT);
            }
            tiempoXFrame = 0; // Reinicia el tiempo para que la animación de golpe comience desde el principio
            actionStateSet = true;
            // Al activarse HIT, anula cualquier movimiento que el super.KeyHandler() haya intentado aplicar.
            estado.x = currentX;
            estado.y = currentY;
        }

        //para ataque especial (ROMPER BASURA IRROMPIBLE PARA OTROS PERSONAJES)
        else if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) { // Usamos la tecla Space para el puñetazo
            if (soundManager != null) soundManager.play("habilidad_knuckles_punch");
            if (lastDirection == EstadoPlayer.LEFT || lastDirection == EstadoPlayer.IDLE_LEFT) {
                setEstadoActual(EstadoPlayer.PUNCH_LEFT);
            } else {
                setEstadoActual(EstadoPlayer.PUNCH_RIGHT);
            }
            tiempoXFrame = 0; // Reinicia la animación
            actionStateSet = true;
            // Anula el movimiento mientras golpea
            estado.x = currentX;
            estado.y = currentY;
            this.haIniciadoGolpeEsteFrame = true;
            System.out.println("[Knuckles] ¡Teclas ESPACIO presionada, iniciando PUNCH y revisando bloques!");
        }

        // Maneja el ROMPE BLOQUES - Esta es una acción continua que SÍ permite movimiento en el eje X.
        else if (Gdx.input.isKeyPressed(Input.Keys.L)) {

            // Determina la dirección del ROMPE BLOQUES basándose en WASD si se presiona.
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                setEstadoActual(EstadoPlayer.SPECIAL_LEFT);
                lastDirection = EstadoPlayer.LEFT; // Actualiza lastDirection para consistencia
                estado.x = currentX;
            } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                setEstadoActual(EstadoPlayer.SPECIAL_RIGHT);
                lastDirection = EstadoPlayer.RIGHT; // Actualiza lastDirection para consistencia
                estado.x = currentX;
            } else {
                // Si L se presiona sin A o D, mantiene el giro en la última dirección horizontal conocida.
                if (lastDirection == EstadoPlayer.LEFT || lastDirection == EstadoPlayer.IDLE_LEFT) {
                    setEstadoActual(EstadoPlayer.SPECIAL_LEFT);
                } else {
                    setEstadoActual(EstadoPlayer.SPECIAL_RIGHT);
                }
            }
            actionStateSet = true; // Indica que se ha manejado una acción.
        }


        // Si ninguna acción especial (J, K, L) fue activada en este frame,
        if (!actionStateSet) {
            // 'isMoving' y 'proposedMovementState' se establecen en el KeyHandler del Player.
            if (isMoving) {
                // Si hay movimiento WASD, establece el estado actual al estado de movimiento propuesto.
                setEstadoActual(proposedMovementState);
            } else {
                // Si no hay movimiento y no se presionó ninguna tecla de acción, vuelve al estado IDLE.
                if (lastDirection == EstadoPlayer.LEFT) {
                    setEstadoActual(EstadoPlayer.IDLE_LEFT);
                } else {
                    setEstadoActual(EstadoPlayer.IDLE_RIGHT);
                }
            }
        }
    }


    /**
     * Obtiene la ruta relativa al sprite sheet de Knuckles.
     *
     * @return Cadena con la ruta del archivo de textura.
     */
    protected String getSpriteSheetPath() {
        return "Entidades/Player/Knuckles/knuckles.png";
    }

    /**
     * Carga las animaciones y sprites específicos de Knuckles desde su sprite sheet.
     */
    @Override
    protected void CargarSprites() {
        Texture coleccionDeSprites = new Texture(Gdx.files.internal(getSpriteSheetPath()));
        setSpriteSheet(coleccionDeSprites);

        TextureRegion[][] matrizDeSprites = TextureRegion.split(getSpriteSheet(), getSpriteSheet().getWidth() / 8, getSpriteSheet().getHeight() / 34);

        frameIdleRight = new TextureRegion[8];
        frameIdleLeft = new TextureRegion[8];
        frameUpRight = new TextureRegion[8];
        frameUpLeft = new TextureRegion[8];
        frameDownRight = new TextureRegion[8];
        frameDownLeft = new TextureRegion[8];
        frameLeft = new TextureRegion[8];
        frameRight = new TextureRegion[8];
        frameHitRight = new TextureRegion[12];
        frameHitLeft = new TextureRegion[12];
        frameKickRight = new TextureRegion[8];
        frameKickLeft = new TextureRegion[8];
        frameSpinRight = new TextureRegion[12];
        frameSpinLeft = new TextureRegion[12];
        //para destruir bloques
        framePunchRight = new TextureRegion[12];
        framePunchLeft = new TextureRegion[12];

        for (int i = 0; i < 4; i++) {
            frameIdleLeft[i] = matrizDeSprites[0][i];
        }
        for (int i = 0; i < 4; i++) {
            frameIdleLeft[i + 4] = matrizDeSprites[0][3 - i];
        }
        for (int i = 0; i < 8; i++) {
            frameIdleRight[i] = new TextureRegion(frameIdleLeft[i]);
            frameIdleRight[i].flip(true, false);
        }

        for (int i = 0; i < 8; i++) {
            frameUpLeft[i] = matrizDeSprites[1][i];
        }
        for (int i = 0; i < 8; i++) {
            frameUpRight[i] = new TextureRegion(frameUpLeft[i]);
            frameUpRight[i].flip(true, false);
        }

        for (int i = 0; i < 8; i++) {
            frameDownLeft[i] = matrizDeSprites[1][i];
        }
        for (int i = 0; i < 8; i++) {
            frameDownRight[i] = new TextureRegion(frameDownLeft[i]);
            frameDownRight[i].flip(true, false);
        }

        for (int i = 0; i < 8; i++) {
            frameLeft[i] = matrizDeSprites[1][i];
        }
        for (int i = 0; i < 8; i++) {
            frameRight[i] = new TextureRegion(frameLeft[i]);
            frameRight[i].flip(true, false);
        }

        for (int i = 0; i < 6; i++) {
            frameHitLeft[i] = matrizDeSprites[8][i];
        }
        for (int i = 0; i < 6; i++) {
            frameHitLeft[i + 6] = matrizDeSprites[8][5 - i];
        }

        for (int i = 0; i < 12; i++) {
            frameHitRight[i] = new TextureRegion(frameHitLeft[i]);
            frameHitRight[i].flip(true, false);
        }

        for (int i = 0; i < 6; i++) {
            frameSpinLeft[i] = matrizDeSprites[10][i]; // Primeros 4 directos
        }
        for (int i = 0; i < 6; i++) {
            frameSpinLeft[i + 6] = matrizDeSprites[10][5 - i]; // Siguientes 4 inversos de los primeros 4
            // Esto asume que tienes suficientes sprites en matrizDeSprites[11] (al menos hasta el índice 3)
        }

        for (int i = 0; i < 12; i++) { // Cambiado de 24 a 8
            frameSpinRight[i] = new TextureRegion(frameSpinLeft[i]);
            frameSpinRight[i].flip(true, false); // Voltear horizontalmente
        }

        for (int i = 0; i < 6; i++) {
            framePunchLeft[i] = matrizDeSprites[9][i];
        }
        for (int i = 0; i < 6; i++) {
            framePunchLeft[i + 6] = matrizDeSprites[9][5 - i];
        }

        for (int i = 0; i < 12; i++) {
            framePunchRight[i] = new TextureRegion(framePunchLeft[i]);
            framePunchRight[i].flip(true, false);
        }

        animations.put(EstadoPlayer.IDLE_RIGHT, new Animation<TextureRegion>(0.12f, frameIdleRight));
        animations.put(EstadoPlayer.IDLE_LEFT, new Animation<TextureRegion>(0.12f, frameIdleLeft));
        animations.put(EstadoPlayer.UP_LEFT, new Animation<TextureRegion>(0.1f, frameUpLeft));
        animations.put(EstadoPlayer.UP_RIGHT, new Animation<TextureRegion>(0.1f, frameUpRight));
        animations.put(EstadoPlayer.DOWN_LEFT, new Animation<TextureRegion>(0.26f, frameDownLeft));
        animations.put(EstadoPlayer.DOWN_RIGHT, new Animation<TextureRegion>(0.26f, frameDownRight));
        animations.put(EstadoPlayer.LEFT, new Animation<TextureRegion>(0.2f, frameLeft));
        animations.put(EstadoPlayer.RIGHT, new Animation<TextureRegion>(0.2f, frameRight));
        animations.put(EstadoPlayer.HIT_RIGHT, new Animation<TextureRegion>(0.08f, frameHitRight));
        animations.put(EstadoPlayer.HIT_LEFT, new Animation<TextureRegion>(0.08f, frameHitLeft));
        animations.put(EstadoPlayer.SPECIAL_RIGHT, new Animation<TextureRegion>(0.07f, frameSpinRight));
        animations.put(EstadoPlayer.SPECIAL_LEFT, new Animation<TextureRegion>(0.07f, frameSpinLeft));

        animations.put(EstadoPlayer.PUNCH_RIGHT, new Animation<TextureRegion>(0.08f, framePunchRight));
        animations.put(EstadoPlayer.PUNCH_LEFT, new Animation<TextureRegion>(0.08f, framePunchLeft));

        animations.get(EstadoPlayer.IDLE_RIGHT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoPlayer.IDLE_LEFT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoPlayer.UP_LEFT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoPlayer.UP_RIGHT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoPlayer.DOWN_LEFT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoPlayer.DOWN_RIGHT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoPlayer.LEFT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoPlayer.RIGHT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoPlayer.HIT_RIGHT).setPlayMode(Animation.PlayMode.NORMAL);
        animations.get(EstadoPlayer.HIT_LEFT).setPlayMode(Animation.PlayMode.NORMAL);
        animations.get(EstadoPlayer.SPECIAL_RIGHT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoPlayer.SPECIAL_LEFT).setPlayMode(Animation.PlayMode.LOOP);
        //para destruir bloques
        animations.get(EstadoPlayer.PUNCH_RIGHT).setPlayMode(Animation.PlayMode.NORMAL);
        animations.get(EstadoPlayer.PUNCH_LEFT).setPlayMode(Animation.PlayMode.NORMAL);

        Animation<TextureRegion> initialAnimation = animations.get(getEstadoActual());
        if (initialAnimation != null) {
            setFrameActual(initialAnimation.getKeyFrame(0));
        }
    }

    /**
     * Inicializa el hitbox del personaje basado en el tamaño de tile y offsets de colisión.
     */
    private void inicializarHitbox() {
        float baseTileSize = getTileSize();

        this.collisionWidth = baseTileSize * 0.6f;
        this.collisionHeight = baseTileSize * 0.75f;

        this.collisionOffsetX = (baseTileSize - collisionWidth) / 2f;
        this.collisionOffsetY = 0;

        this.bounds = new Rectangle(estado.x + collisionOffsetX, estado.y + collisionOffsetY, collisionWidth, collisionHeight);

        Gdx.app.log("Knuckles", "Hitbox inicializado (basado en Entity.tileSize): " + this.bounds.toString());
        Gdx.app.log("Knuckles", "Entity.tileSize usado para hitbox: " + baseTileSize);
        Gdx.app.log("Knuckles", "Offsets del hitbox: x=" + collisionOffsetX + ", y=" + collisionOffsetY);
    }


    /**
     * Actualiza la lógica y la animación de Knuckles cada frame.
     *
     * @param deltaTime tiempo transcurrido desde el último frame en segundos.
     */
    @Override
    public void update(float deltaTime) {


        // Actualizamos temporizadores y la posición del hitbox.
        tiempoDesdeUltimoGolpe += deltaTime;
        bounds.setPosition(estado.x + collisionOffsetX, estado.y + collisionOffsetY);

        // Si el estado actual no tiene una animación cargada, por defecto a IDLE_RIGHT.
        if (!animations.containsKey(getEstadoActual())) {
            Gdx.app.log("Knuckles", "Advertencia: Estado " + getEstadoActual() + " no tiene animación. Cambiando a IDLE_RIGHT.");
            setEstadoActual(EstadoPlayer.IDLE_RIGHT);
        }

        Animation<TextureRegion> targetAnimation = animations.get(getEstadoActual());

        // Cambia la animación si el estado ha cambiado.
        if (this.animacion != targetAnimation) {
            this.tiempoXFrame = 0;
            this.animacion = targetAnimation;
            Gdx.app.log("Knuckles", "Cambio de animación a: " + getEstadoActual());
        }

        // Comprobación de seguridad por si la animación no se carga.
        if (this.animacion == null) {
            Gdx.app.error("Knuckles", "CRÍTICO: 'animacion' es nula en update(). Estado: " + getEstadoActual());
            frameActual = null;
            return;
        }

        // Avanza el tiempo de la animación.
        tiempoXFrame += deltaTime;

        // --- LÓGICA DE TRANSICIÓN PARA ACCIONES DE UN SOLO USO ---
        boolean esAccionDeUnUso = (estado.estadoAnimacion == EstadoPlayer.PUNCH_RIGHT || estado.estadoAnimacion == EstadoPlayer.PUNCH_LEFT ||
            estado.estadoAnimacion == EstadoPlayer.HIT_RIGHT || estado.estadoAnimacion == EstadoPlayer.HIT_LEFT ||
            estado.estadoAnimacion == EstadoPlayer.KICK_RIGHT || estado.estadoAnimacion == EstadoPlayer.KICK_LEFT);

        // Si es una acción de un solo uso y su animación ha terminado...
        if (esAccionDeUnUso && animacion.isAnimationFinished(tiempoXFrame)) {
            // ...volvemos al estado IDLE correspondiente.
            if (lastDirection == EstadoPlayer.LEFT || lastDirection == EstadoPlayer.IDLE_LEFT) {
                setEstadoActual(EstadoPlayer.IDLE_LEFT);
            } else {
                setEstadoActual(EstadoPlayer.IDLE_RIGHT);
            }
            // Reiniciamos el tiempo para la nueva animación IDLE.
            tiempoXFrame = 0;
            Gdx.app.log("Knuckles", "Transición de acción a IDLE: " + getEstadoActual());
        }

        // Actualiza el frame visual del personaje.
        if (animacion != null) {
            frameActual = animacion.getKeyFrame(tiempoXFrame);
        } else {
            frameActual = null;
        }
    }

    /**
     * Indica si Knuckles ha iniciado un golpe en el frame actual.
     *
     * @return true si se inició la acción de golpe, false de lo contrario.
     */
    public boolean haIniciadoGolpe() {
        return haIniciadoGolpeEsteFrame;
    }

    /**
     * Dibuja el personaje Knuckles en pantalla.
     *
     * @param batch SpriteBatch usado para el renderizado.
     */
    @Override
    public void draw(SpriteBatch batch) {
        if (frameActual != null) {
            batch.draw(frameActual, estado.x, estado.y, getTileSize(), getTileSize());
        } else {
            Gdx.app.log("Sonic", "Advertencia: 'frameActual' es nulo en el método draw(). No se puede dibujar a Sonic.");
        }
    }
}
