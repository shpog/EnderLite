package com.EnderLite.Controller;

import java.lang.reflect.Modifier;

import com.EnderLite.Model.*;
import com.EnderLite.Utils.*;

import java.util.*;

public class Controller {

    public Boolean isAuth = false;
    public Model model;
    public User user;
    public List<ClientHandler> handlers;

    public Controller(List<ClientHandler> handlerList) {
        model = new Model();
        handlers = handlerList;
    }

    public void CMD(String cmd) {
        System.out.println("Sending command " + cmd);
        System.out.println("Detected " + handlers.size() + " handlers");

        for (ClientHandler clientHandler : handlers) {
            if (clientHandler.ctrl.user == user)
                continue;
            new CMDHandler(clientHandler.clientSocket, cmd, clientHandler.secretKey).start();

            System.out.println("Message " + cmd + "sent to" +
                    clientHandler.ctrl.user.ID.toString());

        }
    }

    public void CMD(String cmd, UUID uuid) {
        System.out.println("Sending command " + cmd);
        System.out.println("Detected " + handlers.size() + "handlers");

        for (ClientHandler clientHandler : handlers) {
            System.out.println("Detected user " + clientHandler.ctrl.user.ID.toString());
            if (clientHandler.ctrl.user.ID.equals(uuid)) {
                System.out.println("Message " + cmd + " sent to " +
                        clientHandler.ctrl.user.ID.toString());
                new CMDHandler(clientHandler.clientSocket, cmd, clientHandler.secretKey).start();
                return;
            }
        }
    }

    public String AUTH_LOG(String login, String password) {
        try {
            user = model.findUser(login);
            if (user.PasswordHash.equals(password)) {
                isAuth = true;
                return "AUTH_RESP-" + user.Login + "-" + user.Email;
            }
            return "AUTH_RESP-DENIED-";

        } catch (Exception e) {
            return "AUTH_RESP-DENIED-";
        }

    }

    public String AUTH_EMAIL(String email, String password) {
        try {
            user = model.findUser(email);
            if (user.PasswordHash.equals(password)) {
                isAuth = true;
                return "AUTH_RESP-" + user.Login + "-" + user.Email;
            }
            System.out.println("User not found");
            return "AUTH_RESP-DENIED-";

        } catch (Exception e) {
            return "AUTH_RESP-DENIED-";
        }
    }

    public String AUTH_STATUS() {
        if (isAuth) {
            return "AUTH_RESP-login-email";
        }
        return "AUTH-RESPONSE_DENIED-";
    }

