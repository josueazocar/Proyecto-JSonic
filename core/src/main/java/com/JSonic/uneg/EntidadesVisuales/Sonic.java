package com.JSonic.uneg.EntidadesVisuales;

import com.JSonic.uneg.LevelManager;
import com.JSonic.uneg.State.PlayerState;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Matrix4;

import java.util.EnumMap;

/**
 * Clase para el personaje Sonic, con animaciones, habilidades CLEAN y transformación a Super Sonic.
 */
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

    private Matrix4 screenProjectionMatrix;

    // --- ESTADO Y ATRIBUTOS DE SUPER SONIC ---
    private boolean esSuperSonic = false;
    private static final float VELOCIDAD_NORMAL = 2.5f;
    private static final float VELOCIDAD_SUPER = 3.5f;

    // --- RECURSOS PARA SUPER SONIC ---
    private Texture superSonicSpriteSheet;
    protected EnumMap<EstadoPlayer, Animation<TextureRegion>> animationsSuper = new EnumMap<>(EstadoPlayer.class);

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
     * Inicializa recursos adicionales como textura de flash y animación de indicador.
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

    /**
     * Activa el efecto de destello en pantalla.
     */
    public void activarEfectoFlash() {
        this.flashDurationTimer = 0.25f;
    }

    /**
     * Maneja la entrada de teclado de Sonic, incluyendo movimiento y habilidades.
     */
    @Override
    public void KeyHandler() {

        float currentSpeed = esSuperSonic ? VELOCIDAD_SUPER : VELOCIDAD_NORMAL;

        float currentX = estado.x;
        float currentY = estado.y;
        this.speed = currentSpeed;
        super.KeyHandler();

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) ) {

            if (soundManager != null) soundManager.play("habilidad_Sonic_punch");
            Gdx.app.log("Sonic", "Habilidad activada. Cooldown de " + CLEAN_COOLDOWN_SECONDS + "s iniciado.");
        }

        actionStateSet = false;

        if (isActionBlockingMovement()) {
            estado.x = currentX;
            estado.y = currentY;
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            if (soundManager != null) soundManager.play("golpe");
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
            if (soundManager != null) soundManager.play("golpe");
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
            if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
                if (soundManager != null) {
                    soundManager.play("spin");
                }
            }
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {

                setEstadoActual(EstadoPlayer.SPECIAL_LEFT);
                lastDirection = EstadoPlayer.LEFT;

                estado.x -= currentSpeed;
            } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                setEstadoActual(EstadoPlayer.SPECIAL_RIGHT);
                lastDirection = EstadoPlayer.RIGHT;

                estado.x += currentSpeed;
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

    /**
     * Inicia el cooldown visual de la habilidad CLEAN.
     */
    public void iniciarCooldownVisual() {
        this.cleanCooldownTimer = CLEAN_COOLDOWN_SECONDS;
    }

    /**
     * Obtiene la ruta del sprite sheet de Sonic.
     * @return ruta del sprite sheet.
     */
    protected String getSpriteSheetPath() {
        return "Entidades/Player/Sonic/sonic.png";
    }

    /**
     * Indica si la habilidad CLEAN está lista.
     * @return true si el cooldown ha terminado.
     */
    public boolean isCleanAbilityReady() {
        return cleanCooldownTimer <= 0;
    }

    /**
     * Obtiene el frame actual del indicador de cooldown.
     * @return región de textura del indicador.
     */
    public TextureRegion getCooldownIndicatorFrame() {
        return this.cooldownIndicatorFrame;
    }

    /**
     * Carga los sprites y animaciones de Sonic normal.
     */
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

        CargarSuperSprites();

        Animation<TextureRegion> initialAnimation = animations.get(getEstadoActual());
        if (initialAnimation != null) {
            setFrameActual(initialAnimation.getKeyFrame(0));
        }
    }


    /**
     * Carga las animaciones y sprites de Super Sonic.
     */
    private void CargarSuperSprites() {
        Gdx.app.log("Sonic_SuperSprites", "Iniciando carga de recursos para Super Sonic...");
        try {

            superSonicSpriteSheet = new Texture(Gdx.files.internal("Entidades/Player/Sonic/sonicS.png"));

            TextureRegion[][] matrizDeSprites = TextureRegion.split(superSonicSpriteSheet, superSonicSpriteSheet.getWidth() / 8, superSonicSpriteSheet.getHeight() / 30);

            TextureRegion[] frameIdleRight = new TextureRegion[8]; TextureRegion[] frameIdleLeft = new TextureRegion[8];
            TextureRegion[] frameUpRight = new TextureRegion[8]; TextureRegion[] frameUpLeft = new TextureRegion[8];
            TextureRegion[] frameDownRight = new TextureRegion[8]; TextureRegion[] frameDownLeft = new TextureRegion[8];
            TextureRegion[] frameLeft = new TextureRegion[8]; TextureRegion[] frameRight = new TextureRegion[8];
            TextureRegion[] frameHitRight = new TextureRegion[8]; TextureRegion[] frameHitLeft = new TextureRegion[8];
            TextureRegion[] frameKickRight = new TextureRegion[8]; TextureRegion[] frameKickLeft = new TextureRegion[8];
            TextureRegion[] frameSpinRight = new TextureRegion[24]; TextureRegion[] frameSpinLeft = new TextureRegion[24];

            for (int i = 0; i < 4; i++) { frameIdleLeft[i] = matrizDeSprites[0][i]; }
            for (int i = 0; i < 4; i++) { frameIdleLeft[i + 4] = matrizDeSprites[0][3 - i]; }
            for (int i = 0; i < 8; i++) { frameIdleRight[i] = new TextureRegion(frameIdleLeft[i]); frameIdleRight[i].flip(true, false); }
            for (int i = 0; i < 8; i++) { frameUpLeft[i] = matrizDeSprites[1][i]; }
            for (int i = 0; i < 8; i++) { frameUpRight[i] = new TextureRegion(frameUpLeft[i]); frameUpRight[i].flip(true, false); }
            for (int i = 0; i < 8; i++) { frameDownLeft[i] = matrizDeSprites[1][i]; }
            for (int i = 0; i < 8; i++) { frameDownRight[i] = new TextureRegion(frameDownLeft[i]); frameDownRight[i].flip(true, false); }
            for (int i = 0; i < 8; i++) { frameLeft[i] = matrizDeSprites[1][i]; }
            for (int i = 0; i < 8; i++) { frameRight[i] = new TextureRegion(frameLeft[i]); frameRight[i].flip(true, false); }
            for (int i = 0; i < 8; i++) { frameHitLeft[i] = matrizDeSprites[7][i]; }
            for (int i = 0; i < 8; i++) { frameHitRight[i] = new TextureRegion(frameHitLeft[i]); frameHitRight[i].flip(true, false); }
            for (int i = 0; i < 8; i++) { frameKickLeft[i] = matrizDeSprites[8][i]; }
            for (int i = 0; i < 8; i++) { frameKickRight[i] = new TextureRegion(frameKickLeft[i]); frameKickRight[i].flip(true, false); }
            for (int i = 0; i < 24; i++) { frameSpinLeft[i] = matrizDeSprites[18][i % 8]; }
            for (int i = 0; i < 24; i++) { frameSpinRight[i] = new TextureRegion(frameSpinLeft[i]); frameSpinRight[i].flip(true, false); }

            // --- Se rellena el EnumMap 'animationsSuper' con TODAS las animaciones ---
            animationsSuper.put(EstadoPlayer.IDLE_RIGHT, new Animation<>(0.12f, frameIdleRight));
            animationsSuper.put(EstadoPlayer.IDLE_LEFT, new Animation<>(0.12f, frameIdleLeft));
            animationsSuper.put(EstadoPlayer.UP_LEFT, new Animation<>(0.08f, frameUpLeft));
            animationsSuper.put(EstadoPlayer.UP_RIGHT, new Animation<>(0.08f, frameUpRight));
            animationsSuper.put(EstadoPlayer.DOWN_LEFT, new Animation<>(0.20f, frameDownLeft));
            animationsSuper.put(EstadoPlayer.DOWN_RIGHT, new Animation<>(0.20f, frameDownRight));
            animationsSuper.put(EstadoPlayer.LEFT, new Animation<>(0.15f, frameLeft));
            animationsSuper.put(EstadoPlayer.RIGHT, new Animation<>(0.15f, frameRight));
            animationsSuper.put(EstadoPlayer.HIT_RIGHT, new Animation<>(0.07f, frameHitRight));
            animationsSuper.put(EstadoPlayer.HIT_LEFT, new Animation<>(0.07f, frameHitLeft));
            animationsSuper.put(EstadoPlayer.KICK_RIGHT, new Animation<>(0.08f, frameKickRight));
            animationsSuper.put(EstadoPlayer.KICK_LEFT, new Animation<>(0.08f, frameKickLeft));
            animationsSuper.put(EstadoPlayer.SPECIAL_RIGHT, new Animation<>(0.05f, frameSpinRight));
            animationsSuper.put(EstadoPlayer.SPECIAL_LEFT, new Animation<>(0.05f, frameSpinLeft));

            for (Animation<TextureRegion> anim : animationsSuper.values()) {
                anim.setPlayMode(Animation.PlayMode.LOOP);
            }
            animationsSuper.get(EstadoPlayer.HIT_RIGHT).setPlayMode(Animation.PlayMode.NORMAL);
            animationsSuper.get(EstadoPlayer.HIT_LEFT).setPlayMode(Animation.PlayMode.NORMAL);
            animationsSuper.get(EstadoPlayer.KICK_RIGHT).setPlayMode(Animation.PlayMode.NORMAL);
            animationsSuper.get(EstadoPlayer.KICK_LEFT).setPlayMode(Animation.PlayMode.NORMAL);

            Gdx.app.log("Sonic_SuperSprites", "Carga de Super Sonic completada. Total de animaciones cargadas: " + animationsSuper.size());

        } catch (Exception e) {
            Gdx.app.error("Sonic_SuperSprites", "CRÍTICO: Fallo al cargar los recursos de Super Sonic.", e);
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


    /**
     * Actualiza lógica, animaciones y timers de Sonic.
     * @param deltaTime tiempo transcurrido desde el último frame.
     */
    @Override
    public void update(float deltaTime) {
        // Determinamos el mapa de animaciones correcto. Ahora no será nulo.
        EnumMap<EstadoPlayer, Animation<TextureRegion>> currentAnimations = esSuperSonic ? animationsSuper : animations;

        // --- LÓGICA DE TEMPORIZADORES ---
        if (cleanCooldownTimer > 0) { cleanCooldownTimer -= deltaTime; }
        if (flashDurationTimer > 0) { flashDurationTimer -= deltaTime; }

        // --- ACTUALIZAR ANIMACIÓN DEL INDICADOR ---
        cooldownIndicatorTime += deltaTime;
        cooldownIndicatorFrame = tornadoAnimation.getKeyFrame(cooldownIndicatorTime);

        bounds.setPosition(estado.x + collisionOffsetX, estado.y + collisionOffsetY);

        if (currentAnimations == null) {
            Gdx.app.error("Sonic", "CRÍTICO: El mapa de animaciones ('currentAnimations') es nulo. Estado esSuperSonic: " + esSuperSonic);
            return; // Salida segura para evitar más errores.
        }

        if (!currentAnimations.containsKey(getEstadoActual())) {
            Gdx.app.log("Sonic", "Advertencia: Estado " + getEstadoActual() + " no tiene animación. Cambiando a IDLE_RIGHT.");
            setEstadoActual(EstadoPlayer.IDLE_RIGHT);
        }

        Animation<TextureRegion> targetAnimation = currentAnimations.get(getEstadoActual());

        if (this.animacion != targetAnimation) {
            this.tiempoXFrame = 0;
            this.animacion = targetAnimation;
            Gdx.app.log("Sonic", "Cambio de animación a: " + getEstadoActual() + (esSuperSonic ? " (Super)" : " (Normal)"));
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
    /**
     * Dibuja a Sonic, el indicador de cooldown y el efecto de flash en pantalla.
     * @param batch lote de sprites para renderizar.
     */
    @Override
    public void draw(SpriteBatch batch) {
        // Dibuja a Sonic y el indicador con la cámara del juego.
        if (frameActual != null) {
            batch.draw(frameActual, estado.x, estado.y, getTileSize(), getTileSize());
        } else {
            Gdx.app.log("Sonic", "Advertencia: 'frameActual' es nulo en el método draw(). No se puede dibujar a Sonic.");
        }

        if (flashDurationTimer > 0) {
            // Dibuja todo lo que estaba pendiente (Sonic y el indicador) con la cámara del juego.
            batch.flush();

            // Guarda la matriz de la cámara del juego actual.
            Matrix4 oldProjection = batch.getProjectionMatrix().cpy();

            // Configura y aplica una nueva matriz que dibuja en coordenadas de pantalla.
            //    Se actualiza cada vez por si la ventana cambia de tamaño.
            screenProjectionMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.setProjectionMatrix(screenProjectionMatrix);

            // Dibuja el destello. Ahora sí ocupará toda la pantalla.
            Color colorOriginal = batch.getColor().cpy();
            batch.setColor(1, 1, 1, 0.8f);
            batch.draw(texturaBlanca, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.setColor(colorOriginal);

            // Dibuja el destello inmediatamente.
            batch.flush();

            batch.setProjectionMatrix(oldProjection);
        }
    }

    /**
     * Obtiene el tiempo restante del efecto de flash.
     * @return tiempo en segundos.
     */
    public float getFlashDurationTimer() {
        return this.flashDurationTimer;
    }

    /**
     * Libera los recursos de Sonic, incluyendo texturas adicionales.
     */
    @Override
    public void dispose() {
        super.dispose(); // Llama al dispose del padre (Player)
        if (texturaBlanca != null) {
            texturaBlanca.dispose();
            Gdx.app.log("Sonic", "Textura del destello liberada.");
        }
        if (tornadoSheet != null) {
            tornadoSheet.dispose();
            Gdx.app.log("Sonic", "Textura del indicador de cooldown liberada.");
        }
        if (superSonicSpriteSheet != null) {
            superSonicSpriteSheet.dispose();
            Gdx.app.log("Sonic", "Textura de Super Sonic liberada.");
        }
    }

    /**
     * Activa o desactiva el modo Super Sonic.
     * @param esSuper true para activar Super Sonic.
     */
    @Override
    public void setSuper(boolean esSuper) {
        // Comprobamos si hay un cambio de estado real para no loguear innecesariamente.
        if (this.esSuperSonic != esSuper) {
            this.esSuperSonic = esSuper;
            this.estado.isSuper = esSuper;
            if (esSuper) {
                Gdx.app.log("Sonic", "¡ORDEN RECIBIDA! Transformando a Super Sonic.");
                // Aquí podrías añadir un efecto de sonido o visual si quisieras
            } else {
                Gdx.app.log("Sonic", "¡ORDEN RECIBIDA! Revirtiendo a Sonic normal.");
            }
        }
    }

}
