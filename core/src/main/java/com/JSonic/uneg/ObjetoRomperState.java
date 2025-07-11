package com.JSonic.uneg;

public class ObjetoRomperState {
    // Estado del daño: 0 = intacto, 1 = dañado, 2 = destruido
    private int estadoDanio = 0;
    // Indica si el objeto ya fue destruido
    private boolean destruido = false;
    // Posición y tamaño
    private int posicionX;
    private int posicionY;
    private int ancho;
    private int alto;

    public ObjetoRomperState(int posicionX, int posicionY, int ancho, int alto) {
        this.posicionX = posicionX;
        this.posicionY = posicionY;
        this.ancho = ancho;
        this.alto = alto;
    }

    // Getters y setters
    public int getEstadoDanio() {
        return estadoDanio;
    }

    public void setEstadoDanio(int estadoDanio) {
        this.estadoDanio = estadoDanio;
    }

    public boolean isDestruido() {
        return destruido;
    }

    public void setDestruido(boolean destruido) {
        this.destruido = destruido;
    }

    public int getPosicionX() {
        return posicionX;
    }

    public void setPosicionX(int posicionX) {
        this.posicionX = posicionX;
    }

    public int getPosicionY() {
        return posicionY;
    }

    public void setPosicionY(int posicionY) {
        this.posicionY = posicionY;
    }

    public int getAncho() {
        return ancho;
    }

    public void setAncho(int ancho) {
        this.ancho = ancho;
    }

    public int getAlto() {
        return alto;
    }

    public void setAlto(int alto) {
        this.alto = alto;
    }

    // Lógica para recibir golpe
    public void recibirGolpe(boolean esKnuckles, boolean espacioPresionado) {
        if (destruido) return;
        if (esKnuckles && espacioPresionado) {
            estadoDanio++;
            if (estadoDanio >= 2) {
                destruido = true;
            }
        }
    }
}
