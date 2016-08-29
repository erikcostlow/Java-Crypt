/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.jardetails;

import com.costlowcorp.eriktools.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;

/**
 *
 * @author ecostlow
 */
public class JarEntryTreeViewer {
    final StringProperty actualFile;
    final StringProperty showName;
    
    public JarEntryTreeViewer(String actualFile){
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
