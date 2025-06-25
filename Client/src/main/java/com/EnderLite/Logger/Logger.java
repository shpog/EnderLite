package com.EnderLite.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * Logger for storing error and info status
 * Writes info to file 'log.txt' and outputs in console
 * @author Micro9261
 */
public class Logger {
    private static volatile Logger instance;
    private final String infoType = "[LOG] ";
    private final String errorType = "[Error] ";
    private FileWriter fileOut;
    private PrintWriter cmdOut;
    private final String fileName = "log.txt";

    private Logger(){
        cmdOut = new PrintWriter(System.err, true);
        try{
            fileOut = new FileWriter(fileName, false);
            fileOut.close();
        } catch (IOException e) {
            cmdOut.println("Error while overwriting " + fileName);
        }

    }

    /**
     * Used to get Logger instance (singleton)
     * @return Logger instance
     */
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

    /**
     * Used to log informations that not interrupt program runtime
     * @param info message to write
     */
    synchronized public void logInfo(String info){
        String mesg = infoType + info;

        cmdOut.println(mesg);
        try{
            fileOut = new FileWriter(fileName, true);
            fileOut.write("\n" + mesg);
            fileOut.close();
        } catch (IOException e){
            System.err.println("Error while writing to " + fileName);
        }
    }

    /**
     * Used to log error that interrupt program runtime
     * @param error
     */
    synchronized public void logError(String error){
        String mesg = errorType + error;
        cmdOut.println(mesg);
        try{
            fileOut = new FileWriter(fileName, true);
            fileOut.write("\n" + mesg);
            fileOut.close();
        } catch (IOException e){
            System.err.println("Error while writing to " + fileName);
        }
    }

}
