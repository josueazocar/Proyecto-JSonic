package com.JSonic.uneg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Gestiona el guardado y la carga del progreso del jugador de forma persistente.
 * Utiliza las Preferences de LibGDX para almacenar datos localmente.
 */
public class GestorDeProgreso {

    // Nombre único para nuestro archivo de guardado.
    private static final String NOMBRE_PREFERENCIAS = "JSonicProgreso";

    // Clave que usaremos para identificar el dato guardado.
    private static final String CLAVE_NIVEL_MAS_ALTO = "nivelMasAltoDesbloqueado";

    /**
     * Obtiene la instancia de Preferences para nuestro juego.
     * @return El objeto Preferences.
     */
    private static Preferences obtenerPreferencias() {
        return Gdx.app.getPreferences(NOMBRE_PREFERENCIAS);
    }

    /**
     * Guarda el nivel más alto que el jugador ha desbloqueado.
     * La operación solo se realiza si el nuevo nivel es superior al guardado previamente.
     * @param numeroDeNivel El número del nivel a guardar (ej: 2 para el Nivel 2).
     */
    public static void guardarProgresoDeNivel(int numeroDeNivel) {
        Preferences prefs = obtenerPreferencias();
        int nivelGuardado = prefs.getInteger(CLAVE_NIVEL_MAS_ALTO, 1); // El valor por defecto es 1.

        if (numeroDeNivel > nivelGuardado) {
            prefs.putInteger(CLAVE_NIVEL_MAS_ALTO, numeroDeNivel);
            prefs.flush(); // ¡Esencial! Escribe los cambios en el disco duro.
            System.out.println("[GestorDeProgreso] Progreso guardado. Nivel más alto ahora es: " + numeroDeNivel);
        }
    }

    /**
     * Carga el número del nivel más alto que el jugador ha alcanzado.
     * @return El número del nivel más alto desbloqueado. Si no hay nada guardado, devuelve 1.
     */
    public static int cargarNivelMasAltoDesbloqueado() {
        return obtenerPreferencias().getInteger(CLAVE_NIVEL_MAS_ALTO, 1);
    }
}
