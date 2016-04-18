/*
 * Copyright Erik Costlow.
 * Not authorized for use or view by others.
 */
package com.costlowcorp.eriktools;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;

/**
 * FXML Controller class
 *
 * @author Erik Costlow
 */
public class ErrorMessageController implements Initializable {

    @FXML
    private Text title;
    
    @FXML
    private Text message;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    public void setTitle(String title){
        this.title.setText(title);
    }
    
    public void setText(String text){
        this.message.setText(text);
    }
}
