package com.EnderLite.Model;

import org.json.JSONObject;

public class Model {
    public Model() {

    }

    public User getUser() {
        return new User();
    }

    public Chat getChat() {
        return new Chat();
    }

    public void getUserDBData() {
        System.out.println("Reading db");
    }

}
