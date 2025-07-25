package com.JSonic.uneg.EntidadesVisuales;

import com.JSonic.uneg.State.DronState;
import com.JSonic.uneg.LevelManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import network.LocalServer;

import java.util.EnumMap;

/**
 * Clase que representa un dron que sigue a un objetivo (Tails) y planta árboles.
 */
public class Dron_Tails {

    // --- Constantes y Configuración ---
    private static final float DURACION_SEGUIMIENTO = 15.0f;
    private static final float OFFSET_X = -60;
    private static final float OFFSET_Y = 50;
    private static final float LERP_FACTOR = 5.0f;
    public boolean isOnlineMode = false;
    // --- Estado y Referencias ---
    public DronState estado;
    private Tails objetivo;
    public float tiempoDeEstado;
    private float temporizadorSeguimiento;
    private LevelManager levelManager;

    // Vectores para manejar la posición actual y el objetivo del movimiento
    public Vector2 posicion;
    private Vector2 posicionObjetivo;

    // --- Gráficos ---
    private EnumMap<DronState.EstadoDron, Animation<TextureRegion>> animations;
    private TextureRegion frameActual;
    protected Texture spriteSheetAparecer;
    protected Texture spriteSheetSeguir;
    protected Texture spriteSheetDesaparecer;

    /**
     * Constructor del Dron_Tails.
     * Inicializa el estado, las animaciones y la posición del dron.
     *
     * @param id           El identificador del dron.
     * @param levelManager El gestor de niveles para manejar colisiones y generación de árboles.
     */
    public Dron_Tails(int id, LevelManager levelManager) {
        this.estado = new DronState(id);
        this.animations = new EnumMap<>(DronState.EstadoDron.class);
        this.levelManager = levelManager;

        this.posicion = new Vector2();
        this.posicionObjetivo = new Vector2();

        CargarSprites();
    }

    public void setObjetivo(Tails objetivo) {
        this.objetivo = objetivo;
    }


    /**
     * Carga los sprites y las animaciones del dron.
     * Utiliza TextureRegion para dividir las texturas en frames individuales.
     */
    private void CargarSprites() {
        spriteSheetAparecer = new Texture(Gdx.files.internal("Entidades/Dron/Landing.png"));
        TextureRegion[][] matrizAparecer = TextureRegion.split(spriteSheetAparecer, 72, 72);
        Animation<TextureRegion> animAparecer = new Animation<>(0.1f, matrizAparecer[0]);

        spriteSheetSeguir = new Texture(Gdx.files.internal("Entidades/Dron/Walk.png"));
        TextureRegion[][] matrizSeguir = TextureRegion.split(spriteSheetSeguir, 72, 72);
        Animation<TextureRegion> animSeguir = new Animation<>(0.15f, matrizSeguir[0]);

        spriteSheetDesaparecer = new Texture(Gdx.files.internal("Entidades/Dron/Death.png"));
        TextureRegion[][] matrizDesaparecer = TextureRegion.split(spriteSheetDesaparecer, 72, 72);
        Animation<TextureRegion> animDesaparecer = new Animation<>(0.1f, matrizDesaparecer[0]);

        animAparecer.setPlayMode(Animation.PlayMode.NORMAL);
        animSeguir.setPlayMode(Animation.PlayMode.LOOP);
        animDesaparecer.setPlayMode(Animation.PlayMode.NORMAL);

        animations.put(DronState.EstadoDron.APARECIENDO, animAparecer);
        animations.put(DronState.EstadoDron.SIGUIENDO, animSeguir);
        animations.put(DronState.EstadoDron.DESAPARECIENDO, animDesaparecer);
    }

