package network;

import network.interfaces.IGameClient;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Una implementación de IGameClient que se ejecuta localmente en la memoria.
 * No utiliza la red real y se comunica directamente con una instancia de LocalServer.
 */
public class LocalClient implements IGameClient {

    // Referencia directa al servidor local con el que se comunica.
    private final LocalServer servidor;

    // Cola para los paquetes que el servidor nos "envía".
    private final Queue<Object> paquetesRecibidos = new ConcurrentLinkedQueue<>();

    /**
     * El constructor requiere una instancia del servidor local para poder comunicarse con él.
     * @param servidor La instancia del LocalServer.
     */
    public LocalClient(LocalServer servidor) {
        this.servidor = servidor;
    }

    /**
     * En el modo local, la conexión es instantánea y no requiere ninguna acción.
     * @param host La dirección del servidor (ignorado en modo local).
     */
    @Override
    public void connect(String host) {
        System.out.println("[LOCAL CLIENT] Cliente local 'conectado' en memoria.");
        // No hace nada porque ya estamos "conectados" por referencia directa.
    }

    /**
     * Para "enviar" un paquete, simplemente llamamos a un método en el servidor.
     * @param paquete El objeto que representa los datos a enviar.
     */
    @Override
    public void send(Object paquete) {
        // En lugar de usar la red, pasamos el paquete directamente al servidor local.
        servidor.recibirPaqueteDelCliente(paquete);
    }

    /**
     * Devuelve la cola de paquetes que el servidor local ha puesto aquí.
     * @return Una cola (Queue) de objetos recibidos.
     */
    @Override
    public Queue<Object> getPaquetesRecibidos() {
        return this.paquetesRecibidos;
    }

    /**
     * No hay una conexión de red que cerrar, por lo que este método está vacío.
     */
    @Override
    public void dispose() {
        System.out.println("[LOCAL CLIENT] Cliente local detenido.");
        // No hay recursos de red que liberar.
    }


    /**
     * Método para que el LocalServer nos "envíe" paquetes.
     * Añade un paquete a nuestra cola de recibidos para que el juego lo procese.
     * @param paquete El paquete enviado por el servidor.
     */

    public void recibirPaqueteDelServidor(Object paquete) {
        this.paquetesRecibidos.add(paquete);
    }
}
