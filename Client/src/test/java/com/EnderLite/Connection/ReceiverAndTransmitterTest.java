package com.EnderLite.Connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.EnderLite.DataController.ApiMessages.Message;
import com.EnderLite.DataController.ApiMessages.ResponseStatus;
import com.EnderLite.DataController.ApiMessages.ResponseType;
import com.EnderLite.Logger.Logger;

import javafx.util.Pair;

public class ReceiverAndTransmitterTest {
    private Receiver receiver;
    private Transmitter transmitter;
    private DummyServer server;
    private ConnectionController connController;
    private BlockingQueue<String> dataOutQueue;
    private ConcurrentLinkedQueue< Pair<ResponseType, Message> > pendingMesgQueue;
    private boolean setUpReady;

    @BeforeEach
    public void setUp(){
        dataOutQueue = new LinkedBlockingQueue<String>();
        pendingMesgQueue = new ConcurrentLinkedQueue< Pair<ResponseType, Message> >();
        server = new DummyServer(12345, false);
        server.setCmdMode(false);
        server.start();

        setUpReady = false;
        connController = new ConnectionController("localhost", 12345);
        if (connController.establishConnection()){
            receiver = new Receiver();
            receiver.setSecretKey(connController.getAESKey());
            try{
                receiver.setDataInputStream(connController.getDataInputStream());
            } catch (IOException e){
                Logger.getLogger().logError("Error while transfering DataInputStream! (DataController)");
            }
            receiver.setDataQueue(pendingMesgQueue);

            //Transmitter thread configuration
            transmitter = new Transmitter();
            transmitter.setSecretKey(connController.getAESKey());
            try{
                transmitter.setDataOutputStream(connController.getDataOutputStream());
            } catch (IOException e){
                Logger.getLogger().logError("Error while transfering DataOutputStream! (DataController)");
            }
            transmitter.setDataQueue(dataOutQueue);
            receiver.start();
            transmitter.start();
            Logger.getLogger().logInfo("setUp ready!");
            setUpReady = true;
        }
        
    }

    @AfterEach
    public void cleanUp(){
        Logger.getLogger().logInfo("Cleaning....");
        
        try{
            connController.closeStreams();
        } catch (IOException e){
            Logger.getLogger().logError("Error while closing streams (DataCOntroller)");
        }
        server.interrupt();
        transmitter.interrupt();
        receiver.interrupt();
        //wait for thread
        try{
            server.join();
            transmitter.join();
            receiver.join();
        } catch (InterruptedException e){
            Logger.getLogger().logError("Error while waiting for threads (AfterEachtest)");
        }
        // dataOutQueue.notifyAll();
        dataOutQueue.clear();
        // pendingMesgQueue.notifyAll();
        pendingMesgQueue.clear();
        try{
            TimeUnit.MILLISECONDS.sleep(400);
        } catch (InterruptedException e){

        }
    }

    /////////////////////////////////// AUTHORIZATION ///////////////////////////////

