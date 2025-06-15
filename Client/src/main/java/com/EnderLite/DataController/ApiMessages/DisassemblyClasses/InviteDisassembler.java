package com.EnderLite.DataController.ApiMessages.DisassemblyClasses;

import com.EnderLite.DataController.ApiMessages.DisassemblerInterFace;
import com.EnderLite.DataController.ApiMessages.Message;
import com.EnderLite.DataController.ApiMessages.ResponseStatus;

public class InviteDisassembler implements DisassemblerInterFace{
    
    @Override
    public void dissasembly(String message, Message mesg){
        if (message.startsWith("ACCEPT")){
            mesg.setStatus(ResponseStatus.ACCEPTED);
            int indexOfLogin = message.indexOf("-");
            message = message.substring(indexOfLogin + 1);
            mesg.setPassw(message); //login of invited person
        } else if (message.startsWith("DENIED")){
            mesg.setStatus(ResponseStatus.DENIED);
            int indexOfLogin = message.indexOf("-");
            message = message.substring(indexOfLogin + 1);
            mesg.setPassw(message); //login of invited person
        } else {
            mesg.setStatus(ResponseStatus.NO_ANSWER);
            mesg.setLogin(message); //login of person who invites
        }
        
    }
}
