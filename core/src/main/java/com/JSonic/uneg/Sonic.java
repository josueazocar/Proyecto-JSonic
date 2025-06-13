package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.Input.Keys; // Importar Keys para el manejo de teclas

// No es necesario importar EnumMap aquí si ya está importado en Entity y se usa directamente en animations
// import java.util.EnumMap;


public class Sonic extends Player {
    // Atributos - No es necesario declararlos aquí si se heredan y se acceden mediante getters/setters

    // Constructor
    Sonic() {
        super();
        CargarSprites();
        // Inicializamos 'estadoActual' por defecto.
        setEstadoActual(EstadoPlayer.IDLE);
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

        // Inicialización de arreglos para cada movimiento con su fila de sprites correspondientes en la matriz
        // La matriz tiene 8 columnas (índices 0-7)
        TextureRegion[][] matrizDeSprites = TextureRegion.split(getSpriteSheet(), spriteSheet.getWidth() / 8, spriteSheet.getHeight() / 30);
        setFrameIdle(new TextureRegion[8]); // frameIdle tendrá 8 elementos
        setFrameUp(new TextureRegion[8]);
        setFrameDown(new TextureRegion [8]);
        setFrameLeft(new TextureRegion[8]);
        setFrameRight(new TextureRegion [8]);
        setFrameHit(new TextureRegion[16]); // frameHit tendrá 16 elementos

        // Llenado de arreglos con sus sprites correspondientes
        for (int i = 0; i < 4; i++) { // Sprites de animación 'idle' (primeros 4 frames de la matriz)
            frameIdle[i] = matrizDeSprites[0][i];
        }
        // Repetimos los primeros 4 frames, pero en orden inverso para los siguientes 4
        for (int i = 0; i < 4; i++) {
            frameIdle[i + 4] = matrizDeSprites[0][3-i]; // Accede a índices 3, 2, 1, 0 de matrizDeSprites[0]
        }


        for (int i = 0; i < 8; i++) { // Sprites de animación 'up'
            frameUp[i] = matrizDeSprites[5][i];
        }

        for (int i = 0; i < 8; i++) { // Sprites de animación 'down'
            frameDown[i] = matrizDeSprites[1][i];
        }

        for (int intIndex = 0; intIndex < 8; intIndex++) { // Sprites de animación 'left'
            frameLeft[intIndex] = matrizDeSprites[1][intIndex]; // Los sprites de izquierda se toman directamente de la matriz
        }

        // --- CAMBIO AQUÍ: Sprites para ir hacia la derecha (invertidos de los de la izquierda) ---
        for (int i = 0; i < 8; i++) { // Sprites de animación 'right'
            // Creamos una NUEVA TextureRegion a partir del frame izquierdo para evitar modificar el original
            frameRight[i] = new TextureRegion(frameLeft[i]);
            // Volteamos horizontalmente este nuevo TextureRegion para que mire a la derecha
            frameRight[i].flip(true, false);
        }
        // --- FIN DEL CAMBIO ---

        // Llenado de frames para la animación 'hit' (16 frames)
        for (int i = 0; i < 8; i++) { // Primeros 8 frames (directamente de la matriz)
            frameHit[i] = matrizDeSprites[7][i];
        }
        // Siguientes 8 frames (frames inversos de la matriz)
        for (int i = 0; i < 8; i++) {
            frameHit[i+8] = matrizDeSprites[7][7-i]; // Accede a índices 7, 6, ..., 0 de matrizDeSprites[7]
        }

        // ¡Ahora sí, llenamos el mapa de animaciones de forma adecuada!
        // Esto evita crear nuevas instancias de animación en cada frame.
        animations.put(EstadoPlayer.IDLE, new Animation<TextureRegion>(0.12f, getFrameIdle()));
        animations.put(EstadoPlayer.UP, new Animation<TextureRegion>(0.1f, frameUp));
        animations.put(EstadoPlayer.DOWN, new Animation<TextureRegion>(0.26f, frameDown));
        animations.put(EstadoPlayer.LEFT, new Animation<TextureRegion>(0.2f, frameLeft));
        animations.put(EstadoPlayer.RIGHT, new Animation<TextureRegion>(0.2f, frameRight));
        animations.put(EstadoPlayer.HIT, new Animation<TextureRegion>(0.1f, frameHit)); // ¡Aquí definimos la animación de golpe!

        // Configuramos el modo de reproducción para las animaciones, si no es el valor por defecto.
        // Para IDLE, UP, DOWN, LEFT, RIGHT, normalmente queremos que se repitan (LOOP).
        animations.get(EstadoPlayer.IDLE).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoPlayer.UP).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoPlayer.DOWN).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoPlayer.LEFT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoPlayer.RIGHT).setPlayMode(Animation.PlayMode.LOOP);
        // ¡Importante para la animación de golpe! Solo se reproduce una vez (NORMAL).
        animations.get(EstadoPlayer.HIT).setPlayMode(Animation.PlayMode.NORMAL);
    }


    @Override
    public void update(float deltaTime) {
        // Obtenemos la animación actual del mapa.
        Animation<TextureRegion> currentAnimation = animations.get(getEstadoActual());

        // Si la animación actual es nula (no debería pasar si todo está configurado), registramos un error.
        if (currentAnimation == null) {
            Gdx.app.log("Sonic", "Error: No se encontró animación para el estado actual: " + getEstadoActual());
            return; // Salimos para evitar un NullPointerException
        }

        // Aumentamos el tiempo del fotograma solo para la animación actual.
        // Si el estado cambia, 'tiempoXFrame' se reseteará automáticamente para la nueva animación.
        setTiempoXFrame(deltaTime);

        // Manejo de la entrada del teclado
        KeyHandler();

        // Lógica de transición de estado después de la animación de golpe
        if (getEstadoActual() == EstadoPlayer.HIT) {
            // Verificamos si la animación de golpe ha terminado.
            if (currentAnimation.isAnimationFinished(getTiempoXFrame())) {
                setEstadoActual(EstadoPlayer.IDLE); // Si terminó, volvemos a IDLE
                tiempoXFrame = 0; // Reseteamos el tiempo para la nueva animación IDLE
            }
        }

        // Si el estado acaba de cambiar y no es HIT (porque HIT se maneja arriba),
        // reseteamos el tiempo del fotograma para que la nueva animación comience desde el principio.
        // Esto es necesario porque KeyHandler() puede cambiar el estado, y queremos que la nueva animación inicie.
        // Esta lógica podría optimizarse si la transición de estado se maneja centralmente.
        // Por ahora, si el estado actual es diferente de la animación que se estaba reproduciendo
        // antes de este 'update', reiniciamos el tiempo.
        if (animacion != currentAnimation) {
            tiempoXFrame = 0; // Resetear tiempoXFrame para la nueva animación
        }
        animacion = currentAnimation; // Actualizamos la referencia a la animación actual

        // Obtenemos el frame actual de la animación para el estado actual
        // Nos aseguramos de que 'animacion' no sea nula antes de llamar a getKeyFrame
        if (animacion != null) {
            frameActual = animacion.getKeyFrame(tiempoXFrame); // No es necesario 'true' si el playmode ya está configurado
        } else {
            // Este caso debería ser raro si el chequeo inicial de currentAnimation no es nulo.
            Gdx.app.log("Sonic", "Advertencia: 'animacion' es nula después de la asignación en update(). No se puede obtener el frame clave.");
        }
    }


    @Override
    public void KeyHandler() {
        boolean keyPressed = false; // Bandera para saber si alguna tecla de movimiento fue presionada

        // Manejo de movimiento
        if (Gdx.input.isKeyPressed(Keys.W)) { // Arriba
            setPositionY(getSpeed());
            setEstadoActual(EstadoPlayer.UP);
            keyPressed = true;
        }
        if (Gdx.input.isKeyPressed(Keys.S)) { // Abajo
            setPositionY(-getSpeed());
            setEstadoActual(EstadoPlayer.DOWN);
            keyPressed = true;
        }
        if (Gdx.input.isKeyPressed(Keys.A)) { // Izquierda
            setPositionX(-getSpeed());
            setEstadoActual(EstadoPlayer.LEFT);
            keyPressed = true;
        }
        if (Gdx.input.isKeyPressed(Keys.D)) { // Derecha
            setPositionX(getSpeed());
            setEstadoActual(EstadoPlayer.RIGHT);
            keyPressed = true;
        }

        // Manejo del golpe (puñetazo)
        // Usamos isKeyJustPressed para detectar la pulsación de la tecla solo una vez.
        if (Gdx.input.isKeyJustPressed(Keys.J)) { // Tecla para golpe (ej. 'J')
            setEstadoActual(EstadoPlayer.HIT); // Asignación de estado actual a HIT
            tiempoXFrame = 0; // Reiniciamos el tiempo para que la animación de golpe comience desde el principio
            keyPressed = true; // Establecemos la bandera
        }

        // Si ninguna tecla de movimiento o golpe fue presionada, volvemos a IDLE
        // Solo si el estado actual no es HIT (para no interrumpir la animación de golpe)
        if (!keyPressed && getEstadoActual() != EstadoPlayer.HIT) {
            setEstadoActual(EstadoPlayer.IDLE);
        }
    }


    @Override
    public void draw(SpriteBatch batch) {
        batch.begin();
        // Dibujamos el frame actual en la posición actual del jugador
        // ¡Importante! Verificamos que 'frameActual' no sea nulo antes de intentar dibujarlo.
        if (getFrameActual() != null) {
            batch.draw(getFrameActual(), getPositionX(), getPositionY(), getTileSize(), getTileSize());
        } else {
            // Si 'frameActual' es nulo, registramos una advertencia para depuración.
            Gdx.app.log("Sonic", "Advertencia: 'frameActual' es nulo en el método draw(). No se puede dibujar a Sonic.");
        }
        batch.end();
    }
}
