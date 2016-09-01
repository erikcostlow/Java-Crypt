/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.wardetails;

import com.costlowcorp.eriktools.ErikUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;

/**
 *
 * @author ecostlow
 */
public class ArchiveEntryTreeViewer {
    final StringProperty actualFile;
    final StringProperty showName;
    
    public ArchiveEntryTreeViewer(String actualFile){
        this.actualFile = new SimpleStringProperty(actualFile);
        this.actualFile.addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            changeShowName();
        });
        showName = new SimpleStringProperty();
        
        changeShowName();
    }
    
    private void changeShowName(){
        showName.set(ErikUtils.justFilename(actualFile.get()));
    }
    
    public StringProperty actualFileProperty(){
        return actualFile;
    }
    
    public StringProperty showNameProperty(){
        return showName;
    }
}
