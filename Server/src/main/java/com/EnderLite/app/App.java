package com.EnderLite.app;

import com.EnderLite.Model.*;
import java.util.UUID;

public class App {
    public static void main(String[] args) {
        System.out.println("Server is booting up...");

        Model model = new Model();

        User user = model.getUser(UUID.fromString("f7c3de3d-1fea-4d7c-a8b0-29f63c4c3454"));

        System.out.println(user.Login + " " + user.Email + " " + user.PasswordHash + " " + user.ID.toString());

    }
}

/*
 * This command runs server app via -jar created in /target dir
 * java -cp .\Server\target\EnderLite-1.0-SNAPSHOT.jar com.EnderLite.app.App
 */