package com.EnderLite.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Logger {
    private static volatile Logger instance;
    private String infoType = "[LOG] ";
    private String errorType = "[Error] ";
    private FileWriter fileOut;
    private PrintWriter cmdOut;
    private String fileName = "log.txt";

    private Logger(){
        try{
            fileOut = new FileWriter(fileName);
            cmdOut = new PrintWriter(System.out, true);
        } catch (IOException e){
            System.err.println("Error creating loggerFile!\nOnly console messages!");
        }
    }

    public static Logger getLogger(){
        if (instance != null){
            return instance;
        }

        synchronized(Logger.class){
            if (instance == null){
                instance = new Logger();
            }
            return instance;
        }
    }

    synchronized public void logInfo(String info){
        cmdOut.println(infoType + info);
        try{
            fileOut.write(infoType + info);
        } catch (IOException e){
            System.err.println("Error while writing to " + fileName);
        }
    }

    synchronized public void logError(String error){
        cmdOut.println(errorType + error);
        try{
            fileOut.write(errorType + error);
        } catch (IOException e){
            System.err.println("Error while writing to " + fileName);
        }
    }
}
