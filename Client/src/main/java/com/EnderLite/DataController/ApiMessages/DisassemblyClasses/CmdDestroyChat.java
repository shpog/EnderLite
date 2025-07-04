package com.EnderLite.DataController.ApiMessages.DisassemblyClasses;

import com.EnderLite.DataController.ApiMessages.DisassemblerInterFace;
import com.EnderLite.DataController.ApiMessages.Message;
import com.EnderLite.DataController.ApiMessages.ResponseStatus;

/**
 * Dissasembler of destroy chat command
 * @author Micro9261
 */
public class CmdDestroyChat implements DisassemblerInterFace{

    @Override
    public void dissasembly(String message, Message mesg){
        String chatName = message;
        mesg.setEmail(chatName);
        mesg.setStatus(ResponseStatus.NO_ANSWER);
    }
}