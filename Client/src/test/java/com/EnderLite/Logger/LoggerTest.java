package com.EnderLite.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

public class LoggerTest {
    
    @Test
    public void getLogger(){
        
        assertNotEquals(null, Logger.getLogger());
    }

    @Test
    public void singletonPropertyCheck(){
        Logger test1 = Logger.getLogger();
        Logger test2 = Logger.getLogger();

        assertEquals(test1, test2);
    }
}
