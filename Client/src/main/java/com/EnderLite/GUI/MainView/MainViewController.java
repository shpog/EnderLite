package com.EnderLite.GUI.MainView;

import java.io.IOException;
import java.time.LocalTime;

import com.EnderLite.Client;
import com.EnderLite.DataController.ChatData;
import com.EnderLite.DataController.DataController;
import com.EnderLite.GUI.Login.LoginController;
import com.EnderLite.GUI.Settings.SettingsController;
import com.EnderLite.Logger.Logger;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainViewController {
    
    //chats segment (left)
    @FXML
    private ListView<ChatData> chatsListView;
    @FXML
    private Button addChat;

    //messages (middle)
    @FXML
    private ScrollPane chatScrollPane;
    @FXML
    private VBox messContVBox;
    @FXML
    private TextField messageField;
    @FXML
    private Button sendMessageButton;

    //friends and controlls
    @FXML
    private Label loginLabel;
    @FXML
    private Button settingsButton;
    @FXML
    private Button logoutButton;
    @FXML
    private ListView<String> friendsListView;
    @FXML
    private Button addFriendButton;

    @FXML
    public void initialize(){
        //Initialize User label
        loginLabel.textProperty().bind(DataController.getDataController().getUserData().getLoginProperty());
        DataController.getDataController().setMainViewController(this);

        //Initialize control buttons
        EventHandler<MouseEvent> logout = new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event){
                try{
                    handleLogout();
                } catch (IOException e){
                    Logger.getLogger().logError("Error while logging out!");
                }
            }
        };

        logoutButton.setOnMouseClicked(logout);

        EventHandler<MouseEvent> settings = new EventHandler<MouseEvent>() {
            
            @Override
            public void handle(MouseEvent event){
                try{
                    handleSettings();
                } catch (IOException e){
                    Logger.getLogger().logError("Error while loading settings!");
                }
            }
        };

        settingsButton.setOnMouseClicked(settings);

        //initialize ListView
        chatsListView.setEditable(true);
        chatsListView.setItems(DataController.getDataController().getUserChatsList());
        chatsListView.setCellFactory( (ListView<ChatData> l) -> new ChatsCell());

        friendsListView.setEditable(true);
        friendsListView.setItems(DataController.getDataController().getUserFriendList());
        friendsListView.setCellFactory( (ListView<String> l) -> new FriendsCell());

        //MessageChatConfiguration
    }

    static class ChatsCell extends ListCell<ChatData> {

        @Override
        public void updateItem(ChatData item, boolean empty){
            super.updateItem(item, empty);
            /*
             * TODO
             */
        }
    }

    static class FriendsCell extends ListCell<String> {

        @Override
        public void updateItem(String item, boolean empty){
            super.updateItem(item, empty);
            /*
             * TODO
             */
        }
    }

    public void addMessage(String message, String login, LocalTime time){
        /*
         * TODO
         */
    }


    private void handleLogout() throws IOException {
        Parent root = FXMLLoader.load(LoginController.class.getResource("LoginScreen.fxml"));

        Stage stage = (Stage) logoutButton.getScene().getWindow();
        Scene scene = new Scene(root);
        Client.setDefaultStageParam(stage);
        stage.setScene(scene);
        stage.show();
    }

    private void handleSettings() throws IOException {
        Parent root = FXMLLoader.load(SettingsController.class.getResource("Settings.fxml"));

        Stage stage = (Stage) settingsButton.getScene().getWindow();
        Scene scene = new Scene(root);
        Client.setDefaultStageParam(stage);
        stage.setScene(scene);
        stage.show();
    }

}
