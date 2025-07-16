package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Matrix4; // <-- NUEVA IMPORTACIÓN NECESARIA


public class Sonic extends Player {

    protected TextureRegion[] frameSpinRight;
    protected TextureRegion[] frameSpinLeft;

    // --- VARIABLES PARA HABILIDAD Y DESTELLO ---
    private static final float CLEAN_COOLDOWN_SECONDS = 40.0f;
    private float cleanCooldownTimer = 0.0f;
    private float flashDurationTimer = 0.0f;
    private Texture texturaBlanca;

    // --- VARIABLES PARA EL INDICADOR DE COOLDOWN (TORNADO) ---
    private Texture tornadoSheet;
    private Animation<TextureRegion> tornadoAnimation;
    private TextureRegion cooldownIndicatorFrame;
    private float cooldownIndicatorTime = 0f; // Tiempo para la animación del indicador
    private static final float INDICATOR_SIZE = 32f; // Tamaño del icono del tornado

    // --- NUEVA VARIABLE PARA LA MATRIZ DE PROYECCIÓN ---
    private Matrix4 screenProjectionMatrix;


    public Sonic(PlayerState estadoInicial) {
        super(estadoInicial);
        CargarSprites();
        inicializarHitbox();
        inicializarRecursosAdicionales();
        EstadoPlayer estadoInicialAnimacion = (getEstadoActual() != null && animations.containsKey(getEstadoActual())) ? getEstadoActual() : EstadoPlayer.IDLE_RIGHT;
        animacion = animations.get(estadoInicialAnimacion);
        if (animacion == null) {
            Gdx.app.error("Sonic", "ERROR: Animación inicial nula para el estado: " + estadoInicialAnimacion + ". Verifique CargarSprites.");
        }
    }

    public Sonic(PlayerState estadoInicial, LevelManager levelManager) {
        super(estadoInicial, levelManager);
        CargarSprites();
        inicializarHitbox();
        inicializarRecursosAdicionales();
        EstadoPlayer estadoInicialAnimacion = (getEstadoActual() != null && animations.containsKey(getEstadoActual())) ? getEstadoActual() : EstadoPlayer.IDLE_RIGHT;
        animacion = animations.get(estadoInicialAnimacion);
        if (animacion == null) {
            Gdx.app.error("Sonic", "ERROR: Animación inicial nula para el estado: " + estadoInicialAnimacion + ". Verifique CargarSprites.");
        }
    }

    /**
     * Inicializa recursos adicionales como la textura del destello y el indicador de habilidad.
     */
    private void inicializarRecursosAdicionales() {
        // Inicializar textura del destello
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        texturaBlanca = new Texture(pixmap);
        pixmap.dispose();

        // --- INICIALIZAR ANIMACIÓN DEL INDICADOR ---
        tornadoSheet = new Texture(Gdx.files.internal("Entidades/Player/Sonic/Tornado/tornado_icon_16.png"));
        TextureRegion[][] tmpFrames = TextureRegion.split(tornadoSheet, 16, 16);
        tornadoAnimation = new Animation<TextureRegion>(0.08f, tmpFrames[0]);
        tornadoAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // --- INICIALIZAR LA MATRIZ DE PROYECCIÓN ---
        screenProjectionMatrix = new Matrix4();
    }

