package com.JSonic.uneg.EntidadesVisuales;

import com.JSonic.uneg.State.EnemigoState;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.EnumMap;

public class Enemy extends Entity {
    public EnemigoState estado;
    protected Texture spriteSheet;
    protected TextureRegion frameActual;
    protected float tiempoXFrame;

    protected EnumMap<EnemigoState.EstadoEnemigo, Animation<TextureRegion>> animations;
    protected TextureRegion[] frameIdleRight;
    protected TextureRegion[] frameIdleLeft;
    protected TextureRegion[] frameRunRight;
    protected TextureRegion[] frameRunLeft;
    protected TextureRegion[] frameHitRight;
    protected TextureRegion[] frameHitLeft;


    @Override
    public int getVida() {
        return estado.vida;
    }

    @Override
    public void setVida(int vida) {
        this.estado.vida = vida;
    }

    @Override
    protected void setDefaultValues() {

    }

    @Override
    protected void CargarSprites() {

    }

    @Override
    protected void KeyHandler() {

    }

    @Override
    public void draw(SpriteBatch batch) {

    }

    @Override
    public void update(float deltaTime) {

    }
}
