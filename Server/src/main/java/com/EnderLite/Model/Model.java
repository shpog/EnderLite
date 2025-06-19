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

public class Model {
    public Model() {

    }

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
            user.ChatsList.add(UUID.fromString(friendsList.getString(i)));
        }

        return user;
    }

    public User getUser(String uuid) {
        return getUser(UUID.fromString(uuid));
    }

    public User findUser(String key) {
        try {
            Preferences prefs = Preferences.userRoot().node(this.getClass().getName()).node("Users");
            String[] allUsers = prefs.keys();
            for (String uuid : allUsers) {
                JSONObject jsonObject = new JSONObject(uuid);
                if (jsonObject.getString("login") == key || jsonObject.getString("email") == key) {
                    return getUser(prefs.get(uuid,
                            "{\"login\":\"none\",\"email\":\"none\",\"passwordHash\":\"none\"}"));
                }
            }
            prefs.sync();
            return null;
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
        return null;
    }

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

    public MessageEntry getMessageEntry(String uuid, long messageID) {
        return getMessageEntry(UUID.fromString(uuid), messageID);
    }

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

    public Chat getChat(String uuid) {
        return getChat(UUID.fromString(uuid));
    }

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

}
