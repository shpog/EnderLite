package com.EnderLite.Controller;

import java.lang.reflect.Modifier;

import com.EnderLite.Model.*;
import java.util.UUID;

public class Controller {

    public Boolean isAuth = false;
    public Model model;
    public User user;

    public Controller() {
        model = new Model();
    }

    public String AUTH_LOG(String login, String password) {
        try {
            user = model.findUser(login);
            if (user.PasswordHash.equals(password)) {
                isAuth = true;
                return "AUTH_RESP-" + user.Login + "-" + user.Email;
            }
            return "AUTH_RESP-DENIED";

        } catch (Error e) {
            e.printStackTrace();
        }
        return "AUTH_RESP-DENIED";
    }

    public String AUTH_EMAIL(String email, String password) {
        try {
            user = model.findUser(email);
            if (user.PasswordHash.equals(password)) {
                isAuth = true;
                return "AUTH_RESP-" + user.Login + "-" + user.Email;
            }
            return "AUTH_RESP-DENIED";

        } catch (Error e) {
            e.printStackTrace();
        }
        return "AUTH_RESP-DENIED";
    }

    public String AUTH_STATUS() {
        if (isAuth) {
            return "AUTH_RESP-login-email";
        }
        return "AUTH-RESPONSE_DENIED";
    }

    public String REQ_ADD_USER(String login, String email, String password) {
        try {
            user = model.findUser(email);
            if (user != null)
                if (user.Login == login)
                    return "ANS_ADD_USER-DENIED-LOGIN";
                else
                    return "ANS_ADD_USER-DENIED-EMAIL";

            user = new User();
            user.Login = login;
            user.Email = email;
            user.PasswordHash = password;
            model.modifyOrCreateUser(user);
            return "ANS_ADD_USER-" + login + "-" + "email";

        } catch (Error e) {
            e.printStackTrace();
            return "ANS_ADD_USER-ERROR";
        }
    }

    public String REQ_USER_DATA(String requestedLogin) {
        if (!isAuth)
            return "ANS_USER_DATA-DENIED-NOACCESS";

        try {
            User u = model.findUser(requestedLogin);
            if (u == null)
                return "ANS_USER_DATA-DENIED-ERROR";

            String userData = "L=" + requestedLogin
                    + "-E=" + u.Email + "-F={";

            for (UUID uuid : u.FriendsList) {
                userData += model.getUser(uuid).Login + ",";
            }

            userData += "}-C={";
            // userData += "}-C={" + "chatA-USER,chatB-ADMIN";
            // TODO

            for (UUID uuid : u.ChatsList) {
                userData += model.getChat(uuid).Name + ",";
            }

            userData += "}-END";
            return "ANS_USER_DATA-" + userData;

        } catch (Error e) {
            e.printStackTrace();
            return "ANS_USER_DATA-DENIED-ERROR";

        }
    }

    /*
     * public String REQ_USERS_LOG(String phrase) {
     * // Implement logic to retrieve a list of logins starting with 'phrase'
     * // This part requires maintaining state (last sent index) for
     * eachclient/phrase
     * // combination.
     * // For demonstration, a simple example:
     * String[] allLogins = { "userA", "userB", "userABC", "anotherUser" };
     * StringBuilder loginsToSend = new StringBuilder();
     * int count = 0;
     * for (String login : allLogins) { // This needs proper pagination logic
     * if (login.startsWith(phrase) && count < 20) {
     * if (loginsToSend.length() > 0) {
     * loginsToSend.append(",");
     * }
     * loginsToSend.append(login);
     * count++;
     * }
     * }
     * if (loginsToSend.length() > 0) {
     * // Store the last sent index for this client and phrase
     * return "ANS_USERS_LOG-L={" + loginsToSend.toString() + "}_END";
     * }
     * 
     * return "ANS_USERS_LOG-EMPTY";
     * }
     * 
     */

    public void REQ_INV_LOG(String userToInvite, String invitingUser) {
        // if (!isAuth)
        // return "ANS_USER_DATA-DENIED-NOACCESS";
        // TODO: Implement logic to send CMD_INV_LOG to userToInvite
        // This requires knowledge of other connected clients.
        // response = "Server processed invitation request for " + userToInvite + " from
        // " + invitingUser; // Client receives no direct ACK here
        // CMD_INV_LOG-(login_zapraszającego)
    }

