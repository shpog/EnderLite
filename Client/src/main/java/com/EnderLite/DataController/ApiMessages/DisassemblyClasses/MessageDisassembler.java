package com.EnderLite.DataController.ApiMessages.DisassemblyClasses;

import java.util.ArrayList;
import java.util.List;

import com.EnderLite.DataController.ApiMessages.DisassemblerInterFace;
import com.EnderLite.DataController.ApiMessages.Message;
import com.EnderLite.DataController.ApiMessages.ResponseStatus;

public class MessageDisassembler implements DisassemblerInterFace{
    
    @Override
    public void dissasembly(String message, Message mesg){
        String[] parts = message.split("-");
        if (parts[0].equals("ACCEPT")){
            mesg.setStatus(ResponseStatus.ACCEPTED);
            List<String> timeList = new ArrayList<>();
            timeList.add(parts[1]);
            mesg.setLogins(timeList);
        } else if (parts[0].equals("DENIED")){
            if (parts[1].equals("NAME")){
                mesg.setStatus(ResponseStatus.NAME);
            } else if (parts[1].equals("ERROR")){
                mesg.setStatus(ResponseStatus.ERROR);
            } else {
                mesg.setStatus(ResponseStatus.DENIED);
            }   
        }
    }
}
