package com.JSonic.uneg.State;

public class EnemigoState extends EntityState {
    public EnemigoType tipo;
    public boolean mirandoDerecha; // Para la dirección del sprite y el movimiento
    public EstadoEnemigo estadoAnimacion; // Nuevo: Para controlar la animación del enemigo
    public float tiempoEnEstado = 0f;
    public float tiempoDeEnfriamientoAtaque = 0f;
    private transient float tiempoHastaProximoAtaque = 0f;
    private static final float COOLDOWN_ATAQUE_ENEMIGO = 1.0f; // 1 ataque por segundo

    public enum EnemigoType {
        BOMBA,
        ROBOT,
        // Puedes añadir más tipos de enemigos aquí
        ROBOTNIK,

    }

    public enum EstadoEnemigo {
        IDLE_RIGHT,
        IDLE_LEFT,
        UP,
        DOWN,
        RUN_RIGHT,
        RUN_LEFT,
        HIT_RIGHT,
        HIT_LEFT,
        POST_ATAQUE, // Estado después de un ataque
        // Puedes añadir más estados específicos para enemigos si los necesitas
    }

    public EnemigoState() {
    }

    // Constructor para facilitar la inicialización
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

    public void actualizar(float deltaTime) {
        if (tiempoHastaProximoAtaque > 0) {
            tiempoHastaProximoAtaque -= deltaTime;
        }
    }

    public boolean puedeAtacar() {
        return tiempoHastaProximoAtaque <= 0;
    }

    public void reiniciarCooldownAtaque() {
        this.tiempoHastaProximoAtaque = COOLDOWN_ATAQUE_ENEMIGO;
    }
}
