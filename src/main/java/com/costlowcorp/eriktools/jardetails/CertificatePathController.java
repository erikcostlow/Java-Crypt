/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.jardetails;

import com.costlowcorp.eriktools.ShowCertificateController;
import com.costlowcorp.fx.utils.UIUtils;
import java.net.URL;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

/**
 * FXML Controller class
 *
 * @author ecostlow
 */
public class CertificatePathController implements Initializable {

    @FXML
    private TreeView certPathTree;

    @FXML
    private ScrollPane detailsPane;

    private ShowCertificateController showCertificateController;

    private Node showCertificateRoot;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        final FXMLLoader loader = UIUtils.load(ShowCertificateController.class);
        showCertificateController = loader.getController();
        showCertificateRoot = loader.getRoot();
        certPathTree.setShowRoot(false);
        
        detailsPane.setContent(new Label("Choose"));
        certPathTree.setCellFactory(new Callback<TreeView<Certificate>,TreeCell<Certificate>>(){
            @Override
            public TreeCell<Certificate> call(TreeView<Certificate> p) {
                return new TreeCellCertificate();
            }
        });
        certPathTree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<Certificate>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<Certificate>> observable, TreeItem<Certificate> oldValue, TreeItem<Certificate> newValue) {
                if(detailsPane.getContent()!=showCertificateRoot){
                    detailsPane.setContent(showCertificateRoot);
                }
                showCertificateController.initilize(newValue.getValue(), "N/A");
            }
            
        });
    }
    
    public void initialize(CertPath path){
        final TreeItem<Certificate> root = new TreeItem<>();
        certPathTree.setRoot(root);
        TreeItem current=null;
        for(Certificate cert : path.getCertificates()){
            final TreeItem<Certificate> item = convert(cert);
            if(current!=null){
                item.getChildren().add(current);
            }
            current=item;
            current.setExpanded(true);
        }
        certPathTree.getRoot().getChildren().add(current);
    }
    
    private TreeItem<Certificate> convert(Certificate cert){
        final TreeItem<Certificate> item = new TreeItem<>(cert);
        return item;
    }

}
