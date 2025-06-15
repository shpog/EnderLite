package com.EnderLite.DataController.ApiMessages.DisassemblyClasses;

import java.util.ArrayList;
import java.util.List;

import com.EnderLite.DataController.ApiMessages.DisassemblerInterFace;
import com.EnderLite.DataController.ApiMessages.Message;
import com.EnderLite.DataController.ApiMessages.ResponseStatus;

public class MessageDisassembler implements DisassemblerInterFace{
    
    @Override
    public void dissasembly(String message, Message mesg){
        if (message.startsWith("ACCEPT")){
            mesg.setStatus(ResponseStatus.ACCEPTED);
            String time = message.substring(message.indexOf("-") + 1);
            List<String> timeList = new ArrayList<>();
            timeList.add(time);
            mesg.setLogins(timeList);
        } else if (message.startsWith("DENIED")){
            String checkReason = message.substring("DENIED".length());
            if (checkReason.startsWith("-NAME")){
                mesg.setStatus(ResponseStatus.DENIED);
            } else if (checkReason.startsWith("-DENIED")) {
                mesg.setStatus(ResponseStatus.ERROR);
            }
        }
    }
}
