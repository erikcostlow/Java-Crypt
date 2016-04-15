/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools;

import com.costlowcorp.fx.utils.UIUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author ecostlow
 */
public class App extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        if(System.getProperty("os.name").contains("mac")){
            //AquaFx.style();
        }
        final FXMLLoader loader = UIUtils.load(HomeController.class);
        Parent root = loader.getRoot();
        final Scene scene = new Scene(root);
        stage.setWidth(1024);
        stage.setHeight(768);
        scene.getStylesheets().add(App.class.getClassLoader().getResource("main.css").toExternalForm());
        stage.setScene(scene);
        stage.getIcons().addAll(new Image("/Java-support-16x16.png"), new Image("/Java-support-32x32.png"));
        stage.setTitle("Erik's Tools");
        stage.show();
    }
    
    public static void main(String[] args){
        launch(args);
    }
}
