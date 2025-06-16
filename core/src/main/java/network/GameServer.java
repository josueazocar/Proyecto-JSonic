package network;

import com.JSonic.uneg.Entity;
import com.JSonic.uneg.Player;
import com.JSonic.uneg.PlayerState;
import com.JSonic.uneg.Sonic;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.util.HashMap;

public class GameServer {

    private final Server servidor;
    // Este mapa guardará a cada jugador, usando el ID de su conexión como clave.
    private final HashMap<Integer, PlayerState> jugadores = new HashMap<>();

    public GameServer() throws IOException {
        servidor = new Server();

        // Registramos las clases que vamos a usar en la red
        Network.registrar(servidor);

        // Añadimos un "Listener" para reaccionar a eventos de red
        servidor.addListener(new Listener() {
            // Este metodo se ejecuta cuando un nuevo cliente se conecta
            public void connected(Connection conexion) {
                System.out.println("[SERVER] Un cliente se ha conectado: ID " + conexion.getID());

                // Crear el ESTADO para el nuevo jugador.
                PlayerState nuevoEstado = new PlayerState();
                nuevoEstado.id = conexion.getID();
                nuevoEstado.x = 100; // Posición inicial X
                nuevoEstado.y = 100; // Posición inicial Y
                nuevoEstado.estadoAnimacion = Player.EstadoPlayer.IDLE_RIGHT;

                // Ahora, en cuanto se conecte, su ficha ya está en el archivador.
                jugadores.put(conexion.getID(), nuevoEstado);

                Network.PaqueteJugadorConectado packetNuevoJugador = new Network.PaqueteJugadorConectado();
                packetNuevoJugador.nuevoJugador = nuevoEstado;
                servidor.sendToAllExceptTCP(conexion.getID(), packetNuevoJugador);

                // Enviar al NUEVO jugador la lista de los que YA estaban conectados.
                for (PlayerState jugadorExistente : jugadores.values()) {
                    // Nos aseguramos de no enviarle al nuevo jugador su propio estado en esta lista.
                    if (jugadorExistente.id != conexion.getID()) {
                        Network.PaqueteJugadorConectado packetJugadorExistente = new Network.PaqueteJugadorConectado();
                        packetJugadorExistente.nuevoJugador = jugadorExistente;
                        conexion.sendTCP(packetJugadorExistente);
                    }
                }
            }

            // Este metodo se ejecuta cuando recibimos un paquete de un cliente
            public void received(Connection conexion, Object objeto) {

                if (objeto instanceof Network.PaquetePosicionJugador paquete) {

                    // Actualizamos el estado del jugador en el mapa del servidor
                    PlayerState estadoJugador = jugadores.get(paquete.id);
                    if (estadoJugador != null) {
                        estadoJugador.x = paquete.x;
                        estadoJugador.y = paquete.y;

                        // Actualizamos también el estado de la animación en el servidor
                        estadoJugador.estadoAnimacion = paquete.estadoAnimacion;

                        // Retransmitimos la posición a todos los demás
                        servidor.sendToAllExceptTCP(conexion.getID(), paquete);
                    }
                }
                // Comprobamos si el paquete recibido es una petición de login
                if (objeto instanceof Network.SolicitudAccesoPaquete solicitud) {
                    System.out.println("[SERVER] Peticion de login recibida de: " + solicitud.nombreJugador);

                    // Preparamos una respuesta para el cliente
                    Network.RespuestaAccesoPaquete respuesta = new Network.RespuestaAccesoPaquete();
                    respuesta.mensajeRespuesta = "Bienvenido al servidor, " + solicitud.nombreJugador + "!";
                    PlayerState estadoAsignado = jugadores.get(conexion.getID());
                    respuesta.tuEstado = estadoAsignado;
                    // Enviamos la respuesta solo a ese cliente
                    conexion.sendTCP(respuesta);
                }

            }

            // Este metodo se ejecuta cuando un cliente se desconecta
            public void disconnected(Connection conexion) {
                System.out.println("[SERVER] Un cliente se ha desconectado.");
                // Eliminamos al jugador del mapa cuando se desconecta
                jugadores.remove(conexion.getID());
            }
        });

        // Vinculamos el servidor al puerto y lo iniciamos
        servidor.bind(Network.PORT);
        servidor.start(); // El servidor se inicia en un nuevo hilo automáticamente

        System.out.println("[SERVER] Servidor iniciado y escuchando en el puerto " + Network.PORT);
    }


    public static void main(String[] args) throws IOException {
        new GameServer();
    }
}
