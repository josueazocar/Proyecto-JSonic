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
        super(estadoInicial); // Llama al constructor de arriba
        CargarSprites(); // Carga los sprites específicos de Sonic después de la inicialización base
        // Establecer la animación inicial basada en el estado actual del jugador
        animacion = animations.get(getEstadoActual());
        if (animacion != null) {
            animacion.setPlayMode(Animation.PlayMode.LOOP);
        }
        this.levelManager = levelManager; // Asigna el LevelManager
    }


    // --- NUEVO MÉTODO para obtener la ruta del SpriteSheet (implementa un método abstracto que podrías añadir a Player) ---

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
        // Fila 0, Columnas 0-3
        System.arraycopy(matrizDeSprites[0], 0, frameIdleRight, 0, 4);

        frameIdleLeft = new TextureRegion[4]; // 4 frames para IDLE_LEFT
        // Fila 1, Columnas 0-3
        System.arraycopy(matrizDeSprites[1], 0, frameIdleLeft, 0, 4);

        // UP (moviéndose hacia arriba)
        frameUp = new TextureRegion[6]; // 6 frames para UP
        // Fila 2, Columnas 0-5
        System.arraycopy(matrizDeSprites[2], 0, frameUp, 0, 6);

        // DOWN (moviéndose hacia abajo)
        frameDown = new TextureRegion[6]; // 6 frames para DOWN
        // Fila 3, Columnas 0-5
        System.arraycopy(matrizDeSprites[3], 0, frameDown, 0, 6);

        // LEFT (moviéndose a la izquierda)
        frameLeft = new TextureRegion[6]; // 6 frames para LEFT
        // Fila 4, Columnas 0-5
        System.arraycopy(matrizDeSprites[4], 0, frameLeft, 0, 6);

        // RIGHT (moviéndose a la derecha)
        frameRight = new TextureRegion[6]; // 6 frames para RIGHT
        // Fila 5, Columnas 0-5
        System.arraycopy(matrizDeSprites[5], 0, frameRight, 0, 6);

        // HIT_RIGHT (golpeando a la derecha)
        frameHitRight = new TextureRegion[3]; // 3 frames para HIT_RIGHT
        // Fila 6, Columnas 0-2
        System.arraycopy(matrizDeSprites[6], 0, frameHitRight, 0, 3);

        // HIT_LEFT (golpeando a la izquierda)
        frameHitLeft = new TextureRegion[3]; // 3 frames para HIT_LEFT
        // Fila 7, Columnas 0-2
        System.arraycopy(matrizDeSprites[7], 0, frameHitLeft, 0, 3);

        // KICK_RIGHT (pateando a la derecha)
        frameKickRight = new TextureRegion[4]; // 4 frames para KICK_RIGHT
        // Fila 8, Columnas 0-3
        System.arraycopy(matrizDeSprites[8], 0, frameKickRight, 0, 4);

        // KICK_LEFT (pateando a la izquierda)
        frameKickLeft = new TextureRegion[4]; // 4 frames para KICK_LEFT
        // Fila 9, Columnas 0-3
        System.arraycopy(matrizDeSprites[9], 0, frameKickLeft, 0, 4);

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

    // En tu clase Sonic.java

    @Override
    public void update(float deltaTime) {
        // --- FASE 1: LÓGICA DE DECISIÓN DE ESTADO ---
        // Esta sección entera es el nuevo "cerebro" que decide la animación.

        // Primero, comprobamos si una acción como GOLPEAR o PATEAR está en curso y no ha terminado.
        boolean accionEnCurso = (animacion != null && animacion.getPlayMode() == Animation.PlayMode.NORMAL && !animacion.isAnimationFinished(tiempoXFrame));

        // Si una acción está en curso, no leemos ninguna nueva tecla para cambiar el estado.
        // Dejamos que la animación termine.
        if (!accionEnCurso) {
            // Si no hay acción en curso, leemos el teclado para decidir el nuevo estado.
            boolean quiereGolpear = Gdx.input.isKeyJustPressed(Keys.J);
            boolean quierePatear = Gdx.input.isKeyJustPressed(Keys.K);
            boolean quiereGirar = Gdx.input.isKeyPressed(Keys.L); // O la tecla que uses para SPIN
            boolean seEstaMoviendo = (estado.x != lastPosX || estado.y != lastPosY); // Comprobamos si la posición cambió

            // Actualizamos las posiciones anteriores para el siguiente frame
            lastPosX = estado.x;
            lastPosY = estado.y;

            // Sistema de Prioridades para decidir el estado de la animación:
            if (quiereGolpear) {
                setEstadoActual((lastDirection == EstadoPlayer.IDLE_LEFT) ? EstadoPlayer.HIT_LEFT : EstadoPlayer.HIT_RIGHT);
            } else if (quierePatear) {
                setEstadoActual((lastDirection == EstadoPlayer.IDLE_LEFT) ? EstadoPlayer.KICK_LEFT : EstadoPlayer.KICK_RIGHT);
            } else if (quiereGirar) {
                setEstadoActual((lastDirection == EstadoPlayer.IDLE_LEFT) ? EstadoPlayer.SPIN_LEFT : EstadoPlayer.SPIN_RIGHT);
            } else if (seEstaMoviendo) {
                // Si no hay acción pero el personaje se movió (gracias a KeyHandler), decidimos la animación de movimiento.
                // Esta lógica es un ejemplo, puedes ajustarla a la tuya.
                if (lastDirection == EstadoPlayer.IDLE_LEFT) { // Usamos lastDirection que KeyHandler actualiza
                    setEstadoActual(EstadoPlayer.LEFT);
                } else {
                    setEstadoActual(EstadoPlayer.RIGHT);
                }
            } else {
                // Si no pasó nada de lo anterior, el personaje está quieto.
                setEstadoActual((lastDirection == EstadoPlayer.IDLE_LEFT) ? EstadoPlayer.IDLE_LEFT : EstadoPlayer.IDLE_RIGHT);
            }
        }

        // --- FASE 2: ACTUALIZACIÓN DE LA ANIMACIÓN ---
        // Esta sección toma el estado que decidimos arriba y calcula el frame a dibujar.

        // Obtenemos la animación para el estado actual
        Animation<TextureRegion> targetAnimation = animations.get(getEstadoActual());

        // Si la animación del estado actual es diferente a la que se estaba reproduciendo,
        // significa que acabamos de cambiar de estado (ej: de IDLE a RIGHT),
        // así que reiniciamos el tiempo para que la nueva animación empiece desde el principio.
        if (this.animacion != targetAnimation) {
            this.tiempoXFrame = 0;
        }

        this.animacion = targetAnimation;

        // Avanzamos el tiempo de la animación y obtenemos el fotograma correcto
        if (animacion != null) {
            tiempoXFrame += deltaTime;
            frameActual = animacion.getKeyFrame(tiempoXFrame);
        } else {
            // Código de seguridad por si un estado no tiene animación asignada
            Gdx.app.log("Sonic_update", "Advertencia: No hay animación para el estado: " + getEstadoActual());
            frameActual = null;
        }
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
