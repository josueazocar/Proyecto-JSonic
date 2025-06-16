package network;

import com.JSonic.uneg.Entity;
import com.JSonic.uneg.Player;
import com.JSonic.uneg.PlayerState;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;

// Esta clase centraliza el registro de todos los paquetes de red.
public class Network {

    // Puerto en el que el servidor escuchará
    static public final int PORT = 54555;

    // Esta función registrará todas las clases que se enviarán por la red.
    // DEBE ser llamada tanto en el servidor como en el cliente.
    static public void registrar(EndPoint endPoint) {
        Kryo kryo = endPoint.getKryo();
        // Aquí iremos añadiendo las clases de nuestros paquetes
        kryo.register(SolicitudAccesoPaquete.class);
        kryo.register(RespuestaAccesoPaquete.class);
        kryo.register(PaquetePosicionJugador.class);
        kryo.register(PlayerState.class);
        kryo.register(PaqueteJugadorConectado.class);
        kryo.register(Player.EstadoPlayer.class);
    }

    // --- Definición de los Paquetes ---
    // Son clases simples que solo contienen los datos que queremos enviar.

    // Paquete enviado por el cliente al servidor para solicitar unirse
    public static class SolicitudAccesoPaquete {
        public String nombreJugador;
    }

    // Paquete enviado por el servidor al cliente como respuesta
    public static class RespuestaAccesoPaquete {
        public String mensajeRespuesta;
        public PlayerState tuEstado;
    }

    public static class PaquetePosicionJugador {
        public int id;
        public float x;
        public float y;

        public Player.EstadoPlayer estadoAnimacion;
    }

    public static class PaqueteJugadorConectado {
        public PlayerState nuevoJugador;
    }

}

