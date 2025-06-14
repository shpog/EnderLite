package com.EnderLite.Model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

// import javax.json.Json;
// import javax.json.JsonArray;
// import javax.json.JsonObject;
// import javax.json.JsonReader;
// import javax.json.JsonValue;

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

    public Chat getChat() {
        return new Chat();
    }

}
