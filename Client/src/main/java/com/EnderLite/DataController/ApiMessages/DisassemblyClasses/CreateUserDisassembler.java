package com.EnderLite.DataController.ApiMessages.DisassemblyClasses;

import com.EnderLite.DataController.ApiMessages.DisassemblerInterFace;
import com.EnderLite.DataController.ApiMessages.Message;
import com.EnderLite.DataController.ApiMessages.ResponseStatus;

public class CreateUserDisassembler implements DisassemblerInterFace{
    
    @Override
    public void dissasembly(String message, Message mesg){
        String[] parts = message.split("-");
        if (parts[0].equals("DENIED")){
            if (parts.length == 2 ){
                switch (parts[1]) {
                    case "LOGIN":
                        mesg.setStatus(ResponseStatus.LOGIN);
                        break;
                    case "EMAIL":
                        mesg.setStatus(ResponseStatus.EMAIL);
                        break;
                    case "ERROR":
                        mesg.setStatus(ResponseStatus.ERROR);
                        break;
                    default:
                        mesg.setStatus(ResponseStatus.DENIED);
                        break;
                }
            } else {
                mesg.setStatus(ResponseStatus.DENIED);
            }
        } else {
            String login = parts[0];
            String email = parts[1];
            mesg.setStatus(ResponseStatus.ACCEPTED);
            mesg.setEmail(email);
            mesg.setLogin(login);
        }
    }
}
