package network;

import com.JSonic.uneg.Pantallas.PantallaDeJuego;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import network.interfaces.IGameClient;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.io.IOException;
import java.util.Queue;

/**
 * Cliente de red para conectarse al servidor de juego y gestionar el envío y recepción de paquetes.
 */
public class GameClient implements IGameClient {

    public Client cliente;
    private PantallaDeJuego juego;
    public ConcurrentLinkedQueue<Object> paquetesRecibidos = new ConcurrentLinkedQueue<>();

    /**
     * Crea una instancia de GameClient, registra los paquetes de red y configura los listeners para eventos de conexión.
     */
    public GameClient() {
        cliente = new Client();

        Network.registrar(cliente);

        // listener para los eventos del cliente
        cliente.addListener(new Listener() {
            public void connected(Connection conexion) {
                System.out.println("[CLIENT] Conectado al servidor!");

                // Una vez conectados, enviamos una petición de login
                Network.SolicitudAccesoPaquete solicitud = new Network.SolicitudAccesoPaquete();
                solicitud.nombreJugador = "JUGADOR" + cliente.getID(); // Usamos el ID del cliente como nombre único
                cliente.sendTCP(solicitud);
            }

            public void received(Connection connection, Object objeto) {

                paquetesRecibidos.add(objeto);
            }

            public void disconnected(Connection conexion) {
                System.out.println("[CLIENT] Desconectado del servidor.");
            }
        });

        cliente.start(); // El cliente se inicia en un nuevo hilo

    }

    /**
     * Obtiene la cola de paquetes recibidos del servidor.
     *
     * @return cola de objetos recibidos.
     */
    @Override
    public Queue<Object> getPaquetesRecibidos() {
        return this.paquetesRecibidos;
    }

    /**
     * Establece conexión con el servidor en la dirección especificada.
     *
     * @param host dirección IP o nombre de host del servidor.
     */
    @Override
    public void connect(String host) {
        try {
            // Usamos el 'host' que nos pasan como parámetro.
            cliente.connect(5000, host, Network.PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cierra la conexión con el servidor si está activa.
     */
    @Override
    public void disconnect() {
        if (cliente != null) {
            cliente.close(); // Cierra la conexión del cliente
        }
    }

    /**
     * Envía un paquete al servidor usando TCP si el cliente está conectado.
     *
     * @param packet objeto de paquete a enviar.
     */
    @Override
    public void send(Object packet) {
        if (cliente != null && cliente.isConnected()) {
            cliente.sendTCP(packet);
        }
    }
}