    public void REQ_INV_EMAIL(String emailToInvite, String invitingUser) {
        // TODO: Implement logic to send CMD_INV_LOG to userToInvite
        // This requires knowledge of other connected clients.
        // response = "Server processed invitation request for " + userToInvite + " from
        // " + invitingUser; // Client receives no direct ACK here
        // CMD_INV_LOG-(login_zapraszającego)
    }

    /* DODAć to */
    public void REQ_DEL_LOG(String userToDelete, String deletingUser) {
        // TODO: Implement logic to send CMD_INV_LOG to userToInvite
        // This requires knowledge of other connected clients.
        // response = "Server processed invitation request for " + userToInvite + " from
        // " + invitingUser; // Client receives no direct ACK here
        // CMD_INV_LOG-(login_zapraszającego)
    }

    // /* DODAć to */
    // public void CMD_DEL_LOG(String userToInvite, String invitingUser) {
    // // TODO: Implement logic to send CMD_INV_LOG to userToInvite
    // // This requires knowledge of other connected clients.
    // // response = "Server processed invitation request for " + userToInvite + "
    // from
    // // " + invitingUser; // Client receives no direct ACK here
    // // CMD_INV_LOG-(login_zapraszającego)
    // }

    public String REQ_INV_STATUS(String invitingUser, String invitedUser, String status) {
        // TODO: Update friend status in database
        if ("ACCEPTED".equals(status)) {
            // TODO: Add to friends list in database
            return "ANS_INV_LOG-ACCEPT-" + invitedUser;
        } else if ("DENIED".equals(status)) {
            return "ANS_INV_LOG-DENIED-" + invitedUser;
        }

        return "ANS_INV_LOG-DENIED-" + invitedUser;
    }

    public String REQ_CRT_CHAT(String login, String chatname) {
        // TODO: Check if chatName is already used
        boolean chatNameUsed = false; // Replace with actual check
        if (chatNameUsed) {
            return "ANS_CRT_CHAT-DENIED-USED";
        }
        // TODO: Create chat in database, set login as admin
        boolean creationSuccess = true; // Replace with actual creation logic
        if (creationSuccess) {
            return "ANS_CRT_CHAT-ACCEPT";
        }

        return "ANS_CRT_CHAT-DENIED-ERROR";
    }

    public String REQ_ADD_CHAT(String chatName, String sendingLogin, String[] usersToAdd) {
        // TODO: Check if sendingLogin has admin privileges for chatName
        boolean isAdmin = true; // Replace with actual check
        if (isAdmin) {
            // TODO: Add usersToAdd to the chat as regular users in database
            boolean addSuccess = true; // Replace with actual logic
            if (addSuccess) {
                return "ANS_ADD_CHAT-ACCEPT";
            }
            return "ANS_ADD_CHAT-DENIED-ERROR";
        }
        return "ANS_ADD_CHAT-DENIED-NOACCESS";
    }

    public String REQ_CHAN_CHAT_NAME(String login, String oldChatName, String newChatName) {
        // TODO: Check if login has admin privileges for oldChatName
        boolean isAdmin = true; // Replace with actual check
        if (isAdmin) {
            // TODO: Change chat name in database and notify other clients
            // (CMD_CHAN_CHAT_NAME)
            boolean nameChangeSuccess = true; // Replace with actual logic
            if (nameChangeSuccess) {
                // TODO: Send CMD_CHAN_CHAT_NAME to other clients in the chat
                return "ANS_CHAN_CHAT_NAME-ACCEPT";
            }
            return "ANS_CHAN_CHAT_NAME-ERROR";
        }

        return "ANS_CHAN_CHAT_NAME-NOACCESS";
    }

