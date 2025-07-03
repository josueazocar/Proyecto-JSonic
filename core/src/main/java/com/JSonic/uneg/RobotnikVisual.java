package com.JSonic.uneg;

import com.JSonic.uneg.EnemigoState.EstadoEnemigo;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import network.Network;
import network.interfaces.IGameClient;

import java.util.EnumMap; // Se eliminó 'java.awt.*' ya que no se usaba.

public class RobotnikVisual {
    public EnemigoState estado;
    protected Texture spriteSheet;
    protected TextureRegion frameActual;
    protected float tiempoXFrame;

    protected EnumMap<EnemigoState.EstadoEnemigo, Animation<TextureRegion>> animations;
    protected TextureRegion[] frameIdleRight;
    protected TextureRegion[] frameIdleLeft;
    protected TextureRegion[] frameRunRight;
    protected TextureRegion[] frameRunLeft;
    protected TextureRegion[] frameHitRight; // Declarado, pero no usado aún.
    protected TextureRegion[] frameHitLeft;  // Declarado, pero no usado aún.

    private LevelManager levelManager;
    private IGameClient gameClient;

    public RobotnikVisual(EnemigoState estadoInicial, LevelManager levelManager) {
        this.estado = estadoInicial;
        this.levelManager = levelManager;
        animations = new EnumMap<>(EnemigoState.EstadoEnemigo.class);
        tiempoXFrame = 0.0f;
        CargarSprites();
        setEstadoActual(estado.estadoAnimacion);
    }
    public RobotnikVisual(EnemigoState estadoInicial, LevelManager levelManager, IGameClient gameClient) {
        this.estado = estadoInicial;
        this.levelManager = levelManager;
        this.gameClient = gameClient;
        animations = new EnumMap<>(EnemigoState.EstadoEnemigo.class);
        tiempoXFrame = 0.0f;
        CargarSprites();
        setEstadoActual(estado.estadoAnimacion);
    }
    public RobotnikVisual(EnemigoState estadoInicial) {
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
        return "Entidades/Enemy/Robotnik/drR.png"; // Asegúrate de que esta ruta es correcta para los sprites de Robotnik.
    }

    protected void CargarSprites() {
        // Se agregó una verificación para asegurar que el archivo existe antes de intentar cargarlo.
        String spriteSheetPath = getSpriteSheetPath();
        if (!Gdx.files.internal(spriteSheetPath).exists()) {
            Gdx.app.error("RobotnikVisual", "Error: Archivo de hoja de sprites no encontrado: " + spriteSheetPath);
            // Puedes lanzar una RuntimeException aquí si el archivo es crítico para la ejecución.
            throw new RuntimeException("Sprite sheet missing: " + spriteSheetPath);
        }

        spriteSheet = new Texture(Gdx.files.internal(spriteSheetPath));
        // CORRECCIÓN CLAVE AQUÍ: La imagen del sprite tiene 4 columnas y 1 fila.
        TextureRegion[][] matrizDeSprites = TextureRegion.split(spriteSheet, spriteSheet.getWidth() / 4, spriteSheet.getHeight() / 1);

        // Ajustamos el tamaño de los arrays de frames según la imagen proporcionada.
        // Asumiendo que la animación IDLE usa el primer frame.
        frameIdleRight = new TextureRegion[1];
        frameIdleLeft = new TextureRegion[1];
        // Asumiendo que la animación RUN usa los frames 0 y 1.
        frameRunRight = new TextureRegion[2];
        frameRunLeft = new TextureRegion[2];

        // --- CORRECCIÓN PARA IDLE ---
        // Asumiendo que el frame de IDLE LEFT está en matrizDeSprites[0][0]
        frameIdleLeft[0] = matrizDeSprites[0][0]; // Usa el primer frame de la primera fila
        frameIdleRight[0] = new TextureRegion(frameIdleLeft[0]);
        frameIdleRight[0].flip(true, false);


        // --- CORRECCIÓN PARA RUN ---
        // Asumiendo que los frames de RUN LEFT están en matrizDeSprites[0][0] y matrizDeSprites[0][1]
        // Si tu animación de correr usa frames de otra fila, ajusta el primer índice (ej. matrizDeSprites[0][indice_correcto]).
        // Basado en la imagen, los frames de correr podrían ser los frames 0 y 1, o 2 y 3 si son diferentes.
        // Aquí asumimos que los frames de correr son los dos primeros de la única fila.
        frameRunLeft[0] = matrizDeSprites[0][0]; // Primer frame de correr (columna 0)
        frameRunLeft[1] = matrizDeSprites[0][1]; // Segundo frame de correr (columna 1)

        for (int i = 0; i < frameRunLeft.length; i++) { // Itera sobre el tamaño real de frameRunLeft
            frameRunRight[i] = new TextureRegion(frameRunLeft[i]);
            frameRunRight[i].flip(true, false);
        }

        // Si tienes frames para HIT, asegúrate de cargarlos correctamente también.
        // Por ejemplo, si los frames de HIT están en las columnas 2 y 3 de la misma fila:
        // frameHitRight = new TextureRegion[2];
        // frameHitLeft = new TextureRegion[2];
        // frameHitLeft[0] = matrizDeSprites[0][2];
        // frameHitLeft[1] = matrizDeSprites[0][3];
        // frameHitRight[0] = new TextureRegion(frameHitLeft[0]);
        // frameHitRight[0].flip(true, false);
        // frameHitRight[1] = new TextureRegion(frameHitLeft[1]);
        // frameHitRight[1].flip(true, false);


        animations.put(EstadoEnemigo.IDLE_RIGHT, new Animation<TextureRegion>(0.12f, frameIdleRight));
        animations.put(EstadoEnemigo.IDLE_LEFT, new Animation<TextureRegion>(0.12f, frameIdleLeft));
        animations.put(EstadoEnemigo.RUN_RIGHT, new Animation<TextureRegion>(0.1f, frameRunRight));
        animations.put(EstadoEnemigo.RUN_LEFT, new Animation<TextureRegion>(0.1f, frameRunLeft));
        // Si tienes animaciones HIT, descomenta y usa:
        // animations.put(EstadoEnemigo.HIT_RIGHT, new Animation<TextureRegion>(0.08f, frameHitRight));
        // animations.put(EstadoEnemigo.HIT_LEFT, new Animation<TextureRegion>(0.08f, frameHitLeft));


        animations.get(EstadoEnemigo.IDLE_RIGHT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoEnemigo.IDLE_LEFT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoEnemigo.RUN_RIGHT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoEnemigo.RUN_LEFT).setPlayMode(Animation.PlayMode.LOOP);
        // Si tienes animaciones HIT, descomenta y usa:
        // animations.get(EstadoEnemigo.HIT_RIGHT).setPlayMode(Animation.PlayMode.NORMAL);
        // animations.get(EstadoEnemigo.HIT_LEFT).setPlayMode(Animation.PlayMode.NORMAL);


        Animation<TextureRegion> initialAnimation = animations.get(estado.estadoAnimacion);
        if (initialAnimation != null) {
            frameActual = initialAnimation.getKeyFrame(0);
        }
    }

