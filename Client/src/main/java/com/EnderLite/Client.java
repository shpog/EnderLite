package com.EnderLite;


// import com.EnderLite.Connection.DummyServer;
import com.EnderLite.GUI.Login.LoginController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application class
 * @author Micro9261
 */
public class Client extends Application {

    /**
     * Lunch application in JavaFX
     * @param args parameters from console
     */
    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(LoginController.class.getResource("LoginScreen.fxml"));
        Scene scene = new Scene(root);

        setDefaultStageParam(primaryStage);
        primaryStage.setScene(scene);
        primaryStage.show();       
    }

    /**
     * Sets default name to all client stages
     * @param stage target stage for modification
     */
    public static void setDefaultStageParam(Stage stage){
        stage.setTitle("EnderLite");
        stage.setResizable(false);
    }
}