package com.EnderLite.Model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.JSONObject;
import com.owlike.genson.Genson;

public class Model {
    public Model() {

    }

    public User getUser(UUID uuid) {
        // File userFile = new File("./");
        InputStream in = getClass().getResourceAsStream("/Users/" + uuid.toString() + ".json");

        String json = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));

        Map<String, Object> map = (new Genson()).deserialize(json, Map.class);

        // Object map2 = genson.deserialize(json, Object.class);

        // JSONObject data = new JSONObject(JSONData);
        // User user = new User(uuid, data.getString("login"), data.getString("email"),
        // data.getString("passwordHash"));
        User user = new User();
        user.ID = uuid;
        user.Login = map.get("login").toString();
        user.Email = map.get("email").toString();
        user.PasswordHash = map.get("passwordHash").toString();

        return user;
    }

    public Chat getChat() {
        return new Chat();
    }

}