    // Aquí puedes poner getBounds()
    public com.badlogic.gdx.math.Rectangle getBounds() {
        // Asumiendo que el tamaño del hitbox es 48x48. Ajusta si es diferente.
        return new com.badlogic.gdx.math.Rectangle(estado.x, estado.y, 48, 48);
    }

    public void update(float deltaTime) {
        tiempoXFrame += deltaTime;

        // Si tienes lógica de ataque para Robotnik, descomenta y ajusta
        /*
        boolean estaAtacando = estado.estadoAnimacion == EstadoEnemigo.HIT_LEFT || estado.estadoAnimacion == EstadoEnemigo.HIT_RIGHT;
        if (estaAtacando) {
            Animation<TextureRegion> currentAnim = animations.get(estado.estadoAnimacion);
            if (currentAnim != null && currentAnim.isAnimationFinished(tiempoXFrame)) {
                if (gameClient != null) {
                    Network.PaqueteAnimacionEnemigoTerminada paquete = new Network.PaqueteAnimacionEnemigoTerminada();
                    paquete.idEnemigo = estado.id;
                    gameClient.send(paquete);
                }
                tiempoXFrame = 0;
            }
        }
        */

        // 4. Finalmente, actualiza el frame visual que se debe dibujar.
        Animation<TextureRegion> currentAnimation = animations.get(estado.estadoAnimacion);
        if (currentAnimation != null) {
            frameActual = currentAnimation.getKeyFrame(tiempoXFrame, true); // Añadido 'true' para bucle por defecto
        } else {
            Gdx.app.log("RobotnikVisual", "Advertencia: 'currentAnimation' es nula para estado: " + estado.estadoAnimacion);
            frameActual = null;
        }
    }

    public void draw(SpriteBatch batch) {
        if (frameActual != null) {
            // Dibuja el frame actual en la posición del estado.
            // Asegúrate de que el tamaño (48, 48) sea el tamaño deseado para dibujar Robotnik.
            batch.draw(frameActual, estado.x, estado.y, 48, 48);
        } else {
            Gdx.app.log("RobotnikVisual", "Advertencia: 'frameActual' es nulo en draw(). No se puede dibujar el robot.");
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
