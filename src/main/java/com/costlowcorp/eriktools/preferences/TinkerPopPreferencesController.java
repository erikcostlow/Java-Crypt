/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.preferences;

import com.costlowcorp.eriktools.App;
import com.costlowcorp.eriktools.GraphMaker;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author erik
 */
public class TinkerPopPreferencesController implements Initializable {

    @FXML
    private ChoiceBox graphLocation;

    @FXML
    private VBox tinkerpopSpecific;

    private Runnable onSave;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        final ObservableList list = FXCollections.observableList(Arrays.stream(GraphMaker.values()).map(GraphMaker::inEnglish).collect(Collectors.toList()));
        graphLocation.setItems(list);
    }
    
    public void setOnSave(Runnable runnable){
        this.onSave=runnable;
    }

    public void save(ActionEvent event) {
        final String s = String.valueOf(graphLocation.valueProperty().getValue());
        final GraphMaker maker = GraphMaker.fromText(s);
        App.setGraphMaker(maker);
        if (onSave != null) {
            onSave.run();
        }
    }
}
