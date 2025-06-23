package com.JSonic.uneg;

public class PlayerState {
    // Esta clase NO tiene importaciones de LibGDX. Es Java puro.
        public int id;
        public float x;
        public float y;

        public Player.EstadoPlayer estadoAnimacion;

        public PlayerState() {
            this.id = -1; // Valor por defecto para indicar que no está inicializado
            this.x = 0f;
            this.y = 0f;
            this.estadoAnimacion = Player.EstadoPlayer.IDLE_RIGHT; // Estado inicial
        }
}
