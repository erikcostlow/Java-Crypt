/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools;

import com.costlowcorp.fx.utils.UIUtils;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;

/**
 * FXML Controller class
 *
 * @author ecostlow
 */
public class HomeController implements Initializable {

    @FXML
    private TabPane tabs;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public void scanSystem(ActionEvent event) {
        final FXMLLoader loader = UIUtils.load(ListKnownKeyStoresController.class);
        final Pane root = loader.getRoot();
        
        final Tab newTab = new Tab("System", root);
        tabs.getTabs().add(newTab);
    }

    public void openFile(ActionEvent event) {

    }

}
