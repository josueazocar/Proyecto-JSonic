package network.interfaces;

import java.util.Queue;

/**
 * Interfaz que define las operaciones básicas de un cliente de juego.
 * Permite conectar, desconectar, enviar paquetes y recibir datos del servidor.
 */
public interface IGameClient {

    /**
     * Inicia el proceso de conexión a un servidor.
     * @param host La dirección del servidor (IP o localhost).
     */
    void connect(String host);

    void disconnect();

/**
     * Envía un paquete de datos al servidor.
     * @param packet El objeto que representa los datos a enviar. */
    void send(Object packet);

    /**
     * Devuelve la cola de paquetes recibidos desde el servidor para que el juego los procese.
     * @return Una cola (Queue) de objetos recibidos.
     */
    Queue<Object> getPaquetesRecibidos();


}
