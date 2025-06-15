package com.EnderLite.DataController.ApiMessages.DisassemblyClasses;

import com.EnderLite.DataController.ApiMessages.DisassemblerInterFace;
import com.EnderLite.DataController.ApiMessages.Message;
import com.EnderLite.DataController.ApiMessages.ResponseStatus;

public class AuthDisassembler implements DisassemblerInterFace{
    
    @Override
    public void dissasembly(String message, Message mesg){
        if (message.startsWith("DENIED")){
            mesg.setStatus(ResponseStatus.DENIED);
        } else {
            int loginEnd = message.indexOf("-");
            String login = message.substring(0, loginEnd);
            String email = message.substring(loginEnd + 1);
            mesg.setStatus(ResponseStatus.ACCEPTED);
            mesg.setEmail(email);
            mesg.setLogin(login);
        }
    }
}
