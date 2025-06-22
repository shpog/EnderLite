package com.EnderLite.GUI.AccountCreator;


import java.io.IOException;

import com.EnderLite.Client;
import com.EnderLite.DataController.DataController;
import com.EnderLite.DataController.ApiMessages.ResponseStatus;
import com.EnderLite.GUI.MainView.MainViewController;
import com.EnderLite.GUI.Utils.LabelUtil;
import com.EnderLite.GUI.Utils.TextFieldUtil;
import com.EnderLite.Logger.Logger;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class AccountCreatorController {

    //erros
    @FXML
    private Label badPasswLabel;
    @FXML
    private Label badLoginLabel;
    @FXML
    private Label badPasswRepeatLabel;
    @FXML
    private Label badEmailLabel;
    @FXML
    private Label badEmailRepeatLabel;

    //login & passw
    @FXML
    private TextField loginCreateField;
    @FXML
    private PasswordField passwCreateField;
    @FXML
    private PasswordField passwRepeatField;

    //email
    @FXML
    private TextField emailField;
    @FXML
    private TextField emailRepeatField;

    //controlls
    @FXML
    private Button accountCreateButton;

    private boolean connectionEstablished = false;

    @FXML
    public void initialize(){
        connectionEstablished = false;
        EventHandler<MouseEvent> create = new EventHandler<MouseEvent>() {
            
            @Override
            public void handle(MouseEvent event){
                if (checkData()){
                    DataController dataController = DataController.getDataController();
                    try{
                        if ( connectionEstablished ==false &&
                            dataController.establishConnection("localhost", 12345) == false){
                            badLoginLabel.setText("Serwer nie odpowiada!");
                            badLoginLabel.setVisible(true);
                            return;
                        } else {
                            connectionEstablished = true;
                        }
                        if (requestAccountCreate() ){
                            handleSwitchToMainView();
                        } else {
                            dataController.closeConnection();
                            connectionEstablished = false;
                        }
                    } catch (IOException e) {
                        Logger.getLogger().logError("Error while switching from AccountCreator to MainView");
                    }

                }
            }
        };

        accountCreateButton.setOnMouseClicked(create);
    }

    private boolean requestAccountCreate(){
        String login = loginCreateField.getText();
        String passw = passwCreateField.getText();
        String email = emailField.getText();
        DataController dataController = DataController.getDataController();
        dataController.setCredensial(loginCreateField.getText(), emailField.getText());
        dataController.reqCreateUser(login, email, passw);
        ResponseStatus status = null;
        try{
            status = waithForAuth(dataController);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt exception AccountCreate (waitForAuth)");
        }

        if (status == null){
            badLoginLabel.setText("Błąd połączenia! Spróbuj ponownie");
            badLoginLabel.setVisible(true);
            badEmailLabel.setVisible(false);
            badEmailRepeatLabel.setVisible(false);
            badPasswLabel.setVisible(false);
            badPasswRepeatLabel.setVisible(false);
        } else if (status == ResponseStatus.ACCEPTED){
            return true;
        } else if (status == ResponseStatus.LOGIN){
            badLoginLabel.setText("Login już jest zajęty!");
            badLoginLabel.setVisible(true);
            badEmailLabel.setVisible(false);
            badEmailRepeatLabel.setVisible(false);
            badPasswLabel.setVisible(false);
            badPasswRepeatLabel.setVisible(false);
        } else if (status == ResponseStatus.EMAIL){
            badEmailLabel.setText("Email już jest zajęty!");
            badEmailLabel.setVisible(true);
            badLoginLabel.setVisible(false);
            badEmailRepeatLabel.setVisible(false);
            badPasswLabel.setVisible(false);
            badPasswRepeatLabel.setVisible(false);
        }
        
        return false;
    }

     private ResponseStatus waithForAuth(DataController dataController) throws InterruptedException{
        long startTime = System.currentTimeMillis();
        final long TIMEOUT = 1000;
        final long INTERVAL = 50;

        while (System.currentTimeMillis() - startTime < TIMEOUT){
            ResponseStatus status = dataController.getAuthStatus();

            if (status == ResponseStatus.ACCEPTED || status == ResponseStatus.DENIED)
                return status;
            
            Thread.sleep(INTERVAL);
        }

        return null;
    }

    private boolean checkData(){
        boolean dataReady = true;

        if ( TextFieldUtil.checkIfEmpty(loginCreateField)){
            dataReady = LabelUtil.setEmptyAndVisible(badLoginLabel);
        } else {
            badLoginLabel.setVisible(false);
        }

        //password

        if ( TextFieldUtil.checkIfEmpty(passwCreateField) ){
            dataReady = LabelUtil.setEmptyAndVisible(badPasswLabel);
        } else {
            badPasswLabel.setVisible(false);
        }

        if ( TextFieldUtil.checkIfEmpty(passwRepeatField) ){
            dataReady = LabelUtil.setEmptyAndVisible(badPasswRepeatLabel);
        } else if ( !TextFieldUtil.checkIfEmpty(passwCreateField) && !passwCreateField.getText().equals(passwRepeatField.getText()) ){
            dataReady = false;

            badPasswLabel.setText("Hasła są różne!");
            badPasswLabel.setVisible(true);
            passwCreateField.setText("");

            badPasswRepeatLabel.setText("Hasła są różne!");
            badPasswRepeatLabel.setVisible(true);
            passwRepeatField.setText("");
        } else {
            badPasswRepeatLabel.setVisible(false);
        }

        if ( TextFieldUtil.checkIfEmpty(emailField) ){
            dataReady = LabelUtil.setEmptyAndVisible(badEmailLabel);
        } else {
            badEmailLabel.setVisible(false);
        }

        if ( TextFieldUtil.checkIfEmpty(emailRepeatField) ){
            dataReady = LabelUtil.setEmptyAndVisible(badEmailRepeatLabel); 
        } else if ( !TextFieldUtil.checkIfEmpty(emailField) && !emailRepeatField.getText().equals(emailField.getText())) {
            dataReady = false;

            badEmailLabel.setText("Email jest różny!");
            badEmailLabel.setVisible(true);

            badEmailRepeatLabel.setText("Email jest różny!");
            badEmailRepeatLabel.setVisible(true);
        } else {
            badEmailRepeatLabel.setVisible(false);
        }

        return dataReady;
    }



    

    private void handleSwitchToMainView() throws IOException{
        Parent root = FXMLLoader.load(MainViewController.class.getResource("MainView.fxml"));

        Stage stage = (Stage) accountCreateButton.getScene().getWindow();
        Scene scene = new Scene(root);
        Client.setDefaultStageParam(stage);
        stage.setScene(scene);
        stage.show();
    }
    
}
