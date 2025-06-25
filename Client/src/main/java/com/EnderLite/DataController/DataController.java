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

/**
 * Used as bridge between GUI and client's backend.
 * Main controll class.
 * @author Micro9261
 */
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

    /**
     * Used to get DataController instance (singleton)
     * Concurrency safe.
     * @return DataController instance
     */
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

    /**
     * Used to set client's user credensials
     * @param login client's user login
     * @param email client's user email
     */
    public void setCredensial(String login, String email){
        userData = new UserData(login, email);
        
    }

    /**
     * Used to establish connection and start backend if connection established.
     * @param host name of server's host
     * @param port name of server's port
     * @return true if connection established and backend started, false otherwise
     */
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

    /**
     * Used to close connection and backend
     */
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

    /**
     * Used to clear DataController stored data
     */
    public void clearDataController(){
        dataOutQueue.clear();
        pendingMesgQueue.clear();
        authStatus = null;
    }

    /**
     * Used to set active chat
     * @param chatId name of active chat
     */
    public void setChatActive(ChatData chatId){
        activeChat = chatId;
        mainViewController.clearChat();
    }

    /**
     * Used to get active chat
     * @return name of active chat
     */
    public ChatData getActiveChat(){
        return activeChat;
    }

    /**
     * Used to fire EmergencyExit response in GUI
     */
    public void EmergencyExit(){
        mainViewController.emergencyExit();
    }

    //Requests

    /**
     * Send Authorizaation request
     * @param login client's user login
     * @param email client's user email
     * @param passw client's user password
     */
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

    /**
     * Send Authorizaation request without parameters for status answer (use after login or create)
     */
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

    /**
     * Sends user create account request
     * @param login login of created user
     * @param email email of created user
     * @param passw password fo created user
     */
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

    /**
     * Sends request for downloading user data
     */
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

    /**
     * Sends request for inviting user. If login null sends email, otherwise sends login.
     * @param login login of user to be invited
     * @param email emial of user to be invited
     */
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


    /**
     * Sends request for answering invite status
     * @param login login of user who invited client's user
     * @param accepted true if client's user accepted, false otherwise
     */
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


    /**
     * Sends request to remove user from friends
     * @param login login of user to be deleted
     */
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


    /**
     * Sends request to create chat. User who create chat has admin rank.
     * @param chatId name of created chat
     */
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

    /**
     * Sends request to add users to chat
     * @param chatId name of chat to which users will be added
     * @param userlogins List of users to be added to chat
     */
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

    /**
     * Sends request for changing chats name
     * @param oldChatId old chat name
     * @param newChatId new chat name
     */
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

    /**
     * Sends request to change chat rank of given user
     * @param chatId chat name
     * @param login user which rank will be changed
     * @param admin true if admin status reqested, false otherwise
     */
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

    /**
     * Sends request to remove user from chat
     * @param chatId name of chat
     * @param userlogins name of user to be removed
     */
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

    /**
     * Sends request to destroy chat
     * @param chatId name of chat to destroy
     */
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

    /**
     * Sends request to accept written message by client's user
     * @param text message to be sent
     */
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

    /**
     * Sends request to dissconnect with server
     */
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


    /**
     * Used by TaskExecutor to change authorization status of login action
     * @param status
     */
    public void ansAuthStatus(ResponseStatus status){
        this.authStatus = status;
    }

     /**
     * Returns response status of create account authorization
     * @return ACCEPTED if good, DENIED or ERROR otherwise. If null read, answer from server not received yet.
     */
    public ResponseStatus getAuthStatus(){
        ResponseStatus status = null;

        if (authStatus != null){
            status = authStatus;
            authStatus = null;
        }

        return status;
    }

    /**
     * Used by TaskExecutor to change authorization status of account creation
     * @param status
     */
    public void ansCreateStatus(ResponseStatus status){
        this.authStatus = status;
    }

    /**
     * Returns response status of create account authorization
     * @return ACCEPTED if good, DENIED or ERROR otherwise. If null read, answer from server not received yet.
     */
    public ResponseStatus getCreateStatus(){
        return getAuthStatus();
    }

    /**
     * Prints message in chat
     * @param message message text
     * @param login login of user who wrtote message
     * @param time time when message was sent
     * @param user true if client's user message, false otherwise
     */
    public void addMessageToView(String message, String login, String time, boolean user){
        mainViewController.addMessage(message, login, time, user);
    }

    /**
     * Sends notification with message
     * @param text information to be sent
     * @param time true if needs to disappear after 1 second, false otherwise
     */
    void sendNotification(String text, boolean time){
        mainViewController.notification(text, time);
    }

    /**
     * Sends invite notification to GUI
     * @param login login of user who invites client's user
     */
    void sendInviteNotification(String login){
        mainViewController.addNotification(login);
    }

    //Used to retrive data or update userData

    /**
     * Used fo add friend to client's user friends list
     * @param friendName friend name
     */
    public void addFriend(String friendName){
        userData.addFriend(friendName);
    }

    /**
     * Used to add chat to client's user chats list
     * @param chatID chat name
     * @param rank client's user chat rank
     */
    public void addChat(String chatID, Rank rank){
        userData.addChat(chatID, rank);
    }

    /**
     * Used to get User Data
     * @return user data container
     */
    public UserData getUserData(){
        return userData;
    }

    /**
     * USed to get client's user login
     * @return client's user login
     */
    public String getUserLogin(){
        return userData.getLogin();
    }

    /**
     * Used to get client's user friends list
     * @return client's user friends list
     */
    public ObservableList<String> getUserFriendList(){
        return userData.getFriendsList();
    }

    /**
     * Used to get client's user chats list
     * @return client's user chats list
     */
    public ObservableList<ChatData> getUserChatsList(){
        return userData.getChatList();
    }

    /**
     * Sets reference to main view GUI controller
     * @param controller MainViewController instance of main view
     */
    public void setMainViewController(MainViewController controller){
        this.mainViewController = controller;
    }


}
