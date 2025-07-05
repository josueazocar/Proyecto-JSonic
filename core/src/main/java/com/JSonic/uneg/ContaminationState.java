package com.JSonic.uneg;

public class ContaminationState {
    private float percentage; // De 0.0f (limpio) a 100.0f (contaminado)

    public ContaminationState() {
        this.percentage = 0.0f;
    }

    public void increase(float amount) {
        this.percentage += amount;
        if (this.percentage > 100.0f) {
            this.percentage = 100.0f;
        }
    }

    public void decrease(float amount) {
        this.percentage -= amount;
        if (this.percentage < 0.0f) {
            this.percentage = 0.0f;
        }
    }

    public float getPercentage() {
        return percentage;
    }

    public void reset() {
        this.percentage = 0.0f;
    }
}
