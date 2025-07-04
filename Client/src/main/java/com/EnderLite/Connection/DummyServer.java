package com.EnderLite.Connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.EnderLite.Logger.Logger;

/**
 * Dummy server for testing purposes
 * @author Micro9261
 */
public class DummyServer extends Thread{
    /**
     * Port number used by server
     */
    public int port_num;
    /**
     * Indicates if disconnect after handshake
     */
    public boolean handshakeOnly;
    /**
     * Stores server socket
     */
    public ServerSocket serverSocket;
    /**
     * Stores open socket
     */
    public Socket socket;
    /**
     * DataInputStream from open socket
     */
    public DataInputStream socIn;
    /**
     * DataOutputStream from open socket
     */
    public DataOutputStream socOut;
    /**
     * Stores first sent message while handshake in progress
     */
    public volatile String handshakeFirstMessage;
    /**
     * Stores second received message while handshake in progress
     */
    public volatile String handshakeSecondMessage;
    /**
     * Stores third sent message while handshake in progress
     */
    public volatile String handshakeThirdMessage;
    /**
     * Indicates if connection established and AES keys exchanged
     */
    public volatile boolean handshakeAccepted;
    /**
     * AES key for decryption and encryption
     */
    public volatile SecretKey secretKey;
    /**
     * Cipher in decrypt mode
     */
    public Cipher cipherAfter;
    /**
     * Cipher in encrypt mode
     */
    public Cipher cipherAfterEncrypt;
    /**
     * Cmd mode indicator
     */
    public boolean cmdMode;
    /**
     * sets rejection version response
     */
    public boolean good = false;
    /**
     * Stores cmd to send in cmd Mode
     */
    public String cmd;
    /**
     * used to send cmd to client
     */
    public volatile boolean fire;


    /**
     * Constructor 
     * @param port set port for server access
     * @param handshakeOnly if true connection ends on handshake
     */
    public DummyServer(int port, boolean handshakeOnly){
        this.port_num = port;
        this.handshakeOnly = handshakeOnly;
    }

    /**
     * select different rejection status to true
     */
    public void setAccepted(){
        good = true;
    }

    /**
     * select different rejection status to false
     */
    public void setDenied(){
        good = false;
    }

    /**
     * Sets server to cmd send mode
     * @param mode if true server is set in cmd set mode
     */
    public void setCmdMode(boolean mode){
        cmdMode = mode;
    }

    /**
     * Used to send cmd to client
     * @param cmd cmd name
     */
    public void sendCmd(String cmd){
        this.cmd = new String(cmd);
    }

    /**
     * fires cmd set with sendCmd method
     */
    public void fireCmd(){
        fire = true;
    }

    /**
     * Used to open server after config
     * @return true if connection established, false otherwise
     */
    public boolean openServer(){
        try {
            serverSocket = new ServerSocket(this.port_num);
            serverSocket.setSoTimeout(1000);
            socket = serverSocket.accept();
            socIn = new DataInputStream(socket.getInputStream());
            socOut = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.err.println("Error while connecting! (openServer)");
            return false;
        }
         return true;
    }

    /**
     * Closes server and socket streams
     */
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

    /**
     * Used to set handshakeOnly mode to true
     */
    public void setOnlyHandshake(){
        this.handshakeOnly = true;
    }

    /**
     * Reads bytes
     * @return read bytes
     */
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

    /**
     * Sends bytes 
     * @param buffer bytes to be send
     */
    public void sendBytes(byte[] buffer){
        try{
            socOut.writeLong(buffer.length);
            socOut.write(buffer);
        } catch (IOException e){
            System.err.println("Error while transmitting data!");
        }
    }

    /**
     * Checks if connection established
     * @return true if connection established
     */
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

