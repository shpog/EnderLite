package com.EnderLite.Utils;

import com.EnderLite.Controller.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.Authenticator.RequestorType;

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
            out.writeLong(buffer.length);
            out.write(buffer);
        } catch (IOException e) {
            System.err.println("Error while transmitting data!");
        }
    }

    @Override
    public void run() {
        try {

            out = new DataOutputStream(clientSocket.getOutputStream());
            in = new DataInputStream(clientSocket.getInputStream());

            String line;
            String response = "";
            while (true) {
                byte[] receivedBytes = readBytes();
                if (receivedBytes == null || receivedBytes.length == 0) {
                    System.out.println("Client " + clientSocket.getInetAddress() + " disconnected.");
                    break;
                }
                line = new String(receivedBytes);
                System.out.println(clientSocket.getInetAddress() + "$ " + line);

                if (line.startsWith("AUTH_LOG&PASSW-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 3) {
                        String login = parts[1];
                        String password = parts[2];
                        response = AUTH_LOG(login, password);
                    }
                }

                else if (line.startsWith("AUTH_EMAIL&PASSW-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 3) {
                        String email = parts[1];
                        String password = parts[2];
                        response = AUTH_EMAIL(email, password);
                    }
                }

                else if (line.equals("AUTH_STATUS")) {
                    response = AUTH_STATUS();
                }

                else if (line.startsWith("REQ_ADD_USER-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 4) {
                        String login = parts[1];
                        String email = parts[2];
                        String password = parts[3];
                        response = REQ_ADD_USER(login, email, password);
                    }
                }

                else if (line.startsWith("REQ_USER_DATA_LOGIN-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 2) {
                        String requestedLogin = parts[1];
                        response = REQ_USER_DATA_LOGIN(requestedLogin);
                    }
                }

                else if (line.startsWith("REQ_USER_DATA_EMAIL-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 2) {
                        String requestedEmail = parts[1];
                        response = REQ_USER_DATA_EMAIL(requestedEmail);
                    }
                }

                // else if (line.startsWith("REQ_USERS_LOG-")) {
                // String[] parts = line.split("-");
                // if (parts.length == 2) {
                // String phrase = parts[1];
                // response = REQ_USERS_LOG(phrase);
                // }
                // }

                else if (line.startsWith("REQ_INV_LOG-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 3) {
                        String userToInvite = parts[1];
                        String invitingUser = parts[2];

                        REQ_INV_LOG(userToInvite, invitingUser);
                    }
                }

                else if (line.startsWith("REQ_INV_EMAIL-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 3) {
                        String emailToInvite = parts[1];
                        String invitingUser = parts[2];

                        REQ_INV_LOG(emailToInvite, invitingUser);
                    }
                }

                else if (line.startsWith("REQ_INV_STATUS-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 4) {
                        String invitingUser = parts[1];
                        String invitedUser = parts[2];
                        String status = parts[3]; // ACCEPTED or DENIED
                        response = REQ_INV_STATUS(invitingUser, invitedUser, status);
                    }
                }

                else if (line.startsWith("REQ_CRT_CHAT-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 3) {
                        String login = parts[1];
                        String chatName = parts[2];
                        response = REQ_CRT_CHAT(login, chatName);
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
                        response = REQ_ADD_CHAT(chatName, sendingLogin, usersToAdd);
                    }
                }

                else if (line.startsWith("REQ_CHAN_CHAT_NAME-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 4) {
                        String login = parts[1];
                        String oldChatName = parts[2];
                        String newChatName = parts[3];
                        response = REQ_CHAN_CHAT_NAME(login, oldChatName, newChatName);
                    }
                }

                else if (line.startsWith("REQ_CHAN_CHAT_RANK-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 5) {
                        String chatName = parts[1];
                        String requestingLogin = parts[2];
                        String changedLogin = parts[3];
                        String rank = parts[4]; // USER or ADMIN
                        response = REQ_CHAN_CHAT_RANK(chatName, requestingLogin, changedLogin, rank);
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
                        response = REQ_DEL_CHAT(chatName, sendingLogin, usersToRemove);
                    }
                }

                else if (line.startsWith("REQ_DES_CHAT-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 3) {
                        String login = parts[1];
                        String chatName = parts[2];
                        response = REQ_DES_CHAT(login, chatName);
                    }
                }

                else if (line.startsWith("SEND_DATA-")) {
                    String[] parts = line.split("-");
                    if (parts.length >= 4) {
                        String login = parts[1];
                        String chatName = parts[2];
                        String data = line.substring(line.indexOf(chatName) + chatName.length() + 1);
                        response = SEND_DATA(login, chatName, data);
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

                // response = GET_DATA(login, chatName, commandType);
                // }
                // }

                // else if (line.startsWith("SEND_FILE-")) {
                // String[] parts = line.split("-");
                // if (parts.length >= 4) {
                // String login = parts[1];
                // String chatName = parts[2];
                // String fileData = line.substring(line.indexOf(chatName) + chatName.length() +
                // 1);
                // response = SEND_FILE(login, chatName, fileData);
                // }
                // }

                else if (line.equals("REQ_CONN_END")) {
                    response = "ANS_CONN_END";
                    sendBytes(response.getBytes()); // Send response before breaking
                    System.out.println("Client " + clientSocket.getInetAddress() + " requested disconnect.");
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
