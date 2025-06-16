package com.EnderLite.DataController.ApiMessages.DisassemblyClasses;

import com.EnderLite.DataController.ApiMessages.DisassemblerInterFace;
import com.EnderLite.DataController.ApiMessages.Message;
import com.EnderLite.DataController.ApiMessages.ResponseStatus;

public class StatusDisassembler implements DisassemblerInterFace{
    
    @Override
    public void dissasembly(String message, Message mesg){
        String[] parts = message.split("-");
        if (parts[0].equals("ACCEPT")){
            mesg.setStatus(ResponseStatus.ACCEPTED);
        } else if (parts[0].equals("DENIED")){
            switch (parts[1]) {
                case "EMAIL":
                    mesg.setStatus(ResponseStatus.EMAIL);
                    break;
                case "LOGIN":
                    mesg.setStatus(ResponseStatus.LOGIN);
                    break;
                case "NOACCESS":
                    mesg.setStatus(ResponseStatus.NOACCESS);
                    break;
                case "ERROR":
                    mesg.setStatus(ResponseStatus.ERROR);
                    break;
                case "USED":
                    mesg.setStatus(ResponseStatus.USED);
                    break;
                default:
                    mesg.setStatus(ResponseStatus.DENIED);
                    break;
            }
        } else {
            mesg.setStatus(ResponseStatus.NO_ANSWER);
        }
    }
}