    /**
     * Starts dummy server
     */
    @Override
    public void run(){
        while (openServer() == false){
            return;
        }
        try{
            socket.setSoTimeout(20000);
        } catch (SocketException e){
            Logger.getLogger().logError("Socket exception (server)");
        }
        handshakeAccepted = false;
        if (handshakeTest()){ //code after handshake if handshakeOnly false
            handshakeAccepted = true;
            if (handshakeOnly == false){
                initCipher();
                if (cmdMode == false){ //normal work mode
                    while (true){
                    if (isInterrupted()){
                        break;
                    }
                    String message = readAndDecipher();
                    if (message == null)
                        break;
                    System.out.println("Message Server: " + message);
                    String cmd = message.substring(0, message.indexOf("-") + 1);
                    String args = message.substring(message.indexOf("-") + 1);
                    String response = prepareResponse(cmd, args);
                    if (response != null){
                        encryptAndSend(response);
                    }
                    }
                } else { //CMD send test mode while
                    System.out.println("In CMD mode!");
                    while(true){
                        if (isInterrupted()){
                            break;
                        }
                        String command = null;
                        while ( !fire && !isInterrupted());
                        if (isInterrupted())
                            break;
                        fire = false;
                        command = cmd;
                        System.out.println("Message to send: " + command);
                        if (command.equals("STOP")){
                            break;
                        }
                        encryptAndSend(command);
                    }
                    System.out.println("CMD end");
                }
                
            }
        }
        closeServer();
        System.out.println("Server shutdown!");
    }

    /**
     * Read bytes from socket input and decryptes messsage
     * @return message in decrypted String format
     */
    public String readAndDecipher(){
        byte[] encrypteddMessage = null;
            
        try{
            long bytes = socIn.readLong();
            encrypteddMessage = socIn.readNBytes( (int) bytes);
        } catch (IOException e){
            Logger.getLogger().logError("Error while receiving message (ReceiverServer)");
            return null;
        }

        //decripting message
        byte[] decryptedMessage = null;
        try{
            decryptedMessage = cipherAfter.doFinal(encrypteddMessage);
        } catch (BadPaddingException e) {
            Logger.getLogger().logError("BadPaddingException cipher (ReceiverServer)");
        } catch (IllegalBlockSizeException e) {
            Logger.getLogger().logError("IllegalBlockSizeException cipher (ReceiverServer)");
        }

        return new String(decryptedMessage);
    }

