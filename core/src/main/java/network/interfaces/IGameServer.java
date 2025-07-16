package network.interfaces;

import com.JSonic.uneg.LevelManager;
import com.JSonic.uneg.EntidadesVisuales.Player;

public interface IGameServer {

    void start();

    /**
     * Representa el bucle de juego principal del servidor, donde se actualiza la lógica
     * (movimiento de IA, generación de ítems, etc.).
     *
     * @param deltaTime        El tiempo transcurrido desde el último fotograma.
     * @param personajeJugable
     */
    void update(float deltaTime, LevelManager manejadorNivel, Player personajeJugable);

    void dispose();
}
