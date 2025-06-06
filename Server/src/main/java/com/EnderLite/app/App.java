package com.EnderLite.app;

import com.EnderLite.Model.*;

public class App {
    public static void main(String[] args) {
        System.out.println("Server is booting up...");

        Model model = new Model();
        model.getUserDBData();
    }
}

/*
 * This command runs server app via -jar created in /target dir
 * java -cp .\Server\target\EnderLite-1.0-SNAPSHOT.jar com.EnderLite.app.App
 */