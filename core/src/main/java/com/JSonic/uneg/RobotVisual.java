package com.JSonic.uneg;

import com.JSonic.uneg.EnemigoState.EstadoEnemigo;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import network.Network;
import network.interfaces.IGameClient;

import java.awt.*;
import java.util.EnumMap;

public class RobotVisual extends Enemy{

    private LevelManager levelManager;
    private IGameClient gameClient;

    public RobotVisual(EnemigoState estadoInicial, LevelManager levelManager) {
        this.estado = estadoInicial;
        this.levelManager = levelManager;
        animations = new EnumMap<>(EnemigoState.EstadoEnemigo.class);
        tiempoXFrame = 0.0f;
        CargarSprites();
        setEstadoActual(estado.estadoAnimacion);
    }
    public RobotVisual(EnemigoState estadoInicial, LevelManager levelManager, IGameClient gameClient) {
        this.estado = estadoInicial;
        this.levelManager = levelManager;
        this.gameClient = gameClient; // <--- AÑADE ESTA LÍNEA
        animations = new EnumMap<>(EnemigoState.EstadoEnemigo.class);
        tiempoXFrame = 0.0f;
        CargarSprites();
        setEstadoActual(estado.estadoAnimacion);
        setVida(100);
    }
    public RobotVisual(EnemigoState estadoInicial) {
        this.estado = estadoInicial;
        animations = new EnumMap<>(EnemigoState.EstadoEnemigo.class);
        tiempoXFrame = 0.0f;
        CargarSprites();
        setEstadoActual(estado.estadoAnimacion);
    }

    public void setLevelManager(LevelManager levelManager) {
        this.levelManager = levelManager;
    }

    protected String getSpriteSheetPath() {
        return "Entidades/Enemy/Robots/robot.png"; // Asegúrate de que esta ruta es correcta para los sprites del robot.
    }

    protected void CargarSprites() {
        spriteSheet = new Texture(Gdx.files.internal(getSpriteSheetPath()));
        TextureRegion[][] matrizDeSprites = TextureRegion.split(spriteSheet, spriteSheet.getWidth() / 9, spriteSheet.getHeight() / 26);

        frameIdleRight = new TextureRegion[8];
        frameIdleLeft = new TextureRegion[8];
        frameRunRight = new TextureRegion[6];
        frameRunLeft = new TextureRegion[6];
        frameHitRight = new TextureRegion[8];
        frameHitLeft = new TextureRegion[8];

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

        for (int i = 0; i < 6; i++) {
            frameRunLeft[i] = matrizDeSprites[1][i];
        }
        for (int i = 0; i < 6; i++) {
            frameRunRight[i] = new TextureRegion(frameRunLeft[i]);
            frameRunRight[i].flip(true, false);
        }

        for (int i = 0; i < 8; i++) {
            frameHitLeft[i] = matrizDeSprites[6][i];
        }
        for (int i = 0; i < 8; i++) {
            frameHitRight[i] = new TextureRegion(frameHitLeft[i]);
            frameHitRight[i].flip(true, false);
        }

        animations.put(EstadoEnemigo.IDLE_RIGHT, new Animation<TextureRegion>(0.12f, frameIdleRight));
        animations.put(EstadoEnemigo.IDLE_LEFT, new Animation<TextureRegion>(0.12f, frameIdleLeft));
        animations.put(EstadoEnemigo.RUN_RIGHT, new Animation<TextureRegion>(0.1f, frameRunRight));
        animations.put(EstadoEnemigo.RUN_LEFT, new Animation<TextureRegion>(0.1f, frameRunLeft));
        animations.put(EstadoEnemigo.HIT_RIGHT, new Animation<TextureRegion>(0.08f, frameHitRight));
        animations.put(EstadoEnemigo.HIT_LEFT, new Animation<TextureRegion>(0.08f, frameHitLeft));

        animations.get(EstadoEnemigo.IDLE_RIGHT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoEnemigo.IDLE_LEFT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoEnemigo.RUN_RIGHT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoEnemigo.RUN_LEFT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoEnemigo.HIT_RIGHT).setPlayMode(Animation.PlayMode.NORMAL);
        animations.get(EstadoEnemigo.HIT_LEFT).setPlayMode(Animation.PlayMode.NORMAL);

        Animation<TextureRegion> initialAnimation = animations.get(estado.estadoAnimacion);
        if (initialAnimation != null) {
            frameActual = initialAnimation.getKeyFrame(0);
        }
    }

    // Aquí puedes poner getBounds()
    public com.badlogic.gdx.math.Rectangle getBounds() {
        return new com.badlogic.gdx.math.Rectangle(estado.x, estado.y, 48, 48);
    }

    public void update(float deltaTime) {
        tiempoXFrame += deltaTime;

        // 1. Revisa si una animación de ataque ha terminado.
        boolean estaAtacando = estado.estadoAnimacion == EstadoEnemigo.HIT_LEFT || estado.estadoAnimacion == EstadoEnemigo.HIT_RIGHT;
        if (estaAtacando) {
            Animation<TextureRegion> currentAnim = animations.get(estado.estadoAnimacion);
            if (currentAnim != null && currentAnim.isAnimationFinished(tiempoXFrame)) {

                // 2. Si terminó, envía el paquete de notificación al servidor.
                if (gameClient != null) {
                    //System.out.println("[CLIENTE] Animación GOLPE terminada para robot " + estado.id + ". Notificando.");
                    Network.PaqueteAnimacionEnemigoTerminada paquete = new Network.PaqueteAnimacionEnemigoTerminada();
                    paquete.idEnemigo = estado.id;
                    gameClient.send(paquete);
                }

                // 3. Reinicia el tiempo para no enviar el paquete repetidamente.
                tiempoXFrame = 0;
            }
        }

        // 4. Finalmente, actualiza el frame visual que se debe dibujar.
        Animation<TextureRegion> currentAnimation = animations.get(estado.estadoAnimacion);
        if (currentAnimation != null) {
            frameActual = currentAnimation.getKeyFrame(tiempoXFrame);
        } else {
            Gdx.app.log("RobotVisual", "Advertencia: 'currentAnimation' es nula para estado: " + estado.estadoAnimacion);
            frameActual = null;
        }
    }

    public void draw(SpriteBatch batch) {
        if (frameActual != null) {
            batch.draw(frameActual, estado.x, estado.y, 48, 48); // Asumiendo un tamaño de tile de 48x48
        } else {
            Gdx.app.log("RobotVisual", "Advertencia: 'frameActual' es nulo en draw(). No se puede dibujar el robot.");
        }
    }

    public void setEstadoActual(EstadoEnemigo nuevoEstado) {
        // Solo cambia el estado si es diferente para evitar reiniciar animaciones innecesariamente
        if (estado.estadoAnimacion != nuevoEstado) {
            tiempoXFrame = 0; // Reinicia el tiempo si el estado cambia para que la nueva animación empiece de 0
            estado.estadoAnimacion = nuevoEstado;
        }
    }

    public void dispose() {
        if (spriteSheet != null) {
            spriteSheet.dispose();
        }
    }
}
