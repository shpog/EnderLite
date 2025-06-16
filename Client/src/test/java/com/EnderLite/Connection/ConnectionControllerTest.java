package com.EnderLite.Connection;


import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.EnderLite.Logger.Logger;

public class ConnectionControllerTest {
    private ConnectionController controller;
    private DummyServer serverThread;

    @BeforeEach
    public void setUu(){
        controller = new ConnectionController("localhost", 12345);
        serverThread = new DummyServer(12345, false);
    }

    @Test
    public void HandshakeTest(){
        serverThread.setOnlyHandshake();
        serverThread.start();

        assertTrue(controller.establishConnection());
        serverThread.interrupt();
        try{
            serverThread.join();
        } catch (InterruptedException e){
            Logger.getLogger().logError("Error while waiting for threads (Test)");
        }
        try{
            controller.closeStreams();
        } catch (IOException e){
            Logger.getLogger().logError("Erro while closing sockets Test");
        }
        System.out.println(serverThread.handshakeFirstMessage);
        System.out.println("2 message: " + serverThread.handshakeSecondMessage);
        System.out.println("3 message: " + serverThread.handshakeThirdMessage);
        assertTrue(serverThread.handshakeAccepted);
    }
}