    /**
     * Inits ciphers for server
     */
    private void initCipher(){
        try {
            cipherAfter = Cipher.getInstance("AES", "BC");
            cipherAfter.init(Cipher.DECRYPT_MODE, secretKey);
            cipherAfterEncrypt = Cipher.getInstance("AES", "BC");
            cipherAfterEncrypt.init(Cipher.ENCRYPT_MODE, secretKey);
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

    /**
     * Encrypts String and send to server
     * @param message cmd that will be send to server
     */
    public void encryptAndSend(String message){

        //encrypt
        byte[] encryptedMessage = null;
        try {
            encryptedMessage = cipherAfterEncrypt.doFinal(message.getBytes());
        } catch (IllegalBlockSizeException e) {
            Logger.getLogger().logError("IllegalBlockSizeException cipher (TransmitterSever)");
        } catch (BadPaddingException e) {
            Logger.getLogger().logError("BadPaddingException cipher (TransmitterServer)");
        }
        try {
            socOut.writeLong(encryptedMessage.length);
            socOut.write(encryptedMessage);
        } catch (IOException e) {
            Logger.getLogger().logError("Error while sending message (TransmitterServer)");
        }
    }

    /**
     * Test cases for client
     * @param cmd message designator
     * @param args message arguments
     * @return
     */
    public String prepareResponse(String cmd, String args){
        String[] argsParts = args.split("-");
        String result = null;
        // System.out.println("Search: " + cmd);
        switch (cmd) {
            case "AUTH_LOG&PASSW-":
                if (argsParts[0].equals("test") && argsParts[1].equals("test")){
                    result = "AUTH_RESP-" + argsParts[0] + "-" + "test@gmail.com";
                } else {
                    result = "AUTH_RESP-DENIED";
                }
                break;
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            case "AUTH_EMAIL&PASSW-":
                if (argsParts[0].equals("test@gmail.com") && argsParts[1].equals("test")){
                    result = "AUTH_RESP-" + "test" + "-" + argsParts[0];
                } else {
                    result = "AUTH_RESP-DENIED";
                }
                break;
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            case "AUTH_STATUS-":
                if (good){
                    result = "AUTH_RESP-test-test@gmail.com";
                } else {
                    result = "AUTH_RESP-DENIED";
                }
                break;
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            case "REQ_ADD_USER-":
                if (argsParts[0].equals("test") && argsParts[1].equals("test@gmail.com")){
                    result = "ANS_ADD_USER-" + argsParts[0] + "-" + argsParts[1];
                } else if (good){
                    result = "ANS_ADD_USER-DENIED-LOGIN";
                } else {
                    result = "ANS_ADD_USER-DENIED-EMAIL";
                }
                break;
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            case "REQ_USER_DATA-":
                if (argsParts[0].equals("test")){
                    result = "ANS_USER_DATA-L=" + argsParts[0] + "-E=test@gmail.com-F=friend1,friend2,friend3-C=chat1,chat2-END";
                } else {
                    result = "ANS_USER_DATA-DENIED-ERROR";
                }
                break;
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            case "REQ_INV_LOG-":
                if (argsParts[0].equals("test") && argsParts[1].equals("test2")){
                    result = "ANS_INV_LOG-ACCEPT-" + argsParts[0];
                } else {
                    result = "ANS_INV_LOG-DENIED-" + argsParts[0];
                }
                break;
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            case "REQ_INV_EMAIL-":
                if (argsParts[0].equals("test@test.gmail.com") && argsParts[1].equals("test2")){
                    result = "ANS_INV_LOG-ACCEPT-test";
                } else {
                    result = "ANS_INV_LOG-DENIED-test";
                }
                break;
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            case "REQ_INV_STATUS-": //no response for that req
                break;
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            case "REQ_DEL_LOG-":
                if (argsParts[0].equals("test") && argsParts[1].equals("test2")){
                    result = "ANS_DEL_LOG-ACCEPT-" + argsParts[0];
                } else {
                    result = "ANS_DEL_LOG-DENIED-" + argsParts[0];
                }
                break;
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            case "REQ_CRT_CHAT-":
                if (argsParts[0].equals("test")){
                    result = "ANS_CRT_CHAT-ACCEPT";
                } else if (good){
                    result = "ANS_CRT_CHAT-DENIED-USED";
                } else {
                    result = "ANS_CRT_CHAT-DENIED-ERROR";
                }
                break;
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            case "REQ_ADD_CHAT-":
                if (argsParts[1].equals("test")) {
                    result = "ANS_ADD_CHAT-ACCEPT";
                } else if (good){
                    result = "ANS_ADD_CHAT-DENIED-NOACCESS";
                } else {
                    result = "ANS_ADD_CHAT-DENIED-ERROR";
                }
                break;
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            case "REQ_CHAN_CHAT_NAME-":
                if (argsParts[0].equals("test")){
                    result = "ANS_CHAN_CHAT_NAME-ACCEPT";
                } else if (good){
                    result = "ANS_CHAN_CHAT_NAME-DENIED-NOACCESS";
                } else {
                    result = "ANS_CHAN_CHAT_NAME-DENIED-ERROR";
                }
                break;
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            case "REQ_CHAN_CHAT_RANK-":
                if (argsParts[1].equals("test")){
                    result = "ANS_CHAN_CHAT_RANK-ACCEPT";
                } else if (good) {
                    result = "ANS_CHAN_CHAT_RANK-DENIED-NOACCESS";
                } else {
                    result = "ANS_CHAN_CHAT_RANK-DENIED-ERROR";
                }
                break;
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            case "REQ_DEL_CHAT-":
                if (argsParts[1].equals("test")){
                    result = "ANS_DEL_CHAT-ACCEPT";
                } else if (good){
                    result = "ANS_DEL_CHAT-DENIED-NOACCESS";
                } else {
                    result = "ANS_DEL_CHAT-DENIED-ERROR";
                }
                break;
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            case "REQ_DES_CHAT-":
                if (argsParts[0].equals("test")){
                    result = "ANS_DES_CHAT-ACCEPT";
                } else if (good) {
                    result = "ANS_DES_CHAT-DENIED-NOACCESS";
                } else {
                    result = "ANS_DES_CHAT-DENIED-ERROR";
                }
                break;
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            case "SEND_DATA-":
                if (argsParts[0].equals("test")){
                    result = "ANS_SEND_DATA-ACCEPT-test:test"; 
                } else if (good){
                    result = "ANS_SEND_DATA-DENIED-NAME";
                } else {
                    result = "ANS_SEND_DATA-DENIED-ERROR";
                }
                break;
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            case "REQ_CONN_END-":
                result = "ANS_CONN_END-";
                break;
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            default:
                break;
        }
        System.out.println("Server response: " + 
        (result == null ? "null" : result));
        return result;
    }

}
   
