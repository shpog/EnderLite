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
        // File userFile = new File("./");
        InputStream in = getClass().getResourceAsStream("/Users/" + uuid.toString() + ".json");

        // String json = new BufferedReader(
        // new InputStreamReader(in, StandardCharsets.UTF_8))
        // .lines()
        // .collect(Collectors.joining("\n"));

        // JsonReader jsonReader = Json.createReader(in);
        JSONTokener tokener = new JSONTokener(in);
        JSONObject jsonObject = new JSONObject(tokener);// jsonReader.readObject();
        // jsonReader.close();

        // emp.setId(jsonObject.getInt("id"));

        // Map<String, Object> map = (new Genson()).deserialize(json, Map.class);

        // Object map2 = genson.deserialize(json, Object.class);

        // JSONObject data = new JSONObject(JSONData);
        // User user = new User(uuid, data.getString("login"), data.getString("email"),
        // data.getString("passwordHash"));
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
