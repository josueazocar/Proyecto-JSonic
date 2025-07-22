package network;

import com.JSonic.uneg.EntidadesVisuales.Player;
import com.JSonic.uneg.Pantallas.EstadisticasJugador;
import com.JSonic.uneg.State.*;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        kryo.register(PaqueteSolicitudLiberarAnimal.class);
        kryo.register(PaqueteSolicitudMatarAnimal.class);
        kryo.register(PaqueteBloqueDestruido.class);
        kryo.register(PaqueteBloqueConfirmadoDestruido.class);

        kryo.register(PaqueteInvocarDron.class);
        kryo.register(PaqueteArbolNuevo.class);
        kryo.register(PaqueteMensajeUI.class);
        kryo.register(PaqueteDronEstado.class);
        kryo.register(DronState.EstadoDron.class);

        kryo.register(PaqueteBasuraDepositada.class);
        kryo.register(PaqueteSincronizarBloques.class);
        kryo.register(PaqueteSolicitudHabilidadLimpieza.class);
        kryo.register(PaqueteHabilidadLimpiezaSonic.class);
        kryo.register(PaqueteAtaqueJugadorAEnemigo.class);
        kryo.register(PaqueteEntidadEliminada.class);
        kryo.register(PaqueteActualizacionVida.class);
        kryo.register(PortalInfo.class);
        kryo.register(PaqueteTuID.class);
        kryo.register(PaqueteJugadorDesconectado.class);
        kryo.register(PaqueteSalidaDePartida.class);
        kryo.register(PaqueteActualizacionEsmeraldas.class);

        kryo.register(PaqueteResultadosFinales.class);
        kryo.register(EstadisticasJugador.class);

        kryo.register(ForzarFinDeJuegoDebug.class);
    }

    // --- Definición de los Paquetes ---
    // Son clases simples que solo contienen los datos que queremos enviar.
    public static class PaqueteTuID {
        public int id;
    }

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
        public ArrayList<PortalInfo> portales;
        public String nombreMapa;
        public Vector2 posEsmeralda;
    }

    public static class PaqueteOrdenCambiarMapa {
        public String nuevoMapa;
    }

    public static class PaqueteActualizacionPuntuacion {
        // No necesitamos el ID del jugador, porque el servidor
        // se lo enviará solo al cliente que corresponda.
        public int nuevosAnillos;
        public int nuevaBasura;
        public int totalBasuraReciclada;
    }

    public static class PaqueteActualizacionContaminacion {
        public float contaminationPercentage;
    }


    // ANIMALES --Para actualizar el estado de un animal en el juego.--- ANIMALES
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

    public static class PaqueteSolicitudLiberarAnimal {
        public int idAnimal;
    }

    public static class PaqueteSolicitudMatarAnimal {
        public int idAnimal;
    }

    public static class PaqueteBloqueDestruido {
        public int idBloque;
        public int idJugador;
    }

    public static class PaqueteBloqueConfirmadoDestruido {
        public int idBloque; // La ID del bloque a destruir.
    }

    public static class PaqueteInvocarDron {
        // El cliente envía este paquete vacío para pedir que se active el dron.
        // El servidor sabrá quién lo envió por el ID de la conexión.
    }
    public static class PaqueteDronEstado {
        public int ownerId; // ID del jugador dueño
        public DronState.EstadoDron nuevoEstado; // APARECIENDO, DESAPARECIENDO, etc.
        public float x, y; // Posición inicial (solo para APARECIENDO)
    }
    public static class PaqueteArbolNuevo {
        // El servidor envía esto a TODOS los clientes para decirles que dibujen un árbol.
        public float x;
        public float y;
    }

    public static class PaqueteMensajeUI {
        // El servidor envía esto a UN cliente para mostrar un mensaje.
        public String mensaje;
    }
    public static class PaqueteBasuraDepositada {
        public int cantidad;
    }

    // En tu archivo Network.java, junto a los otros paquetes

    public static class PaqueteHabilidadLimpiezaSonic {
        // No necesita contenido, su sola existencia es el mensaje.
    }

    public static class PaqueteSolicitudHabilidadLimpieza {
    }

    public static class PaqueteSincronizarBloques {
        // Enviamos el mismo tipo de mapa que usa el servidor para que sea fácil.
        public HashMap<Integer, Rectangle> todosLosBloques;
    }
    // El cliente envía esto cuando su ataque colisiona con un enemigo.
    public static class PaqueteAtaqueJugadorAEnemigo {
        public int idEnemigo;
        public int danio = 1; // Puedes variar el daño por ataque
    }

    // El servidor envía esto cuando una entidad (jugador o enemigo) muere.
    public static class PaqueteEntidadEliminada {
        public int idEntidad;
        public boolean esJugador; // Para distinguir si el ID es de un jugador o un enemigo
    }

    // El servidor envía esto para actualizar la vida de los jugadores en la UI.
    public static class PaqueteActualizacionVida {
        public int idJugador;
        public int nuevaVida;
    }

    public static class PaqueteActualizacionEsmeraldas {
        public int totalEsmeraldas;
    }

    public static class PortalInfo {
        public float x, y;
        public float destinoX, destinoY;
        public String destinoMapa;

        public PortalInfo() {} // Constructor vacío para KryoNet
    }

    public static class PaqueteJugadorDesconectado {
        public int idJugador; // El ID del jugador que se ha ido.
    }

    public static class PaqueteSalidaDePartida {
        // No necesita campos. Su simple llegada es la notificación.
    }

    public static class PaqueteResultadosFinales {
        public List<EstadisticasJugador> estadisticasFinales;
    }

    public static class ForzarFinDeJuegoDebug {
    }
}

