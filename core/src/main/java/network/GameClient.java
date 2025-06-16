// paquete/network/GameClient.java
package network;

import com.JSonic.uneg.PantallaDeJuego;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.io.IOException;

public class GameClient {

    public Client cliente;
    private final PantallaDeJuego juego;
    public ConcurrentLinkedQueue<Object> paquetesRecibidos = new ConcurrentLinkedQueue<>();

    public GameClient(PantallaDeJuego juego) {
        this.juego= juego;
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

        // Intentamos conectarnos al servidor
        try {
            // "127.0.0.1" es la dirección para conectarse a la misma máquina (localhost)
            cliente.connect(5000, "localhost", Network.PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
