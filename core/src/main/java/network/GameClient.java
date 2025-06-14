// paquete/network/GameClient.java
package network;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;

public class GameClient {

    private Client cliente;

    public GameClient() {
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
                // Comprobamos si hemos recibido una respuesta de login
                if (objeto instanceof Network.RespuestaAccesoPaquete) {
                    Network.RespuestaAccesoPaquete respuesta = (Network.RespuestaAccesoPaquete) objeto;
                    System.out.println("[CLIENT] Respuesta del servidor: " + respuesta.mensajeRespuesta);
                }
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

    public static void main(String[] args) {
        // com.esotericsoftware.minlog.Log.set(com.esotericsoftware.minlog.Log.LEVEL_DEBUG);
        new GameClient();


        // En un juego real, el bucle de renderizado mantendría la aplicación viva.
        // Para esta prueba, necesitamos mantener el hilo principal vivo artificialmente
        // para que el hilo de red de KryoNet pueda seguir funcionando en segundo plano.
        while (true) {
            try {
                // Ponemos el hilo a "dormir" por un momento para que no consuma el 100% del CPU.
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
