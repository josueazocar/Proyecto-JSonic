package com.JSonic.uneg.State;

/**
 * Clase que representa el estado de un ítem en el juego.
 * Hereda de EntityState para incluir propiedades comunes a todas las entidades.
 * Incluye el tipo de ítem y, en caso de ser un teletransporte, sus coordenadas de destino y mapa.
 */
public class ItemState extends EntityState {

    /**
     * Enumeración que define los tipos de ítems disponibles en el juego.
     * Cada tipo tiene un nombre descriptivo que se utiliza para identificar el ítem.
     */
    public enum ItemType {
        ANILLO,
        BASURA,
        PIEZA_PLASTICO,
        TELETRANSPORTE,
        ESMERALDA
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

   /**
     * Constructor para crear un estado de ítem con destino de teletransporte.
     *
     * @param id          Identificador único del ítem.
     * @param x           Posición X del ítem.
     * @param y           Posición Y del ítem.
     * @param tipo        Tipo de ítem (Anillo, Basura, etc.).
     * @param destinoX    Coordenada X del destino del teletransporte.
     * @param destinoY    Coordenada Y del destino del teletransporte.
     * @param destinoMapa Nombre del mapa al que se teletransporta.
     */
    public ItemState(int id, float x, float y, ItemType tipo, float destinoX, float destinoY, String destinoMapa) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.tipo = tipo;
        this.destinoX = destinoX;
        this.destinoY = destinoY;
        this.destinoMapa = destinoMapa;
    }

    /**
     * Constructor para crear un estado de ítem con destino de teletransporte sin ID.
     *
     * @param x           Posición X del ítem.
     * @param y           Posición Y del ítem.
     * @param tipo        Tipo de ítem (Anillo, Basura, etc.).
     * @param destinoX    Coordenada X del destino del teletransporte.
     * @param destinoY    Coordenada Y del destino del teletransporte.
     * @param destinoMapa Nombre del mapa al que se teletransporta.
     */
    public ItemState(float x, float y, ItemType tipo, float destinoX, float destinoY, String destinoMapa) {
        this.x = x;
        this.y = y;
        this.tipo = tipo;
        this.destinoX = destinoX;
        this.destinoY = destinoY;
        this.destinoMapa = destinoMapa;
    }
}
