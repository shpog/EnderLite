package com.EnderLite.Utils;

import com.EnderLite.Controller.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.Authenticator.RequestorType;

import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class ClientHandler implements Runnable {
    private Controller ctrl;
    private final Socket clientSocket;

    private DataOutputStream out;
    private DataInputStream in;

    public volatile SecretKey secretKey;

    private ArrayList<ClientHandler> handlers;

    public ClientHandler(Socket socket, ArrayList<ClientHandler> clients) {
        clientSocket = socket;
        handlers = clients;
        ctrl = new Controller(handlers);

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

    public boolean handshake() {
        Security.addProvider(new BouncyCastleProvider());

        byte[] receivedBytes = readBytes();
        String handshakeFirstMessage = new String(receivedBytes);
        if (!handshakeFirstMessage.startsWith("EnderLite_Client_"))
            return false;
        // Stage 2 Get Public key, Create AES key and send encrypted message
        String key = handshakeFirstMessage.substring("EnderLite_Client_".length());
        byte[] keyBytes = java.util.Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);

        // get RSA public key
        PublicKey publicKey = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
            publicKey = keyFactory.generatePublic(spec);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("No such algorithm! RSA second stage handshake!");
        } catch (InvalidKeySpecException e) {
            System.err.println("Invalid key spec! RSA second stage handshake");
        } catch (NoSuchProviderException e) {
            System.err.println("No such Provider! RSA second stage handshake");
        }

        // generate AES key
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            secretKey = keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("No such algorithm AES second stage handshake!");
        }

        // endcypt and send
        String handshakeSecondMessage = "ACCEPT_SERVER_" +
                java.util.Base64.getEncoder().encodeToString(secretKey.getEncoded());
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            sendBytes(cipher.doFinal(handshakeSecondMessage.getBytes()));
        } catch (Exception e) {
            System.err.println("Don't care now!");
        }

        // Stage 3
        receivedBytes = readBytes();
        try {
            Cipher cipher = Cipher.getInstance("AES", "BC");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            receivedBytes = cipher.doFinal(receivedBytes);
        } catch (Exception e) {

        }

        String handshakeThirdMessage = new String(receivedBytes);
        if (!handshakeThirdMessage.startsWith("ACCEPT_CLIENT_")) {
            return false;
        }

        // getAES key nad check if the same
        String AESKey = handshakeThirdMessage.substring("ACCEPT_CLIENT_".length());
        if (!AESKey.equals(java.util.Base64.getEncoder().encodeToString(secretKey.getEncoded()))) {
            return false;
        }

        return true;
    }

    @Override
    public void run() {
        try {

            out = new DataOutputStream(clientSocket.getOutputStream());
            in = new DataInputStream(clientSocket.getInputStream());

            String line;
            String response = "";

            if (!handshake())
                System.out
                        .println("Client " + clientSocket.getInetAddress() + " handshake failed. Forcing disconnect.");

            while (true) {
                byte[] receivedBytes = readBytes();
                if (receivedBytes == null || receivedBytes.length == 0) {
                    System.out.println("Client " + clientSocket.getInetAddress() + " disconnected");
                    break;
                }
                line = new String(receivedBytes);
                System.out.println(clientSocket.getInetAddress() + "$ " + line);

                if (line.startsWith("AUTH_LOG&PASSW-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 3) {
                        String login = parts[1];
                        String password = parts[2];
                        response = ctrl.AUTH_LOG(login, password);
                    }
                }

                else if (line.startsWith("AUTH_EMAIL&PASSW-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 3) {
                        String email = parts[1];
                        String password = parts[2];
                        response = ctrl.AUTH_EMAIL(email, password);
                    }
                }

                else if (line.equals("AUTH_STATUS")) {
                    response = ctrl.AUTH_STATUS();
                }

                else if (line.startsWith("REQ_ADD_USER-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 4) {
                        String login = parts[1];
                        String email = parts[2];
                        String password = parts[3];
                        response = ctrl.REQ_ADD_USER(login, email, password);
                    }
                }

                else if (line.startsWith("REQ_USER_DATA-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 2) {
                        String requestedLogin = parts[1];
                        response = ctrl.REQ_USER_DATA(requestedLogin);
                    }
                }

                // else if (line.startsWith("REQ_USERS_LOG-")) {
                // String[] parts = line.split("-");
                // if (parts.length == 2) {
                // String phrase = parts[1];
                // response = ctrl.REQ_USERS_LOG(phrase);
                // }
                // }

                else if (line.startsWith("REQ_INV_LOG-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 3) {
                        String userToInvite = parts[1];
                        String invitingUser = parts[2];

                        ctrl.REQ_INV_LOG(userToInvite, invitingUser);
                    }
                }

                else if (line.startsWith("REQ_INV_EMAIL-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 3) {
                        String emailToInvite = parts[1];
                        String invitingUser = parts[2];

                        ctrl.REQ_INV_LOG(emailToInvite, invitingUser);
                    }
                }

                else if (line.startsWith("REQ_INV_STATUS-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 4) {
                        String invitingUser = parts[1];
                        String invitedUser = parts[2];
                        String status = parts[3]; // ACCEPTED or DENIED
                        response = ctrl.REQ_INV_STATUS(invitingUser, invitedUser, status);
                    }
                }

                else if (line.startsWith("REQ_DEL_LOG-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 3) {
                        String userToDelete = parts[1];
                        String deletingUser = parts[2];

                        ctrl.REQ_DEL_LOG(userToDelete, deletingUser);
                    }
                }

                else if (line.startsWith("REQ_CRT_CHAT-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 3) {
                        String login = parts[1];
                        String chatName = parts[2];
                        response = ctrl.REQ_CRT_CHAT(login, chatName);
                    }
                }

                else if (line.startsWith("REQ_ADD_CHAT-")) {
                    String[] parts = line.split("-");
                    if (parts.length >= 4 && parts[0].equals("REQ_ADD_CHAT") && parts[1].startsWith("C=")
                            && parts[3].startsWith("L={")) {
                        String chatName = parts[1].substring(2);
                        String sendingLogin = parts[2];
                        String usersToAddString = parts[3].substring(3, parts[3].length() - 1);
                        String[] usersToAdd = usersToAddString.split(",");
                        response = ctrl.REQ_ADD_CHAT(chatName, sendingLogin, usersToAdd);
                    }
                }

                else if (line.startsWith("REQ_CHAN_CHAT_NAME-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 4) {
                        String login = parts[1];
                        String oldChatName = parts[2];
                        String newChatName = parts[3];
                        response = ctrl.REQ_CHAN_CHAT_NAME(login, oldChatName, newChatName);
                    }
                }

                else if (line.startsWith("REQ_CHAN_CHAT_RANK-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 5) {
                        String chatName = parts[1];
                        String requestingLogin = parts[2];
                        String changedLogin = parts[3];
                        String rank = parts[4]; // USER or ADMIN
                        response = ctrl.REQ_CHAN_CHAT_RANK(chatName, requestingLogin, changedLogin, rank);
                    }
                }

                else if (line.startsWith("REQ_DEL_CHAT-")) {
                    String[] parts = line.split("-");
                    if (parts.length >= 4 && parts[0].equals("REQ_DEL_CHAT") && parts[1].startsWith("C=")
                            && parts[3].startsWith("L={")) {
                        String chatName = parts[1].substring(2);
                        String sendingLogin = parts[2];
                        String usersToRemoveString = parts[3].substring(3, parts[3].length() - 1);
                        String[] usersToRemove = usersToRemoveString.split(",");
                        response = ctrl.REQ_DEL_CHAT(chatName, sendingLogin, usersToRemove);
                    }
                }

                else if (line.startsWith("REQ_DES_CHAT-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 3) {
                        String login = parts[1];
                        String chatName = parts[2];
                        response = ctrl.REQ_DES_CHAT(login, chatName);
                    }
                }

                else if (line.startsWith("SEND_DATA-")) {
                    String[] parts = line.split("-");
                    if (parts.length >= 4) {
                        String login = parts[1];
                        String chatName = parts[2];
                        String data = line.substring(line.indexOf(chatName) + chatName.length() + 1);
                        response = ctrl.SEND_DATA(login, chatName, data);
                    }
                }

                // else if (line.startsWith("GET_DATA-")) {
                // String[] parts = line.split("-");
                // if (parts.length == 3) {
                // String login = parts[1];
                // String chatNameSuffix = parts[2];
                // String chatName = chatNameSuffix.substring(0,
                // chatNameSuffix.lastIndexOf("_"));
                // String commandType = chatNameSuffix.substring(chatNameSuffix.lastIndexOf("_")
                // + 1);

                // response = ctrl.GET_DATA(login, chatName, commandType);
                // }
                // }

                // else if (line.startsWith("SEND_FILE-")) {
                // String[] parts = line.split("-");
                // if (parts.length >= 4) {
                // String login = parts[1];
                // String chatName = parts[2];
                // String fileData = line.substring(line.indexOf(chatName) + chatName.length() +
                // 1);
                // response = ctrl.SEND_FILE(login, chatName, fileData);
                // }
                // }

                else if (line.equals("REQ_CONN_END")) {
                    response = "ANS_CONN_END";
                    sendBytes(response.getBytes()); // Send response before breaking
                    System.out.println("Client " + clientSocket.getInetAddress() + " requested disconnect");
                    break;
                }

                if (response != "")
                    sendBytes(response.getBytes());
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
