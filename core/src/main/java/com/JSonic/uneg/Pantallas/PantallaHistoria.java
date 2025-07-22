// Archivo: src/com/JSonic/uneg/Pantallas/PantallaHistoria.java
package com.JSonic.uneg.Pantallas;

import com.JSonic.uneg.JSonicJuego;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.JSonic.uneg.SoundManager;

public class PantallaHistoria extends PantallaBase {

    private final JSonicJuego juegoApp;
    private final SoundManager soundManager;
    private Array<Texture> imagenes;
    private Image actorImagen;

    private int indiceActual = 0;

    private enum EstadoTransicion { FADE_IN, MOSTRANDO, FADE_OUT }
    private EstadoTransicion estadoActual = EstadoTransicion.FADE_IN;
    private float temporizadorEstado = 0f;

    // --- AJUSTA ESTOS VALORES A TU GUSTO ---
    private static final float TIEMPO_POR_IMAGEN = 3.0f; // 3 segundos visible
    private static final float TIEMPO_FADE = 1.0f;       // 1 segundo para la transición
    private static final int CANTIDAD_IMAGENES = 5;      // El número total de imágenes en tu carpeta /Historia
    // ------------------------------------

    public PantallaHistoria(JSonicJuego juegoApp, SoundManager soundManager) {
        super();
        this.juegoApp = juegoApp;
        this.soundManager = soundManager;

        inicializar();
    }

    @Override
    public void inicializar() {
        imagenes = new Array<>();
        for (int i = 1; i <= CANTIDAD_IMAGENES; i++) {
            String nombreArchivo = "Historia/historia_0" + i + ".png";
            imagenes.add(new Texture(Gdx.files.internal(nombreArchivo)));
        }

        String rutaMusica = "SoundsBackground/MiniHistoriaMusica.mp3";
        soundManager.playBackgroundMusic(rutaMusica, 0.7f, true);

        actorImagen = new Image(imagenes.get(indiceActual));
        actorImagen.setSize(mainStage.getWidth(), mainStage.getHeight());
        actorImagen.getColor().a = 0f; // Empezar transparente para el primer FADE_IN
        mainStage.addActor(actorImagen);


    }

    @Override
    public void actualizar(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY) || Gdx.input.justTouched()) {
            pasarAlMenu();
            return;
        }

        temporizadorEstado += delta;

        switch (estadoActual) {
            case FADE_IN:
                float progresoIn = temporizadorEstado / TIEMPO_FADE;
                actorImagen.getColor().a = Math.min(progresoIn, 1f);
                if (temporizadorEstado >= TIEMPO_FADE) {
                    estadoActual = EstadoTransicion.MOSTRANDO;
                    temporizadorEstado = 0f;
                }
                break;

            case MOSTRANDO:
                if (temporizadorEstado >= TIEMPO_POR_IMAGEN) {
                    estadoActual = EstadoTransicion.FADE_OUT;
                    temporizadorEstado = 0f;
                }
                break;

            case FADE_OUT:
                float progresoOut = 1f - (temporizadorEstado / TIEMPO_FADE);
                actorImagen.getColor().a = Math.max(progresoOut, 0f);
                if (temporizadorEstado >= TIEMPO_FADE) {
                    avanzarDiapositiva();
                }
                break;
        }
    }

    private void avanzarDiapositiva() {
        indiceActual++;
        if (indiceActual >= imagenes.size) {
            pasarAlMenu();
        } else {
            actorImagen.setDrawable(new TextureRegionDrawable(imagenes.get(indiceActual)));
            estadoActual = EstadoTransicion.FADE_IN;
            temporizadorEstado = 0f;
        }
    }

    private void pasarAlMenu() {
        soundManager.stopBackgroundMusic();
        juegoApp.setPantallaActiva(new PantallaMenu(juegoApp, true));
    }

    @Override
    public void dispose() {
        super.dispose();
        for (Texture tex : imagenes) {
            tex.dispose();
        }
    }
}