    public String REQ_CHAN_CHAT_RANK(String chatName, String requestingLogin, String changedLogin, String rank) {
        // TODO: Check if requestingLogin has admin privileges for chatName
        boolean isAdmin = true; // Replace with actual check
        if (isAdmin) {
            // TODO: Change rank of changedLogin in chatName in database
            boolean rankChangeSuccess = true; // Replace with actual logic
            if (rankChangeSuccess) {
                return "ANS_CHAN_CHAT_RANK-ACCEPT";
            }
            return "ANS_CHAN_CHAT_RANK-DENIED-ERROR";
        }
        return "ANS_CHAN_CHAT_RANK-DENIED-NOACCESS";
    }

    public String REQ_DEL_CHAT(String chatName, String sendingLogin, String[] usersToRemove) {
        // TODO: Check if sendingLogin has admin privileges for chatName
        boolean isAdmin = true; // Replace with actual check
        if (isAdmin) {
            // TODO: Remove usersToRemove from the chat in database
            boolean removeSuccess = true; // Replace with actual logic
            if (removeSuccess) {
                return "ANS_DEL_CHAT-ACCEPT";
            }
            return "ANS_DEL_CHAT-DENIED-ERROR";
        }
        return "ANS_DEL_CHAT-DENIED-NOACCESS";
    }

    public String REQ_DES_CHAT(String login, String chatName) {
        // TODO: Check if login has admin privileges for chatName
        boolean isAdmin = true; // Replace with actual check
        if (isAdmin) {
            // TODO: Delete chat from database and notify other clients (CMD_DES_CHAT)
            boolean deleteSuccess = true; // Replace with actual logic
            if (deleteSuccess) {
                // TODO: Send CMD_DES_CHAT to all clients in the chat
                return "ANS_DES_CHAT-ACCEPT";
            }
            return "ANS_DES_CHAT-DENIED-ERROR";
        }
        return "ANS_DES_CHAT-DENIED-NOACCESS";
    }

    public String SEND_DATA(String login, String chatName, String data) {
        // TODO: Store data in database for chatName, associated with login and
        // timestamp
        // And send CMD_WRITE_DATA to other clients in chat
        boolean sendSuccess = true; // Replace with actual logic
        if (sendSuccess) {
            long timestamp = System.currentTimeMillis();
            // TODO: Send CMD_WRITE_DATA to other clients in the chat (excluding the sender)
            return "ANS_SEND_DATA-ACCEPT-" + timestamp;
        }
        // TODO: Determine specific reason for denial (e.g., chat doesn't exist)
        boolean badChatName = false; // Replace with actual check
        if (badChatName) {
            return "ANS_SEND_DATA-DENIED-NAME";
        }
        return "ANS_SEND_DATA-DENIED-ERROR";
    }

    // public String GET_DATA(String login, String chatName, String commandType) {
    // // TODO: Implement logic to retrieve messages from database for chatName
    // // Differentiate based on commandType (CON for continuation, NEW for reset)
    // // Retrieve max 20 messages, newest first for GET_DATA_NEW, and then older
    // for

    // boolean chatExists = true; // Replace with actual check
    // if (chatExists) {
    // // Example messages:
    // String[] messages = { "message1", "message2", "message3" }; // Replace with
    // actual fetched
    // // messages
    // if (messages.length > 0) {
    // StringBuilder dataToSend = new StringBuilder();
    // for (String msg : messages) {
    // if (dataToSend.length() > 0) {
    // dataToSend.append("\n"); // Or your chosen separator
    // }
    // dataToSend.append(msg);
    // }

    // // TODO: Update server-side message index for this client and chat
    // return "ANS_GET_DATA-" + chatName + "-" + dataToSend.toString();
    // }
    // return "ANS_GET_DATA-END";
    // }
    // return "ANS_GET_DATA-DENIED-NAME";
    // }

    // public String SEND_FILE(String login, String chatName, String fileData) {
    // // TODO: Handle file data (e.g., save to a temp location, store metadata in
    // DB)
    // // Send CMD_WRITE_FILE to other clients in chat
    // boolean sendSuccess = true; // Replace with actual logic
    // if (sendSuccess) {
    // // TODO: Send CMD_WRITE_FILE to other clients in the chat (excluding the
    // sender)
    // return "ANS_SEND_FILE-ACCEPT";
    // }
    // return "ANS_SEND_FILE-ERROR";
    // }

}
