package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
//Importa Keys
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils; // Importar para MathUtils.clamp
import com.badlogic.gdx.math.Rectangle;





public class Sonic extends Player {

    protected TextureRegion[] frameSpinRight;
    protected TextureRegion[] frameSpinLeft;


    public Sonic(PlayerState estadoInicial) {
        super(estadoInicial);
        CargarSprites();
        //para que sonic pueda hacer las coliciones
        inicializarHitbox();
        //------------------
        animacion = animations.get(getEstadoActual());
    }

    public Sonic(PlayerState estadoInicial, LevelManager levelManager) {
        super(estadoInicial, levelManager); // Pasa levelManager al constructor de Player
        CargarSprites();
        //inicializar para colisiones
        inicializarHitbox();
        //-------------------
        animacion = animations.get(getEstadoActual());
    }

    protected String getSpriteSheetPath() {
        return "Entidades/Player/Sonic/sonic.png";
    }

    @Override
    protected void CargarSprites() {
        Texture coleccionDeSprites = new Texture(Gdx.files.internal(getSpriteSheetPath()));
        setSpriteSheet(coleccionDeSprites);

        TextureRegion[][] matrizDeSprites = TextureRegion.split(getSpriteSheet(), getSpriteSheet().getWidth() / 8, getSpriteSheet().getHeight() / 30);

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
            frameUp[i] = matrizDeSprites[1][i];
        }
        for (int i = 0; i < 8; i++) {
            frameDown[i] = matrizDeSprites[1][i];
        }
        for (int i = 0; i < 8; i++) {
            frameLeft[i] = matrizDeSprites[1][i];
        }
        for (int i = 0; i < 8; i++) {
            frameRight[i] = new TextureRegion(frameLeft[i]);
            frameRight[i].flip(true, false);
        }
        for (int i = 0; i < 8; i++) {
            frameHitLeft[i] = matrizDeSprites[7][i];
        }
        for (int i = 0; i < 8; i++) {
            frameHitRight[i] = new TextureRegion(frameHitLeft[i]);
            frameHitRight[i].flip(true, false);
        }
        for (int i = 0; i < 8; i++) {
            frameKickLeft[i] = matrizDeSprites[8][i];
        }
        for (int i = 0; i < 8; i++) {
            frameKickRight[i] = new TextureRegion(frameKickLeft[i]);
            frameKickRight[i].flip(true, false);
        }
        for (int i = 0; i < 24; i++) {
            frameSpinLeft[i] = matrizDeSprites[18][i % 8];
        }
        for (int i = 0; i < 24; i++) {
            frameSpinRight[i] = new TextureRegion(frameSpinLeft[i]);
            frameSpinRight[i].flip(true, false);
        }

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
        // CAMBIO IMPORTANTE: SPIN vuelve a ser LOOP
        animations.get(EstadoPlayer.SPIN_RIGHT).setPlayMode(Animation.PlayMode.LOOP);
        animations.get(EstadoPlayer.SPIN_LEFT).setPlayMode(Animation.PlayMode.LOOP);

        Animation<TextureRegion> initialAnimation = animations.get(getEstadoActual());
        if (initialAnimation != null) {
            setFrameActual(initialAnimation.getKeyFrame(0));
        }
    }

    //Colisiones
    /**
     * CAMBIO: Este método ahora calcula el hitbox en relación al getTileSize() de la Entity (48),
     * y asume que el sprite visual de Sonic (que podría ser más pequeño) está dentro de ese "slot".
     * AJUSTA LOS VALORES DE 0.6f y los offsets segun el tamaño real de Sonic dentro de tu tile de 48x48.
     */
    private void inicializarHitbox() {
        float baseTileSize = getTileSize(); // Esto será 48, el tamaño del "slot" donde se dibuja Sonic.

        // CAMBIO: Ajusta estos valores para reflejar el tamaño real del cuerpo de Sonic
        // dentro de su frame de 48x48.
        // Por ejemplo, si Sonic es visualmente 32x32 y está centrado en un tile de 48x48:
        this.collisionWidth = baseTileSize * 0.6f; // Un 60% de 48 = 28.8px (ajusta este porcentaje)
        this.collisionHeight = baseTileSize * 0.75f; // Un 75% de 48 = 36px (ajusta este porcentaje)

        // CAMBIO: Calcula los offsets para centrar el hitbox dentro del tile de 48x48
        // y posicionarlo correctamente (ej. en la parte inferior para colisiones con el suelo).
        this.collisionOffsetX = (baseTileSize - collisionWidth) / 2f; // Centra horizontalmente
        this.collisionOffsetY = 0; // Coloca el hitbox en la base del tile

        this.bounds = new Rectangle(estado.x + collisionOffsetX, estado.y + collisionOffsetY, collisionWidth, collisionHeight);

        Gdx.app.log("Sonic", "Hitbox inicializado (basado en Entity.tileSize): " + this.bounds.toString());
        Gdx.app.log("Sonic", "Entity.tileSize usado para hitbox: " + baseTileSize);
        Gdx.app.log("Sonic", "Offsets del hitbox: x=" + collisionOffsetX + ", y=" + collisionOffsetY);
    }


    //fin de el hixbox

    @Override
    public void update(float deltaTime) {
    //Para las colisiones
        bounds.setPosition(estado.x + collisionOffsetX, estado.y + collisionOffsetY);

        // Obtenemos la animación que DEBERÍA estar activa según el estado actual
        Animation<TextureRegion> targetAnimation = animations.get(getEstadoActual());

        // Si la animación actual (animacion) es diferente de la animación objetivo,
        // significa que acabamos de cambiar de estado y necesitamos resetear tiempoXFrame.
        if (this.animacion != targetAnimation) {
            this.tiempoXFrame = 0; // Reiniciar tiempoXFrame para la nueva animación
            this.animacion = targetAnimation; // Actualizar la referencia a la animación actual
        }

        // Aumentamos el tiempo del fotograma solo si la animación actual no es nula.
        if (animacion != null) {
            tiempoXFrame += deltaTime;
        }

        // Lógica de transición de estado después de que una animación de acción termina
        if ((estado.estadoAnimacion == EstadoPlayer.HIT_RIGHT || estado.estadoAnimacion == EstadoPlayer.HIT_LEFT ||
            estado.estadoAnimacion == EstadoPlayer.KICK_RIGHT || estado.estadoAnimacion == EstadoPlayer.KICK_LEFT ||
            estado.estadoAnimacion == EstadoPlayer.SPIN_RIGHT || estado.estadoAnimacion == EstadoPlayer.SPIN_LEFT) && animacion != null) {

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
        if (frameActual != null) {
            batch.draw(frameActual, estado.x, estado.y, getTileSize(), getTileSize());
        } else {
            Gdx.app.log("Sonic", "Advertencia: 'frameActual' es nulo en el método draw(). No se puede dibujar a Sonic.");
        }
    }
}
