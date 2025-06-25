package com.EnderLite.GUI.Utils;

import javafx.scene.control.TextField;

/**
 * Used for DRY and more clean textField checking
 * @author Micro9261
 */
public final class TextFieldUtil {
    
    public static boolean checkIfEmpty(TextField textField){
        return textField.getText().isEmpty();
    }
}
