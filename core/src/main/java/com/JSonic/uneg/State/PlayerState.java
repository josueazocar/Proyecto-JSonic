package com.JSonic.uneg.State;

import com.JSonic.uneg.EntidadesVisuales.Player;

/**
 * Clase que representa el estado de un jugador en el juego.
 * Hereda de EntityState para incluir propiedades comunes a todas las entidades.
 * Incluye el tipo de personaje, estado de animación y nombre del jugador.
 */
public class PlayerState extends EntityState {

        public CharacterType characterType;
        public Player.EstadoPlayer estadoAnimacion;
        public String nombreJugador;
        public boolean isSuper = false;


 /**
 * Enumeración que define los tipos de personajes disponibles en el juego.
 * Cada tipo tiene un nombre descriptivo que se utiliza para identificar el personaje.
 */
    public enum CharacterType {
        SONIC, TAILS, KNUCKLES
    }
     public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /** * Constructor por defecto para deserialización.
     */
        public PlayerState() {
            this.id = -1; // Valor por defecto para indicar que no está inicializado
            this.x = 0f;
            this.y = 0f;
            this.estadoAnimacion = Player.EstadoPlayer.IDLE_RIGHT; // Estado inicial
            this.vida = 100;
        }
}
