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
import java.util.Map;

/**
 * Clase central responsable de registrar todas las clases de paquetes
 * para ser serializadas y enviadas por la red con KryoNet.
 * Debe invocarse tanto en el cliente como en el servidor.
 */
public class Network {

    /**
     * Puerto en el que el servidor escuchará y al que se conectará el cliente.
     */
    static public final int PORT = 54555;

    /**
     * Registra en el EndPoint todas las clases de paquetes y estados
     * que se enviarán por la red.
     *
     * @param endPoint instancia de Client o Server de KryoNet.
     */
    static public void registrar(EndPoint endPoint) {
        Kryo kryo = endPoint.getKryo();

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
        kryo.register(java.util.concurrent.ConcurrentHashMap.class);

        kryo.register(ItemState.class);
        kryo.register(ItemState.ItemType.class);
        kryo.register(PaqueteItemNuevo.class);
        kryo.register(PaqueteSolicitudRecogerItem.class);
        kryo.register(PaqueteItemEliminado.class);
        kryo.register(PaqueteAnimacionEnemigoTerminada.class);

        kryo.register(PaqueteInformacionMapa.class);
        kryo.register(java.util.ArrayList.class);
        kryo.register(com.badlogic.gdx.math.Rectangle.class);
        kryo.register(com.badlogic.gdx.math.Vector2.class);
        kryo.register(PaqueteOrdenCambiarMapa.class);
        kryo.register(PaqueteActualizacionPuntuacion.class);
        kryo.register(PlayerState.CharacterType.class);
        kryo.register(PaqueteActualizacionContaminacion.class);

        kryo.register(AnimalState.class);
        kryo.register(PaqueteEstadoAnimalActualizado.class);
        kryo.register(PaqueteAnimalNuevo.class);
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
        kryo.register(PaqueteTransformacionSuper.class);
        kryo.register(PaqueteGameOver.class);
        kryo.register(ForzarFinDeJuegoDebug.class);
        kryo.register(PaqueteActualizacionVidaEnemigo.class);
        kryo.register(PaqueteHabilidadDronUsada.class);
        kryo.register(PaqueteEnviarNombre.class);
        kryo.register(PaqueteActualizarLobby.class);
        kryo.register(PaqueteIniciarPartida.class);
    }

    /** Paquete enviado por el servidor al cliente con su ID asignado. */
    public static class PaqueteTuID {
        public int id;
    }

    /** Paquete enviado por el cliente al servidor para solicitar unirse a la partida. */
    public static class SolicitudAccesoPaquete {
        public String nombreJugador;
        public PlayerState.CharacterType characterType;
    }

    /** Paquete enviado por el servidor al cliente como respuesta a la solicitud de acceso. */
    public static class RespuestaAccesoPaquete {
        public String mensajeRespuesta;
        public PlayerState tuEstado;
    }

    /** Paquete que transmite la posición y estado de animación de un jugador. */
    public static class PaquetePosicionJugador {
        public int id;
        public float x;
        public float y;
        public Player.EstadoPlayer estadoAnimacion;
    }

    /** Paquete enviado a clientes cuando un nuevo jugador se conecta. */
    public static class PaqueteJugadorConectado {
        public PlayerState nuevoJugador;
    }

    /** Paquete enviado para informar de la creación de un nuevo enemigo. */
    public static class PaqueteEnemigoNuevo {
        public EnemigoState estadoEnemigo;
    }

    /** Paquete enviado para informar de la aparición de un nuevo ítem. */
    public static class PaqueteItemNuevo {
        public ItemState estadoItem;
    }

    /** Paquete enviado por el cliente al servidor para recoger un ítem. */
    public static class PaqueteSolicitudRecogerItem {
        public int idItem;
    }

    /** Paquete enviado cuando un ítem debe eliminarse de la escena. */
    public static class PaqueteItemEliminado {
        public int idItem;
    }

    /** Paquete con el estado actualizado de todos los enemigos activos. */
    public static class PaqueteActualizacionEnemigos {
        public Map<Integer, EnemigoState> estadosEnemigos;
    }

    /** Paquete enviado por el cliente indicando que la animación de un enemigo ha terminado. */
    public static class PaqueteAnimacionEnemigoTerminada {
        public int idEnemigo;
    }

    /** Paquete que contiene la información del mapa: paredes, portales y esmeralda. */
    public static class PaqueteInformacionMapa {
        public java.util.ArrayList<com.badlogic.gdx.math.Rectangle> paredes;
        public ArrayList<PortalInfo> portales;
        public String nombreMapa;
        public Vector2 posEsmeralda;
    }

    /** Paquete para solicitar o informar el cambio de mapa actual. */
    public static class PaqueteOrdenCambiarMapa {
        public String nuevoMapa;
    }

    /** Paquete con la actualización de la puntuación del jugador (anillos y basura). */
    public static class PaqueteActualizacionPuntuacion {
        public int nuevosAnillos;
        public int nuevaBasura;
        public int totalBasuraReciclada;
    }

    /** Paquete con el porcentaje de contaminación actual de un jugador. */
    public static class PaqueteActualizacionContaminacion {
        public float contaminationPercentage;
    }

    /** Paquete con el estado actualizado de un animal en la escena. */
    public static class PaqueteEstadoAnimalActualizado {
        public int idAnimal;
        public boolean estaVivo;
    }

