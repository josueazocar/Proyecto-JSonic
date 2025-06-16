package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys; // Importa Keys
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils; // Importar para MathUtils.clamp


public class Sonic extends Player {

    protected TextureRegion[] frameSpinRight; // Arreglo para almacenar los sprites de girar a la derecha
    protected TextureRegion[] frameSpinLeft;  // Arreglo para almacenar los sprites de girar a la izquierda

    public Sonic(PlayerState estadoInicial) {
        super(estadoInicial);
        CargarSprites();
        animacion = animations.get(getEstadoActual());
        if (animacion != null) {
            // El PlayMode se establecerá específicamente en CargarSprites
        }
    }

    public Sonic(PlayerState estadoInicial, LevelManager levelManager) {
        super(estadoInicial);
        CargarSprites();
        animacion = animations.get(getEstadoActual());
        if (animacion != null) {
            // El PlayMode se establecerá específicamente en CargarSprites
        }
        this.levelManager = levelManager;
    }

    protected String getSpriteSheetPath() {
        return "Entidades/Player/Sonic/sonic.png";
    }

    @Override
    protected void CargarSprites() {
        Texture coleccionDeSprites = new Texture(Gdx.files.internal(getSpriteSheetPath()));
        setSpriteSheet(coleccionDeSprites);

        TextureRegion[][] matrizDeSprites = TextureRegion.split(getSpriteSheet(), getSpriteSheet().getWidth() / 8, getSpriteSheet().getHeight() / 30);

        // Tamaños de SandboxThiago
        frameIdleRight = new TextureRegion[8];
        frameIdleLeft = new TextureRegion[8];
        frameUp = new TextureRegion[8];
        frameDown = new TextureRegion[8];
        frameLeft = new TextureRegion[8];
        frameRight = new TextureRegion[8];
        frameHitRight = new TextureRegion[8];
        frameHitLeft = new TextureRegion[8];
        frameKickRight = new TextureRegion[8];
        frameKickLeft = new TextureRegion[8];
        frameSpinRight = new TextureRegion[24];
        frameSpinLeft = new TextureRegion[24];

        // Llenado de arreglos como en SandboxThiago
        // --- IDLE LEFT (matriz[0][0-7]) con efecto "respiración" ---
        for (int i = 0; i < 4; i++) { // Primeros 4 frames
            frameIdleLeft[i] = matrizDeSprites[0][i];
        }
        for (int i = 0; i < 4; i++) { // Siguientes 4 frames (inversos)
            frameIdleLeft[i + 4] = matrizDeSprites[0][3 - i];
        }

        // --- IDLE RIGHT (volteando IDLE LEFT) ---
        for (int i = 0; i < 8; i++) {
            frameIdleRight[i] = new TextureRegion(frameIdleLeft[i]);
            frameIdleRight[i].flip(true, false);
        }

        // --- UP (matriz[5][0-7]) ---
        for (int i = 0; i < 8; i++) {
            frameUp[i] = matrizDeSprites[5][i];
        }

        // --- DOWN (matriz[1][0-7]) ---
        for (int i = 0; i < 8; i++) {
            frameDown[i] = matrizDeSprites[1][i];
        }

        // --- LEFT (matriz[1][0-7]) ---
        for (int i = 0; i < 8; i++) {
            frameLeft[i] = matrizDeSprites[1][i];
        }

        // --- RIGHT (volteando LEFT) ---
        for (int i = 0; i < 8; i++) {
            frameRight[i] = new TextureRegion(frameLeft[i]);
            frameRight[i].flip(true, false);
        }

        // --- HIT LEFT (matriz[7][0-7]) ---
        for (int i = 0; i < 8; i++) {
            frameHitLeft[i] = matrizDeSprites[7][i];
        }

        // --- HIT RIGHT (volteando HIT LEFT) ---
        for (int i = 0; i < 8; i++) {
            frameHitRight[i] = new TextureRegion(frameHitLeft[i]);
            frameHitRight[i].flip(true, false);
        }

        // --- KICK LEFT (matriz[8][0-7]) ---
        // Nota: SandboxThiago tenía un comentario "¡RUTA DE EJEMPLO!" aquí.
        // Se asume que la fila 8 es la correcta según la lógica de SandboxThiago.
        for (int i = 0; i < 8; i++) {
            frameKickLeft[i] = matrizDeSprites[8][i];
        }

        // --- KICK RIGHT (volteando KICK LEFT) ---
        for (int i = 0; i < 8; i++) {
            frameKickRight[i] = new TextureRegion(frameKickLeft[i]);
            frameKickRight[i].flip(true, false);
        }

        // --- SPIN LEFT (matriz[18][i % 8] para 24 frames) ---
        // Nota: SandboxThiago tenía un comentario "¡RUTA DE EJEMPLO!" aquí.
        // Se asume que la fila 18 es la correcta según la lógica de SandboxThiago.
        for (int i = 0; i < 24; i++) {
            frameSpinLeft[i] = matrizDeSprites[18][i % 8];
        }

        // --- SPIN RIGHT (volteando SPIN LEFT) ---
        for (int i = 0; i < 24; i++) {
            frameSpinRight[i] = new TextureRegion(frameSpinLeft[i]);
            frameSpinRight[i].flip(true, false);
        }

        // Llenado del mapa de animaciones y PlayModes como en SandboxThiago
        animations.put(EstadoPlayer.IDLE_RIGHT, new Animation<TextureRegion>(0.12f, frameIdleRight));
        animations.put(EstadoPlayer.IDLE_LEFT, new Animation<TextureRegion>(0.12f, frameIdleLeft));
        animations.put(EstadoPlayer.UP, new Animation<TextureRegion>(0.1f, frameUp));
        animations.put(EstadoPlayer.DOWN, new Animation<TextureRegion>(0.26f, frameDown));
        animations.put(EstadoPlayer.LEFT, new Animation<TextureRegion>(0.2f, frameLeft));
        animations.put(EstadoPlayer.RIGHT, new Animation<TextureRegion>(0.2f, frameRight));
        animations.put(EstadoPlayer.HIT_RIGHT, new Animation<TextureRegion>(0.08f, frameHitRight));
        animations.put(EstadoPlayer.HIT_LEFT, new Animation<TextureRegion>(0.08f, frameHitLeft));
        animations.put(EstadoPlayer.KICK_RIGHT, new Animation<TextureRegion>(0.1f, frameKickRight));
        animations.put(EstadoPlayer.KICK_LEFT, new Animation<TextureRegion>(0.1f, frameKickLeft));
        animations.put(EstadoPlayer.SPIN_RIGHT, new Animation<TextureRegion>(0.07f, frameSpinRight));
        animations.put(EstadoPlayer.SPIN_LEFT, new Animation<TextureRegion>(0.07f, frameSpinLeft));

        animations.get(EstadoPlayer.IDLE_RIGHT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoPlayer.IDLE_LEFT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoPlayer.UP).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoPlayer.DOWN).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoPlayer.LEFT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoPlayer.RIGHT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoPlayer.HIT_RIGHT).setPlayMode(Animation.PlayMode.NORMAL);
        animations.get(EstadoPlayer.HIT_LEFT).setPlayMode(Animation.PlayMode.NORMAL);
        animations.get(EstadoPlayer.KICK_RIGHT).setPlayMode(Animation.PlayMode.NORMAL);
        animations.get(EstadoPlayer.KICK_LEFT).setPlayMode(Animation.PlayMode.NORMAL);
        animations.get(EstadoPlayer.SPIN_RIGHT).setPlayMode(Animation.PlayMode.NORMAL); // Como en SandboxThiago
        animations.get(EstadoPlayer.SPIN_LEFT).setPlayMode(Animation.PlayMode.NORMAL); // Como en SandboxThiago

        // Establecer la animación inicial basada en el estado actual.
        // Esto asegura que si el estado inicial es, por ejemplo, IDLE_LEFT, se use su PlayMode.LOOP.
        Animation<TextureRegion> initialAnimation = animations.get(getEstadoActual());
        if (initialAnimation != null) {
            setFrameActual(initialAnimation.getKeyFrame(0));
            // El PlayMode ya está configurado arriba para cada animación específica.
            // this.animacion se asignará en el constructor o en update().
        }
    }

    @Override
    public void update(float deltaTime) {
        // --- FASE 1: LÓGICA DE DECISIÓN DE ESTADO ---
        // (Se mantiene la lógica de la sugerencia anterior, que intenta manejar el movimiento correctamente)

        // Comprobar si una acción de un solo disparo (NORMAL) está en curso.
        boolean accionNormalEnCurso = (animacion != null && animacion.getPlayMode() == Animation.PlayMode.NORMAL && !animacion.isAnimationFinished(tiempoXFrame));

        if (!accionNormalEnCurso) {
            boolean quiereGolpear = Gdx.input.isKeyJustPressed(Keys.J);
            boolean quierePatear = Gdx.input.isKeyJustPressed(Keys.K);
            boolean quiereGirar = Gdx.input.isKeyPressed(Keys.L); // SPIN ahora es NORMAL, así que isKeyJustPressed podría ser mejor si no quieres que se reinicie si se mantiene L.

            // Si SPIN es PlayMode.NORMAL, es mejor usar isKeyJustPressed para iniciarlo.
            // Si se mantiene L, se reiniciará la animación de SPIN cada frame si usamos isKeyPressed.
            // Vamos a cambiarlo a isKeyJustPressed para SPIN para que coincida con el comportamiento de HIT/KICK.
            if (animations.get(EstadoPlayer.SPIN_LEFT).getPlayMode() == Animation.PlayMode.NORMAL) {
                quiereGirar = Gdx.input.isKeyJustPressed(Keys.L);
            }


            boolean seEstaMoviendo = (estado.x != lastPosX || estado.y != lastPosY);
            float dx = estado.x - lastPosX;
            float dy = estado.y - lastPosY;

            if (quiereGolpear) {
                setEstadoActual((lastDirection == EstadoPlayer.IDLE_LEFT) ? EstadoPlayer.HIT_LEFT : EstadoPlayer.HIT_RIGHT);
            } else if (quierePatear) {
                setEstadoActual((lastDirection == EstadoPlayer.IDLE_LEFT) ? EstadoPlayer.KICK_LEFT : EstadoPlayer.KICK_RIGHT);
            } else if (quiereGirar) {
                setEstadoActual((lastDirection == EstadoPlayer.IDLE_LEFT) ? EstadoPlayer.SPIN_LEFT : EstadoPlayer.SPIN_RIGHT);
            } else if (seEstaMoviendo) {
                if (Math.abs(dy) > Math.abs(dx)) {
                    if (dy > 0) {
                        setEstadoActual(EstadoPlayer.UP);
                    } else {
                        setEstadoActual(EstadoPlayer.DOWN);
                    }
                } else {
                    if (dx < 0) {
                        setEstadoActual(EstadoPlayer.LEFT);
                    } else if (dx > 0) {
                        setEstadoActual(EstadoPlayer.RIGHT);
                    } else {
                        setEstadoActual((lastDirection == EstadoPlayer.IDLE_LEFT) ? EstadoPlayer.IDLE_LEFT : EstadoPlayer.IDLE_RIGHT);
                    }
                }
            } else {
                setEstadoActual((lastDirection == EstadoPlayer.IDLE_LEFT) ? EstadoPlayer.IDLE_LEFT : EstadoPlayer.IDLE_RIGHT);
            }
        } // Fin de if (!accionNormalEnCurso)

        // --- FASE 2: ACTUALIZACIÓN DE LA ANIMACIÓN ---
        Animation<TextureRegion> targetAnimation = animations.get(getEstadoActual());

        if (this.animacion != targetAnimation) {
            this.tiempoXFrame = 0; // Reiniciar tiempo para la nueva animación.
            // Si la animación anterior era NORMAL y no había terminado, y la nueva es diferente,
            // esto la cortará. Esto es generalmente el comportamiento deseado si una nueva acción interrumpe.
        }
        this.animacion = targetAnimation;

        if (animacion != null) {
            tiempoXFrame += deltaTime;
            frameActual = animacion.getKeyFrame(tiempoXFrame);

            // Si una animación NORMAL ha terminado, volver a IDLE (lógica de SandboxThiago)
            if (animacion.getPlayMode() == Animation.PlayMode.NORMAL && animacion.isAnimationFinished(tiempoXFrame)) {
                if (lastDirection == EstadoPlayer.IDLE_LEFT) { // Usa la lastDirection de Player.java
                    setEstadoActual(EstadoPlayer.IDLE_LEFT);
                } else {
                    setEstadoActual(EstadoPlayer.IDLE_RIGHT);
                }
                // Al cambiar a IDLE, el siguiente frame en update() reiniciará tiempoXFrame porque targetAnimation será diferente.
                // Y se asignará la animación IDLE correcta.
            }
        } else {
            Gdx.app.log("Sonic_update", "Advertencia: No hay animación para el estado: " + getEstadoActual());
            frameActual = null;
        }

        // Actualizamos lastPosX y lastPosY aquí, después de toda la lógica de decisión Y actualización de animación del frame actual.
        lastPosX = estado.x;
        lastPosY = estado.y;
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (frameActual != null) {
            batch.draw(frameActual, estado.x, estado.y, getTileSize(), getTileSize());
        } else {
            Gdx.app.log("Sonic", "Advertencia: \'frameActual\' es nulo en el método draw(). No se puede dibujar a Sonic.");
        }
    }
}
