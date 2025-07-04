package com.EnderLite.Model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.Date;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.prefs.BackingStoreException;

/**
 * Class representing Model, it interacts directly with the database.
 */
public class Model {

    /**
     * Method requesting User of certain ID
     * 
     * @param uuid requested User ID
     * @return requested User
     * @see User
     */
    public User getUser(UUID uuid) {
        Preferences prefs = Preferences.userRoot().node(this.getClass().getName()).node("Users");
        JSONObject jsonObject = new JSONObject(
                prefs.get(uuid.toString(), "{\"login\":\"none\",\"email\":\"none\",\"passwordHash\":\"none\"}"));

        User user = new User();
        user.ID = uuid;
        user.Login = jsonObject.getString("login");
        user.Email = jsonObject.getString("email");
        user.PasswordHash = jsonObject.getString("passwordHash");

        user.FriendsList = new ArrayList<UUID>();
        JSONArray friendsList = jsonObject.getJSONArray("friendsList");
        for (int i = 0; i < friendsList.length(); i++) {
            user.FriendsList.add(UUID.fromString(friendsList.getString(i)));
        }

        user.ChatsList = new ArrayList<UUID>();
        JSONArray chatsList = jsonObject.getJSONArray("chatsList");

        for (int i = 0; i < chatsList.length(); i++) {
            user.ChatsList.add(UUID.fromString(chatsList.getString(i)));
        }

        return user;
    }

    /**
     * Method requesting User of certain ID
     * 
     * @param uuid requested User ID
     * @return requested User
     * @see User
     */
    public User getUser(String uuid) {
        return getUser(UUID.fromString(uuid));
    }

    /**
     * Method creating or modifying User from User object
     * 
     * @param user User object
     * @return Created or modified User
     * @see User
     */
    public User modifyOrCreateUser(User user) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("login", user.Login);
        jsonObject.put("email", user.Email);
        jsonObject.put("passwordHash", user.PasswordHash);

        JSONArray friendsList = new JSONArray();
        for (int i = 0; i < user.FriendsList.size(); i++) {
            friendsList.put(user.FriendsList.get(i).toString());
        }

        jsonObject.put("friendsList", friendsList);

        JSONArray chatsList = new JSONArray();
        for (int i = 0; i < user.ChatsList.size(); i++) {
            chatsList.put(user.ChatsList.get(i).toString());
        }

        jsonObject.put("chatsList", chatsList);

        Preferences prefs = Preferences.userRoot().node(this.getClass().getName()).node("Users");
        prefs.put(user.ID.toString(), jsonObject.toString());
        try {
            prefs.sync();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }

