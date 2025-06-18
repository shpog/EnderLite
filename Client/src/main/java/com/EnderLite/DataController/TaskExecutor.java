package com.EnderLite.DataController;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import com.EnderLite.DataController.ApiMessages.Message;
import com.EnderLite.DataController.ApiMessages.ResponseStatus;
import com.EnderLite.DataController.ApiMessages.ResponseType;
import com.EnderLite.Logger.Logger;

import javafx.application.Platform;
import javafx.util.Pair;

public class TaskExecutor extends Thread{
    private ConcurrentLinkedQueue< Pair<ResponseType, Message> > pendingMessages;
    private DataController dataController;
    private long interval;
    private boolean shutdown = false;

    public void setDataController(DataController controller){
        dataController = controller;
    }

    public void setPendingQueue(ConcurrentLinkedQueue< Pair<ResponseType, Message> > queue){
        pendingMessages = queue;
    }

    public void setInterval(long interval){
        this.interval = interval;
    }

    @Override
    public void run(){
        Logger.getLogger().logInfo("Started(task executor)");
        while (true) {
            //if client connection end
            if (isInterrupted() || shutdown){
                Logger.getLogger().logInfo("Interrupted (task executor)");
                break;
            }
            try{
                TimeUnit.MILLISECONDS.sleep(interval);
            } catch (InterruptedException e){

            }
            // Logger.getLogger().logInfo("Checking in progress (task executor)");

            if ( !pendingMessages.isEmpty() ){
                Iterator<Pair<ResponseType, Message> > iter = pendingMessages.iterator();
                while (iter.hasNext()){
                    Pair<ResponseType, Message> test = iter.next();
                    if (test.getValue().getStatus() != null ){
                        executeAction(test.getKey(), test.getValue() );
                        pendingMessages.remove(test);
                        Logger.getLogger().logInfo("Task excetuted!");
                    }
                }
            }
        }
        Logger.getLogger().logInfo("Ended (task executor)");
    }    

    private void executeAction(ResponseType actionType, Message mesg){
        switch (actionType) {
            case AUTH_STATUS:
                dataController.ansAuthStatus(mesg.getStatus());
                if (mesg.getStatus().equals(ResponseStatus.ACCEPTED)){
                    dataController.getUserData().setLogin(mesg.getLogin());
                    dataController.getUserData().setEmail(mesg.getEmail());
                }
                break;
            case CREATE_USER:
                dataController.ansCreateStatus(mesg.getStatus());
                if (mesg.getStatus().equals(ResponseStatus.ACCEPTED)){
                    dataController.getUserData().setLogin(mesg.getLogin());
                    dataController.getUserData().setEmail(mesg.getEmail());
                }
                break;
            case USER_DATA:
                if (mesg.getStatus().equals(ResponseStatus.ACCEPTED)){
                    Platform.runLater(()-> {
                        dataController.getUserData().setLogin(mesg.getLogin());
                        dataController.getUserData().setEmail(mesg.getEmail());
                        for (String friend : mesg.getLogins()){
                            dataController.addFriend(friend);
                        }
                        for (String chats : mesg.getChats()){
                            dataController.addChat(chats, Rank.USER);
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        dataController.sendNotification("Błąd pobierania danych", true);
                    });
                }
                break;
            case INV_CMD:
                Platform.runLater(() -> {
                    dataController.sendInviteNotification(mesg.getLogin());
                });
                break;
            case INV_ANS:
                if (mesg.getStatus().equals(ResponseStatus.ACCEPTED)){
                    Platform.runLater(() -> {
                        dataController.addFriend(mesg.getEmail());
                    });
                }
                break;
            case DEL_CMD: //nothing
                break;
            case DEL_ANS: //nothing
                break;
            case CHAT_CREATE:
                if (mesg.getStatus().equals(ResponseStatus.ACCEPTED)){
                    Platform.runLater(() -> {
                        dataController.addChat(mesg.getEmail(), Rank.USER);
                    });
                }
                break;
            case CHAT_ADD_USER:
                break;
            case CHAT_NAME_CHANGE: //nothing
                break;
            case CMD_CHAT_NAME_CHANGE: //nothing
                break;
            case CHAT_RANK_CHANGE: //nothing
                break;
            case CHAT_DEL_USER: //nothing
                break;
            case CHAT_DESTROY: //nothing
                break;
            case CMD_CHAT_DESTROY://nothing
                break;
            case MESSAGE_ANS:
                Platform.runLater(() -> {
                    dataController.addMessageToView(mesg.getPassw(), mesg.getLogin(), mesg.getLogins().get(0), true);
                });
                break;
            case MESSAGE_CMD:
                Platform.runLater(() -> {
                    dataController.addMessageToView(mesg.getPassw(), mesg.getLogin(), mesg.getLogins().get(0), false);
                });
                break;
            case DISCONNECT:
                shutdown = true;
                pendingMessages.notifyAll();
                break;
        }
    }
}

