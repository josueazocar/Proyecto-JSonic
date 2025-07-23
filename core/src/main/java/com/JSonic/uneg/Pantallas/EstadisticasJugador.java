package com.JSonic.uneg.Pantallas;

public class EstadisticasJugador {
    public String nombreJugador;
    public int zonasLimpiadas;
    public int objetosReciclados;
    public int enemigosDerrotados;
    public int puntuacionTotal;

    // Constantes para la lógica de puntuación (esto no cambia)
    private static final int PUNTOS_POR_ZONA = 100;
    private static final int PUNTOS_POR_RECICLAJE = 50;
    private static final int PUNTOS_POR_ENEMIGO = 20;

    public EstadisticasJugador() {
        // Este constructor se deja vacío a propósito.
    }

    public EstadisticasJugador(String nombreJugador) {
        this.nombreJugador = nombreJugador;
        this.zonasLimpiadas = 0;
        this.objetosReciclados = 0;
        this.enemigosDerrotados = 0;
        this.puntuacionTotal = 0;
    }

    private void actualizarPuntuacion() {
        this.puntuacionTotal = (this.zonasLimpiadas * PUNTOS_POR_ZONA) +
            (this.objetosReciclados * PUNTOS_POR_RECICLAJE) +
            (this.enemigosDerrotados * PUNTOS_POR_ENEMIGO);
    }

    public void sumarZonaLimpiada() {
        this.zonasLimpiadas++;
        actualizarPuntuacion();
    }

    public void sumarObjetosReciclados(int cantidad) {
        this.objetosReciclados += cantidad;
        actualizarPuntuacion();
    }

    public void sumarEnemigoDerrotado() {
        this.enemigosDerrotados++;
        actualizarPuntuacion();
    }

    public void setNombreJugador(String nombreJugador) {
        this.nombreJugador = nombreJugador;
    }

    public String getNombreJugador() { return nombreJugador; }
    public int getPuntuacionTotal() { return puntuacionTotal; }
    public int getZonasLimpiadas() { return zonasLimpiadas; }
    public int getObjetosReciclados() { return objetosReciclados; }
    public int getEnemigosDerrotados() { return enemigosDerrotados; }
}
