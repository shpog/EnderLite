package com.EnderLite.Connection;

import java.io.DataInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.EnderLite.DataController.ApiMessages.DisassemblerInterFace;
import com.EnderLite.DataController.ApiMessages.Message;
import com.EnderLite.DataController.ApiMessages.ResponseType;
import com.EnderLite.DataController.ApiMessages.DisassemblyClasses.AuthDisassembler;
import com.EnderLite.DataController.ApiMessages.DisassemblyClasses.CmdChangeChatDisassembler;
import com.EnderLite.DataController.ApiMessages.DisassemblyClasses.CmdMessageDisassembler;
import com.EnderLite.DataController.ApiMessages.DisassemblyClasses.InviteDisassembler;
import com.EnderLite.DataController.ApiMessages.DisassemblyClasses.MessageDisassembler;
import com.EnderLite.DataController.ApiMessages.DisassemblyClasses.StatusDisassembler;
import com.EnderLite.DataController.ApiMessages.DisassemblyClasses.UserDataDisassembler;
import com.EnderLite.Logger.Logger;

import javafx.util.Pair;

public class Receiver extends Thread{
    private ConcurrentLinkedQueue< Pair<ResponseType, Message> > pendingMessages;
    private SecretKey secretKey;
    private Cipher cipher;
    private DataInputStream inStream;
    private final Map<String, Pair<ResponseType, DisassemblerInterFace> > responseMap = new HashMap<String, Pair<ResponseType, DisassemblerInterFace> >();

    {
        Security.addProvider(new BouncyCastleProvider());
        responseMap.put("AUTH_RESP-", new Pair<>(ResponseType.AUTH_STATUS, new AuthDisassembler()) );
        responseMap.put("ANS_ADD_USER-", new Pair<>(ResponseType.CREATE_USER, new AuthDisassembler()) );
        responseMap.put("ANS_USER_DATA-", new Pair<>(ResponseType.USER_DATA, new UserDataDisassembler()) );
        responseMap.put("CMD_INV_LOG-", new Pair<>(ResponseType.INV_CMD, new InviteDisassembler()) );
        responseMap.put("ANS_INV_LOG-", new Pair<>(ResponseType.INV_ANS, new InviteDisassembler()) );
        responseMap.put("CMD_DEL_LOG-", new Pair<>(ResponseType.DEL_CMD, new InviteDisassembler()) );
        responseMap.put("ANS_DEL_LOG-", new Pair<>(ResponseType.DEL_ANS, new InviteDisassembler()) );
        responseMap.put("ANS_CRT_CHAT-", new Pair<>(ResponseType.CHAT_CREATE, new StatusDisassembler()) );
        responseMap.put("ANS_ADD_CHAT-", new Pair<>(ResponseType.CHAT_ADD_USER, new StatusDisassembler()) );
        responseMap.put("ANS_CHAN_CHAT_NAME-", new Pair<>(ResponseType.CHAT_NAME_CHANGE, new StatusDisassembler()) );
        responseMap.put("CMD_CHAN_CHAT_NAME-", new Pair<>(ResponseType.CMD_CHAT_NAME_CHANGE, new CmdChangeChatDisassembler()) );
        responseMap.put("ANS_CHAN_CHAT_RANK-", new Pair<>(ResponseType.CHAT_RANK_CHANGE, new StatusDisassembler()) );
        responseMap.put("ANS_DEL_CHAT-", new Pair<>(ResponseType.CHAT_DEL_USER, new StatusDisassembler()) );
        responseMap.put("ANS_DES_CHAT-", new Pair<>(ResponseType.CHAT_DESTROY, new StatusDisassembler()) );
        responseMap.put("CMD_DES_CHAT-", new Pair<>(ResponseType.CMD_CHAT_DESTROY, new CmdChangeChatDisassembler()) );
        responseMap.put("ANS_SEND_DATA-", new Pair<>(ResponseType.MESSAGE_ANS, new MessageDisassembler()) );
        responseMap.put("CMD_WRITE_DATA-", new Pair<>(ResponseType.MESSAGE_CMD, new CmdMessageDisassembler()) );
        responseMap.put("ANS_CONN_END-", new Pair<>(ResponseType.DISCONNECT, new StatusDisassembler()) );
    }

    public void setDataQueue(ConcurrentLinkedQueue< Pair<ResponseType, Message> > queue){
        pendingMessages = queue;
    }

    public void setSecretKey(SecretKey key){
        secretKey = key;
    }

    public void setDataOutputStream(DataInputStream stream){
        inStream = stream;
    }

    @Override
    public void run(){
        initCipher();
        while (true) {
            //if client connection end
            if (isInterrupted()){
                break;
            }

            //reading message
            byte[] encrypteddMessage = null;
            try{
                long bytes = inStream.readLong();
                encrypteddMessage = inStream.readNBytes( (int) bytes);
            } catch (IOException e){
                Logger.getLogger().logError("Error while receiving message (Receiver)");
            }

            //decripting message
            byte[] decryptedMessage = null;
            try{
                decryptedMessage = cipher.doFinal(encrypteddMessage);
            } catch (BadPaddingException e) {
                Logger.getLogger().logError("BadPaddingException cipher (Receiver)");
            } catch (IllegalBlockSizeException e) {
                Logger.getLogger().logError("IllegalBlockSizeException cipher (Receiver)");
            }

            String message = new String(decryptedMessage);
            fillResponse(message);
        }
    }

    private void initCipher(){
        try {
            cipher = Cipher.getInstance("AES", "BC");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
        } catch (InvalidKeyException e) {
            Logger.getLogger().logError("InvalidKeyException cipher (Receiver)");
        } catch (NoSuchAlgorithmException e) {
            Logger.getLogger().logError("NoSuchAlgorithmException cipher (Receiver)");
        } catch (NoSuchProviderException e) {
            Logger.getLogger().logError("NoSuchProviderException cipher (Receiver)");
        } catch (NoSuchPaddingException e) {
            Logger.getLogger().logError("NoSuchPaddingException cipher (Receiver)");
        }
    }

    private void fillResponse(String message){
        int cmdEndIndex = message.indexOf('-');
        String messageCode = message.substring(0, cmdEndIndex);
        Pair<ResponseType, DisassemblerInterFace> messageType = responseMap.get(messageCode);
        if (messageType != null){
            String args = messageCode.substring(cmdEndIndex + 1);
            ResponseType response = messageType.getKey();
            //if CMD create new Message and insert
            if (response.equals(ResponseType.CMD_CHAT_DESTROY) || response.equals(ResponseType.CMD_CHAT_NAME_CHANGE)
                || response.equals(ResponseType.DEL_CMD) || response.equals(ResponseType.INV_CMD) || response.equals(ResponseType.MESSAGE_CMD)){

                Message mesg = new Message(null, null, null, null, null, null);
                messageType.getValue().dissasembly(args, mesg);
                pendingMessages.add(new Pair<ResponseType,Message>(response, mesg));
            } else { //else respond to sent request
                Iterator<Pair<ResponseType, Message> > iter = pendingMessages.iterator();
                while (iter.hasNext()) {
                    Pair<ResponseType, Message> test = iter.next();
                    if (response.equals(test.getKey()) && test.getValue().getStatus() == null){
                        messageType.getValue().dissasembly(args, test.getValue());
                        break;
                    }
                }
            }
        }
    }

}
