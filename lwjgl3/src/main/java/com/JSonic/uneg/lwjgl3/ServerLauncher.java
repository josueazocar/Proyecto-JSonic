package com.JSonic.uneg.lwjgl3;
import com.JSonic.uneg.PantallaDeJuego;

import network.GameServer; // Importamos tu clase GameServer
/**
 * Esta clase es el punto de entrada para ejecutar el servidor del juego de forma independiente.
 */
public class ServerLauncher {

    public static void main(String[] args) {
        System.out.println("Iniciando GameServer...");

// 1. Creamos una instancia de nuestro GameServer.
// Ya no usamos el constructor con "throws IOException" porque lo manejamos adentro.
        GameServer server = new GameServer();
// 2. Llamamos al método start() que contiene toda la lógica de inicio.
        server.start();

        System.out.println("GameServer iniciado correctamente.");
// El servidor continuará ejecutándose en su propio hilo gracias a KryoNet.
    }
}
