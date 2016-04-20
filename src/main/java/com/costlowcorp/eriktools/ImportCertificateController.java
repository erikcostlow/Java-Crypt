/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools;

import com.costlowcorp.fx.utils.UIUtils;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Pane;

/**
 * FXML Controller class
 *
 * @author ecostlow
 */
public class ImportCertificateController implements Initializable {

    @FXML
    private TextField filename;
    
    @FXML
    private TreeView<Certificate> certChain;
    
    @FXML
    private TextField alias;
    
    @FXML
    private Pane certDetails;
    
    private Node showCertificateNode;
    private ShowCertificateController showCertificateController;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        final FXMLLoader showLoader = UIUtils.load(ShowCertificateController.class);
        showCertificateNode = showLoader.getRoot();
        showCertificateController = showLoader.getController();
        certDetails.getChildren().setAll(showCertificateNode);
        // TODO
        certChain.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue!=null && newValue.getValue()!=null){
                showCertificateController.initilize(newValue.getValue(), alias.getText());
            }
        });
    }    
    
    public void chooseFile(ActionEvent event){
        
    }
    
    public void importCert(ActionEvent event){
        
    }
}
