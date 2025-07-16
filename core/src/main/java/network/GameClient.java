// paquete/network/GameClient.java
package network;

import com.JSonic.uneg.Pantallas.PantallaDeJuego;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import network.interfaces.IGameClient;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.io.IOException;
import java.util.Queue;

public class GameClient implements IGameClient {

    public Client cliente;
    private final PantallaDeJuego juego;
    public ConcurrentLinkedQueue<Object> paquetesRecibidos = new ConcurrentLinkedQueue<>();

    public GameClient(PantallaDeJuego juego) {
        this.juego = juego;
        cliente = new Client();

        Network.registrar(cliente);

        // Añadimos un listener para los eventos del cliente
        cliente.addListener(new Listener() {
            public void connected(Connection conexion) {
                System.out.println("[CLIENT] Conectado al servidor!");

                // Una vez conectados, enviamos una petición de login
                Network.SolicitudAccesoPaquete solicitud = new Network.SolicitudAccesoPaquete();
                solicitud.nombreJugador = "Sonic";
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


    @Override
    public void connect(String host) {
        try {
            // Usamos el 'host' que nos pasan como parámetro.
            // Para tu código original, el puerto es Network.PORT
            cliente.connect(5000, host, Network.PORT);
        } catch (IOException e) {
            // Aquí puedes manejar errores de conexión, como "servidor no encontrado".
            e.printStackTrace();
        }
    }

    @Override
    public void send(Object packet) {
        if (cliente != null && cliente.isConnected()) {
            cliente.sendTCP(packet);
        }
    }

    @Override
    public Queue<Object> getPaquetesRecibidos() {
        return this.paquetesRecibidos;
    }

    @Override
    public void dispose() {
        if (cliente != null) {
            cliente.close();
        }
    }

}