    public String REQ_ADD_USER(String login, String email, String password) {
        try {
            user = model.findUser(email);
            if (user != null)
                if (user.Login.equals(login))
                    return "ANS_ADD_USER-DENIED-LOGIN";
                else
                    return "ANS_ADD_USER-DENIED-EMAIL";

            user = new User();
            user.ID = UUID.randomUUID();
            user.Login = login;
            user.Email = email;
            user.PasswordHash = password;
            model.modifyOrCreateUser(user);
            isAuth = true;

            return "ANS_ADD_USER-" + login + "-" + email;

        } catch (Exception e) {
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
                    + "-E=" + u.Email;

            if (u.FriendsList.size() > 0)
                userData += "-F=";

            for (UUID uuid : u.FriendsList) {
                User friend = model.getUser(uuid);
                userData += friend.Login + ",";
            }

            if (u.ChatsList.size() > 0)
                userData += "-C=";

            for (UUID uuid : u.ChatsList) {

                Chat chat = model.getChat(uuid);
                userData += chat.Name + "-";

                if (chat.Admins.contains(u.ID))
                    userData += "ADMIN";
                else
                    userData += "USER";
                userData += ",";
            }

            userData += "-END";
            return "ANS_USER_DATA-" + userData;

        } catch (Exception e) {
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
        if (!isAuth)
            return;
        try {
            CMD("CMD_INV_LOG-" + invitingUser, model.findUser(userToInvite).ID);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void REQ_INV_EMAIL(String emailToInvite, String invitingUser) {
        if (!isAuth)
            return;
        try {
            CMD("CMD_INV_LOG-" + invitingUser, model.findUser(emailToInvite).ID);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public String REQ_DEL_LOG(String userToDelete, String deletingUser) {
        try {
            CMD("CMD_DEL_LOG-" + deletingUser, model.findUser(userToDelete).ID);
            User u1 = model.findUser(userToDelete);
            User u2 = model.findUser(deletingUser);

            u1.FriendsList.removeIf(n -> n == u2.ID);
            u2.FriendsList.removeIf(n -> n == u1.ID);

            System.out.println("REQ_DEL_STATUS:: " + u2.ID + u1.ID);

            model.modifyOrCreateUser(u1);
            model.modifyOrCreateUser(u2);

            CMD("ANS_INV_LOG-ACCEPT-" + userToDelete, model.findUser(deletingUser).ID);
            return "ANS_DEL_LOG-ACCEPT-" + userToDelete;
        } catch (Exception e) {
            return "ANS_DEL_LOG-DENIED-" + userToDelete;
        }
    }

    public void REQ_INV_STATUS(String invitingUser, String invitedUser, String status) {
        if ("ACCEPT".equals(status)) {
            try {
                User u1 = model.findUser(invitingUser);
                User u2 = model.findUser(invitedUser);

                u1.FriendsList.add(u2.ID);
                u2.FriendsList.add(u1.ID);

                System.out.println("REQ_INV_STATUS:: " + u2.ID + u1.ID);

                model.modifyOrCreateUser(u1);
                model.modifyOrCreateUser(u2);

                CMD("ANS_INV_LOG-ACCEPT-" + invitedUser, model.findUser(invitingUser).ID);
            } catch (Exception e) {
                System.out.println("REQ_INV_STATUS::ERROR");

                e.printStackTrace();
            }

        } else if ("DENIED".equals(status)) {
            try {
                CMD("ANS_INV_LOG-DENIED-" + invitedUser, model.findUser(invitingUser).ID);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public String REQ_CRT_CHAT(String login, String chatName) {
        if (!isAuth)
            return "ANS_CRT_CHAT-DENIED-ERROR";

        try {
            Chat chat = model.findChat(chatName);
            if (chat != null)
                return "ANS_CRT_CHAT-DENIED- USED";

            chat = new Chat();
            chat.ID = UUID.randomUUID();
            chat.Name = chatName;
            chat.Admins.add(model.findUser(login).ID);

            model.modifyOrCreateChat(chat);

            user = model.getUser(user.ID);
            user.ChatsList.add(chat.ID);

            model.modifyOrCreateUser(user);

            return "ANS_CRT_CHAT-ACCEPT";

        } catch (Exception e) {
            e.printStackTrace();
            return "ANS_CRT_CHAT-DENIED-ERROR";
        }
    }

    public String REQ_ADD_CHAT(String chatName, String sendingLogin, String[] usersToAdd) {
        if (!isAuth)
            return "ANS_ADD_CHAT-DENIED-ERROR";

        try {
            Chat chat = model.findChat(chatName);
            User sendingUser = model.findUser(sendingLogin);

            if (!chat.Admins.contains(sendingUser.ID))
                return "ANS_ADD_CHAT-DENIED-NOACCESS";

            System.out.println(usersToAdd[0]);
            for (String userLogin : usersToAdd) {
                User u = model.findUser(userLogin);
                u.ChatsList.add(chat.ID);
                chat.Members.add(u.ID);
                model.modifyOrCreateUser(u);

                CMD("CMD_ADD_CHAT-" + chatName, u.ID);
            }
            model.modifyOrCreateChat(chat);

            return "ANS_ADD_CHAT-ACCEPT";

        } catch (Exception e) {
            e.printStackTrace();
            return "ANS_ADD_CHAT-DENIED-ERROR";
        }
    }

    public String REQ_CHAN_CHAT_NAME(String login, String oldChatName, String newChatName) {
        if (!isAuth)
            return "ANS_CHAN_CHAT_NAME-ERROR";

        try {
            Chat chat = model.findChat(oldChatName);
            User sendingUser = model.findUser(login);

            if (!chat.Admins.contains(sendingUser.ID))
                return "ANS_CHAN_CHAT_NAME-NOACCESS";

            chat.Name = newChatName;
            model.modifyOrCreateChat(chat);

            CMD("CMD_CHAN_CHAT_NAME-" + oldChatName + "-" + newChatName);

            return "ANS_CHAN_CHAT_NAME-ACCEPT";

        } catch (Exception e) {
            e.printStackTrace();
            return "ANS_CHAN_CHAT_NAME-ERROR";
        }
    }

    public String REQ_CHAN_CHAT_RANK(String chatName, String requestingLogin, String changedLogin, String rank) {
        if (!isAuth)
            return "ANS_CHAN_CHAT_RANK-DENIED-ERROR";

        try {
            Chat chat = model.findChat(chatName);
            User sendingUser = model.findUser(requestingLogin);

            if (!chat.Admins.contains(sendingUser.ID))
                return "ANS_CHAN_CHAT_RANK-DENIED-NOACCESS";

            User changedUser = model.findUser(changedLogin);
            if (rank.equals("ADMIN"))
                chat.Admins.add(changedUser.ID);
            else if (chat.Admins.contains(changedUser.ID))
                chat.Admins.removeIf(n -> n == changedUser.ID);
            model.modifyOrCreateChat(chat);

            return "ANS_CHAN_CHAT_RANK-ACCEPT";

        } catch (Exception e) {
            e.printStackTrace();
            return "ANS_CHAN_CHAT_RANK-DENIED-ERROR";
        }
    }

    public String REQ_DEL_CHAT(String chatName, String sendingLogin, String[] usersToRemove) {
        if (!isAuth)
            return "ANS_DEL_CHAT-DENIED-ERROR";

        try {
            Chat chat = model.findChat(chatName);
            User sendingUser = model.findUser(sendingLogin);

            if (!chat.Admins.contains(sendingUser.ID))
                return "ANS_DEL_CHAT-DENIED-NOACCESS";

            for (String loginToRemove : usersToRemove) {
                User userToRemove = model.findUser(loginToRemove);
                if (chat.Members.contains(userToRemove.ID))
                    chat.Members.removeIf(n -> n == userToRemove.ID);
            }

            model.modifyOrCreateChat(chat);

            return "ANS_DEL_CHAT-ACCEPT";

        } catch (Exception e) {
            e.printStackTrace();
            return "ANS_DEL_CHAT-DENIED-ERROR";

        }
    }

    public String REQ_DES_CHAT(String login, String chatName) {
        if (!isAuth)
            return "ANS_DES_CHAT-DENIED-ERROR";

        try {
            Chat chat = model.findChat(chatName);
            User sendingUser = model.findUser(login);

            if (!chat.Admins.contains(sendingUser.ID))
                return "ANS_DES_CHAT-DENIED-NOACCESS";

            model.removeChat(chat.ID);
            CMD("CMD_DES_CHAT-" + chatName);

            return "ANS_DES_CHAT-ACCEPT";

        } catch (Exception e) {
            e.printStackTrace();
            return "ANS_DES_CHAT-DENIED-ERROR";
        }
    }

    public String SEND_DATA(String login, String chatName, String data) {
        // TODO: Store data in database for chatName, associated with login and
        // timestamp
        // And send CMD_WRITE_DATA to other clients in chat
        boolean sendSuccess = true; // Replace with actual logic
        if (sendSuccess) {
            long timestamp = System.currentTimeMillis();
            return "ANS_SEND_DATA-ACCEPT-" + timestamp;
        }

        boolean badChatName = false; // Replace with actual check
        if (badChatName) {
            return "ANS_SEND_DATA-DENIED-NAME";
        }
        return "ANS_SEND_DATA-DENIED-ERROR";
    }

    // public String GET_DATA(String login, String chatName, String commandType) {
    // // TOO: Implement logic to retrieve messages from database for chatName
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

    // // TDO: Update server-side message index for this client and chat
    // return "ANS_GET_DATA-" + chatName + "-" + dataToSend.toString();
    // }
    // return "ANS_GET_DATA-END";
    // }
    // return "ANS_GET_DATA-DENIED-NAME";
    // }

    // public String SEND_FILE(String login, String chatName, String fileData) {
    // // TDO: Handle file data (e.g., save to a temp location, store metadata in
    // DB)
    // // Send CMD_WRITE_FILE to other clients in chat
    // boolean sendSuccess = true; // Replace with actual logic
    // if (sendSuccess) {
    // // TOO: Send CMD_WRITE_FILE to other clients in the chat (excluding the
    // sender)
    // return "ANS_SEND_FILE-ACCEPT";
    // }
    // return "ANS_SEND_FILE-ERROR";
    // }

}
