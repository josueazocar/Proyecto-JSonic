package com.JSonic.uneg.ObjetosDelEntorno;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class BarraDeVidaVillanos {
    private ShapeRenderer shapeRenderer;
    private float vidaActual;
    private float vidaMaxima;
    private float anchoBarra;
    private float altoBarra;
    private float x, y; // La posición de la barra en el mundo del juego

    public BarraDeVidaVillanos(float vidaMaxima, float ancho, float alto) {
        this.shapeRenderer = new ShapeRenderer();
        this.vidaMaxima = vidaMaxima;
        this.vidaActual = vidaMaxima; // Empieza con la vida al máximo
        this.anchoBarra = ancho;
        this.altoBarra = alto;
    }

    /**
     * Actualiza el estado y la posición de la barra de vida.
     * Este método se llama en cada fotograma desde PantallaDeJuego.
     */
    public void actualizar(float nuevaVida, float nuevaX, float nuevaY) {
        this.vidaActual = nuevaVida;
        this.x = nuevaX;
        this.y = nuevaY;
    }

    /**
     * Dibuja la barra de vida en la pantalla.
     * @param camara La cámara del juego, para que la barra se dibuje en el sistema de coordenadas correcto.
     */
    public void dibujar(OrthographicCamera camara) {
        // Le decimos al ShapeRenderer que use la misma vista que la cámara del juego.
        shapeRenderer.setProjectionMatrix(camara.combined);

        // --- Dibuja el fondo de la barra (la vida que falta) ---
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.FIREBRICK); // Un rojo oscuro
        shapeRenderer.rect(x, y, anchoBarra, altoBarra);
        shapeRenderer.end();

        // --- Dibuja la vida actual encima ---
        float anchoVidaActual = (vidaActual / vidaMaxima) * anchoBarra;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.GREEN); // Verde brillante
        shapeRenderer.rect(x, y, anchoVidaActual, altoBarra);
        shapeRenderer.end();

        // --- Opcional: Dibuja un borde para que se vea más nítido ---
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE); // Borde blanco
        shapeRenderer.rect(x, y, anchoBarra, altoBarra);
        shapeRenderer.end();
    }

    /**
     * Libera la memoria del ShapeRenderer. ¡Muy importante!
     */
    public void dispose() {
        shapeRenderer.dispose();
    }
}
