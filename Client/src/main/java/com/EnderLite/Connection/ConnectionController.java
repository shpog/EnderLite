package com.EnderLite.Connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.EnderLite.Logger.Logger;

public final class  ConnectionController {
    private static InetAddress ipAddress;
    private static int port_num;
    private static Socket socket;
    private static DataInputStream inStream;
    private static DataOutputStream outStream;

    private ConnectionController(){
        //
    }

    public static void configureConnection(String host, int port){
        try{
            ipAddress = InetAddress.getByName(host);
            port_num = port;
        } catch (UnknownHostException unHost){
            Logger.getLogger().logError("Bad address!");
        }
    }

    public static boolean establishConnection(){
        //default parameters 
        if (ipAddress == null){
            ConnectionController.configureConnection("localhost", 12345);
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

    public static void closeConnection() throws IOException{
        try{
            inStream.close();
            outStream.close();
            socket.close();
        } catch (IOException e){
            Logger.getLogger().logError("Error while closing socket (ConnectionController)");
        }
    }

    public static DataInputStream getDataInputStream() throws IOException{
        if (socket == null){
            Logger.getLogger().logError("Socket not open! (ConnectionController)");
            throw new IOException("Socket not open!");
        }
            
        return inStream;
    }

    public static DataOutputStream getDataOutputStream() throws IOException{
        if (socket == null){
            Logger.getLogger().logError("Socket not open! (ConnectionController)");
            throw new IOException("Socket not open!");
        }

        return outStream;
    }
    
    private static boolean handshake(){
        try{
            Security.addProvider(new BouncyCastleProvider());

            //generate RSA keys
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "BC");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();

            //get keys
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            
            String firstStageMessage = "EnderLite_Client_" +
                java.util.Base64.getEncoder().encodeToString(publicKey.getEncoded());
            
            byte[] buffer = firstStageMessage.getBytes();
            try{
                outStream.writeLong(buffer.length);
                outStream.write(buffer);
            } catch (IOException e){
                Logger.getLogger().logError("Error while sending first stage handshake message (establish connection)");
            }

            try{
                long bytesToRead = inStream.readLong();
                buffer = new byte[(int)bytesToRead];
                long readBytes = inStream.read(buffer);
                if (readBytes != bytesToRead){
                    Logger.getLogger().logInfo("Bad data received for first stage handshake");
                    closeConnection();
                    return false;
                }
                
            } catch (IOException e){
                Logger.getLogger().logError("Error while receiving first stage handshake message (establish connection)");
            }

            //getting AES key
            

        } catch (NoSuchProviderException e){
            Logger.getLogger().logError("No such provider exception (establish connection)");
        } catch (NoSuchAlgorithmException e){
            Logger.getLogger().logError("No such algorithm exception (establish connection)");
        }
        return true;
    }

}
