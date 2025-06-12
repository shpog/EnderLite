package com.EnderLite.DataController;

// import java.util.concurrent.BlockingQueue;

import com.EnderLite.Connection.ConnectionController;


public class DataController {
    private volatile UserData userData;
    private static volatile DataController instance;
    //queues
    // private BlockingQueue<String> dataOutQueue;
    // private BlockingQueue<String> dataInQueue;
    // private BlockingQueue<String> pendingMessages;

    private DataController(){
        //nothing
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


}
