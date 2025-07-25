package com.JSonic.uneg.State;

/**
 * Representa el estado de contaminación de un objeto en el juego.
 * Mantiene un porcentaje entre 0.0f (limpio) y 100.0f (totalmente contaminado).
 */
public class ContaminationState {
    private float percentage; // De 0.0f (limpio) a 100.0f (contaminado)

    /**
     * Constructor predeterminado que inicializa la contaminación en 0%.
     */
    public ContaminationState() {
        this.percentage = 0.0f;
    }

    /**
     * Incrementa el porcentaje de contaminación en la cantidad indicada.
     * Si supera 100.0f, se fija a 100.0f.
     * @param amount valor a sumar al porcentaje actual.
     */
    public void increase(float amount) {
        this.percentage += amount;
        if (this.percentage > 100.0f) {
            this.percentage = 100.0f;
        }
    }

    /**
     * Disminuye el porcentaje de contaminación en la cantidad indicada.
     * Si baja de 0.0f, se fija a 0.0f.
     * @param amount valor a restar al porcentaje actual.
     */
    public void decrease(float amount) {
        this.percentage -= amount;
        if (this.percentage < 0.0f) {
            this.percentage = 0.0f;
        }
    }

    /**
     * Devuelve el porcentaje actual de contaminación.
     * @return porcentaje entre 0.0f y 100.0f.
     */
    public float getPercentage() {

        return percentage;
    }

    /**
     * Restablece el porcentaje de contaminación a 0.0f.
     */
    public void reset() {
        this.percentage = 0.0f;
    }
}
