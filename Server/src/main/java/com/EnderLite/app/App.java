package com.EnderLite.app;

import com.EnderLite.Model.*;
import com.EnderLite.Utils.*;

import java.util.UUID;

import java.io.*;
import java.net.*;

public class App {
    public static void main(String[] args) {
        System.out.println("Server is booting up...");

        // Here space for testing purpouses
        Model model = new Model();
        User user = model.getUser(UUID.fromString("f7c3de3d-1fea-4d7c-a8b0-29f63c4c3454"));
        System.out.println(user.Login + " " + user.Email + " " + user.PasswordHash + " " + user.ID.toString());
        // Space for testing purpouses ends

        ServerSocket server = null;

        try {
            server = new ServerSocket(12345);
            server.setReuseAddress(true);
            while (true) {
                Socket client = server.accept();
                System.out.println("New client connected"
                        + client.getInetAddress()
                                .getHostAddress());
                ClientHandler clientSock = new ClientHandler(client);
                new Thread(clientSock).start();
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