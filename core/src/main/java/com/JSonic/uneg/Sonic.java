package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Sonic extends Player {
    protected TextureRegion[] frameSpinRight; //Arreglo para almacenar los sprites del spin derecho
    protected TextureRegion[] frameSpinLeft; //Arreglo para almacenar los sprites cuando del spin izquierdo

    // Constructor
    public Sonic(PlayerState estadoInicial) {
        super(estadoInicial);
        CargarSprites();
        // Inicializamos 'animacion' con la animación del estado inicial.
        // Ahora obtenemos la animación del mapa 'animations'.
        animacion = animations.get(getEstadoActual());
        // Aseguramos que la animación inicial tenga su modo de reproducción correcto.
        if (animacion != null) {
            animacion.setPlayMode(Animation.PlayMode.LOOP);
        }
    }

    // Métodos
    @Override
    protected void CargarSprites() {
        // Inicialización de los sprites del personaje
        Texture coleccionDeSprites = new Texture(Gdx.files.internal("Entidades/Player/Sonic/sonic.png"));
        setSpriteSheet(coleccionDeSprites);

        // La matriz tiene 8 columnas y 30 filas.
        // Asegúrate de que las dimensiones coincidan con tu spriteSheet.
        TextureRegion[][] matrizDeSprites = TextureRegion.split(getSpriteSheet(), getSpriteSheet().getWidth() / 8, getSpriteSheet().getHeight() / 30);

        // Aquí inicializamos los arreglos de frames con el tamaño correcto.
        // Ahora TODOS los arreglos de acciones (HIT, KICK, SPIN) tendrán 8 frames.
        frameIdleRight = new TextureRegion[8];
        frameIdleLeft = new TextureRegion[8];
        frameUp = new TextureRegion[8];
        frameDown = new TextureRegion[8];
        frameLeft = new TextureRegion[8];
        frameRight = new TextureRegion[8];
        frameHitRight = new TextureRegion[8]; // Tamaño 8 frames
        frameHitLeft = new TextureRegion[8]; // Tamaño 8 frames
        frameKickRight = new TextureRegion[8]; // Tamaño 8 frames
        frameKickLeft = new TextureRegion[8]; // Tamaño 8 frames
        frameSpinRight = new TextureRegion[24]; // Tamaño 24 frames
        frameSpinLeft = new TextureRegion[24]; // Tamaño 24 frames


        // Llenado de arreglos con sus sprites correspondientes
        // --- IDLE LEFT (matriz[0][0-7]) ---
        // Asumiendo que quieres el efecto de "respiración" para 8 frames.
        for (int i = 0; i < 4; i++) { // Primeros 4 frames
            frameIdleLeft[i] = matrizDeSprites[0][i];
        }
        for (int i = 0; i < 4; i++) { // Siguientes 4 frames (inversos para efecto de "respiración")
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

        // --- LEFT (asumimos matriz[1][0-7] son los sprites para caminar izquierda) ---
        for (int i = 0; i < 8; i++) {
            frameLeft[i] = matrizDeSprites[1][i];
        }

        // --- RIGHT (volteando LEFT) ---
        for (int i = 0; i < 8; i++) {
            frameRight[i] = new TextureRegion(frameLeft[i]);
            frameRight[i].flip(true, false);
        }

        // --- HIT LEFT (matriz[7][0-7] - 8 frames) ---
        // Usamos los primeros 8 frames de la fila 7.
        for (int i = 0; i < 8; i++) {
            frameHitLeft[i] = matrizDeSprites[7][i];
        }

        // --- HIT LEFT (volteando HIT RIGHT - 8 frames) ---
        for (int i = 0; i < 8; i++) {
            frameHitRight[i] = new TextureRegion(frameHitLeft[i]);
            frameHitRight[i].flip(true, false);
        }

        // --- KICK LEFT (Ejemplo: matriz[8][0-7] - 8 frames) ---
        // ¡RUTA DE EJEMPLO! Ajusta 'matrizDeSprites[8]' a la fila real para la patada derecha.
        for (int i = 0; i < 8; i++) {
            frameKickLeft[i] = matrizDeSprites[8][i];
        }
        // --- KICK LEFT (volteando KICK RIGHT - 8 frames) ---
        for (int i = 0; i < 8; i++) {
            frameKickRight[i] = new TextureRegion(frameKickLeft[i]);
            frameKickRight[i].flip(true, false);
        }

        // --- SPIN RIGHT (Ejemplo: matriz[17][0-7] - 8 frames) ---
        // ¡RUTA DE EJEMPLO! Ajusta 'matrizDeSprites[17]' a la fila real para el spin derecho.
        for (int i = 0; i < 24; i++) { // Bucle para 24 frames
            // Accede a los frames del sprite sheet usando el operador módulo (%) para repetir los 8 frames.
            frameSpinLeft[i] = matrizDeSprites[18][i % 8];
        }
        // --- SPIN RIGHT (volteando SPIN LEFT - 24 frames) ---
        for (int i = 0; i < 24; i++) { // Bucle para 24 frames
            frameSpinRight[i] = new TextureRegion(frameSpinLeft[i]);
            frameSpinRight[i].flip(true, false);
        }


        // Ahora, llenamos el mapa de animaciones correctamente con los nuevos arreglos
        // y asignamos los modos de reproducción.
        animations.put(EstadoPlayer.IDLE_RIGHT, new Animation<TextureRegion>(0.12f, frameIdleRight));
        animations.put(EstadoPlayer.IDLE_LEFT, new Animation<TextureRegion>(0.12f, frameIdleLeft));
        animations.put(EstadoPlayer.UP, new Animation<TextureRegion>(0.1f, frameUp));
        animations.put(EstadoPlayer.DOWN, new Animation<TextureRegion>(0.26f, frameDown));
        animations.put(EstadoPlayer.LEFT, new Animation<TextureRegion>(0.2f, frameLeft));
        animations.put(EstadoPlayer.RIGHT, new Animation<TextureRegion>(0.2f, frameRight));
        animations.put(EstadoPlayer.HIT_RIGHT, new Animation<TextureRegion>(0.08f, frameHitRight)); // Ajusta el frameDuration
        animations.put(EstadoPlayer.HIT_LEFT, new Animation<TextureRegion>(0.08f, frameHitLeft)); // Ajusta el frameDuration
        animations.put(EstadoPlayer.KICK_RIGHT, new Animation<TextureRegion>(0.1f, frameKickRight)); // Ajusta el frameDuration
        animations.put(EstadoPlayer.KICK_LEFT, new Animation<TextureRegion>(0.1f, frameKickLeft)); // Ajusta el frameDuration
        animations.put(EstadoPlayer.SPIN_RIGHT, new Animation<TextureRegion>(0.07f, frameSpinRight)); // Ajusta el frameDuration
        animations.put(EstadoPlayer.SPIN_LEFT, new Animation<TextureRegion>(0.07f, frameSpinLeft)); // Ajusta el frameDuration


        // Configuramos el modo de reproducción para las animaciones
        // La mayoría se repiten (LOOP). Las acciones como HIT, KICK, SPIN se reproducen una vez (NORMAL).
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
        animations.get(EstadoPlayer.SPIN_RIGHT).setPlayMode(Animation.PlayMode.NORMAL);
        animations.get(EstadoPlayer.SPIN_LEFT).setPlayMode(Animation.PlayMode.NORMAL);
    }

    @Override
    public void update(float deltaTime) {
        // Obtenemos la animación que DEBERÍA estar activa según el estado actual
        Animation<TextureRegion> targetAnimation = animations.get(getEstadoActual());

        // Si la animación actual (animacion) es diferente de la animación objetivo,
        // significa que acabamos de cambiar de estado y necesitamos resetear tiempoXFrame.
        if (this.animacion != targetAnimation) {
            this.tiempoXFrame = 0; // Reiniciar tiempoXFrame para la nueva animación
            this.animacion = targetAnimation; // Actualizar la referencia a la animación actual
        }

        // Manejo de la entrada del teclado (llama a KeyHandler en Player)
       // KeyHandler();

        // Aumentamos el tiempo del fotograma solo si la animación actual no es nula.
        if (animacion != null) {
            tiempoXFrame += deltaTime;
        }

        // Lógica de transición de estado después de que una animación de acción termina
        if ((estadoActual == EstadoPlayer.HIT_RIGHT || estadoActual == EstadoPlayer.HIT_LEFT ||
            estadoActual == EstadoPlayer.KICK_RIGHT || estadoActual == EstadoPlayer.KICK_LEFT ||
            estadoActual == EstadoPlayer.SPIN_RIGHT || estadoActual == EstadoPlayer.SPIN_LEFT) && animacion != null) {

            // Verificamos si la animación de acción ha terminado.
            if (animacion.isAnimationFinished(tiempoXFrame)) {
                // Si terminó, volvemos a IDLE según la última dirección horizontal guardada
                if (lastDirection == EstadoPlayer.LEFT || lastDirection == EstadoPlayer.IDLE_LEFT) {
                    setEstadoActual(EstadoPlayer.IDLE_LEFT);
                } else {
                    setEstadoActual(EstadoPlayer.IDLE_RIGHT);
                }
                tiempoXFrame = 0; // Reseteamos el tiempo para la nueva animación IDLE
            }
        }

        // Obtenemos el frame actual de la animación para el estado actual
        if (animacion != null) {
            // isAnimationFinished ya chequea el PlayMode.NORMAL, así que solo usamos getKeyFrame
            frameActual = animacion.getKeyFrame(tiempoXFrame);
        } else {
            Gdx.app.log("Sonic", "Advertencia: 'animacion' es nula en update(). No se puede obtener el frame clave para estado: " + getEstadoActual());
            frameActual = null; // Asegurarse de que frameActual sea nulo si no hay animación
        }
    }


    @Override
    public void draw(SpriteBatch batch) {

        // Dibujamos el frame actual en la posición actual del jugador
        if (frameActual != null) {
            // Dibujamos el sprite en la posición actual y con el tamaño del tile
            batch.draw(frameActual, estado.x, estado.y, getTileSize(), getTileSize());
        } else {
            // Si 'frameActual' es nulo, registramos una advertencia para depuración.
            Gdx.app.log("Sonic", "Advertencia: 'frameActual' es nulo en el método draw(). No se puede dibujar a Sonic.");
        }

    }
}
