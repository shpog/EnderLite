package com.EnderLite.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Stage stage = new Stage();
        Text text = new Text("Hello world");
        StackPane stackPane = new StackPane(text);
        Scene scene = new Scene(stackPane, 500, 500);
        stage.setScene(scene);
        stage.setTitle("TEST Client");

        stage.show();
    }
}
