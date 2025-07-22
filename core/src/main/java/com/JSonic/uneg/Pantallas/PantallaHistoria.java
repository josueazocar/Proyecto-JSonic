// Archivo: src/com/JSonic/uneg/Pantallas/PantallaHistoria.java

package com.JSonic.uneg.Pantallas;

import com.JSonic.uneg.JSonicJuego;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;

public class PantallaHistoria extends PantallaBase {

    private final JSonicJuego juegoApp;
    private Array<Texture> imagenes; // Usamos Array de LibGDX, es eficiente.
    private Image actorImagen;       // Un actor para mostrar la imagen actual en el Stage.

    private int indiceActual = 0;
    private float temporizador = 0f;

    // --- PUEDES AJUSTAR ESTOS VALORES ---
    private static final float TIEMPO_POR_IMAGEN = 4.0f; // 4 segundos por imagen.
    private static final int CANTIDAD_IMAGENES = 5;      // Cambia esto al número total de imágenes que tengas.
    // ------------------------------------

    public PantallaHistoria(JSonicJuego juegoApp) {
        super(); // Llama al constructor de PantallaBase, que a su vez llama a inicializar().
        this.juegoApp = juegoApp;
    }

    @Override
    public void inicializar() {
        // 1. Cargar todas las imágenes de la historia en orden.
        imagenes = new Array<>();
        for (int i = 1; i <= CANTIDAD_IMAGENES; i++) {
            // Asumimos que las imágenes se llaman historia_01.png, historia_02.png, etc.
            // Asegúrate de que el nombre y la ruta sean correctos.
            String nombreArchivo = "Historia/historia_0" + i + ".png";
            imagenes.add(new Texture(Gdx.files.internal(nombreArchivo)));
        }

        // 2. Crear el actor Image, que mostraremos en el Stage.
        // Empezamos con la primera imagen.
        actorImagen = new Image(imagenes.get(indiceActual));
        actorImagen.setSize(mainStage.getWidth(), mainStage.getHeight()); // Que ocupe toda la pantalla.
        mainStage.addActor(actorImagen);
    }

    @Override
    public void actualizar(float delta) {
        // Permitir al jugador saltar la cinemática con cualquier tecla o clic.
        if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY) || Gdx.input.justTouched()) {
            pasarAlMenu();
            return; // Salimos para evitar procesar el resto de la lógica.
        }

        temporizador += delta;

        // 3. Comprobar si ha pasado el tiempo para cambiar de imagen.
        if (temporizador >= TIEMPO_POR_IMAGEN) {
            avanzarDiapositiva();
        }
    }

    private void avanzarDiapositiva() {
        temporizador = 0f; // Reiniciamos el temporizador.
        indiceActual++;    // Pasamos a la siguiente imagen.

        // 4. Comprobar si la historia ha terminado.
        if (indiceActual >= imagenes.size) {
            pasarAlMenu();
        } else {
            // Si no ha terminado, actualizamos la imagen que se muestra.
            actorImagen.setDrawable(new TextureRegionDrawable(imagenes.get(indiceActual)));
        }
    }

    private void pasarAlMenu() {
        // Cambiamos a la pantalla del menú, pasándole 'true' para que no muestre
        // de nuevo el "Presione cualquier tecla".
        juegoApp.setPantallaActiva(new PantallaMenu(juegoApp, true));
    }

    @Override
    public void dispose() {
        super.dispose();
        // ¡Muy importante! Liberar la memoria de todas las texturas cargadas.
        for (Texture tex : imagenes) {
            tex.dispose();
        }
    }
}
