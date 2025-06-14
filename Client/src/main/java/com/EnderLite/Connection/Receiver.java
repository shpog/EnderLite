package com.EnderLite.Connection;

import com.EnderLite.Logger.Logger;

public class Receiver implements Runnable {

    @Override
    public void run(){
        
        Logger.getLogger().logInfo("Receiver started");
    }
    
}