        return user;
    }

    /**
     * Method requesting MessageEntry from chat of certain ID
     * 
     * @param uuid      requested Chat ID
     * @param messageID requested message ID
     * @return requested MessageEntry
     * @see MessageEntry
     */
    public MessageEntry getMessageEntry(UUID uuid, long messageID) {
        Preferences prefs = Preferences.userRoot().node(this.getClass().getName()).node("Chats").node(uuid.toString())
                .node("History");
        JSONObject jsonObject = new JSONObject(prefs.get(String.valueOf(messageID),
                "{\"sender\":\"none\",\"date\":\"0\",\"content\":\"\"}"));

        MessageEntry message = new MessageEntry();
        message.Sender = getUser(jsonObject.get("sender").toString());
        message.Date = new Date(messageID);
        message.Content = jsonObject.getString("content");

        return message;
    }

    /**
     * Method requesting MessageEntry from chat of certain ID
     * 
     * @param uuid      requested Chat ID
     * @param messageID requested message ID
     * @return requested MessageEntry
     * @see MessageEntry
     */
    public MessageEntry getMessageEntry(String uuid, long messageID) {
        return getMessageEntry(UUID.fromString(uuid), messageID);
    }

    /**
     * Method creating or modifying MessageEntry from MessageEntry object and Chat
     * ID
     * 
     * @param uuid    Chat to which we send new MessageEntry
     * @param message MessageEntry object we send
     * @return Created or modified MessageEntry
     * @see MessageEntry
     */
    public MessageEntry createMessageEntry(UUID uuid, MessageEntry message) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sender", message.Sender);
        jsonObject.put("date", message.Date.getTime());
        jsonObject.put("content", message.Content);

        Preferences prefs = Preferences.userRoot().node(this.getClass().getName()).node("Chats").node(uuid.toString())
                .node("History");
        prefs.put(String.valueOf(message.Date.getTime()), jsonObject.toString());
        try {
            prefs.sync();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
        return message;
    }

    /**
     * Method requesting Chat of certain ID
     * 
     * @param uuid requested Chat ID
     * @return requested Chat
     * @see Chat
     */

    public Chat getChat(UUID uuid) {
        Preferences prefs = Preferences.userRoot().node(this.getClass().getName()).node("Chats");
        JSONObject jsonObject = new JSONObject(prefs.get(uuid.toString(), "{\"name\": \"none\",\"members\": []}"));

        Chat chat = new Chat();
        chat.ID = uuid;
        chat.Name = jsonObject.getString("name");
        chat.Members = new ArrayList<UUID>();
        chat.Admins = new ArrayList<UUID>();

        JSONArray members = jsonObject.getJSONArray("members");
        for (int i = 0; i < members.length(); i++) {
            chat.Members.add(UUID.fromString(members.getString(i)));
        }

        JSONArray admins = jsonObject.getJSONArray("admins");
        for (int i = 0; i < admins.length(); i++) {
            chat.Admins.add(UUID.fromString(admins.getString(i)));
        }

        // TODO GET MESSAGES

        return chat;
    }

    /**
     * Method requesting Chat of certain ID
     * 
     * @param uuid requested Chat ID
     * @return requested Chat
     * @see Chat
     */

    public Chat getChat(String uuid) {
        return getChat(UUID.fromString(uuid));
    }

    /**
     * Method searching for User with known login or password
     * 
     * @param key Known login or password
     * @return requested User
     * @see User
     */

    public User findUser(String key) {
        try {
            Preferences prefs = Preferences.userRoot().node(this.getClass().getName()).node("Users");

            prefs.sync();

            String[] allUsers = prefs.keys();
            for (String uuid : allUsers) {

                JSONObject jsonObject = new JSONObject(prefs.get(uuid,
                        "{\"login\":\"none\",\"email\":\"none\",\"passwordHash\":\"none\"}"));
                if (jsonObject.getString("login").equals(key) || jsonObject.getString("email").equals(key)) {
                    return getUser(uuid);
                }
            }
            prefs.sync();
            prefs.flush();
            return null;
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method searching for Chat of known name
     * 
     * @param key Known name
     * @return requestedChat
     * @see Chat
     */

    public Chat findChat(String key) {
        try {
            Preferences prefs = Preferences.userRoot().node(this.getClass().getName()).node("Chats");

            prefs.sync();

            String[] allChats = prefs.keys();
            for (String uuid : allChats) {
                JSONObject jsonObject = new JSONObject(prefs.get(uuid,
                        "{\"name\":\"none\"}"));
                if (jsonObject.getString("name").equals(key)) {
                    return getChat(uuid);
                }
            }

            prefs.flush();

            return null;
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method creating or modifying Chat from Chat object
     * 
     * @param chat Chat object
     * @return Created or modified Chat
     * @see Chat
     */

    public Chat modifyOrCreateChat(Chat chat) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", chat.Name);
        // jsonObject.put("email", user.Email);
        // jsonObject.put("passwordHash", user.PasswordHash);

        JSONArray members = new JSONArray();
        for (int i = 0; i < chat.Members.size(); i++) {
            members.put(chat.Members.get(i).toString());
        }

        jsonObject.put("members", members);

        JSONArray admins = new JSONArray();
        for (int i = 0; i < chat.Admins.size(); i++) {
            admins.put(chat.Admins.get(i).toString());
        }

        jsonObject.put("admins", admins);

        // TODO GET MESSAGES

        Preferences prefs = Preferences.userRoot().node(this.getClass().getName()).node("Chats");
        prefs.put(chat.ID.toString(), jsonObject.toString());
        try {
            prefs.sync();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
        return chat;
    }

    /**
     * Method removing Chat from known ID
     * 
     * @param uuid Known ID
     * @see Chat
     */

    public void removeChat(UUID uuid) throws BackingStoreException {
        Preferences prefs = Preferences.userRoot().node(this.getClass().getName()).node("Chats");
        prefs.remove(uuid.toString());
    }

    /**
     * Method removing User from known ID
     * 
     * @param uuid Known ID
     * @see User
     */

    public void removeUser(UUID uuid) throws BackingStoreException {
        Preferences prefs = Preferences.userRoot().node(this.getClass().getName()).node("Users");
        prefs.remove(uuid.toString());
    }

    /**
     * Method removing all Users from database
     * 
     * @see User
     */

    public void removeAllUsers() throws BackingStoreException {
        Preferences prefs = Preferences.userRoot().node(this.getClass().getName()).node("Users");

        for (String key : prefs.keys()) {
            prefs.remove(key);
        }
    }

    /**
     * Method removing all Chats from database
     * 
     * @see User
     */

    public void removeAllChats() throws BackingStoreException {
        Preferences prefs = Preferences.userRoot().node(this.getClass().getName()).node("Chats");

        for (String key : prefs.keys()) {
            prefs.remove(key);
        }
    }

}
