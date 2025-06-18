package com.EnderLite;

import java.util.concurrent.TimeUnit;

import com.EnderLite.Connection.DummyServer;
import com.EnderLite.DataController.DataController;
import com.EnderLite.GUI.Login.LoginController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Client extends Application {

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(LoginController.class.getResource("LoginScreen.fxml"));
        Scene scene = new Scene(root);

        DummyServer server = new DummyServer(12345, false);
        server.setCmdMode(false);
        server.start();

        // try{
        //     TimeUnit.MILLISECONDS.sleep(1000);
        // } catch (InterruptedException e){

        // }
        while (DataController.getDataController().establishConnection("localhost", 12345) == false){
            System.out.println("No connection");
            try{
                TimeUnit.MILLISECONDS.sleep(400);
            } catch (InterruptedException e){

            }
        }

        setDefaultStageParam(primaryStage);
        primaryStage.setScene(scene);
        primaryStage.show();

        
    }

    public static void setDefaultStageParam(Stage stage){
        stage.setTitle("EnderLite");
        stage.setResizable(false);
    }
}