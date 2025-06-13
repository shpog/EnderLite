package com.EnderLite.DataController;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.EnderLite.Connection.ConnectionController;
import com.EnderLite.DataController.ApiMessages.Message;
import com.EnderLite.DataController.ApiMessages.ResponseStatus;
import com.EnderLite.DataController.ApiMessages.ResponseType;
import com.EnderLite.GUI.MainView.MainViewController;
import com.EnderLite.Logger.Logger;

import javafx.collections.ObservableList;
import javafx.util.Pair;


public class DataController {
    private volatile UserData userData;
    private static volatile DataController instance;
    private volatile ChatData activeChat;
    //viewCOntrollers references
    private MainViewController mainViewController;
    //queues
    private BlockingQueue<String> dataOutQueue;
    private ConcurrentLinkedQueue< Pair<ResponseType, Message> > pendingMesgQueue;
    //Status
    private volatile ResponseStatus authStatus;


    private DataController(){
        dataOutQueue = new LinkedBlockingQueue<String>();
        pendingMesgQueue = new ConcurrentLinkedQueue< Pair<ResponseType, Message> >();
    }

    public static DataController getDataController(){
        DataController tmpInstance = instance;
        if (tmpInstance != null){
            return tmpInstance;
        }

        synchronized(DataController.class){
            if (instance == null){
                instance = new DataController();
            }
            return instance;
        }
    }

    public void setCredensial(String login, String email){
        userData = new UserData(login, email);
        
    }

    public boolean establishConnection(String host, int port){
        if (host != null){
            ConnectionController.configureConnection(host, port);
        }

        return ConnectionController.establishConnection();
    }

    public void setChatActive(ChatData chatId){
        activeChat = chatId;
    }

    public ChatData getActiveChat(){
        return activeChat;
    }

    //Request
    public void reqAuth(String login, String email, String passw){
        String mesg = null;
        //set to null for indication of no replay yet
        ansAuthStatus(null);
        if (email == null){
            mesg = "AUTH_LOG&PASSW-" + login + "-" + passw;
        } else {
            mesg = "AUTH_EMAIL&PASSW-" + email + "-" + passw;
        }
        try{
            dataOutQueue.put(mesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        Message dataContainer = new Message(login, email, passw, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.AUTH_STATUS, dataContainer));
    }

    public void reqAuthStatus(){
        String mesg = "AUTH_STATUS";
        try{
            dataOutQueue.put(mesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        Message dataContainer = new Message(getUserLogin(), null, null, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.AUTH_STATUS, dataContainer));
    }

    public void reqCreateUser(String login, String email, String passw){
        String mesg = "REQ_ADD_USER-" + login + "-" + email + "-" + passw;
        try{
            dataOutQueue.put(mesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }
        ansCreateStatus(null);

        Message dataContainer = new Message(login, email, passw, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_CREATE, dataContainer));
    }

    public void reqUserData(){
        String mesg = "REQ_USER_DATA-" + userData.getLogin();
        try{
            dataOutQueue.put(mesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        Message dataContainer = new Message(getUserLogin(), null, null, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_CREATE, dataContainer));
    }

    public void reqInvUser(String login, String email) {
        String mesg;
        if ( login != null ){
            mesg = "REQ_INV_LOG-" + login + "-" + userData.getLogin();
        } else {
            mesg = "REQ_INV_EMAIL-" + email + "-" + userData.getLogin();
        }
        try{
            dataOutQueue.put(mesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        Message dataContainer = new Message(getUserLogin(), email, login, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_CREATE, dataContainer));
    }

    public void reqCreateChat(String chatId) {
        String mesg = "REQ_CRT_CHAT-" + userData.getLogin() + "-" + chatId;
        try{
            dataOutQueue.put(mesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        Message dataContainer = new Message(getUserLogin(), chatId, null, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_CREATE, dataContainer));
    }

    public void reqAddUserToChat(String chatId, List<String> userlogins) {
        StringBuilder mesg = new StringBuilder("REQ_ADD_CHAT-" + chatId + "-" + userData.getLogin());
        for ( String user : userlogins){
            mesg.append("-" + user);
        }
        try{
            dataOutQueue.put(mesg.toString());
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        Message dataContainer = new Message(getUserLogin(), chatId, null, userlogins, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_CREATE, dataContainer));
    }

    public void reqChangeChatName(String oldChatId, String newChatId){
        String mesg = "REQ_CHAN_CHAT_RANK-" + userData.getLogin() + "-" + oldChatId + "-" + newChatId;
        try{
            dataOutQueue.put(mesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        Message dataContainer = new Message(getUserLogin(), oldChatId, newChatId, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_CREATE, dataContainer));
    }

    public void reqChangeChatRank(String chatId, String login) {
        String mesg = "REQ_CHAN_CHAT_RANK-" + chatId + "-" + userData.getLogin() + "-" + login;
        try{
            dataOutQueue.put(mesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        Message dataContainer = new Message(getUserLogin(), chatId, login, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_CREATE, dataContainer));
    }

    public void reqRemoveUserFromChat(String chatId, List<String> userlogins){
        StringBuilder mesg = new StringBuilder( "REQ_DEL_CHAT-" + chatId + "-" + userData.getLogin() );
        for (String user : userlogins) {
            mesg.append("-" + user);
        }
        try{
            dataOutQueue.put(mesg.toString());
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        Message dataContainer = new Message(getUserLogin(), chatId, null, userlogins, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_CREATE, dataContainer));
    }

    public void reqDestroyChat(String chatId){
        String mesg = "REQ_DES_CHAT-" + userData.getLogin() + "-" + chatId;
        try{
            dataOutQueue.put(mesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        Message dataContainer = new Message(getUserLogin(), chatId, null, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_CREATE, dataContainer));
    }

    public void reqSendMessage(String chatId, String text){
        String mesg = "SEND_DATA-" + userData.getLogin() + "-" + chatId + "-" + text;
        try{
            dataOutQueue.put(mesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        Message dataContainer = new Message(getUserLogin(), chatId, text, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_CREATE, dataContainer));
    }

    public void reqDisconnect(){
        String mesg = "REQ_CONN_END";
        try{
            dataOutQueue.put(mesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        Message dataContainer = new Message(null, null, null, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_CREATE, dataContainer));
    }

    //Response status and action


    public void ansAuthStatus(ResponseStatus status){
        this.authStatus = status;
    }

    public ResponseStatus getAuthStatus(){
        ResponseStatus status = null;

        if (authStatus != null){
            status = authStatus;
            authStatus = null;
        }

        return status;
    }

    public void ansCreateStatus(ResponseStatus status){
        this.authStatus = status;
    }

    public ResponseStatus getCreateStatus(){
        return getAuthStatus();
    }

    public void addMessageToView(String message, String login, LocalTime time){
        mainViewController.addMessage(message, login, time);
    }

    //Used to retrive data or update userData

    public void addFriend(String friendName){
        userData.addFriend(friendName);
    }

    public void addChat(String chatID, Rank rank){
        userData.addChat(chatID, rank);
    }

    public UserData getUserData(){
        return userData;
    }

    public String getUserLogin(){
        return userData.getLogin();
    }

    public ObservableList<String> getUserFriendList(){
        return userData.getFriendsList();
    }

    public ObservableList<ChatData> getUserChatsList(){
        return userData.getChatList();
    }

    //Used to get reference for view controllers actions
    public void setMainViewController(MainViewController controller){
        this.mainViewController = controller;
    }

}
