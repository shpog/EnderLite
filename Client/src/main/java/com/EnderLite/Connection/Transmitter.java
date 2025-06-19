package com.EnderLite.Connection;

import java.io.DataOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.concurrent.BlockingQueue;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.EnderLite.Logger.Logger;

public class Transmitter extends Thread {
    private SecretKey secretKey;
    private BlockingQueue<String> dataQueue;
    private DataOutputStream outStream;
    private Cipher cipher;

    {
        Security.addProvider(new BouncyCastleProvider());
    }

    public void setDataQueue(BlockingQueue<String> queue){
        dataQueue = queue;
    }

    public void setSecretKey(SecretKey key){
        secretKey = key;
    }

    public void setDataOutputStream(DataOutputStream stream){
        outStream = stream;
    }

    @Override
    public void run(){
        initCipher();
        while (true){
            //if client connection end
            if (isInterrupted()){
                break;
            }
            //transmitting messages
            String toTransfer = null;
            try{
                toTransfer = dataQueue.take();
            } catch (InterruptedException e) {
                Logger.getLogger().logInfo("Transmitter interrupted. Exiting(Transmitter)");
                break;
            }

            //encrypt
            byte[] encryptedMessage = null;
            try {
                encryptedMessage = cipher.doFinal(toTransfer.getBytes());
            } catch (IllegalBlockSizeException e) {
                Logger.getLogger().logError("IllegalBlockSizeException cipher (Transmitter)");
            } catch (BadPaddingException e) {
                Logger.getLogger().logError("BadPaddingException cipher (Transmitter)");
            }

            //send encryptedMessage
            try {
                outStream.writeLong(encryptedMessage.length);
                outStream.write(encryptedMessage);
            } catch (IOException e) {
                Logger.getLogger().logError("Error while sending message (Transmitter)");
                break;
            }
        }
        Logger.getLogger().logInfo("Transmitter shutdown!");
    }

    private void initCipher(){
        try {
            cipher = Cipher.getInstance("AES", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        } catch (InvalidKeyException e) {
            Logger.getLogger().logError("InvalidKeyException cipher (Transmitter)");
        } catch (NoSuchAlgorithmException e) {
            Logger.getLogger().logError("NoSuchAlgorithmException cipher (Transmitter)");
        } catch (NoSuchProviderException e) {
            Logger.getLogger().logError("NoSuchProviderException cipher (Transmitter)");
        } catch (NoSuchPaddingException e) {
            Logger.getLogger().logError("NoSuchPaddingException cipher (Transmitter)");
        }
    }
    
}
