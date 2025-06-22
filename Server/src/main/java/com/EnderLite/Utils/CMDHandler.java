package com.EnderLite.Utils;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.crypto.SecretKey;

public class CMDHandler extends Thread {
    private final Socket clientSocket;

    private DataOutputStream out;
    private DataInputStream in;

    public volatile SecretKey secretKey;

    public final ArrayList<String> IncomingCMDs;
    String cmd;

    public CMDHandler(Socket socket, String command, SecretKey key) {
        clientSocket = socket;
        IncomingCMDs = new ArrayList<String>();
        secretKey = key;
        cmd = command;
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
            out.writeLong(buffer.length);
            out.write(buffer);
        } catch (IOException e) {
            System.err.println("Error while transmitting data!");
        }
    }

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
