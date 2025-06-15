package com.EnderLite.DataController.ApiMessages.DisassemblyClasses;

import com.EnderLite.DataController.ApiMessages.DisassemblerInterFace;
import com.EnderLite.DataController.ApiMessages.Message;

public class CmdChangeChatDisassembler implements DisassemblerInterFace{
    @Override
    public void dissasembly(String message, Message mesg){
        int newChatNameIndex = message.indexOf("-");
        String oldChatName = message.substring(0, newChatNameIndex);
        String newChatName = message.substring(newChatNameIndex + 1);
        mesg.setEmail(oldChatName);
        mesg.setPassw(newChatName);
    }
}
