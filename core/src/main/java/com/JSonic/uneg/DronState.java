package com.JSonic.uneg;

// Un enum para definir los estados posibles del Dron.
// Es público para que otras clases como Dron_Tails puedan usarlo.
public class DronState {

    public int id;
    public float x;
    public float y;

    // El estado actual del dron, clave para la lógica y animación.
    public EstadoDron estadoActual;

    public DronState(int id) {
        this.id = id;
        this.x = 0;
        this.y = 0;
        this.estadoActual = EstadoDron.INACTIVO;
    }
    enum EstadoDron {
        INACTIVO,
        SIGUIENDO,
        DESAPARECIENDO,
        APARECIENDO
    }
}
