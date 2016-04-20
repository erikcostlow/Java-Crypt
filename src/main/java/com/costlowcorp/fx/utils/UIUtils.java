/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.fx.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author ecostlow
 */
public class UIUtils {

    private UIUtils() {
    }

    public static final String resource(Class<? extends Initializable> clazz) {
        final String className = clazz.getSimpleName();
        return className.replace('.', '/').replace("Controller", "") + ".fxml";
    }

    /**
     *
     * @param clazz
     * @return An FXMLLoader loaded with the right FXML
     */
    public static FXMLLoader load(Class<? extends Initializable> clazz) {
        final String resource = resource(clazz);
        try {
            final URL url = clazz.getResource(resource);
            if (url == null) {
                throw new FileNotFoundException("Unable to load " + url);
            }
            final FXMLLoader loader = new FXMLLoader(url);
            loader.load();
            return loader;
        } catch (IOException ex) {
            Logger.getLogger(UIUtils.class.getName()).log(Level.SEVERE, String.format("Unable to load %s because %s", resource, ex.getMessage()), ex);
        }
        return null;
    }
    
    private static final Double ZERO = Double.valueOf(0);

    public static void setAnchors(Node node, int top, int right, int bottom, int left) {
        setAnchors(node, Double.valueOf(top), Double.valueOf(right), Double.valueOf(bottom), Double.valueOf(left));
    }
    
    public static void setAnchors(Node node, Double top, Double right, Double bottom, Double left) {
        AnchorPane.setTopAnchor(node, top);
        AnchorPane.setRightAnchor(node, right);
        AnchorPane.setBottomAnchor(node, bottom);
        AnchorPane.setLeftAnchor(node, left);
        if(ZERO.compareTo(top)==0 && ZERO.compareTo(bottom)==0){
            VBox.setVgrow(node, Priority.ALWAYS);
        }
        if(ZERO.compareTo(left)==0 && ZERO.compareTo(right)==0){
            HBox.setHgrow(node, Priority.ALWAYS);
        }
    }
}
