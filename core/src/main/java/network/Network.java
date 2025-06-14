// paquete/network/Network.java
package network;

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
    }

    // --- Definición de los Paquetes ---
    // Son clases simples que solo contienen los datos que queremos enviar.

    // Paquete enviado por el cliente al servidor para solicitar unirse
    public static class SolicitudAccesoPaquete {
        public String nombreJugador; // Ejemplo de dato a enviar
    }

    // Paquete enviado por el servidor al cliente como respuesta
    public static class RespuestaAccesoPaquete {
        public String mensajeRespuesta;
    }
}
