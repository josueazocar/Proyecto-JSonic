package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Rectangle;


public class Tails extends Player {

    protected TextureRegion[] frameSpinRight;
    protected TextureRegion[] frameSpinLeft;
    private Dron_Tails miDron;

    private transient BitmapFont font; // Se usa 'transient' para que no se intente serializar en red
    private transient GlyphLayout glyphLayout; // Ayuda a medir el texto para centrarlo
    private String mensajeUI;
    private float tiempoMensajeVisible;
    private static final float DURACION_MENSAJE = 3.0f; // Mensaje visible por 3 segundos


    public Tails(PlayerState estadoInicial) {
        super(estadoInicial);
        CargarSprites();
        inicializarHitbox();
        // Asegúrate de que animacion no sea nula al inicio
        // Si getEstadoActual() es nulo o no tiene una animación, usa un estado por defecto.
        EstadoPlayer estadoInicialAnimacion = (getEstadoActual() != null && animations.containsKey(getEstadoActual())) ? getEstadoActual() : EstadoPlayer.IDLE_RIGHT;
        animacion = animations.get(estadoInicialAnimacion);
        if (animacion == null) {
            Gdx.app.error("Sonic", "ERROR: Animación inicial nula para el estado: " + estadoInicialAnimacion + ". Verifique CargarSprites.");
        }
        miDron = new Dron_Tails(estadoInicial.id);

        this.font = new BitmapFont();
        this.font.getData().setScale(1);
        this.glyphLayout = new GlyphLayout();
        this.mensajeUI = "";
    }

    public Tails(PlayerState estadoInicial, LevelManager levelManager) {
        super(estadoInicial, levelManager);
        CargarSprites();
        inicializarHitbox();
        // Asegúrate de que animacion no sea nula al inicio
        // Si getEstadoActual() es nulo o no tiene una animación, usa un estado por defecto.
        EstadoPlayer estadoInicialAnimacion = (getEstadoActual() != null && animations.containsKey(getEstadoActual())) ? getEstadoActual() : EstadoPlayer.IDLE_RIGHT;
        animacion = animations.get(estadoInicialAnimacion);
        if (animacion == null) {
            Gdx.app.error("Sonic", "ERROR: Animación inicial nula para el estado: " + estadoInicialAnimacion + ". Verifique CargarSprites.");
        }
        miDron = new Dron_Tails(estadoInicial.id);

        this.font = new BitmapFont();
        this.font.getData().setScale(1);
        this.glyphLayout = new GlyphLayout();
        this.mensajeUI = "";
    }

    @Override
    public void KeyHandler() {
        // Guarda la posición actual antes de que el KeyHandler del padre la modifique
        float currentX = estado.x;
        float currentY = estado.y;

        // Llama primero al KeyHandler del padre para manejar el movimiento básico (WASD).
        // Esto calcula proposedMovementState y actualiza estado.x/y si no hay colisión,
        // pero aún NO QUEREMOS que esto se aplique si hay una acción bloqueante.
        super.KeyHandler();

        // Reinicia actionStateSet para el frame actual
        actionStateSet = false;

        // --- INVOCACIÓN DEL DRON ---
        // Se coloca aquí para que sea una acción independiente que no interrumpe otras.
        // Se comprueba en cada frame, permitiendo a Tails llamar al dron en cualquier momento.
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) { // O la tecla que prefieras
            miDron.invocar(this);
        }

        // --- Lógica para MANEJAR TECLAS DE ACCIÓN ---
        // (Golpe, Patada, Spin)

        // 1. Prioriza las acciones bloqueantes (HIT, KICK)
        // Si Sonic ya está en una acción bloqueante (que impide el movimiento), no procesa más entrada para movimiento u otras acciones.
        if (isActionBlockingMovement()) {
            // Si está en una acción bloqueante, RESTAURA la posición a la que estaba antes del super.KeyHandler()
            // para evitar cualquier desplazamiento no deseado.
            estado.x = currentX;
            estado.y = currentY;
            return; // Termina la ejecución del KeyHandler para este frame
        }

        // 2. Maneja las teclas de acción inmediata (J, K) que deben anular el movimiento continuo por un corto período.
        // Estas son acciones que impiden el movimiento mientras duran.
        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
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


