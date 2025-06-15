package com.EnderLite.DataController.ApiMessages.DisassemblyClasses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.EnderLite.DataController.ApiMessages.DisassemblerInterFace;
import com.EnderLite.DataController.ApiMessages.Message;
import com.EnderLite.DataController.ApiMessages.ResponseStatus;

public class UserDataDisassembler implements DisassemblerInterFace{
    
    @Override
    public void dissasembly(String message, Message mesg){
        if (message.startsWith("-DENIED")){
            mesg.setStatus(ResponseStatus.ERROR);
        } else {
            String login = null;
            String email = null;
            List<String> friendsLogins = new ArrayList<String>();
            List<String> chatList = new ArrayList<String>();

            String[] parts = message.split("-");
            for (String val : parts){
                if ( val.startsWith("L=") ){
                    login = val.substring(2);
                } else if ( val.startsWith("E=") ){
                    email = val.substring(2);
                } else if ( val.startsWith("F=") ){
                    friendsLogins = Arrays.asList(val.substring(2).split(","));
                } else if ( val.startsWith("C=") ){
                    chatList = Arrays.asList(val.substring(2).split(","));
                }
            }

            mesg.setLogin(login);
            mesg.setEmail(email);
            mesg.setChats(chatList);
            mesg.setLogins(friendsLogins);
            mesg.setStatus(ResponseStatus.ACCEPTED);
        }
    }
}