    /**
     * Actualiza el estado del dron
     */
    public void update(float delta) {
        if (estado.estadoActual == DronState.EstadoDron.INACTIVO) {
            return;
        }

        tiempoDeEstado += delta;

        switch (estado.estadoActual) {
            case APARECIENDO:
                if (animations.get(DronState.EstadoDron.APARECIENDO).isAnimationFinished(tiempoDeEstado)) {
                    cambiarEstado(DronState.EstadoDron.SIGUIENDO);
                }
                break;

            case SIGUIENDO:
                temporizadorSeguimiento += delta;
                if (objetivo != null) {
                    //Se Calcula la posición donde el dron QUIERE estar
                    posicionObjetivo.set(objetivo.estado.x + OFFSET_X, objetivo.estado.y + OFFSET_Y);

                    //Se Usamos lerp para mover la posición ACTUAL hacia la del OBJETIVO suavemente
                    posicion.lerp(posicionObjetivo, LERP_FACTOR * delta);

                    //Sincronizamos la posición del vector con el estado (para la red, si es necesario)
                    estado.x = posicion.x;
                    estado.y = posicion.y;
                }
                if (temporizadorSeguimiento >= DURACION_SEGUIMIENTO) {
                    cambiarEstado(DronState.EstadoDron.DESAPARECIENDO);
                }
                break;

            case DESAPARECIENDO:
                if (animations.get(DronState.EstadoDron.DESAPARECIENDO).isAnimationFinished(tiempoDeEstado)) {
                    if (!isOnlineMode) {
                        Rectangle hitboxPruebaArbol = new Rectangle(this.posicion.x, this.posicion.y, 64, 64); // Ajusta el tamaño si es necesario

                        // Le preguntamos al LevelManager si ese lugar ya está ocupado.
                        if (levelManager != null && !levelManager.colisionaConMapa(hitboxPruebaArbol)) {


                            levelManager.generarArbol(posicion.x, posicion.y);
                            System.out.println("[DRON LOCAL] Árbol plantado localmente.");
                            LocalServer.decreaseContamination(15);


                            if (objetivo != null) {
                                objetivo.mostrarMensaje("¡Árbol sembrado!");
                            }

                        } else {
                            // SI ESTÁ OCUPADO: No generamos nada y mostramos el mensaje de error.
                            if (objetivo != null) {
                                objetivo.mostrarMensaje("Lugar no apto para sembrar");
                            }
                        }
                    }
                    // Finalmente, el dron se desactiva como siempre.
                    cambiarEstado(DronState.EstadoDron.INACTIVO);
                }
                break;
        }

        Animation<TextureRegion> currentAnim = animations.get(estado.estadoActual);
        if (currentAnim != null) {
            frameActual = currentAnim.getKeyFrame(tiempoDeEstado);
        }
    }

    /**
     * Dibuja el dron en la pantalla.
     * Solo dibuja si el dron no está inactivo y tiene un frame actual.
     *
     * @param batch El SpriteBatch utilizado para dibujar el dron.
     */
    public void draw(SpriteBatch batch) {
        if (estado.estadoActual != DronState.EstadoDron.INACTIVO && frameActual != null) {
            // Usamos la posición del vector, que es la que se actualiza suavemente
            batch.draw(frameActual, posicion.x, posicion.y, 72, 72);
        }
    }

    /**
     * Invoca al dron para que comience a sembrar un árbol.
     * Cambia su estado a APARECIENDO y establece la posición inicial.
     *
     * @param tails El objetivo Tails que invoca al dron.
     */
    public void invocar(Tails tails) {
        if (estado.estadoActual == DronState.EstadoDron.INACTIVO) {
            this.objetivo = tails;
            cambiarEstado(DronState.EstadoDron.APARECIENDO);
            this.objetivo.mostrarMensaje("Sembrando árbol...");

            // Establecemos la posición inicial del dron y del estado.
            float startX = objetivo.estado.x + OFFSET_X;
            float startY = objetivo.estado.y + OFFSET_Y;
            this.posicion.set(startX, startY);
            this.estado.x = startX;
            this.estado.y = startY;
        }
    }

    /**
     * Cambia el estado del dron a uno nuevo.
     * Reinicia el temporizador de estado y, si es necesario, el temporizador de seguimiento.
     *
     * @param nuevoEstado El nuevo estado al que se cambiará el dron.
     */
    void cambiarEstado(DronState.EstadoDron nuevoEstado) {
        estado.estadoActual = nuevoEstado;
        tiempoDeEstado = 0f;
        if (nuevoEstado == DronState.EstadoDron.SIGUIENDO) {
            temporizadorSeguimiento = 0f;
        }
    }

    /**
     * Libera los recursos utilizados por el dron.
     * Destruye las texturas de los sprites.
     */
    public void dispose() {
        if (spriteSheetAparecer != null) {
            spriteSheetAparecer.dispose();
        }
        if (spriteSheetSeguir != null) {
            spriteSheetSeguir.dispose();
        }
        if (spriteSheetDesaparecer != null) {
            spriteSheetDesaparecer.dispose();
        }
    }
}
