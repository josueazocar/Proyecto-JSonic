package com.JSonic.uneg;



public class AnimalState  {
    public int id;
    public float x, y;
    public String texturaPath; // Ruta a la textura para que el cliente sepa qué dibujar
    public boolean estaVivo = true;

    // Constructor vacío requerido para la serialización de red
    public AnimalState() {}

    public AnimalState(int id, float x, float y, String texturaPath) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.texturaPath = texturaPath;
    }
}
