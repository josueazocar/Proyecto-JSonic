package com.JSonic.uneg.State;

/**
 * Representa el estado de un dron en el juego.
 * Mantiene identificadores, posición, estado de animación y tiempo de vida.
 */
public class DronState extends EntityState {

    public int ownerId; // Para identificar al jugador dueño del dron.
    public float temporizador; // El servidor usará esto para la cuenta regresiva.


    // El estado actual del dron, clave para la lógica y animación.
    public EstadoDron estadoActual;

    /**
     * Crea un estado de dron con valores iniciales por defecto.
     * @param id identificador único del dron.
     */
    public DronState(int id) {
        this.id = id;
        this.x = 0;
        this.y = 0;
        this.estadoActual = EstadoDron.INACTIVO;
    }

    /**
     * Crea un estado de dron inicializado en posición y propietario específicos.
     * Comienza en estado APARECIENDO con temporizador de seguimiento.
     * @param id identificador único del dron.
     * @param ownerId identificador del jugador dueño del dron.
     * @param startX coordenada X de inicio.
     * @param startY coordenada Y de inicio.
     */
    public DronState(int id, int ownerId, float startX, float startY) {
        this.id = id;
        this.ownerId = ownerId;
        this.x = startX;
        this.y = startY;
        this.estadoActual = EstadoDron.APARECIENDO; // Inicia en el estado de aparición.
        this.temporizador = 15.0f; // La duración del seguimiento en segundos.
    }

    /**
     * Actualiza el temporizador del dron cuando está apareciendo o siguiendo.
     * @param delta tiempo transcurrido desde la última llamada (en segundos).
     * @return true si el temporizador ha expirado (<= 0), false en caso contrario.
     */
    public boolean update(float delta) {
        if (estadoActual == EstadoDron.APARECIENDO || estadoActual == EstadoDron.SIGUIENDO) {
            temporizador -= delta;
        }
        return temporizador <= 0;
    }

    public enum EstadoDron {
        INACTIVO,
        SIGUIENDO,
        DESAPARECIENDO,
        APARECIENDO
    }
}
