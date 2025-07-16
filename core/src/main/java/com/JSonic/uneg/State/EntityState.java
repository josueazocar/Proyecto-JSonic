package com.JSonic.uneg.State;

public abstract class EntityState {
    public int id;
    public float x;
    public float y;
    public int vida;

    public EntityState() {
    }

    public EntityState(int id, float x, float y, int vida) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.vida = vida;
    }

}
