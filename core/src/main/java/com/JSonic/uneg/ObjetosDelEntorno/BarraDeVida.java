// Archivo: src/com/JSonic/uneg/ObjetosDelEntorno/BarraDeVida.java
package com.JSonic.uneg.ObjetosDelEntorno;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class BarraDeVida extends Table {
    private Image imagenVida;
    private TextureAtlas vidaAtlas;

    private float tiempoFlash = 0f; // El temporizador para el efecto de daño
    private final float DURACION_FLASH = 0.3f; // Duración del flash en segundos. ¡Puedes ajustar esto!

    public BarraDeVida(TextureAtlas atlas) {
        super();
        this.vidaAtlas = atlas;
        this.imagenVida = new Image(vidaAtlas.findRegion("vida100"));
        this.add(imagenVida);
    }

    /**
     * El método act se llama automáticamente en cada fotograma por el Stage.
     * Lo usamos para manejar la lógica del temporizador.
     */
    @Override
    public void act(float delta) {
        super.act(delta); // Llama al método del padre (importante)
        if (tiempoFlash > 0) {
            tiempoFlash -= delta;
        }
    }

    /**
     * Este es el método que llamaremos desde fuera para activar el efecto visual.
     */
    public void mostrarPerdidaDeVida() {
        this.tiempoFlash = DURACION_FLASH;
    }

    /**
     * Actualiza la imagen de la barra de vida. Ahora también considera
     * si el efecto de "daño" está activo.
     */
    public void actualizar(float vidaActual, float vidaMaxima) {
        String nombreRegionBase = getNombreRegionVida(vidaActual, vidaMaxima);
        String nombreRegionFinal = nombreRegionBase;

        // Si el temporizador de flash está activo, buscamos la imagen de "pérdida"
        if (tiempoFlash > 0) {
            // Comprobamos si existe una región de pérdida para este nivel de vida
            if (vidaAtlas.findRegion(nombreRegionBase + "_perdida") != null) {
                nombreRegionFinal += "_perdida";
            }
        }

        // Actualizamos la imagen que se muestra
        ((TextureRegionDrawable) imagenVida.getDrawable()).setRegion(vidaAtlas.findRegion(nombreRegionFinal));
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
