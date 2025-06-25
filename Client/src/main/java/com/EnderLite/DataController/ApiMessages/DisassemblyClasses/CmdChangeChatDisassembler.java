package com.EnderLite.DataController.ApiMessages.DisassemblyClasses;

import com.EnderLite.DataController.ApiMessages.DisassemblerInterFace;
import com.EnderLite.DataController.ApiMessages.Message;
import com.EnderLite.DataController.ApiMessages.ResponseStatus;

/**
 * Dissasembler of change chat name command
 * @author Micro9261
 */
public class CmdChangeChatDisassembler implements DisassemblerInterFace{
    
    @Override
    public void dissasembly(String message, Message mesg){
        String[] parts = message.split("-");
        String oldChatName = parts[0];
        String newChatName = parts[1];
        mesg.setEmail(oldChatName);
        mesg.setPassw(newChatName);
        mesg.setStatus(ResponseStatus.NO_ANSWER);
    }
}
