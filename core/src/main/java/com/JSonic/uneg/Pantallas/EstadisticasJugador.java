package com.JSonic.uneg.Pantallas;

/**
 * La clase EstadisticasJugador se encarga de gestionar y calcular las estadísticas
 * de un jugador a lo largo del juego. Esto incluye el seguimiento de las zonas limpiadas,
 * los objetos reciclados, los enemigos derrotados y la puntuación total.
 */
public class EstadisticasJugador {
    public String nombreJugador;
    public int zonasLimpiadas;
    public int objetosReciclados;
    public int enemigosDerrotados;
    public int puntuacionTotal;

    // Constantes para la lógica de puntuación
    private static final int PUNTOS_POR_ZONA = 100;
    private static final int PUNTOS_POR_RECICLAJE = 50;
    private static final int PUNTOS_POR_ENEMIGO = 20;

    /**
     * Constructor por defecto de la clase EstadisticasJugador.
     */
    public EstadisticasJugador() {
    }

    /**
     * Constructor que inicializa las estadísticas para un jugador específico.
     *
     * @param nombreJugador El nombre del jugador.
     */
    public EstadisticasJugador(String nombreJugador) {
        this.nombreJugador = nombreJugador;
        this.zonasLimpiadas = 0;
        this.objetosReciclados = 0;
        this.enemigosDerrotados = 0;
        this.puntuacionTotal = 0;
    }

    /**
     * Actualiza la puntuación total del jugador basándose en las acciones realizadas.
     * La puntuación se calcula sumando los puntos obtenidos por cada tipo de logro.
     */
    private void actualizarPuntuacion() {
        this.puntuacionTotal = (this.zonasLimpiadas * PUNTOS_POR_ZONA) +
            (this.objetosReciclados * PUNTOS_POR_RECICLAJE) +
            (this.enemigosDerrotados * PUNTOS_POR_ENEMIGO);
    }

    /**
     * Incrementa en uno el contador de zonas limpiadas y actualiza la puntuación.
     */
    public void sumarZonaLimpiada() {
        this.zonasLimpiadas++;
        actualizarPuntuacion();
    }

    /**
     * Suma una cantidad específica de objetos reciclados al total y actualiza la puntuación.
     *
     * @param cantidad El número de objetos reciclados a añadir.
     */
    public void sumarObjetosReciclados(int cantidad) {
        this.objetosReciclados += cantidad;
        actualizarPuntuacion();
    }

    /**
     * Incrementa en uno el contador de enemigos derrotados y actualiza la puntuación.
     */
    public void sumarEnemigoDerrotado() {
        this.enemigosDerrotados++;
        actualizarPuntuacion();
    }

    /**
     * Establece el nombre del jugador.
     *
     * @param nombreJugador El nuevo nombre para el jugador.
     */
    public void setNombreJugador(String nombreJugador) {
        this.nombreJugador = nombreJugador;
    }

    /**
     * Obtiene el nombre del jugador.
     *
     * @return El nombre del jugador.
     */
    public String getNombreJugador() {
        return nombreJugador;
    }

    /**
     * Obtiene la puntuación total del jugador.
     *
     * @return La puntuación total.
     */
    public int getPuntuacionTotal() {
        return puntuacionTotal;
    }

    /**
     * Obtiene el número de zonas limpiadas por el jugador.
     *
     * @return El número de zonas limpiadas.
     */
    public int getZonasLimpiadas() {
        return zonasLimpiadas;
    }

    /**
     * Obtiene el número de objetos reciclados por el jugador.
     *
     * @return El número de objetos reciclados.
     */
    public int getObjetosReciclados() {
        return objetosReciclados;
    }

    /**
     * Obtiene el número de enemigos derrotados por el jugador.
     *
     * @return El número de enemigos derrotados.
     */
    public int getEnemigosDerrotados() {
        return enemigosDerrotados;
    }
}
