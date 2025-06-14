package com.EnderLite.DataController;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

public class DataControllerTest {
    

    @Test
    public void getDataController(){

        assertNotEquals(null, DataController.getDataController());
    }

    @Test
    public void singletonPropertyCheck(){
        DataController test1 = DataController.getDataController();
        DataController test2 = DataController.getDataController();

        assertEquals(test1, test2);
    }


}
