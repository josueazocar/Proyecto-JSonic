package network.interfaces;

import com.JSonic.uneg.LevelManager;

public interface IGameServer {

    void start();

    /**
     * Representa el bucle de juego principal del servidor, donde se actualiza la lógica
     * (movimiento de IA, generación de ítems, etc.).
     * @param deltaTime El tiempo transcurrido desde el último fotograma.
     */
    void update(float deltaTime, com.JSonic.uneg.LevelManager manejadorNivel);

    void dispose();
}
