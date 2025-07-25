package com.JSonic.uneg.State;

/**
 * Representa el estado de un enemigo en el juego.
 * Mantiene tipo, dirección, animación y control de tiempos de ataque.
 */
public class EnemigoState extends EntityState {
    public EnemigoType tipo;
    public boolean mirandoDerecha;
    public EstadoEnemigo estadoAnimacion;
    public float tiempoEnEstado = 0f;
    public float tiempoDeEnfriamientoAtaque = 0f;
    private transient float tiempoHastaProximoAtaque = 0f;
    private static final float COOLDOWN_ATAQUE_ENEMIGO = 1.0f; // 1 ataque por segundo

    public enum EnemigoType {
        ROBOT,
        // Puedes añadir más tipos de enemigos aquí
        ROBOTNIK,

    }

    public enum EstadoEnemigo {
        IDLE_RIGHT,
        IDLE_LEFT,
        RUN_RIGHT,
        RUN_LEFT,
        HIT_RIGHT,
        HIT_LEFT,
        POST_ATAQUE, // Estado después de un ataque

    }

    /**
     * Constructor por defecto para deserialización.
     */
    public EnemigoState() {
    }

    /**
     * Crea un estado de enemigo con parámetros iniciales.
     *
     * @param id    identificador único del enemigo
     * @param x     posición X inicial
     * @param y     posición Y inicial
     * @param vida  puntos de vida iniciales
     * @param tipo  tipo de enemigo
     */
    public EnemigoState(int id, float x, float y, int vida, EnemigoType tipo) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.vida = vida;
        this.tipo = tipo;
        this.mirandoDerecha = true; // Por defecto mirará a la derecha
        this.estadoAnimacion = EstadoEnemigo.IDLE_RIGHT; // Estado inicial por defecto
        this.tiempoEnEstado = 0f;
        this.tiempoDeEnfriamientoAtaque = 0f;
    }

    /**
     * Actualiza el tiempo restante hasta el próximo ataque.
     *
     * @param deltaTime tiempo transcurrido desde el último frame (segundos)
     */
    public void actualizar(float deltaTime) {
        if (tiempoHastaProximoAtaque > 0) {
            tiempoHastaProximoAtaque -= deltaTime;
        }
    }

    /**
     * Indica si el enemigo puede realizar un ataque.
     *
     * @return true si ha pasado el cooldown, false en caso contrario
     */
    public boolean puedeAtacar() {
        return tiempoHastaProximoAtaque <= 0;
    }

    /**
     * Reinicia el temporizador de cooldown de ataque al valor por defecto.
     */
    public void reiniciarCooldownAtaque() {
        this.tiempoHastaProximoAtaque = COOLDOWN_ATAQUE_ENEMIGO;
    }
}