        // 3. Maneja el ROMPE BLOQUES - Esta es una acción continua que SÍ permite movimiento en el eje X.
        // Se usa 'else if' para que SPIN solo se active si J o K no fueron presionadas.
        else if (Gdx.input.isKeyPressed(Input.Keys.L)) {
            // Determina la dirección del ROMPE BLOQUES basándose en WASD si se presiona.
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                setEstadoActual(EstadoPlayer.SPECIAL_LEFT);
                lastDirection = EstadoPlayer.LEFT; // Actualiza lastDirection para consistencia
                estado.x -= speed;
            } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                setEstadoActual(EstadoPlayer.SPECIAL_RIGHT);
                lastDirection = EstadoPlayer.RIGHT; // Actualiza lastDirection para consistencia
                estado.x += speed;
            } else {
                // Si L se presiona sin A o D, mantiene el giro en la última dirección horizontal conocida.
                // Aquí también se permite el movimiento si WASD fue presionado, el super.KeyHandler() ya lo aplicó.
                if (lastDirection == EstadoPlayer.LEFT || lastDirection == EstadoPlayer.IDLE_LEFT) {
                    setEstadoActual(EstadoPlayer.SPECIAL_LEFT);
                } else {
                    setEstadoActual(EstadoPlayer.SPECIAL_RIGHT);
                }
            }
            actionStateSet = true; // Indica que se ha manejado una acción.
        }

        // 4. Si ninguna acción especial (J, K, L) fue activada en este frame,
        // determina el estado basándose en el movimiento WASD o IDLE.
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


    protected String getSpriteSheetPath() {
        return "Entidades/Player/Tails/tails.png";
    }

    @Override
    protected void CargarSprites() {
        Texture coleccionDeSprites = new Texture(Gdx.files.internal(getSpriteSheetPath()));
        setSpriteSheet(coleccionDeSprites);

        TextureRegion[][] matrizDeSprites = TextureRegion.split(getSpriteSheet(), getSpriteSheet().getWidth() / 8, getSpriteSheet().getHeight() / 40);//40

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
        frameSpinRight = new TextureRegion[6];
        frameSpinLeft = new TextureRegion[6];

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
        }for (int i = 0; i < 6; i++) {
            frameHitLeft[i + 6] = matrizDeSprites[8][5 - i];
        }

        for (int i = 0; i < 12; i++) {
            frameHitRight[i] = new TextureRegion(frameHitLeft[i]);
            frameHitRight[i].flip(true, false);
        }



        frameSpinLeft[0] = matrizDeSprites[2][5];
        frameSpinLeft[1] = matrizDeSprites[2][6];
        frameSpinLeft[2] = matrizDeSprites[2][6];
        frameSpinLeft[3] = matrizDeSprites[2][6];
        frameSpinLeft[4] = matrizDeSprites[2][7];
        frameSpinLeft[5] = matrizDeSprites[2][7];

        // Tercer bucle: Crear frameSpinRight invirtiendo los frames de frameSpinLeft
        for (int i = 0; i < 6; i++) { // Cambiado de 24 a 8
            frameSpinRight[i] = new TextureRegion(frameSpinLeft[i]);
            frameSpinRight[i].flip(true, false); // Voltear horizontalmente
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
        bounds.setPosition(estado.x + collisionOffsetX, estado.y + collisionOffsetY);

        // Si el estado actual no tiene una animación cargada, por defecto a IDLE_RIGHT
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

        // Lógica de transición de estado después de que una animación de acción termina
        // Solo para animaciones de PlayMode.NORMAL como HIT o KICK.
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
        miDron.update(deltaTime);

        if (tiempoMensajeVisible > 0) {
            tiempoMensajeVisible -= deltaTime;
        } else {
            mensajeUI = ""; // Borramos el mensaje cuando el tiempo se acaba
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (frameActual != null) {
            batch.draw(frameActual, estado.x, estado.y, getTileSize(), getTileSize());
        } else {
            Gdx.app.log("Sonic", "Advertencia: 'frameActual' es nulo en el método draw(). No se puede dibujar a Sonic.");
        }
        miDron.draw(batch);

        if (tiempoMensajeVisible > 0 && !mensajeUI.isEmpty()) {
            glyphLayout.setText(font, mensajeUI);

            // 1. Calculamos la posición X para centrar el texto sobre Tails
            //    (posición de Tails + centro del sprite) - (mitad del ancho del texto)
            float textX = estado.x + (getTileSize() / 2) - (glyphLayout.width / 2);

            // 2. Calculamos la posición Y para que flote justo encima de Tails
            //    (posición de Tails + altura del sprite) + (un pequeño espacio)
            float textY = estado.y + getTileSize() + 20; // 20px de espacio por encima

            // 3. Dibujamos el texto en las coordenadas del mundo del juego
            font.draw(batch, glyphLayout, textX, textY);
        }
    }

    @Override
    public void dispose() {
        if (getSpriteSheet() != null) {
            getSpriteSheet().dispose();
        }
        if (miDron != null) {
            miDron.dispose();
        }
        if (font != null) {
            font.dispose();
        }
    }

    public void mostrarMensaje(String texto) {
        this.mensajeUI = texto;
        this.tiempoMensajeVisible = DURACION_MENSAJE;
    }
}