    /** Paquete indicando la aparición de un nuevo animal. */
    public static class PaqueteAnimalNuevo {
        public AnimalState estadoAnimal;
    }

    /** Paquete con la colección de estados de todos los animales. */
    public static class PaqueteActualizacionAnimales {
        public Map<Integer, AnimalState> estadosAnimales;
    }

    /** Paquete enviado por el cliente al servidor para liberar un animal. */
    public static class PaqueteSolicitudLiberarAnimal {
        public int idAnimal;
    }

    /** Paquete enviado por el cliente al servidor para matar un animal. */
    public static class PaqueteSolicitudMatarAnimal {
        public int idAnimal;
    }

    /** Paquete indicando que un bloque ha sido destruido por un jugador. */
    public static class PaqueteBloqueDestruido {
        public int idBloque;
        public int idJugador;
    }

    /** Paquete confirmando la destrucción de un bloque. */
    public static class PaqueteBloqueConfirmadoDestruido {
        public int idBloque;
    }

    /** Paquete enviado para solicitar la invocación de un dron. */
    public static class PaqueteInvocarDron {
        // El cliente envía este paquete vacío para pedir que se active el dron.
        // El servidor sabrá quién lo envió por el ID de la conexión.
    }

    /** Paquete que notifica el estado de un dron (posición y acción). */
    public static class PaqueteDronEstado {
        public int ownerId; // ID del jugador dueño
        public DronState.EstadoDron nuevoEstado; // APARECIENDO, DESAPARECIENDO, etc.
        public float x, y; // Posición inicial (solo para APARECIENDO)
    }

    /** Paquete para informar de un nuevo árbol generado. */
    public static class PaqueteArbolNuevo {
        public float x;
        public float y;
    }

    /** Paquete con un mensaje para la interfaz de usuario. */
    public static class PaqueteMensajeUI {
        public String mensaje;
    }

    /** Paquete con la cantidad de basura depositada. */
    public static class PaqueteBasuraDepositada {
        public int cantidad;
    }

    /** Paquete enviado para activar la habilidad de limpieza Sonic. */
    public static class PaqueteHabilidadLimpiezaSonic {

    }

    /** Paquete enviado para solicitar la habilidad de limpieza. */
    public static class PaqueteSolicitudHabilidadLimpieza {
    }

    /** Paquete con la sincronización de todos los bloques rompibles. */
    public static class PaqueteSincronizarBloques {
        // Enviamos el mismo tipo de mapa que usa el servidor para que sea fácil.
        public HashMap<Integer, Rectangle> todosLosBloques;
    }
    /** Paquete enviando un ataque de jugador a un enemigo. */
    public static class PaqueteAtaqueJugadorAEnemigo {
        public int idEnemigo;
        public int danio = 1; // Puedes variar el daño por ataque
        public int idJugador;
    }

    /** Paquete notificando la eliminación de una entidad (jugador/enemigo). */
    public static class PaqueteEntidadEliminada {
        public int idEntidad;
        public boolean esJugador; // Para distinguir si el ID es de un jugador o un enemigo
    }

    /** Paquete con la actualización de vida de un jugador. */
    public static class PaqueteActualizacionVida {
        public int idJugador;
        public int nuevaVida;
    }

    /** Paquete con el total de esmeraldas recolectadas. */
    public static class PaqueteActualizacionEsmeraldas {
        public int totalEsmeraldas;
    }

    /** Clase auxiliar que define la información de un portal (posición y destino). */
    public static class PortalInfo {
        public float x, y;
        public float destinoX, destinoY;
        public String destinoMapa;

        public PortalInfo() {} // Constructor vacío para KryoNet
    }

    /** Paquete notificando la desconexión de un jugador. */
    public static class PaqueteJugadorDesconectado {
        public int idJugador; // El ID del jugador que se ha ido.
    }

    /** Paquete indicando que un jugador sale de la partida. */
    public static class PaqueteSalidaDePartida {

    }

    /** Paquete con las estadísticas finales de la partida. */
    public static class PaqueteResultadosFinales {
        public List<EstadisticasJugador> estadisticasFinales;
    }

    /** Paquete utilizado para forzar el fin de juego en modo depuración. */
    public static class ForzarFinDeJuegoDebug {
    }

    /** Paquete para activar o desactivar la transformación Super de un jugador. */
    public static class PaqueteTransformacionSuper {
        public int idJugador; // Para saber quién se transforma
        public boolean esSuper; // Para saber si se activa o desactiva
    }

    /** Paquete indicando el fin de la partida (Game Over). */
    public static class PaqueteGameOver {

    }

    /** Paquete con la actualización de vida de un enemigo. */
    public static class PaqueteActualizacionVidaEnemigo {
        public int idEnemigo;
        public int nuevaVida;
    }

    /** Paquete notificando el uso de la habilidad del dron. */
    public static class PaqueteHabilidadDronUsada {

    }

    /** Paquete con el nombre de un jugador. */
    public static class PaqueteEnviarNombre {
        public String nombre;
    }

    /** Paquete con la lista de nombres de jugadores en el lobby. */
    public static class PaqueteActualizarLobby {
        public java.util.ArrayList<String> nombres;
    }
    /** Paquete indicando el inicio de la partida. */
    public static class PaqueteIniciarPartida {

    }
}
