/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.files.image;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import org.controlsfx.control.PlusMinusSlider;

/**
 * FXML Controller class
 *
 * @author ecostlow
 */
public class ImageShowerController implements Initializable {

    @FXML
    private Text horizontalPixels;

    @FXML
    private Text verticalPixels;

    @FXML
    private Text filesize;

    @FXML
    private Text fileUnit;

    @FXML
    private PlusMinusSlider magnifier;

    @FXML
    private ImageView imageView;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        magnifier.setOnValueChanged((PlusMinusSlider.PlusMinusEvent event) -> {
            final double value = 1+event.getValue();
            imageView.setScaleX(value);
            imageView.setScaleY(value);
        });
    }
    
    public void populateWith(Image image){
        imageView.setImage(image);
    }

}
