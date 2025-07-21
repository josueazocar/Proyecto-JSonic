package com.JSonic.uneg.ObjetosDelEntorno;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class BarraDeVida extends Table{
    private Image imagenVida;
    private TextureAtlas vidaAtlas;

    public BarraDeVida(TextureAtlas atlas) {
        super(); // Llama al constructor de la clase Table (importante)
        this.vidaAtlas = atlas;

        // Al empezar, siempre mostramos la vida al 100%
        this.imagenVida = new Image(vidaAtlas.findRegion("vida100"));

        // Como nuestra clase es una Tabla, podemos añadirle la imagen directamente.
        this.add(imagenVida);
    }

    public void actualizar(float vidaActual, float vidaMaxima) {
        String nombreRegion = getNombreRegionVida(vidaActual, vidaMaxima);
        ((TextureRegionDrawable) imagenVida.getDrawable()).setRegion(vidaAtlas.findRegion(nombreRegion));
    }

    private String getNombreRegionVida(float vidaActual, float vidaMaxima) {
        if (vidaMaxima <= 0) return "vida0";
        float porcentaje = (vidaActual / vidaMaxima) * 100;

        if (porcentaje > 75) {
            return "vida100";
        } else if (porcentaje > 50) {
            return "vida75";
        } else if (porcentaje > 25) {
            return "vida50";
        } else if (porcentaje > 0) {
            return "vida25";
        } else {
            return "vida0";
        }
    }

}
