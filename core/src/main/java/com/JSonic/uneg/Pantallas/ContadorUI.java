package com.JSonic.uneg.Pantallas;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;

/**
 * Clase que representa un contador visual que muestra números en la interfaz de usuario.
 * Permite mostrar un número dinámico con dígitos representados por imágenes.
 * Los dígitos se cargan desde una textura que contiene los números del 0 al 9.
 */
public class ContadorUI implements Disposable {

    private Texture digitosTextura;
    private TextureRegion[] digitosRegiones;
    private Table tabla;

    /**
     * Crea un contador visual con un número de dígitos dinámico.
     *
     * @param rutaTextura Ruta al archivo de imagen con los dígitos (0-9).
     */
    public ContadorUI(String rutaTextura) {
        this.digitosTextura = new Texture(rutaTextura);
        this.tabla = new Table();

        // Dividir la hoja de sprites en regiones para cada dígito (0-9)
        int frameCount = 10; // 10 digitos de 0 a 9
        int frameWidth = digitosTextura.getWidth() / frameCount;
        int frameHeight = digitosTextura.getHeight();
        TextureRegion[][] tmp = TextureRegion.split(digitosTextura, frameWidth, frameHeight);

        digitosRegiones = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            digitosRegiones[i] = tmp[0][i];
        }

        // Inicializar con el valor 0
        setValor(0);
    }

    /**
     * Actualiza el valor mostrado en el contador. El número de dígitos se ajusta
     * dinámicamente.
     *
     * @param valor El nuevo número a mostrar.
     */
    public void setValor(int valor) {
        String valorComoString = String.valueOf(valor);

        // Si el número de dígitos es diferente, reconstruir la tabla
        if (valorComoString.length() != tabla.getChildren().size) {
            tabla.clear();
            for (int i = 0; i < valorComoString.length(); i++) {
                char c = valorComoString.charAt(i);
                int indiceDigito = Character.getNumericValue(c);
                Image imagenDigito = new Image(digitosRegiones[indiceDigito]);
                tabla.add(imagenDigito);
            }
        } else { // Si no, solo actualizar las imágenes existentes
            for (int i = 0; i < valorComoString.length(); i++) {
                char c = valorComoString.charAt(i);
                int indiceDigito = Character.getNumericValue(c);
                Image imagenDigito = (Image) tabla.getChildren().get(i);
                imagenDigito.setDrawable(new TextureRegionDrawable(digitosRegiones[indiceDigito]));
            }
        }
    }

    /**
     * Obtiene la tabla que contiene los dígitos del contador.
     *
     * @return La tabla con los dígitos.
     */
    public Table getTabla() {
        return tabla;
    }

    /**
     * Limpia la tabla y libera los recursos utilizados por la textura de los dígitos.
     * Este método debe ser llamado cuando el contador ya no sea necesario.
     */
    @Override
    public void dispose() {
        if (digitosTextura != null) {
            digitosTextura.dispose();
        }
    }
}
