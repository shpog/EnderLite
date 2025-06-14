package com.EnderLite.Connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;

public class DummyServer extends Thread{
    public int port_num;
    public boolean handshakeOnly;
    public ServerSocket serverSocket;
    public Socket socket;
    public DataInputStream socIn;
    public DataOutputStream socOut;
    public volatile String handshakeFirstMessage;
    public volatile String handshakeSecondMessage;
    public volatile String handshakeThirdMessage;
    public volatile boolean handshakeAccepted;
    public volatile SecretKey secretKey;


    public DummyServer(int port, boolean handshakeOnly){
        this.port_num = port;
        this.handshakeOnly = handshakeOnly;
    }

    public boolean openServer(){
        try {
            serverSocket = new ServerSocket(this.port_num);
            serverSocket.setSoTimeout(1000);
            socket = serverSocket.accept();
            socIn = new DataInputStream(socket.getInputStream());
            socOut = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.err.println("Error while connecting!");
            return false;
        }
         return true;
    }

    public void closeServer(){
        try{
            if (socket.isClosed() == false) {
                socIn.close();
                socOut.close();
                socket.close();
            }
            if (serverSocket.isClosed() == false){
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error while closing socket!");
        }
    }

    public void setOnlyHandshake(){
        this.handshakeOnly = true;
    }

    public byte[] readBytes(){
        byte[] buffer = null;
        try{
            long bytes = socIn.readLong();
            buffer = socIn.readNBytes( (int)bytes);
        } catch (IOException e){
            System.err.println("Error while receiving data!");
        }
        return buffer;
    }

    public void sendBytes(byte[] buffer){
        try{
            socOut.writeLong(buffer.length);
            socOut.write(buffer);
        } catch (IOException e){
            System.err.println("Error while transmitting data!");
        }
    }

    public boolean handshakeTest(){
        Security.addProvider(new BouncyCastleProvider());

        byte[] receivedBytes = readBytes();
        handshakeFirstMessage = new String(receivedBytes);
        if ( !handshakeFirstMessage.startsWith("EnderLite_Client_"))
            return false;
        //Stage 2 Get Public key, Create AES key and send encrypted message
        String key = handshakeFirstMessage.substring("EnderLite_Client_".length());
        byte[] keyBytes = java.util.Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);

        //get RSA public key
        PublicKey publicKey = null;
        try{
            KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
            publicKey = keyFactory.generatePublic(spec);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("No such algorithm! RSA second stage handshake!");
        } catch (InvalidKeySpecException e) {
            System.err.println("Invalid key spec! RSA second stage handshake");
        } catch (NoSuchProviderException e) {
            System.err.println("No such Provider! RSA second stage handshake");
        }

        //generate AES key
        try{
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            secretKey = keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("No such algorithm AES second stage handshake!");
        }

        //endcypt and send
        handshakeSecondMessage = "ACCEPT_SERVER_" + 
            java.util.Base64.getEncoder().encodeToString(secretKey.getEncoded());
        try{
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            sendBytes(cipher.doFinal(handshakeSecondMessage.getBytes()));
        } catch (Exception e){
            System.err.println("Don't care now!");
        }

        //Stage 3
        receivedBytes = readBytes();
        try{
            Cipher cipher = Cipher.getInstance("AES", "BC");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            receivedBytes = cipher.doFinal(receivedBytes);
        } catch (Exception e) {

        }

        handshakeThirdMessage = new String(receivedBytes);
        if ( !handshakeThirdMessage.startsWith("ACCEPT_CLIENT_")){
            return false;
        }

        //getAES key nad check if the same
        String AESKey = handshakeThirdMessage.substring("ACCEPT_CLIENT_".length());
        if ( !AESKey.equals(java.util.Base64.getEncoder().encodeToString(secretKey.getEncoded()))){
            return false;
        }

        return true;
    }

    @Override
    public void run(){
        while (openServer() == false){
            if (this.isInterrupted()){
                return;
            }
        }
        handshakeAccepted = false;
        if (handshakeTest()){ //code after handshake if handshakeOnly false
            handshakeAccepted = true;
            if (handshakeOnly == false){

            }
        }
        closeServer();
    }
}
   
