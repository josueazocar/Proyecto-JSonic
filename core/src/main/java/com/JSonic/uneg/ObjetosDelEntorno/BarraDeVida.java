package com.JSonic.uneg.ObjetosDelEntorno;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Clase que representa la barra de vida en la interfaz de usuario.
 * Muestra el nivel de vida actual y aplica un efecto de flash al recibir daño.
 */
public class BarraDeVida extends Table {
    private Image imagenVida;
    private TextureAtlas vidaAtlas;

    private float tiempoFlash = 0f; // El temporizador para el efecto de daño
    private final float DURACION_FLASH = 0.3f; // Duración del flash en segundos

    /**
     * Crea una nueva barra de vida usando el atlas de texturas proporcionado.
     * @param atlas TextureAtlas que contiene las regiones de vida.
     */
    public BarraDeVida(TextureAtlas atlas) {
        super();
        this.vidaAtlas = atlas;
        this.imagenVida = new Image(vidaAtlas.findRegion("vida100"));
        this.add(imagenVida);
    }

    /**
     * Se llama cada frame para actualizar lógica interna, incluyendo el temporizador de flash.
     * @param delta tiempo transcurrido desde el último frame en segundos.
     */
    @Override
    public void act(float delta) {
        super.act(delta); // Llama al método del padre (importante)
        if (tiempoFlash > 0) {
            tiempoFlash -= delta;
        }
    }

    /**
     * Inicia el efecto visual de pérdida de vida (flash) durante una duración fija.
     */
    public void mostrarPerdidaDeVida() {
        this.tiempoFlash = DURACION_FLASH;
    }

    /**
     * Actualiza la imagen de la barra de vida según los valores de vida actual y máxima.
     * Aplica el efecto de flash si está activo.
     * @param vidaActual valor actual de vida.
     * @param vidaMaxima valor máximo de vida.
     */
    public void actualizar(float vidaActual, float vidaMaxima) {
        System.out.println("[DEBUG-HUD] El método 'actualizar' recibió vida: " + vidaActual);

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

    /**
     * Calcula la región de atlas correspondiente al porcentaje de vida.
     * @param vidaActual valor actual de vida.
     * @param vidaMaxima valor máximo de vida.
     * @return nombre de la región de vida.
     */
    private String getNombreRegionVida(float vidaActual, float vidaMaxima) {
        if (vidaMaxima <= 0) return "vida0";
        float porcentaje = (vidaActual / vidaMaxima) * 100;

        String regionResultante;
        if (porcentaje > 75) {
            regionResultante = "vida100";
        } else if (porcentaje > 50) {
            regionResultante = "vida75";
        } else if (porcentaje > 25) {
            regionResultante = "vida50";
        } else if (porcentaje > 0) {
            regionResultante = "vida25";
        } else {
            regionResultante = "vida0";
        }

        System.out.println("[DEBUG-HUD] vidaActual=" + vidaActual + " -> porcentaje=" + porcentaje + " -> regionResultante='" + regionResultante + "'");
        return regionResultante;
    }
}
