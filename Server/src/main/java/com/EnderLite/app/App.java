package com.EnderLite.app;

import com.EnderLite.Model.*;
import com.EnderLite.Utils.*;

import java.util.*;
import java.io.*;
import java.net.*;

/**
 * Main class
 */
public class App {

    /**
     * Main method
     * 
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        System.out.println("Server is booting up...");

        // Here space for testing purpouses

        // try {
        // Model model = new Model();
        // model.removeAllUsers();
        // model.removeAllChats();

        // } catch (Exception e) {
        // }

        // Space for testing purpouses ends

        ServerSocket server = null;

        try {
            server = new ServerSocket(12345);
            server.setReuseAddress(true);
            System.out.println("Server is running on " + String.valueOf(server.getInetAddress()) + ":"
                    + String.valueOf(server.getLocalPort()));

            List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<ClientHandler>());
            while (true) {
                Socket client = server.accept();

                System.out.println("New client connected "
                        + client.getInetAddress()
                                .getHostAddress());
                ClientHandler handler = new ClientHandler(client, clients);
                clients.add(handler);

                handler.start();

                for (ClientHandler c : clients) {
                    c.updateHandlers(clients);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

/*
 * This command runs server app via -jar created in /target dir
 * java -cp .\Server\target\EnderLite-1.0-SNAPSHOT.jar com.EnderLite.app.App
 * java -cp
 * ".\Server\target\EnderLite-1.0-SNAPSHOT.jar;C:\Users\Shaman\.m2\repository\org\json\json\20240303\json-20240303.jar"
 * com.EnderLite.app.App
 */