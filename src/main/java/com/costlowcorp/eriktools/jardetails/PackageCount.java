/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.jardetails;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author ecostlow
 */
public class PackageCount {

    private final StringProperty packageName;
    private final IntegerProperty classCount;
    private final BooleanProperty sealed;

    PackageCount(String packageName, int classCount, boolean sealed) {
        this.packageName = new SimpleStringProperty(packageName);
        this.classCount = new SimpleIntegerProperty(classCount);
        this.sealed = new SimpleBooleanProperty(sealed);
    }

    public StringProperty packageNameProperty() {
        return packageName;
    }

    public IntegerProperty classCountProperty() {
        return classCount;
    }

    public BooleanProperty sealedProperty() {
        return sealed;
    }

}
