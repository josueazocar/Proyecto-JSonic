package com.JSonic.uneg.EntidadesVisuales;

import com.JSonic.uneg.LevelManager;
import com.JSonic.uneg.Pantallas.PantallaDeJuego;
import com.JSonic.uneg.State.DronState;
import com.JSonic.uneg.State.PlayerState;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Rectangle;
import network.Network;
import network.interfaces.IGameClient;


/**
 * Clase para el personaje Tails, extiende Player y gestiona su dron y animaciones.
 */
public class Tails extends Player {

    protected TextureRegion[] frameSpinRight;
    protected TextureRegion[] frameSpinLeft;
    private Dron_Tails miDron;
    public boolean isOnlineMode = false;
    private transient BitmapFont font;
    private transient GlyphLayout glyphLayout;

    /**
     * Constructor de Tails con estado inicial.
     *
     * @param estadoInicial Estado inicial del jugador.
     */
    public Tails(PlayerState estadoInicial) {
        super(estadoInicial);
        CargarSprites();
        inicializarHitbox();
        EstadoPlayer estadoInicialAnimacion = (getEstadoActual() != null && animations.containsKey(getEstadoActual())) ? getEstadoActual() : EstadoPlayer.IDLE_RIGHT;
        animacion = animations.get(estadoInicialAnimacion);
        if (animacion == null) {
            Gdx.app.error("Sonic", "ERROR: Animación inicial nula para el estado: " + estadoInicialAnimacion + ". Verifique CargarSprites.");
        }
        miDron = new Dron_Tails(estadoInicial.id, null);

        this.font = new BitmapFont();
        this.font.getData().setScale(1);
        this.glyphLayout = new GlyphLayout();
        this.mensajeUI = "";
    }

    /**
     * Constructor de Tails con estado inicial, gestor de nivel y cliente de red.
     *
     * @param estadoInicial Estado inicial del jugador.
     * @param levelManager  gestor de nivel para colisiones y entorno.
     * @param gameClient    cliente de red para sincronización.
     */
    public Tails(PlayerState estadoInicial, LevelManager levelManager, IGameClient gameClient) {
        super(estadoInicial, levelManager);
        this.gameClient = gameClient;
        CargarSprites();
        inicializarHitbox();
        EstadoPlayer estadoInicialAnimacion = (getEstadoActual() != null && animations.containsKey(getEstadoActual())) ? getEstadoActual() : EstadoPlayer.IDLE_RIGHT;
        animacion = animations.get(estadoInicialAnimacion);
        if (animacion == null) {
            Gdx.app.error("Sonic", "ERROR: Animación inicial nula para el estado: " + estadoInicialAnimacion + ". Verifique CargarSprites.");
        }
        miDron = new Dron_Tails(estadoInicial.id, levelManager);

        this.font = new BitmapFont();
        this.font.getData().setScale(1);
        this.glyphLayout = new GlyphLayout();
        this.mensajeUI = "";
    }

    /**
     * Constructor de Tails con estado inicial y gestor de nivel.
     *
     * @param estadoInicial Estado inicial del jugador.
     * @param levelManager  gestor de nivel para colisiones y entorno.
     */
    public Tails(PlayerState estadoInicial, LevelManager levelManager) {
        super(estadoInicial, levelManager);
        this.gameClient = gameClient;
        CargarSprites();
        inicializarHitbox();
        EstadoPlayer estadoInicialAnimacion = (getEstadoActual() != null && animations.containsKey(getEstadoActual())) ? getEstadoActual() : EstadoPlayer.IDLE_RIGHT;
        animacion = animations.get(estadoInicialAnimacion);
        if (animacion == null) {
            Gdx.app.error("Sonic", "ERROR: Animación inicial nula para el estado: " + estadoInicialAnimacion + ". Verifique CargarSprites.");
        }
        miDron = new Dron_Tails(estadoInicial.id, levelManager);

        this.font = new BitmapFont();
        this.font.getData().setScale(1);
        this.glyphLayout = new GlyphLayout();
        this.mensajeUI = "";
    }

    /**
     * Obtiene el dron asociado a Tails.
     *
     * @return instancia de Dron_Tails.
     */
    public Dron_Tails getDron() {
        return miDron;
    }

    /**
     * Define el modo online para Tails y su dron.
     *
     * @param online true para modo online, false para local.
     */
    public void setOnlineMode(boolean online) {
        this.isOnlineMode = online;
        if (this.miDron != null) {
            this.miDron.isOnlineMode = online;
        }
    }

