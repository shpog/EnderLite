package com.EnderLite.DataController;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.EnderLite.DataController.ApiMessages.Message;
import com.EnderLite.DataController.ApiMessages.ResponseStatus;
import com.EnderLite.DataController.ApiMessages.ResponseType;
import com.EnderLite.Logger.Logger;

import javafx.util.Pair;

public class TaskExecutor extends Thread{
    private ConcurrentLinkedQueue< Pair<ResponseType, Message> > pendingMessages;
    private DataController dataController;
    private long interval;

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
        while (true) {
            //if client connection end
            if (isInterrupted()){
                break;
            }

            try{
                wait(interval);
            } catch (InterruptedException e) {
                Logger.getLogger().logError("Error while waiting (TaskExecutor)");
            }

            if ( !pendingMessages.isEmpty() ){
                Iterator<Pair<ResponseType, Message> > iter = pendingMessages.iterator();
                while (iter.hasNext()){
                    Pair<ResponseType, Message> test = iter.next();
                    if (test.getValue().getStatus() != null ){
                        executeAction(test.getKey(), test.getValue() );
                        pendingMessages.remove(test);
                    }
                }
            }
        }
    }    

    private void executeAction(ResponseType actionType, Message mesg){
        switch (actionType) {
            case AUTH_STATUS:
                dataController.ansAuthStatus(mesg.getStatus());
                break;
            case CREATE_USER:
                dataController.ansCreateStatus(mesg.getStatus());
                break;
            case USER_DATA:
                if (mesg.getStatus().equals(ResponseStatus.ACCEPTED)){
                    
                }
                break;
            case INV_CMD:
                break;
            case INV_ANS:
                break;
            case DEL_CMD:
                break;
            case DEL_ANS:
                break;
            case CHAT_CREATE:
                break;
            case CHAT_ADD_USER:
                break;
            case CHAT_NAME_CHANGE:
                break;
            case CMD_CHAT_NAME_CHANGE:
                break;
            case CHAT_RANK_CHANGE:
                break;
            case CHAT_DEL_USER:
                break;
            case CHAT_DESTROY:
                break;
            case CMD_CHAT_DESTROY:
                break;
            case MESSAGE_ANS:
                dataController.addMessageToView(mesg.getPassw(), mesg.getLogin(), null, true);
                break;
            case MESSAGE_CMD:
                dataController.addMessageToView(mesg.getPassw(), mesg.getLogin(), null, false);
                break;
            case DISCONNECT:
                break;
        }
    }
}

