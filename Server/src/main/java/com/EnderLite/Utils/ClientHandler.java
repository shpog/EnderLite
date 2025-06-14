package com.EnderLite.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;

    private DataOutputStream out;
    private DataInputStream in;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;

    }

    public byte[] readBytes() {
        byte[] buffer = null;
        try {
            long bytes = in.readLong();
            buffer = in.readNBytes((int) bytes);
        } catch (IOException e) {
            System.err.println("Error while receiving data!");
        }
        return buffer;
    }

    public void sendBytes(byte[] buffer) {
        try {
            socOut.writeLong(buffer.length);
            socOut.write(buffer);
        } catch (IOException e) {
            System.err.println("Error while transmitting data!");
        }
    }

    public void run() {
        // PrintWriter out = null;
        // BufferedReader in = null;
        try {

            out = new DataOutputStream(clientSocket.getOutputStream());
            in = new DataInputStream(clientSocket.getInputStream());

            // in = new BufferedReader(
            // new InputStreamReader(clientSocket.getInputStream()));

            String line;
            while ((line = new String(readBytes())) != null) {
                if (line.startsWith("EnderLite_Client_")) {

                }

                // // here command analysis
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
