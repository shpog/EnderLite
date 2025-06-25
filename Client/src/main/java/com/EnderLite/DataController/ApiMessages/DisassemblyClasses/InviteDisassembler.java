package com.EnderLite.DataController.ApiMessages.DisassemblyClasses;

import com.EnderLite.DataController.ApiMessages.DisassemblerInterFace;
import com.EnderLite.DataController.ApiMessages.Message;
import com.EnderLite.DataController.ApiMessages.ResponseStatus;

/**
 * Dissasembler of invite request
 * @author Micro9261
 */
public class InviteDisassembler implements DisassemblerInterFace{
    
    @Override
    public void dissasembly(String message, Message mesg){
        String[] parts = message.split("-");
        switch (parts[0]) {
            case "ACCEPT":
                mesg.setStatus(ResponseStatus.ACCEPTED);
                break;
            case "DENIED":
                mesg.setStatus(ResponseStatus.DENIED);
                break;
            default:
                mesg.setStatus(ResponseStatus.NO_ANSWER);
                break;
        }       
        if (mesg.getStatus().equals(ResponseStatus.NO_ANSWER)){
            mesg.setLogin(parts[0]);
        } else {
            mesg.setPassw(parts[1]);
        }
        
    }
}
