package com.EnderLite.Connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.EnderLite.Logger.Logger;

public final class  ConnectionController {
    private InetAddress ipAddress;
    private int port_num;
    private Socket socket;
    private DataInputStream inStream;
    private DataOutputStream outStream;
    private SecretKey secretKey;

    public ConnectionController(String host, int port){
        this.configureConnection(host, port);
    }

    public void configureConnection(String host, int port){
        try{
            ipAddress = InetAddress.getByName(host);
            port_num = port;
        } catch (UnknownHostException unHost){
            Logger.getLogger().logError("Bad address!");
        }
    }

    public boolean establishConnection(){
        //default parameters 
        if (ipAddress == null){
            configureConnection("localhost", 12345);
        }

        try{
            Logger.getLogger().logInfo("Connecting to server...");
            socket = new Socket(ipAddress, port_num);
            inStream = new DataInputStream(socket.getInputStream());
            outStream = new DataOutputStream(socket.getOutputStream());
            Logger.getLogger().logInfo("Connected to server. Starting handshake...");
        } catch (IOException e){
            Logger.getLogger().logError("Server not found! (ConnectionController)");
        }

        //check if server responds
        handshake();

        return true;
    }

    public SecretKey getAESKey(){
        byte[] keyBytes = secretKey.getEncoded();
        String algorithm = secretKey.getAlgorithm();

        return new SecretKeySpec(keyBytes, algorithm);
    }

    public void closeStreams() throws IOException{
        try{
            inStream.close();
            outStream.close();
            socket.close();
        } catch (IOException e){
            Logger.getLogger().logError("Error while closing socket (ConnectionController)");
        }
    }

    public DataInputStream getDataInputStream() throws IOException{
        if (socket == null){
            Logger.getLogger().logError("Socket not open! (ConnectionController)");
            throw new IOException("Socket not open!");
        }
            
        return inStream;
    }

    public DataOutputStream getDataOutputStream() throws IOException{
        if (socket == null){
            Logger.getLogger().logError("Socket not open! (ConnectionController)");
            throw new IOException("Socket not open!");
        }

        return outStream;
    }
    
    private boolean handshake(){
        try{
            Security.addProvider(new BouncyCastleProvider());

            //generate RSA keys
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "BC");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();

            //get keys
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();
            Logger.getLogger().logInfo("RSA Keys generated");
            
            String firstStageMessage = "EnderLite_Client_" +
                java.util.Base64.getEncoder().encodeToString(publicKey.getEncoded());
            
            byte[] buffer = firstStageMessage.getBytes();
            Logger.getLogger().logInfo("Message to byte[] buffer written!");
            try{
                outStream.writeLong(buffer.length);
                outStream.write(buffer);
                Logger.getLogger().logInfo("First stage message send: " + firstStageMessage);
            } catch (IOException e){
                Logger.getLogger().logError("Error while sending first stage handshake message (establish connection)");
            }

            try{
                long bytesToRead = inStream.readLong();
                Logger.getLogger().logInfo("Reading second stage Message!");
                buffer = inStream.readNBytes( (int) bytesToRead);
            } catch (IOException e){
                Logger.getLogger().logError("Error while receiving first stage handshake message (establish connection)");
            }

            //decipher message from 2 stage
            try{
                Logger.getLogger().logInfo("Decrypting message from second stage");
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
                cipher.init(Cipher.DECRYPT_MODE, privateKey);
                buffer = cipher.doFinal(buffer);
                Logger.getLogger().logInfo("Message decrypted");
            } catch (InvalidKeyException e){
                Logger.getLogger().logError("InvalidKeyException cipher (ConnectionController)");
            } catch (NoSuchPaddingException e){
                Logger.getLogger().logError("NoSuchPaddingException cipher (ConnectionController)");
            } catch (IllegalBlockSizeException e) {
                Logger.getLogger().logError("IllegalBlockSizeException cipher (ConnectionController)");
            } catch (BadPaddingException e) {
                Logger.getLogger().logError("BadPaddingException cipher (ConnectionController)");
            }

            String secondStageMessage = new String(buffer);
            try {
                if ( !secondStageMessage.startsWith("ACCEPT_SERVER_") ) {
                    Logger.getLogger().logInfo("Bad message_received: " + secondStageMessage.substring("ACCEPT_SERVER_".length()));
                closeStreams();
                return false;
                }
            } catch (IOException e){
                Logger.getLogger().logError("Error while reveiving second stage handshake message (establish connection)");
            }
            //get AES key
            Logger.getLogger().logInfo("Getting AES key");
            
            String AESKey = secondStageMessage.substring("ACCEPT_SERVER_".length());
            byte[] keyBytes = java.util.Base64.getDecoder().decode(AESKey);
            secretKey = new SecretKeySpec(keyBytes, "AES");
            
            String thirdStageMessage = "ACCEPT_CLIENT_" + AESKey;
            try {
                Cipher cipher = Cipher.getInstance("AES", "BC");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                buffer = cipher.doFinal(thirdStageMessage.getBytes());
            } catch (InvalidKeyException e){
                Logger.getLogger().logError("InvalidKeyException cipher (ConnectionController)");
            } catch (NoSuchPaddingException e){
                Logger.getLogger().logError("NoSuchPaddingException cipher (ConnectionController)");
            } catch (IllegalBlockSizeException e) {
                Logger.getLogger().logError("IllegalBlockSizeException cipher (ConnectionController)");
            } catch (BadPaddingException e) {
                Logger.getLogger().logError("BadPaddingException cipher (ConnectionController)");
            }

            try{
                Logger.getLogger().logInfo("Sending 3 stage message...");
                outStream.writeLong(buffer.length);
                outStream.write(buffer);
                Logger.getLogger().logInfo("Message sent: " + thirdStageMessage);
            } catch (IOException e){
                Logger.getLogger().logError("Error while sending third stage handshake message (establish connection)");
            }
            

        } catch (NoSuchProviderException e){
            Logger.getLogger().logError("No such provider exception (establish connection)");
        } catch (NoSuchAlgorithmException e){
            Logger.getLogger().logError("No such algorithm exception (establish connection)");
        }
        return true;
    }


}
