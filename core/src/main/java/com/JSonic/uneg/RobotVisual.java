package com.JSonic.uneg;

import com.JSonic.uneg.EnemigoState.EstadoEnemigo;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.EnumMap;

public class RobotVisual {
    public EnemigoState estado;
    protected Texture spriteSheet;
    protected TextureRegion frameActual;
    protected float tiempoXFrame;
    protected float speed = 1; // VALOR A AJUSTAR: Velocidad del robot (ej: 50.0f pixeles/segundo)
    protected float detectionRange = 300f; // VALOR A AJUSTAR: Rango de detección de Sonic (ej: 300 pixeles)
    protected float attackRange = 10f; // VALOR A AJUSTAR: Rango para iniciar la animación de ataque (ej: 50 pixeles)

    protected EnumMap<EnemigoState.EstadoEnemigo, Animation<TextureRegion>> animations;
    protected TextureRegion[] frameIdleRight;
    protected TextureRegion[] frameIdleLeft;
    protected TextureRegion[] frameRunRight;
    protected TextureRegion[] frameRunLeft;
    protected TextureRegion[] frameHitRight;
    protected TextureRegion[] frameHitLeft;

    private LevelManager levelManager;

    public RobotVisual(EnemigoState estadoInicial, LevelManager levelManager) {
        this.estado = estadoInicial;
        this.levelManager = levelManager;
        animations = new EnumMap<>(EnemigoState.EstadoEnemigo.class);
        tiempoXFrame = 0.0f;
        CargarSprites();
        setEstadoActual(estado.estadoAnimacion);
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

    public void update(float deltaTime) {
        tiempoXFrame += deltaTime;

        // Si LevelManager o el jugador no están disponibles, el enemigo se queda inactivo.
        if (levelManager == null || levelManager.getPlayer() == null) {
            if (estado.mirandoDerecha) {
                setEstadoActual(EstadoEnemigo.IDLE_RIGHT);
            } else {
                setEstadoActual(EstadoEnemigo.IDLE_LEFT);
            }
            // Aún debemos actualizar el frame de animación aunque el enemigo esté inactivo.
            Animation<TextureRegion> currentAnimation = animations.get(estado.estadoAnimacion);
            if (currentAnimation != null) {
                frameActual = currentAnimation.getKeyFrame(tiempoXFrame);
            } else {
                Gdx.app.log("RobotVisual", "Advertencia: 'currentAnimation' es nula para estado: " + estado.estadoAnimacion);
                frameActual = null;
            }
            return; // Salir del método update ya que no hay un objetivo para perseguir.
        }

        Player sonic = levelManager.getPlayer();
        float sonicX = sonic.estado.x;
        float sonicY = sonic.estado.y;

        float dx = sonicX - estado.x;
        float dy = sonicY - estado.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // --- Lógica de IA para el comportamiento del enemigo ---
        // Prioridad de acciones: Atacar > Perseguir > Inactivo

        // Paso 1: Si Sonic está DENTRO del rango de ataque
        if (distance <= attackRange) {
            // El enemigo se detiene y activa la animación de golpe/ataque.
            // Solo cambia al estado de golpe si no está ya en él.
            if (estado.estadoAnimacion != EstadoEnemigo.HIT_RIGHT && estado.estadoAnimacion != EstadoEnemigo.HIT_LEFT) {
                if (estado.mirandoDerecha) {
                    setEstadoActual(EstadoEnemigo.HIT_RIGHT);
                } else {
                    setEstadoActual(EstadoEnemigo.HIT_LEFT);
                }
                tiempoXFrame = 0; // Reiniciar el tiempo de animación para que empiece desde el inicio
            }
            // Importante: No actualizamos estado.x ni estado.y aquí, el enemigo se "para" para atacar.
        }
        // Paso 2: Si Sonic está fuera del rango de ataque pero DENTRO del rango de detección
        else if (distance > attackRange && distance <= detectionRange) {
            // El enemigo persigue a Sonic.
            if (dx > 0) { // Si Sonic está a la derecha del enemigo
                estado.x += speed; // Mueve al enemigo hacia la derecha
                estado.mirandoDerecha = true; // El enemigo mira a la derecha
                if (estado.estadoAnimacion != EstadoEnemigo.RUN_RIGHT) {
                    setEstadoActual(EstadoEnemigo.RUN_RIGHT); // Cambia a animación de correr a la derecha
                }
            } else if (dx < 0) { // Si Sonic está a la izquierda del enemigo
                estado.x -= speed; // Mueve al enemigo hacia la izquierda
                estado.mirandoDerecha = false; // El enemigo mira a la izquierda
                if (estado.estadoAnimacion != EstadoEnemigo.RUN_LEFT) {
                    setEstadoActual(EstadoEnemigo.RUN_LEFT); // Cambia a animación de correr a la izquierda
                }
            }else if(dx == 0){
                estado.mirandoDerecha = false; // El enemigo mira a la izquierda
                if (estado.estadoAnimacion != EstadoEnemigo.RUN_LEFT) {
                    setEstadoActual(EstadoEnemigo.RUN_LEFT); // Cambia a animación de correr a la izquierda
                }
            } else { // Si Sonic está en la misma X horizontalmente (o muy cerca)
                // No hay movimiento horizontal, se mantiene la dirección y estado IDLE o RUN
                if (estado.mirandoDerecha) {
                    setEstadoActual(EstadoEnemigo.IDLE_RIGHT);
                } else {
                    setEstadoActual(EstadoEnemigo.IDLE_LEFT);
                }
            }
            // Ajustar la posición vertical (Y) para seguir a Sonic
            if (dy > 0) { // Si Sonic está arriba del enemigo
                estado.y += speed; // Mueve al enemigo hacia arriba
            } else if (dy < 0) { // Si Sonic está abajo del enemigo
                estado.y -= speed; // Mueve al enemigo hacia abajo
            }
        }
        // Paso 3: Si Sonic está FUERA del rango de detección
        else { // Esto cubre `distance > detectionRange`
            // El enemigo deja de perseguir y vuelve a un estado inactivo (idle).
            if (estado.mirandoDerecha) {
                setEstadoActual(EstadoEnemigo.IDLE_RIGHT);
            } else {
                setEstadoActual(EstadoEnemigo.IDLE_LEFT);
            }
            // Importante: No movemos estado.x ni estado.y aquí. El enemigo se detiene.
        }

        // Lógica de transición de estado después de que una animación de golpe termina
        // Esto asegura que el enemigo no se quede "golpeando" indefinidamente.
        if (estado.estadoAnimacion == EstadoEnemigo.HIT_RIGHT || estado.estadoAnimacion == EstadoEnemigo.HIT_LEFT) {
            Animation<TextureRegion> currentAnim = animations.get(estado.estadoAnimacion);
            if (currentAnim != null && currentAnim.isAnimationFinished(tiempoXFrame)) {
                // Una vez que la animación de golpe ha terminado, el enemigo reevalúa su situación.
                if (distance <= detectionRange) {
                    // Si Sonic todavía está dentro del rango de detección (incluso si está fuera del de ataque)
                    // vuelve a perseguirlo.
                    if (estado.mirandoDerecha) {
                        setEstadoActual(EstadoEnemigo.RUN_RIGHT);
                    } else {
                        setEstadoActual(EstadoEnemigo.RUN_LEFT);
                    }
                } else {
                    // Si Sonic ya está fuera del rango de detección, el enemigo se queda en IDLE.
                    if (estado.mirandoDerecha) {
                        setEstadoActual(EstadoEnemigo.IDLE_RIGHT);
                    } else {
                        setEstadoActual(EstadoEnemigo.IDLE_LEFT);
                    }
                }
                tiempoXFrame = 0; // Reiniciar el tiempo para la nueva animación
            }
        }

        // Finalmente, obtiene el frame actual de la animación para dibujarlo.
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
