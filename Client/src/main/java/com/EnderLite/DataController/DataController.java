package com.EnderLite.DataController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.EnderLite.Connection.ConnectionController;
import com.EnderLite.Connection.Receiver;
import com.EnderLite.Connection.Transmitter;
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
    private volatile ConnectionController connectionController;
    //message transmission and execute
    private Receiver receiver;
    private Transmitter transmitter;
    private TaskExecutor taskExecutor;
    //viewControllers references
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
                instance.activeChat = null;
            }
            return instance;
        }
    }

    public void setCredensial(String login, String email){
        userData = new UserData(login, email);
        
    }

    public boolean establishConnection(String host, int port){
        connectionController = new ConnectionController(host, port);
        boolean connection = connectionController.establishConnection();
        if (connection){
            //Receiver thread configuration
            receiver = new Receiver();
            receiver.setSecretKey(connectionController.getAESKey());
            try{
                receiver.setDataInputStream(connectionController.getDataInputStream());
            } catch (IOException e){
                Logger.getLogger().logError("Error while transfering DataInputStream! (DataController)");
            }
            receiver.setDataQueue(pendingMesgQueue);

            //Transmitter thread configuration
            transmitter = new Transmitter();
            transmitter.setSecretKey(connectionController.getAESKey());
            try{
                transmitter.setDataOutputStream(connectionController.getDataOutputStream());
            } catch (IOException e){
                Logger.getLogger().logError("Error while transfering DataOutputStream! (DataController)");
            }
            transmitter.setDataQueue(dataOutQueue);
            
            //task executor configuration
            taskExecutor = new TaskExecutor();
            taskExecutor.setInterval(400);
            taskExecutor.setDataController(instance);
            taskExecutor.setPendingQueue(pendingMesgQueue);

            //start threads
            receiver.start();
            transmitter.start();
            taskExecutor.start();
            Logger.getLogger().logInfo("setUp ready!");
        }

        return connection;
    }

    public void closeConnection(){
        transmitter.interrupt();
        receiver.interrupt();
        taskExecutor.shutdown();
        try{
            connectionController.closeStreams();
            
        } catch (IOException e){
            Logger.getLogger().logError("Error while closing streams (DataCOntroller)");
        }
    }

    public void clearDataController(){
        dataOutQueue.clear();
        pendingMesgQueue.clear();
        authStatus = null;
    }

    public void setChatActive(ChatData chatId){
        activeChat = chatId;
        mainViewController.clearChat();
    }

    public ChatData getActiveChat(){
        return activeChat;
    }

    public void EmergencyExit(){
        mainViewController.emergencyExit();
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
        String mesg = "AUTH_STATUS-";
        try{
            dataOutQueue.put(mesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        Message dataContainer = new Message(null, null, null, null, null, null);
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
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CREATE_USER, dataContainer));
    }

    public void reqUserData(){
        String mesg = "REQ_USER_DATA-" + userData.getLogin();
        try{
            dataOutQueue.put(mesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        Message dataContainer = new Message(getUserLogin(), null, null, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.USER_DATA, dataContainer));
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
            Logger.getLogger().logError("Interrupt while adding friend request!");
        }

        Message dataContainer = new Message(getUserLogin(), email, login, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.INV_ANS, dataContainer));
    }

    public void reqInvAnswer(String login, boolean accepted){
        String mesg;
        String status = accepted ? "-ACCEPT" : "-DENIED";
        mesg = "REQ_INV_STATUS-" + login + "-" + userData.getLogin() + status;
        try{
            dataOutQueue.put(mesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while responding to add friend request!");
        }
        if (accepted)
            addFriend(login);
    }

    public void reqRemoveUser(String login){
        String mesg;
        mesg = "REQ_DEL_LOG-" + login + "-" + userData.getLogin();
        try{
            dataOutQueue.put(mesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while removind friend request!");
        }
        Message dataContainer = new Message(getUserLogin(), null, login, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.DEL_ANS, dataContainer));
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
        StringBuilder mesg = new StringBuilder("REQ_ADD_CHAT-" + chatId + "-" + userData.getLogin() + "-L=");
        for ( String user : userlogins){
            mesg.append(user + ",");
        }
        mesg.delete(mesg.length() - 1, mesg.length()); // delete last ','
        try{
            dataOutQueue.put(mesg.toString());
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        Message dataContainer = new Message(getUserLogin(), chatId, null, userlogins, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_ADD_USER, dataContainer));
    }

    public void reqChangeChatName(String oldChatId, String newChatId){
        String mesg = "REQ_CHAN_CHAT_NAME-" + userData.getLogin() + "-" + oldChatId + "-" + newChatId;
        try{
            dataOutQueue.put(mesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        Message dataContainer = new Message(getUserLogin(), oldChatId, newChatId, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_NAME_CHANGE, dataContainer));
    }

    public void reqChangeChatRank(String chatId, String login, boolean admin) {
        String mesg = "REQ_CHAN_CHAT_RANK-" + chatId + "-" + userData.getLogin() + "-" + login
            + "-" + (admin ? "ADMIN" : "USER");
        try{
            dataOutQueue.put(mesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }
        List<String> ranga = new ArrayList<>();
        ranga.add( admin ? "A" : "U");
        Message dataContainer = new Message(getUserLogin(), chatId, login, ranga, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_RANK_CHANGE, dataContainer));
    }

    public void reqRemoveUserFromChat(String chatId, List<String> userlogins){
        StringBuilder mesg = new StringBuilder( "REQ_DEL_CHAT-" + chatId + "-" + userData.getLogin() + "-L=");
        for (String user : userlogins) {
            mesg.append(user + ",");
        }
        mesg.delete(mesg.length() - 1, mesg.length());
        try{
            dataOutQueue.put(mesg.toString());
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        Message dataContainer = new Message(getUserLogin(), chatId, null, userlogins, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_DEL_USER, dataContainer));
    }

    public void reqDestroyChat(String chatId){
        String mesg = "REQ_DES_CHAT-" + userData.getLogin() + "-" + chatId;
        try{
            dataOutQueue.put(mesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        Message dataContainer = new Message(getUserLogin(), chatId, null, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_DESTROY, dataContainer));
    }

    public void reqSendMessage(String text){
        String mesg = "SEND_DATA-" + userData.getLogin() + "-" + activeChat.getId() + "-" + text;
        try{
            dataOutQueue.put(mesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        Message dataContainer = new Message(getUserLogin(), activeChat.getId(), text, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.MESSAGE_ANS, dataContainer));
    }

    public void reqDisconnect(){
        String mesg = "REQ_CONN_END";
        try{
            dataOutQueue.put(mesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        Message dataContainer = new Message(null, null, null, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.DISCONNECT, dataContainer));
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

    public void addMessageToView(String message, String login, String time, boolean user){
        mainViewController.addMessage(message, login, time, user);
    }

    void sendNotification(String text, boolean time){
        mainViewController.notification(text, time);
    }

    void sendInviteNotification(String login){
        mainViewController.addNotification(login);
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
