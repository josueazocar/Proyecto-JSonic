// paquete/network/GameServer.java
package network;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;

public class GameServer {

    private Server servidor;

    public GameServer() throws IOException {
        servidor = new Server(); // Creamos la instancia del servidor

        // Registramos las clases que vamos a usar en la red
        Network.registrar(servidor);

        // Añadimos un "Listener" para reaccionar a eventos de red
        servidor.addListener(new Listener() {
            // Este metodo se ejecuta cuando un nuevo cliente se conecta
            public void connected(Connection connection) {
                System.out.println("[SERVER] Un cliente se ha conectado: " + connection.getRemoteAddressTCP());
            }

            // Este metodo se ejecuta cuando recibimos un paquete de un cliente
            public void received(Connection conexion, Object objeto) {
                // Comprobamos si el paquete recibido es una petición de login
                if (objeto instanceof Network.SolicitudAccesoPaquete) {
                    Network.SolicitudAccesoPaquete solicitud = (Network.SolicitudAccesoPaquete) objeto;
                    System.out.println("[SERVER] Peticion de login recibida de: " + solicitud.nombreJugador);

                    // Preparamos una respuesta para el cliente
                    Network.RespuestaAccesoPaquete respuesta = new Network.RespuestaAccesoPaquete();
                    respuesta.mensajeRespuesta = "Bienvenido al servidor, " + solicitud.nombreJugador + "!";

                    // Enviamos la respuesta solo a ese cliente
                    conexion.sendTCP(respuesta);
                }
            }

            // Este metodo se ejecuta cuando un cliente se desconecta
            public void disconnected(Connection conexion) {
                System.out.println("[SERVER] Un cliente se ha desconectado.");
            }
        });

        // Vinculamos el servidor al puerto y lo iniciamos
        servidor.bind(Network.PORT);
        servidor.start(); // El servidor se inicia en un nuevo hilo automáticamente

        System.out.println("[SERVER] Servidor iniciado y escuchando en el puerto " + Network.PORT);
    }


    public static void main(String[] args) throws IOException {
    // com.esotericsoftware.minlog.Log.set(com.esotericsoftware.minlog.Log.LEVEL_DEBUG);
        new GameServer();
    }
}
