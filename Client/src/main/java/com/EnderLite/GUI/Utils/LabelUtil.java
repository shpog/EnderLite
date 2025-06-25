package com.EnderLite.GUI.Utils;

import javafx.scene.control.Label;

//Provides utilities to change label for often used checkups
/**
 * Used for DRY and more clean label modification
 * @author Micro9261
 */
public final class LabelUtil {
    
    public static boolean setEmptyAndVisible(Label label){
        label.setText("To pole jest wymagane!");
        label.setVisible(true);
        return false;
    }


}
