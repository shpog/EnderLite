package com.EnderLite.GUI.MainView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.EnderLite.Client;
import com.EnderLite.DataController.ChatData;
import com.EnderLite.DataController.DataController;
import com.EnderLite.DataController.Rank;
import com.EnderLite.GUI.Login.LoginController;
import com.EnderLite.GUI.Settings.SettingsController;
import com.EnderLite.Logger.Logger;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Class for JavaFX to controll main view in client
 * @author Micro9261
 */
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

    /**
     * Initializes everything to default
     */
    @FXML
    public void initialize(){
        settingsButton.setVisible(false);
        //Initialize User label
        loginLabel.textProperty().bind(DataController.getDataController().getUserData().getLoginProperty());
        DataController.getDataController().setMainViewController(this);
        DataController.getDataController().setChatActive(null);

        //Initialize control buttons
        EventHandler<MouseEvent> logout = new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event){
                DataController.getDataController().reqDisconnect();
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
        chatsListView.setCellFactory( (ListView<ChatData> l) -> 
            new ChatsCell(chatsListView.getScene().getWindow()));
        chatsListView.getSelectionModel().selectedItemProperty().addListener(
            
            (observable, oldValue, newValue) -> {
                DataController controller = DataController.getDataController();
                if (controller.getActiveChat() == null || 
                    (newValue != null && !newValue.getId().equals(controller.getActiveChat().getId()))){
                    controller.setChatActive(newValue);
                    messageField.setVisible(true);
                    sendMessageButton.setVisible(true);
                }
            }
        );

        friendsListView.setEditable(true);
        friendsListView.setItems(DataController.getDataController().getUserFriendList());
        friendsListView.setCellFactory( (ListView<String> l) -> new FriendsCell());

        //Message chat configuration
        messageField.setVisible(false);
        sendMessageButton.setVisible(false);
        chatScrollPane.setVvalue(1.0);
        messContVBox.setAlignment(Pos.BOTTOM_LEFT);
        messContVBox.setSpacing(3);

        EventHandler<MouseEvent> sendMessage = new EventHandler<MouseEvent>() {
            
            @Override
            public void handle(MouseEvent event){
                if (!messageField.getText().isEmpty()){
                    DataController.getDataController().reqSendMessage(messageField.getText());
                    messageField.setText("");
                }
            }
        };

        sendMessageButton.setOnMouseClicked(sendMessage);

        //add Friend and chat controlls setup
        EventHandler<MouseEvent> friendAdd = new EventHandler<MouseEvent>() {
            
            @Override
            public void handle(MouseEvent event){
                final Popup popup = new Popup();
                popup.setX(addFriendButton.getScene().getWindow().getWidth()/2 + 400);
                popup.setY(addFriendButton.getScene().getWindow().getHeight()/2 + 100);
                Label whatToDo = new Label("Write nick to add:");
                TextField textField = new TextField("");
                Button add = new Button("Add");
                VBox vBox = new VBox(whatToDo, textField, add);
                vBox.setAlignment(Pos.CENTER);
                Rectangle plane = new Rectangle(160, 80, Color.WHITE);
                plane.setStroke(Color.BLACK);
                popup.getContent().addAll(plane, vBox);
                popup.show(addFriendButton.getScene().getWindow());

                EventHandler<MouseEvent> addFriend = new EventHandler<MouseEvent>() {
                    
                    @Override
                    public void handle(MouseEvent event){
                        if (textField.getText().equals("")){
                            popup.hide();
                            return;
                        }
                        DataController.getDataController().reqInvUser(textField.getText(), null);
                        textField.setText("");
                        popup.hide();
                    }
                };
                add.setOnMouseClicked(addFriend);
            }
        };

        addFriendButton.setOnMouseClicked(friendAdd);

        EventHandler<MouseEvent> chatAdd = new EventHandler<MouseEvent>() {
            
            @Override
            public void handle(MouseEvent event){
                final Popup popup = new Popup();
                popup.setX(addFriendButton.getScene().getWindow().getWidth()/2 + 400);
                popup.setY(addFriendButton.getScene().getWindow().getHeight()/2 + 100);
                Label whatToDo = new Label("Write name to create:");
                TextField textField = new TextField("");
                Button add = new Button("Create");
                VBox vBox = new VBox(whatToDo, textField, add);
                vBox.setAlignment(Pos.CENTER);
                Rectangle plane = new Rectangle(160, 80, Color.WHITE);
                plane.setStroke(Color.BLACK);
                popup.getContent().addAll(plane, vBox);
                popup.show(addChat.getScene().getWindow());

                EventHandler<MouseEvent> addChat = new EventHandler<MouseEvent>() {
                    
                    @Override
                    public void handle(MouseEvent event){
                        if (textField.getText().equals("")){
                            popup.hide();
                            return;
                        }
                        DataController.getDataController().reqCreateChat(textField.getText());
                        textField.setText("");
                        popup.hide();
                    }
                };

                add.setOnMouseClicked(addChat);          
            }
        };

        addChat.setOnMouseClicked(chatAdd);
    }

    /**
     * Class used for creating chats cells with add user buttons
     */
    class ChatsCell extends ListCell<ChatData> {
        private Window window;

        /**
         * Used to get actual window parameter need for button function
         * @param window
         */
        ChatsCell(Window window){
            this.window = window;
        }

        @Override
        public void updateItem(ChatData item, boolean empty){
            super.updateItem(item, empty);
            
            if (empty || item == null){
                setGraphic(null);
            } else {
                Text text = new Text(item.getId());
                text.setFill(Color.CORAL);
                text.setFont(new Font("Montserrat", 14));
                Rectangle borders = new Rectangle(160, 30, Color.WHITE);
                borders.setStroke(Color.BLACK);
                Text textRank = new Text(item.getRank().equals(Rank.USER) ? "U" : "A");
                textRank.setFont(new Font("Montserrat", 14));
                Button addUser = new Button("+");
                addUser.setFont(new Font("Montserrat", 10));
                HBox hBox = new HBox(text, textRank, addUser);
                hBox.setAlignment(Pos.CENTER);
                hBox.setSpacing(20);
                StackPane stack = new StackPane(borders, hBox);
                setGraphic(stack);

                EventHandler<MouseEvent> inviteUser = new EventHandler<MouseEvent>() {
                    
                    @Override
                    public void handle(MouseEvent event){
                        final Popup popup = new Popup();
                        popup.setX(window.getWidth()/2 + 400);
                        popup.setY(window.getWidth()/2 + 100);
                        Label whatToDo = new Label("Write nick to add:");
                        TextField textField = new TextField("");
                        Button add = new Button("Add");
                        VBox vBox = new VBox(whatToDo, textField, add);
                        vBox.setAlignment(Pos.CENTER);
                        Rectangle plane = new Rectangle(160, 80, Color.WHITE);
                        plane.setStroke(Color.BLACK);
                        popup.getContent().addAll(plane, vBox);
                        popup.show(window);

                        EventHandler<MouseEvent> sendInvite = new EventHandler<MouseEvent>() {
                            
                            @Override
                            public void handle(MouseEvent event){
                                if (textField.getText().equals("")){
                                    popup.hide();
                                    return;
                                }
                                DataController controller = DataController.getDataController();
                                List<String> login = new ArrayList<>();
                                login.add(textField.getText());
                                controller.reqAddUserToChat(item.getId(), login);
                                textField.setText("");
                                popup.hide();
                            }
                        };

                        add.setOnMouseClicked(sendInvite);
                    }
                };

                addUser.setOnMouseClicked(inviteUser);
            }
        }
    }

    /**
     * Class used for creating friends cells with add user to chat and delete buttons
     */
    static class FriendsCell extends ListCell<String> {

        @Override
        public void updateItem(String item, boolean empty){
            super.updateItem(item, empty);
            
            if (empty || item == null){
                setGraphic(null);
            } else {
                Text text = new Text(item);
                text.setFont(new Font("Montserrat", 14));
                Rectangle borders = new Rectangle(160,25, Color.WHITE);
                borders.setStroke(Color.BLACK);
                Button addToChat = new Button("+");
                addToChat.setMaxHeight(20);
                addToChat.setMaxWidth(20);
                addToChat.setFont(new Font("Montserrat", 10));
                Button removeFriend = new Button("-");
                removeFriend.setFont(new Font("Montserrat", 10));
                HBox hBox = new HBox(text, addToChat, removeFriend);
                hBox.setAlignment(Pos.CENTER);
                hBox.setSpacing(10);
                StackPane stack = new StackPane(borders, hBox);
                stack.setAlignment(Pos.CENTER);
                setGraphic(stack);

                EventHandler<MouseEvent> removeFriendAction = new EventHandler<MouseEvent>() {
                    
                    @Override
                    public void handle(MouseEvent event){
                        DataController.getDataController().reqRemoveUser(item);
                    }
                };

                removeFriend.setOnMouseClicked(removeFriendAction);

                EventHandler<MouseEvent> addToChatAction = new EventHandler<MouseEvent>() {
                    
                    @Override
                    public void handle(MouseEvent event){
                        List<String> list = new ArrayList<String>();
                        list.add(item);
                        DataController dataController = DataController.getDataController();
                        if (dataController.getActiveChat() != null){
                            DataController.getDataController().reqAddUserToChat(DataController.getDataController().getActiveChat().getId(), list);
                        }
                    }
                };

                addToChat.setOnMouseClicked(addToChatAction);
            }
        }
    }

    /**
     * Used to print message in client
     * @param message text to send
     * @param login user nickname
     * @param time time of sending
     * @param user indicates if user of given client send message (true) or other (false)
     */
    public void addMessage(String message, String login, String time, boolean user){
        Label loginLabel = new Label(login);
        Text text = new Text(message);
        text.setFont(new Font(15));
        Label timeLabel = new Label(time);
        HBox hBox = new HBox(loginLabel, timeLabel);
        hBox.setSpacing(10);
        VBox vBox = new VBox(hBox, text);
        if (user){
            hBox.setAlignment(Pos.BASELINE_RIGHT);
            vBox.setAlignment(Pos.BASELINE_RIGHT);
        } else {
            hBox.setAlignment(Pos.BASELINE_LEFT);
            vBox.setAlignment(Pos.BASELINE_LEFT);
        }

        //add to chat
        messContVBox.getChildren().add(vBox);
        chatScrollPane.layout();
        chatScrollPane.setVvalue(1.0);
    }

    /**
     * used to clear chats while changing chats
     */
    public void clearChat(){
        messContVBox.getChildren().clear();
    }

    /**
     * Popups notification and hides after 1 second
     * @param text message to appear
     * @param hideAfterTime if true notification will hide after 1 second otherwise it will stay forever
     */
    public void notification(String text, boolean hideAfterTime){
        final Popup popup = new Popup();
        popup.setX(addFriendButton.getScene().getWindow().getWidth()/2 + 400);
        popup.setY(addFriendButton.getScene().getWindow().getHeight()/2 + 100);
        Label issueLabel = new Label(text);
        Rectangle plane = new Rectangle(160, 80, Color.WHITE);
        plane.setStroke(Color.BLACK);
        StackPane stackPane = new StackPane(plane, issueLabel);
        popup.getContent().addAll(stackPane);
        popup.show(addChat.getScene().getWindow());
        Thread timer = new Thread(){
            @Override
            public void run(){
                try{
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e){

                }
                Platform.runLater(() -> {
                    popup.hide();
                });
            }
        };
        timer.start();
        
    }

    /**
     * Used to send friend request accept or deny notification.
     * Waits until user select to accept or deny.
     * @param login user name that invites
     */
    public void addNotification(String login){
        final Popup popup = new Popup();
        popup.setX(addFriendButton.getScene().getWindow().getWidth()/2 + 400);
        popup.setY(addFriendButton.getScene().getWindow().getHeight()/2 + 100);
        Label inviteLabel = new Label(login + " invites!");
        Rectangle plane = new Rectangle(160, 80, Color.WHITE);
        Button accept = new Button("Accept");
        Button deny = new Button("Deny");
        VBox buttons = new VBox(accept, deny);
        buttons.setAlignment(Pos.CENTER);
        buttons.setSpacing(10);
        plane.setStroke(Color.BLACK);
        HBox info = new HBox(inviteLabel, buttons);
        info.setAlignment(Pos.CENTER);
        info.setSpacing(10);
        StackPane stackPane = new StackPane(plane, info);
        popup.getContent().addAll(stackPane);
        popup.show(addChat.getScene().getWindow());
        
        EventHandler<MouseEvent> addAccept = new EventHandler<MouseEvent>() {
            
            @Override
            public void handle(MouseEvent event){
                DataController.getDataController().reqInvAnswer(login, true);
                popup.hide();
            }
        };

        EventHandler<MouseEvent> denied = new EventHandler<MouseEvent>() {
            
            @Override
            public void handle(MouseEvent event){
                DataController.getDataController().reqInvAnswer(login, false);
                popup.hide();
            }
        };

        accept.setOnMouseClicked(addAccept);
        deny.setOnMouseClicked(denied);
    }

    /**
     * Exits everything with emergency notification
     */
    public void emergencyExit(){
        final Popup popup = new Popup();
        popup.setX(addFriendButton.getScene().getWindow().getWidth()/2 + 400);
        popup.setY(addFriendButton.getScene().getWindow().getHeight()/2 + 100);
        Label issueLabel = new Label("Connection problem logging out!");
        Rectangle plane = new Rectangle(160, 80, Color.WHITE);
        plane.setStroke(Color.BLACK);
        StackPane stackPane = new StackPane(plane, issueLabel);
        popup.getContent().addAll(stackPane);
        popup.show(addChat.getScene().getWindow());
        try{
            wait(2000);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Error while waiting for 2 s after emergencyExit");
        }
        popup.hide();
        try{
            handleLogout();
        } catch (IOException e) {
            Logger.getLogger().logError("Error while loginout in emergency mode!");
        }
    }


    private void handleLogout() throws IOException {
        Parent root = FXMLLoader.load(LoginController.class.getResource("LoginScreen.fxml"));
        DataController dataController = DataController.getDataController();
        dataController.closeConnection();
        dataController.clearDataController();

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
