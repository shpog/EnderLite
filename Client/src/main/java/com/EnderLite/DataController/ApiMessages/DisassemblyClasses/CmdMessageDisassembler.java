package com.EnderLite.DataController.ApiMessages.DisassemblyClasses;

import java.util.ArrayList;
import java.util.List;

import com.EnderLite.DataController.ApiMessages.DisassemblerInterFace;
import com.EnderLite.DataController.ApiMessages.Message;
import com.EnderLite.DataController.ApiMessages.ResponseStatus;

/**
 * Dissasembler of write message command
 * @author Micro9261
 */
public class CmdMessageDisassembler implements DisassemblerInterFace{
    
    @Override
    public void dissasembly(String message, Message mesg){
        String [] parts = message.split("-");
        mesg.setLogin(parts[1]);
        mesg.setEmail(parts[0]);
        mesg.setPassw(parts[2]);
        List<String> time = new ArrayList<>();
        time.add(parts[3]);
        mesg.setLogins(time);
        mesg.setStatus(ResponseStatus.NO_ANSWER);
    }
}