    @Test
    public void authorization(){
        assertTrue(setUpReady);

        String login = "test";
        String email = "test@gmail.com";
        String passw = "test";

        String mesgLogin = "AUTH_LOG&PASSW-" + login + "-" + passw;
        String mesgEmail = "AUTH_EMAIL&PASSW-" + email + "-" + passw;
        String mesgBadLogin = "AUTH_LOG&PASSW-" + "notTest" + "-" + passw;
        String mesgBadEmail = "AUTH_EMAIL&PASSW-" + "notTest@gmail.com" + "-" + passw;

        //good data
        Message dataContainer = new Message(login, null, passw, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.AUTH_STATUS, dataContainer));
        dataContainer = new Message(email, passw, passw, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.AUTH_STATUS, dataContainer));
        //bad data
        dataContainer = new Message("notTest", null, passw, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.AUTH_STATUS, dataContainer));
        dataContainer = new Message(null, "notTest@gmail.com", passw, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.AUTH_STATUS, dataContainer));
        //auth_status
        String authStatus = "AUTH_STATUS-";
        dataContainer = new Message(null, null, null, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.AUTH_STATUS, dataContainer));

        try{
            dataOutQueue.put(mesgLogin);
            dataOutQueue.put(mesgEmail);
            dataOutQueue.put(mesgBadLogin);
            dataOutQueue.put(mesgBadEmail);
            dataOutQueue.put(authStatus);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        try{
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e){

        }


        server.setAccepted();
        dataContainer = new Message(null, null, null, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.AUTH_STATUS, dataContainer));
        try{
            dataOutQueue.put(authStatus);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }


        try{
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e){

        }

        //good
        Pair<ResponseType,Message> check1 = pendingMesgQueue.poll();
        Pair<ResponseType,Message> check2 = pendingMesgQueue.poll();
        //bad
        Pair<ResponseType,Message> badcheck1 = pendingMesgQueue.poll();
        Pair<ResponseType,Message> badCheck2 = pendingMesgQueue.poll();
        //AUTH_STATUS
        Pair<ResponseType,Message> badAuth = pendingMesgQueue.poll();
        Pair<ResponseType,Message> goodAuth = pendingMesgQueue.poll();
        //goodData
        assertEquals(ResponseStatus.ACCEPTED, check1.getValue().getStatus());
        assertEquals("test", check1.getValue().getLogin());
        assertEquals("test@gmail.com", check1.getValue().getEmail());
        assertEquals(ResponseStatus.ACCEPTED, check2.getValue().getStatus());
        assertEquals("test", check2.getValue().getLogin());
        assertEquals("test@gmail.com", check2.getValue().getEmail());
        //badData
        assertEquals(ResponseStatus.DENIED, badcheck1.getValue().getStatus());
        assertEquals(ResponseStatus.DENIED, badCheck2.getValue().getStatus());
        //check AUTH_STATUS
        assertEquals(ResponseStatus.DENIED, badAuth.getValue().getStatus());
        assertEquals(ResponseStatus.ACCEPTED, goodAuth.getValue().getStatus());
        assertEquals("test", goodAuth.getValue().getLogin());
        assertEquals("test@gmail.com", goodAuth.getValue().getEmail());
        assertTrue(pendingMesgQueue.isEmpty());
    }

    ////////////////////////////////// CREATING USER ////////////////////////////////

    @Test
    public void createUser(){
        assertTrue(setUpReady);
        String login = "test";
        String email = "test@gmail.com";
        String mesg = "REQ_ADD_USER-" + login + "-" + email;
        String badMesg = "REQ_ADD_USER-" + "not" + login + "-" + email;

        Message dataContainer = new Message(login, email, "12345", null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CREATE_USER, dataContainer));
        
        dataContainer = new Message("not "+ login, email, "12345", null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CREATE_USER, dataContainer));
        
        try{
            dataOutQueue.put(mesg);
            dataOutQueue.put(badMesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        try{
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e){

        }
        server.setAccepted();
        dataContainer = new Message("not "+ login, email, "12345", null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CREATE_USER, dataContainer));
        
        try{
            dataOutQueue.put(badMesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        try{
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e){

        }

        //good
        Pair<ResponseType,Message> check = pendingMesgQueue.poll();
        //bad
        Pair<ResponseType,Message> badcheck1 = pendingMesgQueue.poll();
        Pair<ResponseType,Message> badCheck2 = pendingMesgQueue.poll();

        //good
        assertEquals(ResponseStatus.ACCEPTED, check.getValue().getStatus());
        assertEquals("test", check.getValue().getLogin());
        assertEquals("test@gmail.com", check.getValue().getEmail());
        //bad
        assertEquals(ResponseStatus.EMAIL, badcheck1.getValue().getStatus());
        assertEquals(ResponseStatus.LOGIN, badCheck2.getValue().getStatus());
        assertTrue(pendingMesgQueue.isEmpty());
    }

    ////////////////////////////////// USER DATA LOADING //////////////////////////////////

    @Test
    public void requestUserData(){
        assertTrue(setUpReady);

        String login = "test";
        String mesg = "REQ_USER_DATA-" + login;
        String badMesg = "REQ_USER_DATA-" + "not" + login;

        Message dataContainer = new Message(login, null, null, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.USER_DATA, dataContainer));

        dataContainer = new Message("not"+login, null, null, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.USER_DATA, dataContainer));

        try{
            dataOutQueue.put(mesg);
            dataOutQueue.put(badMesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        try{
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e){

        }
    

        //good
        Pair<ResponseType,Message> check = pendingMesgQueue.poll();
        //bad
        Pair<ResponseType,Message> bad = pendingMesgQueue.poll();

        //good
        assertEquals(ResponseStatus.ACCEPTED, check.getValue().getStatus());
        assertEquals("test", check.getValue().getLogin());
        assertEquals("test@gmail.com", check.getValue().getEmail());
        assertEquals("friend1", check.getValue().getLogins().get(0));
        assertEquals("friend2", check.getValue().getLogins().get(1));
        assertEquals("friend3", check.getValue().getLogins().get(2));
        assertEquals(3, check.getValue().getLogins().size());
        assertEquals("chat1", check.getValue().getChats().get(0));
        assertEquals("chat2", check.getValue().getChats().get(1));
        assertEquals(2, check.getValue().getChats().size());
        assertEquals(ResponseStatus.ERROR, bad.getValue().getStatus());
        assertTrue(pendingMesgQueue.isEmpty());
    }

    /////////////////////////// FRIENDS MANIPULATION ///////////////////////////

    @Test
    public void requestInviteFriend(){
        assertTrue(setUpReady);

        String login = "test";
        String email = "test@test.gmail.com";
        //good messages
        String mesgLogin = "REQ_INV_LOG-" + login + "-" + "test2";
        String mesgEmail = "REQ_INV_EMAIL-" + email + "-" + "test2";
        String badLogin = "REQ_INV_LOG-" + "not" + login + "-" + "test2";
        String badEmail = "REQ_INV_EMAIL-" + "not" + email + "-" + "test2";

        Message dataContainer = new Message("test2", email, login, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.INV_ANS, dataContainer));

        dataContainer = new Message("test2", email, login, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.INV_ANS, dataContainer));

        dataContainer = new Message("test2", email, login, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.INV_ANS, dataContainer));

        dataContainer = new Message("test2", email, login, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.INV_ANS, dataContainer));

        try{
            dataOutQueue.put(mesgLogin);
            dataOutQueue.put(mesgEmail);
            dataOutQueue.put(badLogin);
            dataOutQueue.put(badEmail);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        try{
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e){

        }

        //good
        Pair<ResponseType,Message> check = pendingMesgQueue.poll();
        Pair<ResponseType,Message> check2 = pendingMesgQueue.poll();
        //bad
        Pair<ResponseType,Message> bad = pendingMesgQueue.poll();
        Pair<ResponseType,Message> bad2 = pendingMesgQueue.poll();

        //good
        assertEquals(ResponseStatus.ACCEPTED, check.getValue().getStatus());
        assertEquals(login, check.getValue().getPassw());
        assertEquals(ResponseStatus.ACCEPTED, check2.getValue().getStatus());
        assertEquals(login, check2.getValue().getPassw());
        //bad
        assertEquals(ResponseStatus.DENIED, bad2.getValue().getStatus());
        assertEquals("nottest", bad.getValue().getPassw());
        assertEquals(ResponseStatus.DENIED, bad2.getValue().getStatus());
        assertEquals("test", bad2.getValue().getPassw());
        assertTrue(pendingMesgQueue.isEmpty());
    }

    @Test
    public void requestRemoveFriend(){
        assertTrue(setUpReady);

        String login = "test2";
        String loginToErase = "test";
        String mesg = "REQ_DEL_LOG-" + loginToErase + "-" + login;
        String badMesg = "REQ_DEL_LOG-" + "not" + loginToErase + "-" + login;

        Message dataContainer = new Message(login, null, loginToErase, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.DEL_ANS, dataContainer));

        dataContainer = new Message(login, null, "not" + loginToErase, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.DEL_ANS, dataContainer));
        
        try{
            dataOutQueue.put(mesg);
            dataOutQueue.put(badMesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        try{
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e){

        }

        //good
        Pair<ResponseType,Message> check = pendingMesgQueue.poll();
        //bad
        Pair<ResponseType,Message> bad = pendingMesgQueue.poll();

        assertEquals(ResponseStatus.ACCEPTED, check.getValue().getStatus());
        assertEquals(loginToErase, check.getValue().getPassw());
        assertEquals(ResponseStatus.DENIED, bad.getValue().getStatus());
        assertEquals("not" + loginToErase, bad.getValue().getPassw());
        assertTrue(pendingMesgQueue.isEmpty());
    }

    //////////////////////////////////////// CHATS ////////////////////////////////////
    
    @Test
    public void createChat(){
        assertTrue(setUpReady);

        String login = "test";
        String chatID = "testChat1234";
        String mesg = "REQ_CRT_CHAT-" + login + "-" + "test";
        String badMesg = "REQ_CRT_CHAT-" + "not" + login + "-" + "test";

        Message dataContainer = new Message(login, chatID, null, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_CREATE, dataContainer));
        
        dataContainer = new Message("not" + login, chatID, null, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_CREATE, dataContainer));
        
        try{
            dataOutQueue.put(mesg);
            dataOutQueue.put(badMesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        try{
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e){

        }
        server.setAccepted();
        dataContainer = new Message("not" + login, chatID, null, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_CREATE, dataContainer));
        
        try{
            dataOutQueue.put(badMesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        try{
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e){

        }

        //good
        Pair<ResponseType,Message> check = pendingMesgQueue.poll();
        //bad
        Pair<ResponseType,Message> bad = pendingMesgQueue.poll();
        Pair<ResponseType,Message> bad2 = pendingMesgQueue.poll();

        assertEquals(ResponseStatus.ACCEPTED, check.getValue().getStatus());
        assertEquals(login, check.getValue().getLogin());
        assertEquals(chatID, check.getValue().getEmail());
        assertEquals(ResponseStatus.ERROR, bad.getValue().getStatus());
        assertEquals("not" + login, bad.getValue().getLogin());
        assertEquals(ResponseStatus.USED, bad2.getValue().getStatus());
        assertEquals("not" + login, bad2.getValue().getLogin());
        assertTrue(pendingMesgQueue.isEmpty());
    }

    @Test
    public void addUsersToChat(){
        assertTrue(setUpReady);
        String login = "test";
        String chatID = "testChat1234";
        String mesg = "REQ_ADD_CHAT-" + chatID + "-" + login + "-L=Ela,Magda";
        String badMesg = "REQ_ADD_CHAT-" +  chatID + "-" + "not" + login + "-L=Ela,Magda";

        Message dataContainer = new Message(login, chatID, null, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_ADD_USER, dataContainer));
        
        dataContainer = new Message("not" + login, chatID, null, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_ADD_USER, dataContainer));
        
        try{
            dataOutQueue.put(mesg);
            dataOutQueue.put(badMesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        try{
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e){

        }
        server.setAccepted();
        dataContainer = new Message("not" + login, chatID, null, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_ADD_USER, dataContainer));
        
        try{
            dataOutQueue.put(badMesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        try{
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e){

        }

        //good
        Pair<ResponseType,Message> check = pendingMesgQueue.poll();
        //bad
        Pair<ResponseType,Message> bad = pendingMesgQueue.poll();
        Pair<ResponseType,Message> bad2 = pendingMesgQueue.poll();

        assertEquals(ResponseStatus.ACCEPTED, check.getValue().getStatus());
        assertEquals(login, check.getValue().getLogin());
        assertEquals(ResponseStatus.ERROR, bad.getValue().getStatus());
        assertEquals("not" + login, bad.getValue().getLogin());
        assertEquals(ResponseStatus.NOACCESS, bad2.getValue().getStatus());
        assertEquals("not" + login, bad2.getValue().getLogin());
        assertTrue(pendingMesgQueue.isEmpty());
    }

    @Test
    public void chatNameChange(){
        assertTrue(setUpReady);
        String oldChatId = "test";
        String newChatId = "test2";
        String login = "test";

        String mesg = "REQ_CHAN_CHAT_NAME-" + login + "-" + oldChatId + "-" + newChatId;
        String badMesg = "REQ_CHAN_CHAT_NAME-" + "not" + login + "-" + oldChatId + "-" + newChatId;

        Message dataContainer = new Message(login, oldChatId, newChatId, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_NAME_CHANGE, dataContainer));
    
        dataContainer = new Message("not" + login, oldChatId, newChatId, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_NAME_CHANGE, dataContainer));
        
        try{
            dataOutQueue.put(mesg);
            dataOutQueue.put(badMesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        try{
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e){

        }

        server.setAccepted();
        dataContainer = new Message("not" + login, oldChatId, newChatId, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_NAME_CHANGE, dataContainer));
        
        try{
            dataOutQueue.put(badMesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        try{
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e){

        }

        //good
        Pair<ResponseType,Message> check = pendingMesgQueue.poll();
        //bad
        Pair<ResponseType,Message> bad = pendingMesgQueue.poll();
        Pair<ResponseType,Message> bad2 = pendingMesgQueue.poll();

        assertEquals(ResponseStatus.ACCEPTED, check.getValue().getStatus());
        assertEquals(login, check.getValue().getLogin());
        assertEquals(ResponseStatus.ERROR, bad.getValue().getStatus());
        assertEquals("not" + login, bad.getValue().getLogin());
        assertEquals(ResponseStatus.NOACCESS, bad2.getValue().getStatus());
        assertEquals("not" + login, bad2.getValue().getLogin());
        assertTrue(pendingMesgQueue.isEmpty());
    }

    @Test
    public void chatRankChange(){
        assertTrue(setUpReady);
        String chatId = "test";
        boolean admin = true;
        String login = "test";

        String mesg = "REQ_CHAN_CHAT_RANK-" + chatId + "-" + login + "-" + login
            + "-" + (admin ? "ADMIN" : "USER");
        String badMesg = "REQ_CHAN_CHAT_RANK-" + chatId + "-" + "not" + login + "-" + login
            + "-" + (admin ? "ADMIN" : "USER");

        List<String> ranga = new ArrayList<>();
        ranga.add( admin ? "A" : "U");
        Message dataContainer = new Message(login, chatId, login, ranga, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_RANK_CHANGE, dataContainer));
    
        dataContainer = new Message("not" + login, chatId, login, ranga, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_RANK_CHANGE, dataContainer));
     
        try{
            dataOutQueue.put(mesg);
            dataOutQueue.put(badMesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        try{
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e){

        }

        server.setAccepted();
        dataContainer = new Message("not" + login, chatId, login, ranga, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_RANK_CHANGE, dataContainer));
    
        try{
            dataOutQueue.put(badMesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        try{
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e){

        }

        //good
        Pair<ResponseType,Message> check = pendingMesgQueue.poll();
        //bad
        Pair<ResponseType,Message> bad = pendingMesgQueue.poll();
        Pair<ResponseType,Message> bad2 = pendingMesgQueue.poll();

        assertEquals(ResponseStatus.ACCEPTED, check.getValue().getStatus());
        assertEquals(login, check.getValue().getLogin());
        assertEquals(ResponseStatus.ERROR, bad.getValue().getStatus());
        assertEquals("not" + login, bad.getValue().getLogin());
        assertEquals(ResponseStatus.NOACCESS, bad2.getValue().getStatus());
        assertEquals("not" + login, bad2.getValue().getLogin());
        assertTrue(pendingMesgQueue.isEmpty());
    }

    @Test
    public void removeUserFromChat(){
        assertTrue(setUpReady);

        String login = "test";
        String chatId = "testchat1234";
        String userToDelete = "L=Magda,Ela";

        String mesg = "REQ_DEL_CHAT-" + chatId + "-" + login + "-" + userToDelete;
        String badMesg = "REQ_DEL_CHAT-" + chatId + "-" + "not" + login + "-" + userToDelete;

        List<String> userlogins = new ArrayList<>();
        userlogins.add("Magda");
        userlogins.add("Ela");

        Message dataContainer = new Message(login, chatId, null, userlogins, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_DEL_USER, dataContainer));

        dataContainer = new Message("not" + login, chatId, null, userlogins, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_DEL_USER, dataContainer));

        try{
            dataOutQueue.put(mesg);
            dataOutQueue.put(badMesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        try{
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e){

        }

        server.setAccepted();
        dataContainer = new Message("not" + login, chatId, null, userlogins, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_DEL_USER, dataContainer));
    
        try{
            dataOutQueue.put(badMesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        try{
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e){

        }

        //good
        Pair<ResponseType,Message> check = pendingMesgQueue.poll();
        //bad
        Pair<ResponseType,Message> bad = pendingMesgQueue.poll();
        Pair<ResponseType,Message> bad2 = pendingMesgQueue.poll();

        assertEquals(ResponseStatus.ACCEPTED, check.getValue().getStatus());
        assertEquals(login, check.getValue().getLogin());
        assertEquals(chatId, check.getValue().getEmail());
        assertEquals("Magda", check.getValue().getLogins().get(0));
        assertEquals("Ela", check.getValue().getLogins().get(1));
        assertEquals(ResponseStatus.ERROR, bad.getValue().getStatus());
        assertEquals("not" + login, bad.getValue().getLogin());
        assertEquals("Magda", bad.getValue().getLogins().get(0));
        assertEquals("Ela", bad.getValue().getLogins().get(1));
        assertEquals(ResponseStatus.NOACCESS, bad2.getValue().getStatus());
        assertEquals("not" + login, bad2.getValue().getLogin());
        assertEquals("Magda", bad2.getValue().getLogins().get(0));
        assertEquals("Ela", bad2.getValue().getLogins().get(1));
        assertTrue(pendingMesgQueue.isEmpty());
    }

    @Test
    public void destroyChat(){
        assertTrue(setUpReady);

        String login = "test";
        String chatId = "testchat1234";

        String mesg = "REQ_DES_CHAT-" + login + "-" + chatId;
        String badMesg = "REQ_DES_CHAT-" + "not" + login + "-" + chatId;

        Message dataContainer = new Message(login, chatId, null, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_DESTROY, dataContainer));
    
        dataContainer = new Message("not" + login, chatId, null, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_DESTROY, dataContainer));
        
        try{
            dataOutQueue.put(mesg);
            dataOutQueue.put(badMesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        try{
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e){

        }

        server.setAccepted();
        dataContainer = new Message("not" + login, chatId, null, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.CHAT_DESTROY, dataContainer));
        
        try{
            dataOutQueue.put(badMesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        try{
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e){

        }

        //good
        Pair<ResponseType,Message> check = pendingMesgQueue.poll();
        //bad
        Pair<ResponseType,Message> bad = pendingMesgQueue.poll();
        Pair<ResponseType,Message> bad2 = pendingMesgQueue.poll();

        assertEquals(ResponseStatus.ACCEPTED, check.getValue().getStatus());
        assertEquals(login, check.getValue().getLogin());
        assertEquals(ResponseStatus.ERROR, bad.getValue().getStatus());
        assertEquals("not" + login, bad.getValue().getLogin());
        assertEquals(ResponseStatus.NOACCESS, bad2.getValue().getStatus());
        assertEquals("not" + login, bad2.getValue().getLogin());
        assertTrue(pendingMesgQueue.isEmpty());
    }

    @Test
    public void sendData(){
        assertTrue(setUpReady);

        String login = "test";
        String chatId = "testchat1234";
        String text = "test String for fun";

        String mesg = "SEND_DATA-" + login + "-" + chatId + "-" + text;
        String badMesg = "SEND_DATA-" + "not" + login + "-" + chatId + "-" + text;

        Message dataContainer = new Message(login, chatId, text, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.MESSAGE_ANS, dataContainer));
        
        dataContainer = new Message("not" + login, chatId, text, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.MESSAGE_ANS, dataContainer));
        Pair<ResponseType,Message> check4 = pendingMesgQueue.element();
        System.out.println(check4.getValue().getEmail() + " : " + check4.getValue().getPassw());
        try{
            dataOutQueue.put(mesg);
            dataOutQueue.put(badMesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        try{
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e){

        }

        server.setAccepted();
        dataContainer = new Message("not" + login, chatId, text, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.MESSAGE_ANS, dataContainer));
        
        try{
            dataOutQueue.put(badMesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        try{
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e){

        }
        //good
        Pair<ResponseType,Message> check = pendingMesgQueue.poll();
        //bad
        Pair<ResponseType,Message> bad = pendingMesgQueue.poll();
        Pair<ResponseType,Message> bad2 = pendingMesgQueue.poll();

        //good
        assertEquals(ResponseStatus.ACCEPTED, check.getValue().getStatus());
        assertEquals(login, check.getValue().getLogin());
        assertEquals(chatId, check.getValue().getEmail());
        assertEquals(text, check.getValue().getPassw());
        assertNotEquals(null, check.getValue().getLogins().get(0));
        //bad
        assertEquals(ResponseStatus.ERROR, bad.getValue().getStatus());
        assertEquals("not" + login, bad.getValue().getLogin());
        assertEquals(chatId, bad.getValue().getEmail());
        assertEquals(text, bad.getValue().getPassw());
        assertEquals(ResponseStatus.NAME, bad2.getValue().getStatus());
        assertEquals("not" + login, bad2.getValue().getLogin());
        assertEquals(chatId, bad2.getValue().getEmail());
        assertEquals(text, bad2.getValue().getPassw());
        assertTrue(pendingMesgQueue.isEmpty());
    }

    @Test
    public void disconnect(){
        assertTrue(setUpReady);

        String mesg = "REQ_CONN_END-";

        Message dataContainer = new Message(null, null, null, null, null, null);
        pendingMesgQueue.add(new Pair<ResponseType,Message>(ResponseType.DISCONNECT, dataContainer));
        
        try{
            dataOutQueue.put(mesg);
        } catch (InterruptedException e){
            Logger.getLogger().logError("Interrupt while adding request!");
        }

        try{
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e){

        }

        //good
        Pair<ResponseType,Message> check = pendingMesgQueue.poll();

        assertEquals(null, check.getValue().getLogin());
        assertEquals(null, check.getValue().getEmail());
        assertEquals(null, check.getValue().getPassw());
        assertEquals(null, check.getValue().getLogins());
        assertEquals(null, check.getValue().getChats());
        assertEquals(ResponseStatus.NO_ANSWER, check.getValue().getStatus());
        assertTrue(pendingMesgQueue.isEmpty());
    }
}
