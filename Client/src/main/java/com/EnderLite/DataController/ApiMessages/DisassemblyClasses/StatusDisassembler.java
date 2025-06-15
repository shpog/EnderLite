package com.EnderLite.DataController.ApiMessages.DisassemblyClasses;

import com.EnderLite.DataController.ApiMessages.DisassemblerInterFace;
import com.EnderLite.DataController.ApiMessages.Message;
import com.EnderLite.DataController.ApiMessages.ResponseStatus;

public class StatusDisassembler implements DisassemblerInterFace{
    
    @Override
    public void dissasembly(String message, Message mesg){
        if ( message.startsWith("ACCEPT") ){
            mesg.setStatus(ResponseStatus.ACCEPTED);
        } else if (message.startsWith("DENIED")){
            String checkReason = message.substring("DENIED".length());
            if (checkReason.startsWith("-EMAIL")){
                mesg.setStatus(ResponseStatus.EMAIL);
            } else if (checkReason.startsWith("-LOGIN")){
                mesg.setStatus(ResponseStatus.LOGIN);
            } else if (checkReason.startsWith("-DENIED")) {
                mesg.setStatus(ResponseStatus.ERROR);
            } else {
                mesg.setStatus(ResponseStatus.DENIED);
            }
        } else {
            mesg.setStatus(ResponseStatus.NO_ANSWER);
        }
    }
}
