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

    // Constructor unificado: recibe el estado inicial (de red) y el LevelManager
    public Sonic(PlayerState estadoInicial) {
        super(estadoInicial); // Llama al constructor de la superclase (Player) con PlayerState
        CargarSprites(); // Carga los sprites específicos de Sonic después de la inicialización base
        // Establecer la animación inicial basada en el estado actual del jugador
        animacion = animations.get(getEstadoActual());
        if (animacion != null) {
            animacion.setPlayMode(Animation.PlayMode.LOOP);
        }
    }

    // Nuevo constructor que también permite pasar el LevelManager (para el jugador local en PantallaDeJuego)
    public Sonic(PlayerState estadoInicial, LevelManager levelManager) {
        this(estadoInicial); // Llama al constructor de arriba
        this.levelManager = levelManager; // Asigna el LevelManager
    }


    // --- NUEVO MÉTODO para obtener la ruta del SpriteSheet (implementa un método abstracto que podrías añadir a Player) ---
    @Override
    protected String getSpriteSheetPath() {
        return "Entidades/Player/Sonic/sonic.png";
    }
    // --- FIN NUEVO MÉTODO ---

    @Override
    protected void CargarSprites() {
        // Carga la Texture principal para Sonic
        Texture coleccionDeSprites = new Texture(Gdx.files.internal(getSpriteSheetPath()));
        setSpriteSheet(coleccionDeSprites); // Establece la Texture en la superclase

        // Dividir el sprite sheet en una matriz de TextureRegion
        // Asegúrate de que las dimensiones de tu sprite sheet coincidan con esta división
        // (ancho total / 8 columnas, alto total / 30 filas)
        TextureRegion[][] matrizDeSprites = TextureRegion.split(getSpriteSheet(), getSpriteSheet().getWidth() / 8, getSpriteSheet().getHeight() / 30);

        // Inicializar los arreglos de frames y llenarlos con las regiones correctas
        // IDLE
        frameIdleRight = new TextureRegion[4]; // 4 frames para IDLE_RIGHT
        for (int i = 0; i < 4; i++) {
            frameIdleRight[i] = matrizDeSprites[0][i]; // Fila 0, Columnas 0-3
        }

        frameIdleLeft = new TextureRegion[4]; // 4 frames para IDLE_LEFT
        for (int i = 0; i < 4; i++) {
            frameIdleLeft[i] = matrizDeSprites[1][i]; // Fila 1, Columnas 0-3
        }

        // UP (moviéndose hacia arriba)
        frameUp = new TextureRegion[6]; // 6 frames para UP
        for (int i = 0; i < 6; i++) {
            frameUp[i] = matrizDeSprites[2][i]; // Fila 2, Columnas 0-5
        }

        // DOWN (moviéndose hacia abajo)
        frameDown = new TextureRegion[6]; // 6 frames para DOWN
        for (int i = 0; i < 6; i++) {
            frameDown[i] = matrizDeSprites[3][i]; // Fila 3, Columnas 0-5
        }

        // LEFT (moviéndose a la izquierda)
        frameLeft = new TextureRegion[6]; // 6 frames para LEFT
        for (int i = 0; i < 6; i++) {
            frameLeft[i] = matrizDeSprites[4][i]; // Fila 4, Columnas 0-5
        }

        // RIGHT (moviéndose a la derecha)
        frameRight = new TextureRegion[6]; // 6 frames para RIGHT
        for (int i = 0; i < 6; i++) {
            frameRight[i] = matrizDeSprites[5][i]; // Fila 5, Columnas 0-5
        }

        // HIT_RIGHT (golpeando a la derecha)
        frameHitRight = new TextureRegion[3]; // 3 frames para HIT_RIGHT
        for (int i = 0; i < 3; i++) {
            frameHitRight[i] = matrizDeSprites[6][i]; // Fila 6, Columnas 0-2
        }

        // HIT_LEFT (golpeando a la izquierda)
        frameHitLeft = new TextureRegion[3]; // 3 frames para HIT_LEFT
        for (int i = 0; i < 3; i++) {
            frameHitLeft[i] = matrizDeSprites[7][i]; // Fila 7, Columnas 0-2
        }

        // KICK_RIGHT (pateando a la derecha)
        frameKickRight = new TextureRegion[4]; // 4 frames para KICK_RIGHT
        for (int i = 0; i < 4; i++) {
            frameKickRight[i] = matrizDeSprites[8][i]; // Fila 8, Columnas 0-3
        }

        // KICK_LEFT (pateando a la izquierda)
        frameKickLeft = new TextureRegion[4]; // 4 frames para KICK_LEFT
        for (int i = 0; i < 4; i++) {
            frameKickLeft[i] = matrizDeSprites[9][i]; // Fila 9, Columnas 0-3
        }

        // SPIN_RIGHT (girando a la derecha) - Asumimos 24 frames
        frameSpinRight = new TextureRegion[24];
        for (int i = 0; i < 24; i++) {
            // Asegura que toma los frames de las filas correctas y las columnas que existen
            frameSpinRight[i] = matrizDeSprites[10 + (i / 8)][i % 8]; // Filas 10, 11, 12; Columnas 0-7
        }

        // SPIN_LEFT (girando a la izquierda) - Asumimos 24 frames
        frameSpinLeft = new TextureRegion[24];
        for (int i = 0; i < 24; i++) {
            frameSpinLeft[i] = matrizDeSprites[13 + (i / 8)][i % 8]; // Filas 13, 14, 15; Columnas 0-7
        }

        // Crear animaciones y asignarlas al mapa
        animations.put(EstadoPlayer.IDLE_RIGHT, new Animation<TextureRegion>(0.1f, frameIdleRight));
        animations.put(EstadoPlayer.IDLE_LEFT, new Animation<TextureRegion>(0.1f, frameIdleLeft));
        animations.put(EstadoPlayer.UP, new Animation<TextureRegion>(0.08f, frameUp));
        animations.put(EstadoPlayer.DOWN, new Animation<TextureRegion>(0.08f, frameDown));
        animations.put(EstadoPlayer.LEFT, new Animation<TextureRegion>(0.08f, frameLeft));
        animations.put(EstadoPlayer.RIGHT, new Animation<TextureRegion>(0.08f, frameRight));
        animations.put(EstadoPlayer.HIT_RIGHT, new Animation<TextureRegion>(0.08f, frameHitRight));
        animations.put(EstadoPlayer.HIT_LEFT, new Animation<TextureRegion>(0.08f, frameHitLeft));
        animations.put(EstadoPlayer.KICK_RIGHT, new Animation<TextureRegion>(0.08f, frameKickRight));
        animations.put(EstadoPlayer.KICK_LEFT, new Animation<TextureRegion>(0.08f, frameKickLeft));
        animations.put(EstadoPlayer.SPIN_RIGHT, new Animation<TextureRegion>(0.05f, frameSpinRight));
        animations.put(EstadoPlayer.SPIN_LEFT, new Animation<TextureRegion>(0.05f, frameSpinLeft));

        // Establecer el modo de reproducción para cada animación
        animations.get(EstadoPlayer.IDLE_RIGHT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoPlayer.IDLE_LEFT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoPlayer.UP).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoPlayer.DOWN).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoPlayer.LEFT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoPlayer.RIGHT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoPlayer.HIT_RIGHT).setPlayMode(Animation.PlayMode.NORMAL); // Acción de un solo disparo
        animations.get(EstadoPlayer.HIT_LEFT).setPlayMode(Animation.PlayMode.NORMAL);
        animations.get(EstadoPlayer.KICK_RIGHT).setPlayMode(Animation.PlayMode.NORMAL);
        animations.get(EstadoPlayer.KICK_LEFT).setPlayMode(Animation.PlayMode.NORMAL);
        animations.get(EstadoPlayer.SPIN_RIGHT).setPlayMode(Animation.PlayMode.LOOP); // Spin es continuo
        animations.get(EstadoPlayer.SPIN_LEFT).setPlayMode(Animation.PlayMode.LOOP);

        // Inicializa el frame actual. Podrías tomar el primer frame del estado por defecto (IDLE_RIGHT).
        if (animations.get(getEstadoActual()) != null) {
            setFrameActual(animations.get(getEstadoActual()).getKeyFrame(0));
        }
    }

    @Override
    public void update(float deltaTime) {
        // Manejo de la entrada del teclado y actualización de la posición
        // Esta parte se llama en PantallaDeJuego para el jugador local.
        KeyHandler();

        // Obtener la animación actual basada en el estado
        Animation<TextureRegion> currentAnimation = animations.get(estadoActual);

        // Aumentamos el tiempo del fotograma solo si la animación actual no es nula.
        if (currentAnimation != null) {
            // Si la animación actual es diferente de la que se estaba reproduciendo, resetea tiempoXFrame
            if (animacion != currentAnimation) {
                tiempoXFrame = 0; // Reinicia el tiempo cuando la animación cambia
            }
            animacion = currentAnimation; // Actualiza la referencia a la animación actual

            // Lógica de transición de estado después de que una animación de acción termina
            if ((estadoActual == EstadoPlayer.HIT_RIGHT || estadoActual == EstadoPlayer.HIT_LEFT ||
                estadoActual == EstadoPlayer.KICK_RIGHT || estadoActual == EstadoPlayer.KICK_LEFT) &&
                animacion.isAnimationFinished(tiempoXFrame)) {
                // Si la animación de acción terminó, volvemos a IDLE según la última dirección horizontal guardada
                if (lastDirection == EstadoPlayer.LEFT || lastDirection == EstadoPlayer.IDLE_LEFT) {
                    setEstadoActual(EstadoPlayer.IDLE_LEFT);
                } else {
                    setEstadoActual(EstadoPlayer.IDLE_RIGHT);
                }
                tiempoXFrame = 0; // Reseteamos el tiempo para la nueva animación IDLE
                // Asegurar que la animación de IDLE se actualice.
                animacion = animations.get(estadoActual);
                if (animacion == null) {
                    Gdx.app.error("Sonic", "Animación IDLE nula después de una acción.");
                }
            } else if ((estadoActual == EstadoPlayer.SPIN_RIGHT || estadoActual == EstadoPlayer.SPIN_LEFT) &&
                !Gdx.input.isKeyPressed(Keys.SPACE)) { // Asumiendo que SPACE es para Spin
                // Si estaba en SPIN y la tecla de SPIN ya no está presionada
                if (lastDirection == EstadoPlayer.LEFT || lastDirection == EstadoPlayer.IDLE_LEFT) {
                    setEstadoActual(EstadoPlayer.IDLE_LEFT);
                } else {
                    setEstadoActual(EstadoPlayer.IDLE_RIGHT);
                }
                tiempoXFrame = 0; // Reseteamos el tiempo
                animacion = animations.get(estadoActual);
                if (animacion == null) {
                    Gdx.app.error("Sonic", "Animación IDLE nula después de salir de SPIN.");
                }
            }
        } else {
            Gdx.app.log("Sonic", "Advertencia: No hay animación para el estado actual: " + estadoActual);
            // Si no hay animación, al menos intenta mostrar el primer frame del IDLE_RIGHT por defecto
            animacion = animations.get(EstadoPlayer.IDLE_RIGHT);
            if (animacion != null) {
                setFrameActual(animacion.getKeyFrame(0));
            }
            return; // No podemos avanzar si no hay animación
        }

        // Acumular tiempo para la animación
        tiempoXFrame += deltaTime;

        // Obtener el frame actual de la animación
        TextureRegion currentFrame = animacion.getKeyFrame(tiempoXFrame);
        setFrameActual(currentFrame);
    }

    @Override
    public void draw(SpriteBatch batch) {
        // Dibujamos el frame actual en la posición actual del jugador (usando estado.x/y)
        if (frameActual != null) {
            batch.draw(frameActual, estado.x, estado.y, getTileSize(), getTileSize());
        } else {
            Gdx.app.log("Sonic", "Advertencia: 'frameActual' es nulo en el método draw(). No se puede dibujar a Sonic.");
        }
    }
}
