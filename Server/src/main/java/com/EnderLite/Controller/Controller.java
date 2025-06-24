package com.EnderLite.Controller;

import java.lang.reflect.Modifier;

import com.EnderLite.Model.*;
import com.EnderLite.Utils.*;

import java.util.*;

/**
 * Controller class - responisble for all decisions, operations and verification
 */
public class Controller {

    /**
     * Stores information about authortization of the current session.
     */
    public Boolean isAuth = false;
    /**
     * Model corresponding to this controller
     */
    public Model model;
    /**
     * User connected to this controller
     */
    public User user;
    /**
     * List of all handlers; used in async messages
     */
    public List<ClientHandler> handlers;

    /**
     * Initializes model and assigns handlers
     * 
     * @param handlerList List of handlers
     */
    public Controller(List<ClientHandler> handlerList) {
        model = new Model();
        handlers = handlerList;
    }

    /**
     * Method responsible for sending async messages for every connected client
     * (except the sender)
     * 
     * @param cmd Command to send
     * 
     */
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

    /**
     * Method responsible for sending async messages for known user
     * 
     * @param cmd  Command to send
     * @param uuid ID of known user
     * 
     */
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

    /**
     * Responsible for authorization by login
     * 
     * @param login    User login
     * @param password User password
     * 
     * @return Response
     * 
     */
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

    /**
     * Responsible for authorization by email
     * 
     * @param email    User email
     * @param password User password
     * 
     * @return Response
     * 
     */
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

    /**
     * Responsible for confirming authorization
     * 
     * @return Response
     * 
     */
    public String AUTH_STATUS() {
        if (isAuth) {
            return "AUTH_RESP-login-email";
        }
        return "AUTH-RESPONSE_DENIED-";
    }

    /**
     * Responsible for creating new account
     * 
     * @param login    User login
     * @param email    User email
     * @param password User password
     * 
     * @return Response
     * 
     */
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

    /**
     * Responsible for retrieving user data from database
     * Can be efficiently invoked only when session is Authorized
     * 
     * @param requestedLogin User login
     * 
     * @return Response
     * 
     */
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
    /**
     * Responsible for sending friend invites
     * Can be efficiently invoked only when session is Authorized
     * 
     * @param userToInvite Invited user login
     * @param invitingUser Sending user login
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

    /**
     * Responsible for sending friend invites
     * Can be efficiently invoked only when session is Authorized
     * 
     * @param emailToInvite Invited user email
     * @param invitingUser  Sending user login
     * 
     */
    public void REQ_INV_EMAIL(String emailToInvite, String invitingUser) {
        if (!isAuth)
            return;
        try {
            CMD("CMD_INV_LOG-" + invitingUser, model.findUser(emailToInvite).ID);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    /**
     * Responsible for deleting friends from friends list
     * Can be efficiently invoked only when session is Authorized
     * 
     * @param userToDelete Deleted user email
     * @param deletingUser Deleting user login
     * 
     * @return Response
     * 
     */
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

    /**
     * Responsible for confirmation of friend invites
     * Can be efficiently invoked only when session is Authorized
     * 
     * @param invitingUser Sending user login
     * @param invitedUser  Invited user email
     * @param status       Invite status; ACCEPT to accept an invite, DENIED to deny
     *                     it
     * 
     */
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

    /**
     * Responsible for creating Chat
     * Can be efficiently invoked only when session is Authorized
     * 
     * @param login    User login
     * @param chatName Created chat name
     * 
     * @return Response
     * 
     */
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
            chat.Members.add(model.findUser(login).ID);

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

    /**
     * Responsible for inviting users to chat
     * Can be efficiently invoked only when session is Authorized
     * 
     * @param sendingLogin User login
     * @param chatName     Selected chat name
     * @param usersToAdd   Array of logins of users that should be added to the chat
     * 
     * @return Response
     * 
     */
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

    /**
     * Responsible for changing chat name
     * Can be efficiently invoked only when session is Authorized
     * 
     * @param login       User login
     * @param oldChatName Old chat name
     * @param newChatName New chat name
     * 
     * @return Response
     * 
     */
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

    /**
     * Responsible for changing members rank in the chat
     * Can be efficiently invoked only when session is Authorized
     * 
     * @param requestingLogin Sender user login
     * @param chatName        Selected chat name
     * @param changedLogin    Target user login
     * @param rank            New rank; Can be either ADMIN or USER
     * 
     * @return Response
     * 
     */
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

    /**
     * Responsible for removing Users from Chat
     * Can be efficiently invoked only when session is Authorized
     * 
     * @param sendingLogin  User login
     * @param chatName      Created chat name
     * @param usersToRemove Array of logins of users that should be removed from the
     *                      chat
     * @return Response
     * 
     */
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

    /**
     * Responsible for destroying Chat
     * Can be efficiently invoked only when session is Authorized
     * 
     * @param login    User login
     * @param chatName Destroyed chat name
     * 
     * @return Response
     * 
     */
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

    /**
     * Responsible for sending messages to a chat
     * Can be efficiently invoked only when session is Authorized
     * 
     * @param login    User login
     * @param chatName Chat name
     * @param data     Message content
     * 
     * @return Response
     * 
     */
    public String SEND_DATA(String login, String chatName, String data) {
        if (!isAuth)
            return "ANS_SEND_DATA-DENIED-ERROR";

        try {
            Chat chat = model.findChat(chatName);
            User sendingUser = model.findUser(login);

            if (!chat.Members.contains(sendingUser.ID))
                return "ANS_SEND_DATA-DENIED-ERROR";

            MessageEntry msg = new MessageEntry();
            msg.Sender = sendingUser;
            msg.Date = new Date();
            long timestamp = msg.Date.getTime();
            msg.Content = data;

            model.createMessageEntry(chat.ID, msg);

            CMD("CMD_WRITE_DATA-" + chatName + "-" + login + "-" + data + "-" + String.valueOf(timestamp));

            return "ANS_SEND_DATA-ACCEPT-" + String.valueOf(timestamp);

        } catch (Exception e) {
            e.printStackTrace();
            return "ANS_SEND_DATA-DENIED-NAME";
        }
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
