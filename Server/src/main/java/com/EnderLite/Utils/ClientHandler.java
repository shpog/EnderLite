package com.EnderLite.Utils;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    public void run() {
        PrintWriter out = null;
        BufferedReader in = null;
        try {

            out = new PrintWriter(
                    clientSocket.getOutputStream(), true);

            long numBytes = (new DataInputStream(clientSocket.getInputStream()).readLong());

            in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));

            String line;
            while ((line = DataDehasher.Caesar(in.readLine(), numBytes)) != null) {

                // here command analysis
                // System.out.printf(
                // " Sent from the client: %s\n",
                // line);
                // out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
