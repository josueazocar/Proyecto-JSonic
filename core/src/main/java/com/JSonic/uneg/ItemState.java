// Archivo: src/com/JSonic/uneg/ItemState.java
package com.JSonic.uneg;

// Esta clase es Java puro, sin LibGDX.
// Es ideal para enviar por red al servidor y a otros clientes.

public class ItemState {

    // El enum puede estar fuera o dentro de la clase.
    // Ponerlo aquí es una buena práctica si solo se usa con ItemState.
    public enum ItemType {
        ANILLO,
        BASURA,
        PIEZA_PLASTICO
    }

    public int id;
    public float x;
    public float y;
    public ItemType tipo;

    // Constructor vacío es útil para la serialización en red (KryoNet).
    public ItemState() {}

    // Constructor para facilitar la creación de ítems.
    public ItemState(int id, float x, float y, ItemType tipo) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.tipo = tipo;
    }
}
