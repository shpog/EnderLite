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

                // HERE START OF BULLSHIT (HOPEFULLY NOT)

                if (line.startsWith("AUTH_LOG&PASSW-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 3) {
                        String login = parts[1];
                        String password = parts[2];
                        // TODO: Implement actual login authentication with login and password
                        // For now, a placeholder response:
                        if ("test_login".equals(login) && "test_password".equals(password)) {
                            response = "AUTH_RESP-ACCEPTED";
                            // Optionally, store client's authenticated status here
                        } else {
                            response = "AUTH_RESP-DENIED";
                        }
                    } else {
                        response = "ERROR: Malformed AUTH_LOG&PASSW command.";
                    }
                } else if (line.startsWith("AUTH_EMAIL&PASSW-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 3) {
                        String email = parts[1];
                        String password = parts[2];
                        // TODO: Implement actual email authentication with email and password
                        // For now, a placeholder response:
                        if ("test@example.com".equals(email) && "test_password".equals(password)) {
                            response = "AUTH_RESP-ACCEPTED";
                            // Optionally, store client's authenticated status here
                        } else {
                            response = "AUTH_RESP-DENIED";
                        }
                    } else {
                        response = "ERROR: Malformed AUTH_EMAIL&PASSW command.";
                    }
                } else if (line.equals("AUTH_STATUS")) {
                    // TODO: Check client's authentication status
                    // For now, assuming authenticated:
                    boolean isAuthenticated = false; // Replace with actual status check
                    if (isAuthenticated) {
                        response = "AUTH-RESPONSE_ACCEPTED";
                    } else {
                        response = "AUTH-RESPONSE_DENIED";
                    }
                } else if (line.startsWith("REQ_ADD_USER-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 4) {
                        String login = parts[1];
                        String email = parts[2];
                        String password = parts[3];
                        // TODO: Implement user creation logic
                        // Check for existing login or email in your database
                        boolean loginExists = false; // Replace with actual check
                        boolean emailExists = false; // Replace with actual check

                        if (loginExists) {
                            response = "ANS_ADD_USER-DENIED-LOGIN";
                        } else if (emailExists) {
                            response = "ANS_ADD_USER-DENIED-EMAIL";
                        } else {
                            // TODO: Add user to database
                            response = "ANS_ADD_USER-ACCEPT";
                        }
                    } else {
                        response = "ERROR: Malformed REQ_ADD_USER command.";
                    }
                } else if (line.startsWith("REQ_USER_DATA_LOGIN-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 2) {
                        String requestedLogin = parts[1];
                        // TODO: Verify if the requesting client is authorized and if requestedLogin
                        // matches client's login
                        // For now, a placeholder:
                        boolean authorizedAndMatching = true; // Replace with actual logic
                        if (authorizedAndMatching) {
                            // TODO: Fetch user data from database for requestedLogin
                            // Example data format:
                            // L=login-E=email-F={login1,login2}-C={chat1-rank,chat2}-END
                            String userData = "L=" + requestedLogin
                                    + "-E=some@email.com-F={friend1,friend2}-C={chatA-USER,chatB-ADMIN}-END";
                            response = "ANS_USER_DATA-" + userData;
                        } else {
                            response = "ERROR: Unauthorized or mismatching login.";
                        }
                    } else {
                        response = "ERROR: Malformed REQ_USER_DATA_LOGIN command.";
                    }
                } else if (line.startsWith("REQ_USER_DATA_EMAIL-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 2) {
                        String requestedEmail = parts[1];
                        // TODO: Verify if the requesting client is authorized and if requestedEmail
                        // matches client's email
                        // For now, a placeholder:
                        boolean authorizedAndMatching = true; // Replace with actual logic
                        if (authorizedAndMatching) {
                            // TODO: Fetch user data from database for requestedEmail
                            // Example data format:
                            // L=login-E=email-F={login1,login2}-C={chat1-rank,chat2}-END
                            String userData = "L=someLogin-E=" + requestedEmail
                                    + "-F={friendA,friendB}-C={chatX-USER,chatY-ADMIN}-END";
                            response = "ANS_USER_DATA-" + userData;
                        } else {
                            response = "ERROR: Unauthorized or mismatching email.";
                        }
                    } else {
                        response = "ERROR: Malformed REQ_USER_DATA_EMAIL command.";
                    }
                } else if (line.startsWith("REQ_USERS_LOG-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 2) {
                        String phrase = parts[1];
                        // TODO: Implement logic to retrieve a list of logins starting with 'phrase'
                        // This part requires maintaining state (last sent index) for each client/phrase
                        // combination.
                        // For demonstration, a simple example:
                        String[] allLogins = { "userA", "userB", "userABC", "anotherUser" };
                        StringBuilder loginsToSend = new StringBuilder();
                        int count = 0;
                        for (String login : allLogins) { // This needs proper pagination logic
                            if (login.startsWith(phrase) && count < 20) {
                                if (loginsToSend.length() > 0) {
                                    loginsToSend.append(",");
                                }
                                loginsToSend.append(login);
                                count++;
                            }
                        }
                        if (loginsToSend.length() > 0) {
                            response = "ANS_USERS_LOG-L={" + loginsToSend.toString() + "}_END";
                            // TODO: Store the last sent index for this client and phrase
                        } else {
                            response = "ANS_USERS_LOG-EMPTY";
                        }
                    } else {
                        // response = "ERROR: Malformed REQ_USERS_LOG command.";
                    }
                } /*
                   * else if (line.equals("ANS_USERS_LOG-EMPTY")) {
                   * // This is a server-side response, client shouldn't send it.
                   * response = "ERROR: Invalid client command: ANS_USERS_LOG-EMPTY.";
                   * }
                   */ else if (line.startsWith("REQ_INV_LOG-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 3) {
                        String userToInvite = parts[1];
                        String invitingUser = parts[2];
                        // TODO: Implement logic to send CMD_INV_LOG to userToInvite
                        // This requires knowledge of other connected clients.
                        // response = "Server processed invitation request for " + userToInvite + " from
                        // " + invitingUser; // Client receives no direct ACK here
                    } else {
                        // response = "ERROR: Malformed REQ_INV_LOG command.";
                    }
                } else if (line.startsWith("REQ_INV_EMAIL-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 3) {
                        String emailToInvite = parts[1];
                        String invitingUser = parts[2];
                        // TODO: Implement logic to send CMD_INV_LOG to user with emailToInvite
                        // This requires knowledge of other connected clients.
                        // response = "Server processed invitation request for " + emailToInvite + "
                        // from " + invitingUser; // Client receives no direct ACK here
                    } else {
                        // response = "ERROR: Malformed REQ_INV_EMAIL command.";
                    }
                } else if (line.startsWith("REQ_INV_STATUS-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 4) {
                        String invitingUser = parts[1];
                        String invitedUser = parts[2];
                        String status = parts[3]; // ACCEPTED or DENIED
                        // TODO: Update friend status in database
                        if ("ACCEPTED".equals(status)) {
                            // TODO: Add to friends list in database
                            response = "ANS_INV_LOG-ACCEPT-" + invitedUser;
                        } else if ("DENIED".equals(status)) {
                            response = "ANS_INV_LOG-DENIED-" + invitedUser;
                        } else {
                            // response = "ERROR: Invalid invitation status.";
                        }
                    } else {
                        // response = "ERROR: Malformed REQ_INV_STATUS command.";
                    }
                } else if (line.startsWith("REQ_CRT_CHAT-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 3) {
                        String login = parts[1];
                        String chatName = parts[2];
                        // TODO: Check if chatName is already used
                        boolean chatNameUsed = false; // Replace with actual check
                        if (chatNameUsed) {
                            response = "ANS_CRT_CHAT-DENIED-USED";
                        } else {
                            // TODO: Create chat in database, set login as admin
                            boolean creationSuccess = true; // Replace with actual creation logic
                            if (creationSuccess) {
                                response = "ANS_CRT_CHAT-ACCEPT";
                            } else {
                                response = "ANS_CRT_CHAT-DENIED-ERROR";
                            }
                        }
                    } else {
                        // response = "ERROR: Malformed REQ_CRT_CHAT command.";
                    }
                } else if (line.startsWith("REQ_ADD_CHAT-")) {
                    String[] parts = line.split("-");
                    if (parts.length >= 4 && parts[0].equals("REQ_ADD_CHAT") && parts[1].startsWith("C=")
                            && parts[3].startsWith("L={")) {
                        String chatName = parts[1].substring(2); // Extract chat name
                        String sendingLogin = parts[2];
                        String usersToAddString = parts[3].substring(3, parts[3].length() - 1); // Remove L={} and {}
                        String[] usersToAdd = usersToAddString.split(",");

                        // TODO: Check if sendingLogin has admin privileges for chatName
                        boolean isAdmin = true; // Replace with actual check
                        if (isAdmin) {
                            // TODO: Add usersToAdd to the chat as regular users in database
                            boolean addSuccess = true; // Replace with actual logic
                            if (addSuccess) {
                                response = "ANS_ADD_CHAT-ACCEPT";
                            } else {
                                response = "ANS_ADD_CHAT-ERROR";
                            }
                        } else {
                            response = "ANS_ADD_CHAT-NOACCESS";
                        }
                    } else {
                        // response = "ERROR: Malformed REQ_ADD_CHAT command.";
                    }
                } else if (line.startsWith("REQ_CHAN_CHAT_NAME-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 4) {
                        String login = parts[1];
                        String oldChatName = parts[2];
                        String newChatName = parts[3];
                        // TODO: Check if login has admin privileges for oldChatName
                        boolean isAdmin = true; // Replace with actual check
                        if (isAdmin) {
                            // TODO: Change chat name in database and notify other clients
                            // (CMD_CHAN_CHAT_NAME)
                            boolean nameChangeSuccess = true; // Replace with actual logic
                            if (nameChangeSuccess) {
                                response = "ANS_CHAN_CHAT_NAME-ACCEPT";
                                // TODO: Send CMD_CHAN_CHAT_NAME to other clients in the chat
                            } else {
                                response = "ANS_CHAN_CHAT_NAME-ERROR";
                            }
                        } else {
                            response = "ANS_CHAN_CHAT_NAME-NOACCESS";
                        }
                    } else {
                        // response = "ERROR: Malformed REQ_CHAN_CHAT_NAME command.";
                    }
                } else if (line.startsWith("REQ_CHAN_CHAT_RANK-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 5) {
                        String chatName = parts[1];
                        String requestingLogin = parts[2];
                        String changedLogin = parts[3];
                        String rank = parts[4]; // USER or ADMIN
                        // TODO: Check if requestingLogin has admin privileges for chatName
                        boolean isAdmin = true; // Replace with actual check
                        if (isAdmin) {
                            // TODO: Change rank of changedLogin in chatName in database
                            boolean rankChangeSuccess = true; // Replace with actual logic
                            if (rankChangeSuccess) {
                                response = "ANS_CHAN_CHAT_RANK-ACCEPT";
                            } else {
                                response = "ANS_CHAN_CHAT_RANK-ERROR";
                            }
                        } else {
                            response = "ANS_CHAN_CHAT_RANK-NOACCESS";
                        }
                    } else {
                        // response = "ERROR: Malformed REQ_CHAN_CHAT_RANK command.";
                    }
                } else if (line.startsWith("REQ_DEL_CHAT-")) {
                    String[] parts = line.split("-");
                    if (parts.length >= 4 && parts[0].equals("REQ_DEL_CHAT") && parts[1].startsWith("C=")
                            && parts[3].startsWith("L={")) {
                        String chatName = parts[1].substring(2); // Extract chat name
                        String sendingLogin = parts[2];
                        String usersToRemoveString = parts[3].substring(3, parts[3].length() - 1); // Remove L={} and {}
                        String[] usersToRemove = usersToRemoveString.split(",");

                        // TODO: Check if sendingLogin has admin privileges for chatName
                        boolean isAdmin = true; // Replace with actual check
                        if (isAdmin) {
                            // TODO: Remove usersToRemove from the chat in database
                            boolean removeSuccess = true; // Replace with actual logic
                            if (removeSuccess) {
                                response = "ANS_DEL_CHAT-ACCEPT";
                            } else {
                                response = "ANS_DEL_CHAT-ERROR";
                            }
                        } else {
                            response = "ANS_DEL_CHAT-NOACCESS";
                        }
                    } else {
                        // response = "ERROR: Malformed REQ_DEL_CHAT command.";
                    }
                } else if (line.startsWith("REQ_DES_CHAT-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 3) {
                        String login = parts[1];
                        String chatName = parts[2];
                        // TODO: Check if login has admin privileges for chatName
                        boolean isAdmin = true; // Replace with actual check
                        if (isAdmin) {
                            // TODO: Delete chat from database and notify other clients (CMD_DES_CHAT)
                            boolean deleteSuccess = true; // Replace with actual logic
                            if (deleteSuccess) {
                                response = "ANS_DES_CHAT-ACCEPT";
                                // TODO: Send CMD_DES_CHAT to all clients in the chat
                            } else {
                                response = "ANS_DES_CHAT-DENIED-ERROR";
                            }
                        } else {
                            response = "ANS_DES_CHAT-DENIED-NOACCESS";
                        }
                    } else {
                        // response = "ERROR: Malformed REQ_DES_CHAT command.";
                    }
                } else if (line.startsWith("SEND_DATA-")) {
                    String[] parts = line.split("-");
                    if (parts.length >= 4) { // login-chatname-data (data can contain hyphens)
                        String login = parts[1];
                        String chatName = parts[2];
                        String data = line.substring(line.indexOf(chatName) + chatName.length() + 1); // Get all data
                                                                                                      // after chatName
                        // TODO: Store data in database for chatName, associated with login and
                        // timestamp
                        // And send CMD_WRITE_DATA to other clients in chat
                        boolean sendSuccess = true; // Replace with actual logic
                        if (sendSuccess) {
                            long timestamp = System.currentTimeMillis(); // Example timestamp
                            response = "ANS_SEND_DATA-ACCEPT-" + timestamp;
                            // TODO: Send CMD_WRITE_DATA to other clients in the chat (excluding the sender)
                        } else {
                            // TODO: Determine specific reason for denial (e.g., chat doesn't exist)
                            boolean badChatName = false; // Replace with actual check
                            if (badChatName) {
                                response = "ANS_SEND_DATA-DENIED-NAME";
                            } else {
                                response = "ANS_SEND_DATA-DENIED-ERROR";
                            }
                        }
                    } else {
                        // response = "ERROR: Malformed SEND_DATA command.";
                    }
                } else if (line.startsWith("GET_DATA-")) {
                    String[] parts = line.split("-");
                    if (parts.length == 3) {
                        String login = parts[1];
                        String chatNameSuffix = parts[2]; // e.g., nazwa_czatu_CON or nazwa_czatu_NEW
                        String chatName = chatNameSuffix.substring(0, chatNameSuffix.lastIndexOf("_"));
                        String commandType = chatNameSuffix.substring(chatNameSuffix.lastIndexOf("_") + 1);

                        // TODO: Implement logic to retrieve messages from database for chatName
                        // Differentiate based on commandType (CON for continuation, NEW for reset)
                        // Retrieve max 20 messages, newest first for GET_DATA_NEW, and then older for
                        // GET_DATA_CON
                        boolean chatExists = true; // Replace with actual check

                        /*
                         * 
                         * 
                         * 
                         * TO ZOBACZYC BO JAKIEŚ DZIWNE
                         * 
                         * 
                         * 
                         */
                        if (chatExists) {
                            // Example messages:
                            String[] messages = { "message1", "message2", "message3" }; // Replace with actual fetched
                                                                                        // messages
                            if (messages.length > 0) {
                                StringBuilder dataToSend = new StringBuilder();
                                for (String msg : messages) {
                                    if (dataToSend.length() > 0) {
                                        dataToSend.append("\n"); // Or your chosen separator
                                    }
                                    dataToSend.append(msg);
                                }
                                response = "ANS_GET_DATA-" + chatName + "-" + dataToSend.toString();
                                // TODO: Update server-side message index for this client and chat
                            } else {
                                response = "ANS_GET_DATA-END";
                            }
                        } else {
                            response = "ANS_GET_DATA-DENIED-NAME";
                        }
                    } else {
                        // response = "ERROR: Malformed GET_DATA command.";
                    }
                } else if (line.startsWith("SEND_FILE-")) {
                    String[] parts = line.split("-");
                    if (parts.length >= 4) { // login-chatname-data (data can contain hyphens)
                        String login = parts[1];
                        String chatName = parts[2];
                        String fileData = line.substring(line.indexOf(chatName) + chatName.length() + 1); // Get all
                                                                                                          // data after
                                                                                                          // chatName
                        // TODO: Handle file data (e.g., save to a temp location, store metadata in DB)
                        // Send CMD_WRITE_FILE to other clients in chat
                        boolean sendSuccess = true; // Replace with actual logic
                        if (sendSuccess) {
                            response = "ANS_SEND_FILE-ACCEPT";
                            // TODO: Send CMD_WRITE_FILE to other clients in the chat (excluding the sender)
                        } else {
                            response = "ANS_SEND_FILE-ERROR";
                        }
                    } else {
                        // response = "ERROR: Malformed SEND_FILE command.";
                    }
                } else if (line.equals("REQ_CONN_END")) {
                    response = "ANS_CONN_END";
                    sendBytes(response.getBytes()); // Send response before breaking
                    System.out.println("Client " + clientSocket.getInetAddress() + " requested disconnect.");
                    break; // Exit the loop to close the connection
                } else {
                    // response = "ERROR: Unknown command.";
                }

                // Send the response back to the client
                sendBytes(response.getBytes());

                // HERE END

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
