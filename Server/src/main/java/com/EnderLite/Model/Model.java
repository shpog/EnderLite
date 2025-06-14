package com.EnderLite.Model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONArray;
import org.json.JSONException;

public class Model {
    public Model() {

    }

    public User getUser(UUID uuid) {
        InputStream in = getClass().getResourceAsStream("/Users/" + uuid.toString() + ".json");

        JSONTokener tokener = new JSONTokener(in);
        JSONObject jsonObject = new JSONObject(tokener);

        User user = new User();
        user.ID = uuid;
        user.Login = jsonObject.getString("login");
        user.Email = jsonObject.getString("email");
        user.PasswordHash = jsonObject.getString("passwordHash");

        return user;
    }

    // public User createUser(UUID uuid) {
    // InputStream in = getClass().getResourceAsStream("/Users/" + uuid.toString() +
    // ".json");

    // JSONTokener tokener = new JSONTokener(in);
    // JSONObject jsonObject = new JSONObject(tokener);

    // User user = new User();
    // user.ID = uuid;
    // user.Login = jsonObject.getString("login");
    // user.Email = jsonObject.getString("email");
    // user.PasswordHash = jsonObject.getString("passwordHash");

    // return user;
    // }

    public MessageEntry getMessageEntry(UUID chatUUID, long messageID) {
        InputStream in = getClass()
                .getResourceAsStream("/Chats/" + uuid.toString() + "/History/" + String.valueOf(messageID) + ".json");

        JSONTokener tokener = new JSONTokener(in);
        JSONObject jsonObject = new JSONObject(tokener);

        MessageEntry message = new MessageEntry();
        message.Sender = getUser(jsonObject.getString("sender"));
        message.Date = new Date(messageID);
        message.Content = jsonObject.getString("content");

        return message;
    }

    public Chat getChat() {
        InputStream in = getClass().getResourceAsStream("/Chats/" + uuid.toString() + "/" + uuid.toString() + ".json");

        JSONTokener tokener = new JSONTokener(in);
        JSONObject jsonObject = new JSONObject(tokener);

        Chat chat = new Chat();
        chat.ID = uuid;
        chat.Name = jsonObject.getString("name");
        chat.Members = new ArrayList<User>();

        JSONArray members = obj.getJSONArray("members");
        for (int i = 0; i < members.length(); i++) {
            chat.Members.add(getUser(members.getString(i)));
        }

        return chat;
    }

}
