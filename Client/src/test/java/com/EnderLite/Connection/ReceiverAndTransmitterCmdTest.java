package com.EnderLite.Connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
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

public class ReceiverAndTransmitterCmdTest {
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
        server.setCmdMode(true);
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
        server.interrupt();
        transmitter.interrupt();
        receiver.interrupt();
        try{
            connController.closeStreams();
        } catch (IOException e){
            Logger.getLogger().logError("Error while closing streams (DataCOntroller)");
        }
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
    }

    @Test
    public void inviteAnswer(){
        assertTrue(setUpReady);
        
        server.sendCmd("CMD_INV_LOG-test");
        server.fireCmd();

        try{
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e){

        }

        Pair<ResponseType,Message> check = pendingMesgQueue.poll();
        assertEquals(ResponseStatus.NO_ANSWER, check.getValue().getStatus());
        assertEquals("test", check.getValue().getLogin());
    }

    @Test
    public void removeFriendCmd(){
        assertTrue(setUpReady);

        server.sendCmd("CMD_DEL_LOG-test");
        server.fireCmd();

        try{
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e){

        }

        Pair<ResponseType,Message> check = pendingMesgQueue.poll();
        assertEquals(ResponseStatus.NO_ANSWER, check.getValue().getStatus());
        assertEquals("test", check.getValue().getLogin());
    }

    @Test
    public void changeChatNameCmd(){
        assertTrue(setUpReady);

        server.sendCmd("CMD_CHAN_CHAT_NAME-test-testNew");
        server.fireCmd();

        try{
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e){

        }

        Pair<ResponseType,Message> check = pendingMesgQueue.poll();
        assertEquals(ResponseStatus.NO_ANSWER, check.getValue().getStatus());
        assertEquals("test", check.getValue().getEmail());
        assertEquals("testNew", check.getValue().getPassw());
    }

    @Test
    public void destroyChatCmd(){
        assertTrue(setUpReady);

        server.sendCmd("CMD_DES_CHAT-test");
        server.fireCmd();

        try{
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e){

        }

        Pair<ResponseType,Message> check = pendingMesgQueue.poll();
        assertEquals(ResponseStatus.NO_ANSWER, check.getValue().getStatus());
        assertEquals("test", check.getValue().getEmail());
    }

    @Test
    public void writeDataCmd(){
        assertTrue(setUpReady);

        server.sendCmd("CMD_WRITE_DATA-chat1-login1-TestText-23:15");
        server.fireCmd();

        try{
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e){

        }

        Pair<ResponseType,Message> check = pendingMesgQueue.poll();
        assertEquals(ResponseStatus.NO_ANSWER, check.getValue().getStatus());
        assertEquals("chat1", check.getValue().getEmail());
        assertEquals("TestText", check.getValue().getPassw());
        assertEquals("login1", check.getValue().getLogin());
        assertEquals("23:15", check.getValue().getLogins().get(0));
        assertEquals(null, check.getValue().getChats());
    }
}