    public void activarEfectoFlash() {
        this.flashDurationTimer = 0.25f;
    }
    @Override
    public void KeyHandler() {
        float currentX = estado.x;
        float currentY = estado.y;

        super.KeyHandler();

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) ) {
          //  this.flashDurationTimer = 0.25f;
          //  this.cleanCooldownTimer = CLEAN_COOLDOWN_SECONDS;
           Gdx.app.log("Sonic", "Habilidad activada. Cooldown de " + CLEAN_COOLDOWN_SECONDS + "s iniciado.");

        }

        actionStateSet = false;

        if (isActionBlockingMovement()) {
            estado.x = currentX;
            estado.y = currentY;
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            if (lastDirection == EstadoPlayer.LEFT || lastDirection == EstadoPlayer.IDLE_LEFT) {
                setEstadoActual(EstadoPlayer.HIT_LEFT);
            } else {
                setEstadoActual(EstadoPlayer.HIT_RIGHT);
            }
            tiempoXFrame = 0;
            actionStateSet = true;
            estado.x = currentX;
            estado.y = currentY;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
            if (lastDirection == EstadoPlayer.LEFT || lastDirection == EstadoPlayer.IDLE_LEFT) {
                setEstadoActual(EstadoPlayer.KICK_LEFT);
            } else {
                setEstadoActual(EstadoPlayer.KICK_RIGHT);
            }
            tiempoXFrame = 0;
            actionStateSet = true;
            estado.x = currentX;
            estado.y = currentY;
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.L)) {
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                setEstadoActual(EstadoPlayer.SPECIAL_LEFT);
                lastDirection = EstadoPlayer.LEFT;
                estado.x -= speed;
            } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                setEstadoActual(EstadoPlayer.SPECIAL_RIGHT);
                lastDirection = EstadoPlayer.RIGHT;
                estado.x += speed;
            } else {
                if (lastDirection == EstadoPlayer.LEFT || lastDirection == EstadoPlayer.IDLE_LEFT) {
                    setEstadoActual(EstadoPlayer.SPECIAL_LEFT);
                } else {
                    setEstadoActual(EstadoPlayer.SPECIAL_RIGHT);
                }
            }
            actionStateSet = true;
        }

        if (!actionStateSet) {
            if (isMoving) {
                setEstadoActual(proposedMovementState);
            } else {
                if (lastDirection == EstadoPlayer.LEFT) {
                    setEstadoActual(EstadoPlayer.IDLE_LEFT);
                } else {
                    setEstadoActual(EstadoPlayer.IDLE_RIGHT);
                }
            }
        }
    }

    public void iniciarCooldownVisual() {
        this.cleanCooldownTimer = CLEAN_COOLDOWN_SECONDS;
    }

    protected String getSpriteSheetPath() {
        return "Entidades/Player/Sonic/sonic.png";
    }

    public boolean isCleanAbilityReady() {
        return cleanCooldownTimer <= 0;
    }

    public TextureRegion getCooldownIndicatorFrame() {
        return this.cooldownIndicatorFrame;
    }

    @Override
    protected void CargarSprites() {
        Texture coleccionDeSprites = new Texture(Gdx.files.internal(getSpriteSheetPath()));
        setSpriteSheet(coleccionDeSprites);

        TextureRegion[][] matrizDeSprites = TextureRegion.split(getSpriteSheet(), getSpriteSheet().getWidth() / 8, getSpriteSheet().getHeight() / 30);

        frameIdleRight = new TextureRegion[8];
        frameIdleLeft = new TextureRegion[8];
        frameUpRight = new TextureRegion[8];
        frameUpLeft = new TextureRegion[8];
        frameDownRight = new TextureRegion[8];
        frameDownLeft = new TextureRegion[8];
        frameLeft = new TextureRegion[8];
        frameRight = new TextureRegion[8];
        frameHitRight = new TextureRegion[8];
        frameHitLeft = new TextureRegion[8];
        frameKickRight = new TextureRegion[8];
        frameKickLeft = new TextureRegion[8];
        frameSpinRight = new TextureRegion[24];
        frameSpinLeft = new TextureRegion[24];

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
        for (int i = 0; i < 8; i++) {
            frameHitLeft[i] = matrizDeSprites[7][i];
        }
        for (int i = 0; i < 8; i++) {
            frameHitRight[i] = new TextureRegion(frameHitLeft[i]);
            frameHitRight[i].flip(true, false);
        }
        for (int i = 0; i < 8; i++) {
            frameKickLeft[i] = matrizDeSprites[8][i];
        }
        for (int i = 0; i < 8; i++) {
            frameKickRight[i] = new TextureRegion(frameKickLeft[i]);
            frameKickRight[i].flip(true, false);
        }
        for (int i = 0; i < 24; i++) {
            frameSpinLeft[i] = matrizDeSprites[18][i % 8];
        }
        for (int i = 0; i < 24; i++) {
            frameSpinRight[i] = new TextureRegion(frameSpinLeft[i]);
            frameSpinRight[i].flip(true, false);
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
        animations.put(EstadoPlayer.KICK_RIGHT, new Animation<TextureRegion>(0.1f, frameKickRight));
        animations.put(EstadoPlayer.KICK_LEFT, new Animation<TextureRegion>(0.1f, frameKickLeft));
        animations.put(EstadoPlayer.SPECIAL_RIGHT, new Animation<TextureRegion>(0.07f, frameSpinRight));
        animations.put(EstadoPlayer.SPECIAL_LEFT, new Animation<TextureRegion>(0.07f, frameSpinLeft));

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
        animations.get(EstadoPlayer.KICK_RIGHT).setPlayMode(Animation.PlayMode.NORMAL);
        animations.get(EstadoPlayer.KICK_LEFT).setPlayMode(Animation.PlayMode.NORMAL);
        animations.get(EstadoPlayer.SPECIAL_RIGHT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoPlayer.SPECIAL_LEFT).setPlayMode(Animation.PlayMode.LOOP);

        Animation<TextureRegion> initialAnimation = animations.get(getEstadoActual());
        if (initialAnimation != null) {
            setFrameActual(initialAnimation.getKeyFrame(0));
        }
    }

    private void inicializarHitbox() {
        float baseTileSize = getTileSize();
        this.collisionWidth = baseTileSize * 0.6f;
        this.collisionHeight = baseTileSize * 0.75f;
        this.collisionOffsetX = (baseTileSize - collisionWidth) / 2f;
        this.collisionOffsetY = 0;
        this.bounds = new Rectangle(estado.x + collisionOffsetX, estado.y + collisionOffsetY, collisionWidth, collisionHeight);
        Gdx.app.log("Sonic", "Hitbox inicializado (basado en Entity.tileSize): " + this.bounds.toString());
        Gdx.app.log("Sonic", "Entity.tileSize usado para hitbox: " + baseTileSize);
        Gdx.app.log("Sonic", "Offsets del hitbox: x=" + collisionOffsetX + ", y=" + collisionOffsetY);
    }

    @Override
    public void update(float deltaTime) {
        // --- LÓGICA DE TEMPORIZADORES ---
        if (cleanCooldownTimer > 0) {
            cleanCooldownTimer -= deltaTime;
        }
        if (flashDurationTimer > 0) {
            flashDurationTimer -= deltaTime;
        }

        // --- ACTUALIZAR ANIMACIÓN DEL INDICADOR ---
        cooldownIndicatorTime += deltaTime;
        cooldownIndicatorFrame = tornadoAnimation.getKeyFrame(cooldownIndicatorTime);
        // --- FIN DE LÓGICA DE INDICADOR ---

        bounds.setPosition(estado.x + collisionOffsetX, estado.y + collisionOffsetY);

        if (!animations.containsKey(getEstadoActual())) {
            Gdx.app.log("Sonic", "Advertencia: Estado " + getEstadoActual() + " no tiene animación. Cambiando a IDLE_RIGHT.");
            setEstadoActual(EstadoPlayer.IDLE_RIGHT);
        }

        Animation<TextureRegion> targetAnimation = animations.get(getEstadoActual());

        if (this.animacion != targetAnimation) {
            this.tiempoXFrame = 0;
            this.animacion = targetAnimation;
            Gdx.app.log("Sonic", "Cambio de animación a: " + getEstadoActual());
        }

        if (this.animacion == null) {
            Gdx.app.error("Sonic", "CRÍTICO: 'animacion' es nula en update() después de la reasignación. Estado: " + getEstadoActual());
            frameActual = null;
            return;
        }

        tiempoXFrame += deltaTime;

        if ((estado.estadoAnimacion == EstadoPlayer.HIT_RIGHT || estado.estadoAnimacion == EstadoPlayer.HIT_LEFT ||
            estado.estadoAnimacion == EstadoPlayer.KICK_RIGHT || estado.estadoAnimacion == EstadoPlayer.KICK_LEFT) && animacion != null) {

            if (animacion.getPlayMode() == Animation.PlayMode.NORMAL && animacion.isAnimationFinished(tiempoXFrame)) {
                if (lastDirection == EstadoPlayer.LEFT || lastDirection == EstadoPlayer.IDLE_LEFT) {
                    setEstadoActual(EstadoPlayer.IDLE_LEFT);
                } else {
                    setEstadoActual(EstadoPlayer.IDLE_RIGHT);
                }
                tiempoXFrame = 0;
                Gdx.app.log("Sonic", "Transición de acción a IDLE: " + getEstadoActual());
            }
        }

        if (animacion != null) {
            frameActual = animacion.getKeyFrame(tiempoXFrame);
        } else {
            Gdx.app.log("Sonic", "Advertencia: 'animacion' es nula en update(). No se puede obtener el frame clave para estado: " + getEstadoActual());
            frameActual = null;
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        // Dibuja a Sonic y el indicador con la cámara del juego.
        if (frameActual != null) {
            batch.draw(frameActual, estado.x, estado.y, getTileSize(), getTileSize());
        } else {
            Gdx.app.log("Sonic", "Advertencia: 'frameActual' es nulo en el método draw(). No se puede dibujar a Sonic.");
        }
        

        // --- LÓGICA DE DESTELLO CORREGIDA ---
        if (flashDurationTimer > 0) {
            // 1. Dibuja todo lo que estaba pendiente (Sonic y el indicador) con la cámara del juego.
            batch.flush();

            // 2. Guarda la matriz de la cámara del juego actual.
            Matrix4 oldProjection = batch.getProjectionMatrix().cpy();

            // 3. Configura y aplica una nueva matriz que dibuja en coordenadas de pantalla.
            //    Se actualiza cada vez por si la ventana cambia de tamaño.
            screenProjectionMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.setProjectionMatrix(screenProjectionMatrix);

            // 4. Dibuja el destello. Ahora sí ocupará toda la pantalla.
            Color colorOriginal = batch.getColor().cpy();
            batch.setColor(1, 1, 1, 0.8f);
            batch.draw(texturaBlanca, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.setColor(colorOriginal);

            // 5. Dibuja el destello inmediatamente.
            batch.flush();

            // 6. ¡MUY IMPORTANTE! Restaura la matriz de la cámara del juego original.
            batch.setProjectionMatrix(oldProjection);
        }
    }

    // ---[AÑADE ESTE MÉTODO]---
    public float getFlashDurationTimer() {
        return this.flashDurationTimer;
    }

    @Override
    public void dispose() {
        super.dispose(); // Llama al dispose del padre (Player)
        if (texturaBlanca != null) {
            texturaBlanca.dispose();
            Gdx.app.log("Sonic", "Textura del destello liberada.");
        }
        // --- LIBERAR TEXTURA DEL INDICADOR ---
        if (tornadoSheet != null) {
            tornadoSheet.dispose();
            Gdx.app.log("Sonic", "Textura del indicador de cooldown liberada.");
        }
    }
}
