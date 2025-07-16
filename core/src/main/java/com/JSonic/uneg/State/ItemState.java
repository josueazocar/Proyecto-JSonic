// Archivo: src/com/JSonic/uneg/ItemState.java
package com.JSonic.uneg.State;

// Esta clase es Java puro, sin LibGDX.
// Es ideal para enviar por red al servidor y a otros clientes.

public class ItemState extends EntityState {

    // El enum puede estar fuera o dentro de la clase.
    // Ponerlo aquí es una buena práctica si solo se usa con ItemState.
    public enum ItemType {
        ANILLO,
        BASURA,
        PIEZA_PLASTICO,
        TELETRANSPORTE
    }

    public ItemType tipo;
    //campos de teletransporte
    public float destinoX;
    public float destinoY;
    public String destinoMapa;

    // Constructor vacío es útil para la serialización en red (KryoNet).
    public ItemState() {}


    // Constructor para facilitar la creación de ítems.
    public ItemState(int id, float x, float y, ItemType tipo) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.tipo = tipo;
    }

    //constructor para portales
    public ItemState(int id, float x, float y, ItemType tipo, float destinoX, float destinoY, String destinoMapa) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.tipo = tipo;
        this.destinoX = destinoX;
        this.destinoY = destinoY;
        this.destinoMapa = destinoMapa;
    }

    public ItemState(float x, float y, ItemType tipo, float destinoX, float destinoY, String destinoMapa) {
        this.x = x;
        this.y = y;
        this.tipo = tipo;
        this.destinoX = destinoX;
        this.destinoY = destinoY;
        this.destinoMapa = destinoMapa;
    }
}
