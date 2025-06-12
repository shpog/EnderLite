package com.EnderLite.GUI.AccountCreator;


import java.io.IOException;

import com.EnderLite.Client;
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

    @FXML
    public void initialize(){

        EventHandler<MouseEvent> create = new EventHandler<MouseEvent>() {
            
            @Override
            public void handle(MouseEvent event){
                try{
                    /*
                     * TODO
                     */
                    checkData();
                    if ( requestAccountCreate() ){
                        handleSwitchToMainView();
                    }
                } catch (IOException e) {
                    Logger.getLogger().logError("Error while switching from accountCreator to MainView");
                }
            }
        };

        accountCreateButton.setOnMouseClicked(create);
    }

    private boolean requestAccountCreate(){
        String login = loginCreateField.getText();
        String passw = passwCreateField.getText();
        String email = emailField.getText();
        /*
         * TODO
         */
        return true;
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
