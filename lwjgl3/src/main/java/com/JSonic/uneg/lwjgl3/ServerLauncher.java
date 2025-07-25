package com.JSonic.uneg.lwjgl3;

import network.GameServer; // Importamos tu clase GameServer

/**
 * Esta clase es el punto de entrada para ejecutar el servidor del juego de forma independiente.
 */
public class ServerLauncher {
    /**
     * Método principal que inicia el servidor de juego.
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Iniciando GameServer...");

        // Creamos una instancia de nuestro GameServer.
        GameServer server = new GameServer();

        // Llamamos al método start() que contiene toda la lógica de inicio.
        server.start();

        System.out.println("GameServer iniciado correctamente.");
        // El servidor continuará ejecutándose en su propio hilo gracias a KryoNet.
    }
}
