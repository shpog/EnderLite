package com.EnderLite.app;

import com.EnderLite.Model.*;
import com.EnderLite.Utils.*;

import java.util.ArrayList;
import java.util.UUID;

import java.io.*;
import java.net.*;

public class App {
    public static void main(String[] args) {
        System.out.println("Server is booting up...");

        // Here space for testing purpouses

        // Model model = new Model();
        // UUID uuid = UUID.fromString("f7c3de3d-1fea-4d7c-a8b0-29f63c4c3454");

        // User user0 = model.getUser(uuid);
        // System.out.println(user0.Login + " " + user0.Email + " " + user0.PasswordHash
        // + " " + user0.ID.toString());

        // User user = new User();
        // user.ID = uuid;
        // user.Login = "shaman";
        // user.Email = "sh@mn.xd";
        // user.PasswordHash = "efweghojdwoij";

        // model.modifyOrCreateUser(user);
        // User user2 = model.getUser(uuid);
        // System.out.println(user2.Login + ", " + user2.Email + ", " +
        // user2.PasswordHash
        // + ", " + user2.ID.toString());

        // Space for testing purpouses ends

        ServerSocket server = null;

        try {
            server = new ServerSocket(12345);
            server.setReuseAddress(true);
            System.out.println("Server is running on " + String.valueOf(server.getInetAddress()) + ":"
                    + String.valueOf(server.getLocalPort()));

            ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();
            while (true) {
                Socket client = server.accept();

                System.out.println("New client connected"
                        + client.getInetAddress()
                                .getHostAddress());
                // ClientHandler clientSock = new ClientHandler(client);
                ClientHandler handler = new ClientHandler(client, clients);
                clients.add(handler);
                new Thread(handler).start();
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