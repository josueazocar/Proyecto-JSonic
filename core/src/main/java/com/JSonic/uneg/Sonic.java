package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Sonic extends Player { // Ahora extiende Player, que ya implementa Disposable

    protected TextureRegion[] frameSpinRight; // Arreglo para almacenar los sprites de girar a la derecha
    protected TextureRegion[] frameSpinLeft;  // Arreglo para almacenar los sprites de girar a la izquierda

    // --- CONSTRUCTOR MODIFICADO (Opción más limpia) ---
    Sonic(LevelManager levelManager) { // Recibe el LevelManager
        super(levelManager); // Llama al constructor de la superclase (Player) con LevelManager
        // Asegúrate de que setDefaultValues() en Player configure los valores iniciales.
        CargarSprites(); // Carga los sprites específicos de Sonic después de la inicialización base
        // Establecer la animación inicial
        animacion = animations.get(getEstadoActual());
        if (animacion != null) {
            animacion.setPlayMode(Animation.PlayMode.LOOP);
        }
    }
    // --- FIN CONSTRUCTOR MODIFICADO ---

    // --- NUEVO MÉTODO para obtener la ruta del SpriteSheet (implementa un método abstracto que podrías añadir a Player) ---
    @Override
    protected String getSpriteSheetPath() {
        return "Entidades/Player/Sonic/sonic.png";
    }
    // --- FIN NUEVO MÉTODO ---

    @Override
    protected void CargarSprites() {
        // --- MODIFICADO: Llama al método de la superclase para cargar la Texture principal ---
        // Esto asume que Player.CargarSprites() o un nuevo método en Player
        // ahora maneja la carga de la spriteSheet basándose en getSpriteSheetPath().
        // Si Player.CargarSprites() no existe, o no hace esto, tendrías que cargar la Texture aquí
        // Y luego llamar a setSpriteSheet(coleccionDeSprites);
        // Si mantienes el patron de que cada subclase carga su Texture:
        Texture coleccionDeSprites = new Texture(Gdx.files.internal(getSpriteSheetPath()));
        setSpriteSheet(coleccionDeSprites);
        // --- FIN MODIFICADO ---

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
        KeyHandler(); // Maneja la entrada del teclado y actualiza la posición y el estado

        // Obtener la animación actual basada en el estado
        Animation<TextureRegion> currentAnimation = animations.get(estadoActual);

        if (currentAnimation != null) {
            // Reiniciar el tiempoXFrame si la animación acaba de cambiar (para acciones de un solo disparo)
            if (animacion != currentAnimation) {
                tiempoXFrame = 0; // Reinicia el tiempo cuando la animación cambia
            }
            animacion = currentAnimation; // Actualiza la referencia a la animación actual

            // Si la animación es de un solo disparo (NORMAL) y ya terminó, vuelve a IDLE.
            if (animacion.getPlayMode() == Animation.PlayMode.NORMAL && animacion.isAnimationFinished(tiempoXFrame)) {
                // Volver al estado IDLE correspondiente a la última dirección horizontal.
                setEstadoActual(lastDirection);
                tiempoXFrame = 0; // Reiniciar el tiempo para el nuevo estado IDLE
                // Asegurar que la animación de IDLE se actualice.
                animacion = animations.get(estadoActual);
                if (animacion == null) {
                    Gdx.app.error("Sonic", "Animación IDLE nula después de una acción.");
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

        // Estas llamadas ahora son responsabilidad de PantallaDeJuego.render()
        if (frameActual != null) {
            batch.draw(frameActual, positionX, positionY, getTileSize(), getTileSize());
        } else {
            Gdx.app.log("Sonic", "Advertencia: 'frameActual' es nulo en el método draw(). No se puede dibujar a Sonic.");
        }
    }
    
}
