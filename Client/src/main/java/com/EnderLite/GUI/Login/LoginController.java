package com.EnderLite.GUI.Login;


import java.io.IOException;

import com.EnderLite.Client;
import com.EnderLite.DataController.DataController;
import com.EnderLite.DataController.ApiMessages.ResponseStatus;
import com.EnderLite.GUI.AccountCreator.AccountCreatorController;
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

/**
 * Class for JavaFX to controll login view in client
 * @author Micro9261
 */
public class LoginController{
    
    //errors
    @FXML
    private Label badPassw;
    @FXML
    private Label badLoginEmail;

    //login/email
    @FXML 
    private Label loginLabel;
    @FXML
    private TextField loginField;
    @FXML
    private Label recoverLoginLabel;

    //password
    @FXML
    private Label passwLabel;
    @FXML
    private PasswordField passwField;
    @FXML
    private Label recoverPasswLabel;

    //controll
    @FXML
    private Button loginButton;
    @FXML
    private Button accountCreateButton;
    @FXML
    private Label privacyPolicyLabel;

    private boolean connectionEstablished = false;

    /**
     * Initializes everything to default
     */
    @FXML
    public void initialize(){
        badPassw.setVisible(false);
        badLoginEmail.setVisible(false);
        connectionEstablished = false;

        EventHandler<MouseEvent> logIn = new EventHandler<MouseEvent>(){

            @Override
            public void handle(MouseEvent event){
                if (checkCredentials()){
                    DataController dataController = DataController.getDataController();
                    ResponseStatus status = null;
                    try{
                        if ( connectionEstablished ==false &&
                            dataController.establishConnection("localhost", 12345) == false){
                            badLoginEmail.setText("Serwer nie odpowiada!");
                            badLoginEmail.setVisible(true);
                            return;
                        } else {
                            connectionEstablished = true;
                        }
                        
                        status = waithForAuth(dataController);
                    } catch (InterruptedException e){
                        Logger.getLogger().logError("Interrupt exception login (waitForAuth)");
                    }
                    
                    if (status == null){
                        badPassw.setText("Błąd połączenia! Spróbuj ponownie");
                        badPassw.setVisible(true);
                        passwField.setText("");

                        badLoginEmail.setText("Błąd połączenia! Spróbuj ponownie");
                        badLoginEmail.setVisible(true);
                        dataController.closeConnection();
                        connectionEstablished = false;
                    } else if (status == ResponseStatus.ACCEPTED){
                        try{
                            dataController.reqUserData();
                            handleSwitchToMainView();
                        } catch (IOException e){
                            Logger.getLogger().logError("Error while changing from login to main view");
                        }
                    } else {
                        badPassw.setText("Błędne dane!");
                        badPassw.setVisible(true);
                        passwField.setText("");

                        badLoginEmail.setText("Błędne dane!");
                        badLoginEmail.setVisible(true);
                        dataController.closeConnection();
                        connectionEstablished = false;
                    }
                    
                }
            }
        };

        loginButton.setOnMouseClicked(logIn);

        EventHandler<MouseEvent> createAccount = new EventHandler<MouseEvent>() {
            
            @Override
            public void handle(MouseEvent event){
                try{
                    handleSwitchToAccountCreator();
                } catch (IOException e){
                    Logger.getLogger().logError("Error while changing from login to accout creator");
                }
            }
        };

        accountCreateButton.setOnMouseClicked(createAccount);
    }


    private boolean checkCredentials(){
        String passw = null;
        String login = null;
        String email = null;
        boolean dataReady = true;

        if ( TextFieldUtil.checkIfEmpty(loginField) ){
            dataReady = LabelUtil.setEmptyAndVisible(badLoginEmail);
        } else if ( !TextFieldUtil.checkIfEmpty(passwField) ){
            String test = loginField.getText();
            int indexOfFirstMailIndicator = test.indexOf('@', 0);

            if (indexOfFirstMailIndicator != -1){
                int dotAfterIndicator = test.indexOf('.', indexOfFirstMailIndicator);
                if (dotAfterIndicator != -1){
                    login = null;
                    email = test;
                }
            } else {
                login = test;
                email = null;
            }

            passw = passwField.getText();
            DataController dataController = DataController.getDataController();
            dataController.setCredensial(login, email);
            dataController.reqAuth(login, email, passw);
        } else {
            dataReady = LabelUtil.setEmptyAndVisible(badPassw);
        }

        return dataReady;
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

    private void handleSwitchToMainView() throws IOException{
        Parent root = FXMLLoader.load(MainViewController.class.getResource("MainView.fxml"));

        Stage stage = (Stage) loginButton.getScene().getWindow();
        Scene scene = new Scene(root);
        Client.setDefaultStageParam(stage);
        stage.setScene(scene);
        stage.show();
    }

    private void handleSwitchToAccountCreator() throws IOException{
        Parent root = FXMLLoader.load(AccountCreatorController.class.getResource("CreateAccount.fxml"));

        Stage stage = (Stage) accountCreateButton.getScene().getWindow();
        Scene scene = new Scene(root);
        Client.setDefaultStageParam(stage);
        stage.setScene(scene);
        stage.show();
    }
}
