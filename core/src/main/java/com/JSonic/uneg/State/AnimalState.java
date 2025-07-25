package com.JSonic.uneg.State;

/**
 * Clase que representa el estado de un animal en el juego.
 * Hereda de EntityState para incluir propiedades comunes a todas las entidades.
 * Incluye la ruta de la textura y un indicador de si el animal está vivo.
 */
public class AnimalState extends EntityState {

    public String texturaPath; // Ruta a la textura para que el cliente sepa qué dibujar
    public boolean estaVivo = true;

    // Constructor vacío requerido para la serialización de red
    public AnimalState() {}

    /**
     * Constructor para crear un estado de animal con ID, posición y textura.
     *
     * @param id          Identificador único del animal.
     * @param x           Posición X del animal.
     * @param y           Posición Y del animal.
     * @param texturaPath Ruta a la textura del animal.
     */
    public AnimalState(int id, float x, float y, String texturaPath) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.texturaPath = texturaPath;
    }
}
