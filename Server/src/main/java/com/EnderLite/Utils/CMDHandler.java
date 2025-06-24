package com.EnderLite.Utils;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.crypto.SecretKey;

/**
 * Class that is responsible for sending async messages.
 */
public class CMDHandler extends Thread {

    /**
     * Socket connected to the server
     */
    private final Socket clientSocket;

    /**
     * Output datastream, used by sendBytes
     * 
     * @see sendBytes
     */
    private DataOutputStream out;

    /**
     * Input datastream, used by readBytes
     * 
     * @see readBytes
     */
    private DataInputStream in;

    /**
     * Key used to encode command.
     */
    public volatile SecretKey secretKey;

    /**
     * Command to send.
     */
    String cmd;

    /**
     * Method responsible for sending encoded buffer directly through the socket
     * 
     * @param socket  Socket to send messages through
     * @param command Command to send.
     * @param key     Key used to encode command.
     */
    public CMDHandler(Socket socket, String command, SecretKey key) {
        clientSocket = socket;
        secretKey = key;
        cmd = command;
    }

    /**
     * Method responsible for reading encoded buffer directly from the socket
     * 
     * @return Buffer read from the socket.
     */

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

    /**
     * Method responsible for sending encoded buffer directly through the socket
     * 
     * @param buffer Buffer to send.
     */

    public void sendBytes(byte[] buffer) {
        try {
            out.writeLong(buffer.length);
            out.write(buffer);
        } catch (IOException e) {
            System.err.println("Error while transmitting data!");
        }
    }

    /**
     * Main Thread method, responsible for sending command.
     */

    @Override
    public void run() {
        try {
            Thread.sleep(100);
            out = new DataOutputStream(clientSocket.getOutputStream());
            in = new DataInputStream(clientSocket.getInputStream());
            try {
                System.out.println("CMD sent: " + cmd);
                sendBytes(DataEncryptor.encrypt(cmd, secretKey));
            } catch (Exception e) {
                System.out.println("Error sending: " + cmd);
                e.printStackTrace();
            }

        } catch (Exception e) {
        }
    }

}