    /**
     * Gestiona el dron en base a un estado recibido desde red.
     *
     * @param nuevoEstado estado del dron.
     * @param startX      coordenada X donde aparece el dron.
     * @param startY      coordenada Y donde aparece el dron.
     */
    public void gestionarDronDesdeRed(DronState.EstadoDron nuevoEstado, float startX, float startY) {
        if (miDron == null) return; // Seguridad por si el dron no existe

        switch (nuevoEstado) {
            case APARECIENDO:

                miDron.estado.estadoActual = DronState.EstadoDron.APARECIENDO;
                miDron.tiempoDeEstado = 0f;

                miDron.posicion.set(startX, startY);
                miDron.estado.x = startX;
                miDron.estado.y = startY;

                if (this.gameClient != null) {
                    miDron.setObjetivo(this);
                }
                break;

            case INACTIVO:
                // El servidor nos ordena que el dron se ha ido.
                miDron.cambiarEstado(DronState.EstadoDron.INACTIVO);
                break;


        }
    }

    /**
     * Maneja la entrada de teclas, incluyendo invocación del dron y acciones de jugador.
     */
    @Override
    public void KeyHandler() {
        // Guarda la posición actual antes de que el KeyHandler del padre la modifique
        float currentX = estado.x;
        float currentY = estado.y;

        // Llama primero al KeyHandler del padre para manejar el movimiento básico (WASD).
        super.KeyHandler();

        // Reinicia actionStateSet para el frame actual
        actionStateSet = false;

        // --- INVOCACIÓN DEL DRON CON DEPURACIÓN ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (soundManager != null) soundManager.play("habilidad_Tails_punch");
            // Mensaje de diagnóstico para saber qué modo está detectando

            // Si la bandera isOnlineMode es verdadera, estamos en una partida MULTIJUGADOR real.
            if (isOnlineMode) {
                // Doble verificación para seguridad, aunque no debería ser nulo en este modo.
                if (gameClient != null) {
                    gameClient.send(new Network.PaqueteInvocarDron());
                } else {
                    System.err.println("[CLIENTE ERROR] Se esperaba un gameClient en modo online, pero es nulo.");
                }
            } else {
                // Verificamos que el dron exista para evitar errores.
                if (miDron != null && PantallaDeJuego.getBasuraTotal() >= 20) {
                    System.out.println("[CLIENTE] MODO LOCAL: Invocando el dron localmente.");
                    gameClient.send(new Network.PaqueteHabilidadDronUsada());
                    miDron.invocar(this);
                } else {
                    System.err.println("Se necesitan 20 basuras para invocar el dron");
                    mostrarMensaje("Necesitas recoger 20 basuras");
                }
            }
        }

        // --- Lógica para MANEJAR TECLAS DE ACCIÓN ---
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
        } else if (Gdx.input.isKeyPressed(Input.Keys.L)) {

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


    /**
     * Proporciona la ruta al sprite sheet de Tails.
     *
     * @return ruta del recurso de textura.
     */
    protected String getSpriteSheetPath() {
        return "Entidades/Player/Tails/tails.png";
    }

    /**
     * Comprueba colisión futura, ignorándola en modo vuelo de Tails.
     *
     * @param newX posición X futura del jugador.
     * @param newY posición Y futura del jugador.
     * @return true si colisiona con mapa u objetos, false si no o en vuelo.
     */
    @Override
    protected boolean checkCollision(float newX, float newY) {
        EstadoPlayer estadoActual = getEstadoActual();

        // Si Tails está en el estado de "vuelo" (activado con la tecla 'L'),
        // ignoramos las colisiones con el mapa.
        if (estadoActual == EstadoPlayer.SPECIAL_LEFT || estadoActual == EstadoPlayer.SPECIAL_RIGHT) {
            return false; // Retorna 'false' para indicar que NO hay colisión.
        }

        // Para cualquier otro estado, usamos la lógica de colisión normal de la clase Player.
        return super.checkCollision(newX, newY);
    }

    /**
     * Carga los sprites y genera los frames para las animaciones de Tails.
     */
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
        }
        for (int i = 0; i < 6; i++) {
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

    /**
     * Inicializa la hitbox de Tails basada en el tamaño del tile.
     * Ajusta los offsets y dimensiones para una colisión precisa.
     */
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

            float textX = estado.x + (getTileSize() / 2) - (glyphLayout.width / 2);

            float textY = estado.y + getTileSize() + 20;

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

    /**
     * Muestra un mensaje en la UI de Tails.
     * Este mensaje se mostrará durante un tiempo determinado.
     *
     * @param texto El texto del mensaje a mostrar.
     */
    @Override
    public void mostrarMensaje(String texto) {
        this.mensajeUI = texto;
        this.tiempoMensajeVisible = DURACION_MENSAJE;
    }
}
