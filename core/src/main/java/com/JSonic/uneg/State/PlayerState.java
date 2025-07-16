package com.JSonic.uneg.State;

import com.JSonic.uneg.EntidadesVisuales.Player;

public class PlayerState extends EntityState {
    // Esta clase NO tiene importaciones de LibGDX. Es Java puro.
        public CharacterType characterType;
        public Player.EstadoPlayer estadoAnimacion;

    public enum CharacterType {
        SONIC, TAILS, KNUCKLES
    }
     public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

        public PlayerState() {
            this.id = -1; // Valor por defecto para indicar que no está inicializado
            this.x = 0f;
            this.y = 0f;
            this.estadoAnimacion = Player.EstadoPlayer.IDLE_RIGHT; // Estado inicial
            this.vida = 100;
        }
}
