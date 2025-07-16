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
    protected static final float COOLDOWN_ENTRE_GOLPES = 0.5f; // Medio segundo entre golpes
    protected float tiempoDesdeUltimoGolpe = 0f;

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

    public void marcarComoGolpeado() {
        this.tiempoDesdeUltimoGolpe = COOLDOWN_ENTRE_GOLPES;
    }

    public boolean haSidoGolpeadoRecientemente() {
        return tiempoDesdeUltimoGolpe > 0;
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
