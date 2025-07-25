package com.JSonic.uneg.State;

/** * Clase abstracta que representa el estado de una entidad en el juego.
 * Contiene propiedades comunes como ID, posición (x, y) y vida.
 * Esta clase es ideal para enviar por red al servidor y a otros clientes.
 */
public abstract class EntityState {
    public int id;
    public float x;
    public float y;
    public int vida;

    public EntityState() {
    }

    /**
     * Constructor para crear un estado de entidad con ID, posición y vida.
     *
     * @param id   Identificador único de la entidad.
     * @param x    Posición X de la entidad.
     * @param y    Posición Y de la entidad.
     * @param vida Puntos de vida de la entidad.
     */
    public EntityState(int id, float x, float y, int vida) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.vida = vida;
    }

}
