package com.EnderLite.GUI.Utils;

import javafx.scene.control.TextField;

public final class TextFieldUtil {
    
    public static boolean checkIfEmpty(TextField textField){
        return textField.getText().isEmpty();
    }
}
