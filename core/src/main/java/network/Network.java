package network;

import com.JSonic.uneg.*;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;

import java.util.HashMap;

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

        kryo.register(EnemigoState.class);
        kryo.register(EnemigoState.EstadoEnemigo.class);
        kryo.register(EnemigoState.EnemigoType.class);
        kryo.register(PaqueteEnemigoNuevo.class);
        kryo.register(PaqueteActualizacionEnemigos.class);
        kryo.register(java.util.HashMap.class);

        kryo.register(ItemState.class);
        kryo.register(ItemState.ItemType.class);
        kryo.register(PaqueteItemNuevo.class);
        kryo.register(PaqueteSolicitudRecogerItem.class);
        kryo.register(PaqueteItemEliminado.class);
        kryo.register(PaqueteAnimacionEnemigoTerminada.class);

        kryo.register(PaqueteInformacionMapa.class);
        kryo.register(java.util.ArrayList.class);
        kryo.register(com.badlogic.gdx.math.Rectangle.class);
        kryo.register(PaqueteOrdenCambiarMapa.class);
        kryo.register(PaqueteActualizacionPuntuacion.class);
        kryo.register(PlayerState.CharacterType.class);
        kryo.register(PaqueteActualizacionContaminacion.class);
        //animales
        // --- Nuevas adiciones para los animales ---
        kryo.register(AnimalState.class); // Clase del estado del animal
        kryo.register(PaqueteEstadoAnimalActualizado.class); // Paquete para un solo animal actualizado
        kryo.register(PaqueteAnimalNuevo.class); // Paquete para un animal nuevo
        kryo.register(PaqueteActualizacionAnimales.class);
    }

    // --- Definición de los Paquetes ---
    // Son clases simples que solo contienen los datos que queremos enviar.

    // Paquete enviado por el cliente al servidor para solicitar unirse
    public static class SolicitudAccesoPaquete {
        public String nombreJugador;
        public PlayerState.CharacterType characterType;
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

    public static class PaqueteEnemigoNuevo {
        public EnemigoState estadoEnemigo; // Enviamos el estado completo del nuevo enemigo
    }

    public static class PaqueteItemNuevo {
        public ItemState estadoItem; // Enviamos el estado completo del nuevo ítem
    }
    // El cliente envía este paquete al servidor cuando intenta recoger un ítem.
    public static class PaqueteSolicitudRecogerItem {
        public int idItem;
    }

    // El servidor envía este paquete a todos cuando un ítem es recogido y debe ser eliminado.
    public static class PaqueteItemEliminado {
        public int idItem;
    }
    // Este paquete contendrá el estado actualizado de TODOS los enemigos activos.
    public static class PaqueteActualizacionEnemigos {
        public HashMap<Integer, EnemigoState> estadosEnemigos;
    }
    // Paquete enviado por el cliente al servidor cuando un enemigo termina una animación clave.
    public static class PaqueteAnimacionEnemigoTerminada {
        public int idEnemigo;
    }
    public static class PaqueteInformacionMapa {
        public java.util.ArrayList<com.badlogic.gdx.math.Rectangle> paredes;
    }

    public static class PaqueteOrdenCambiarMapa {
        public String nuevoMapa;
        public float nuevaPosX;
        public float nuevaPosY;
    }

    public static class PaqueteActualizacionPuntuacion {
        // No necesitamos el ID del jugador, porque el servidor
        // se lo enviará solo al cliente que corresponda.
        public int nuevosAnillos;
        public int nuevaBasura;
    }

    public static class PaqueteActualizacionContaminacion {
        public float contaminationPercentage;
    }

    // Para actualizar el estado de un animal en el juego.
    public static class PaqueteEstadoAnimalActualizado {
        public int idAnimal;
        public boolean estaVivo;
    }

    public static class PaqueteAnimalNuevo {
        public AnimalState estadoAnimal;
    }

    // Este paquete es para sincronizar periódicamente todos los animales
    public static class PaqueteActualizacionAnimales {
        // Usamos HashMap<Integer, AnimalState> para enviar el estado de todos los animales
        public HashMap<Integer, AnimalState> estadosAnimales;
    }

}

