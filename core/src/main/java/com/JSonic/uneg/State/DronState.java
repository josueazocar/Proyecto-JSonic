package com.JSonic.uneg.State;

// Un enum para definir los estados posibles del Dron.
// Es público para que otras clases como Dron_Tails puedan usarlo.
public class DronState extends EntityState {

    public int ownerId; // Para identificar al jugador dueño del dron.
    public float temporizador; // El servidor usará esto para la cuenta regresiva.


    // El estado actual del dron, clave para la lógica y animación.
    public EstadoDron estadoActual;

    public DronState(int id) {
        this.id = id;
        this.x = 0;
        this.y = 0;
        this.estadoActual = EstadoDron.INACTIVO;
    }

    public DronState(int id, int ownerId, float startX, float startY) {
        this.id = id;
        this.ownerId = ownerId;
        this.x = startX;
        this.y = startY;
        this.estadoActual = EstadoDron.APARECIENDO; // Inicia en el estado de aparición.
        this.temporizador = 15.0f; // La duración del seguimiento en segundos.
    }

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
