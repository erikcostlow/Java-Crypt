/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools;

import com.costlowcorp.eriktools.code.CodeEditor;
import com.costlowcorp.eriktools.wardetails.ScanningScreenController;
import com.costlowcorp.fx.utils.UIUtils;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(r -> {
        final Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    });
    
    private static Application SELF;
    public static Application getSELF(){
        return SELF;
    }

    @Override
    public void start(Stage stage) throws Exception {
        if (System.getProperty("os.name").contains("mac")) {
            //AquaFx.style();
        }
        SELF = this;
        //final FXMLLoader loader = UIUtils.load(HomeController.class);
        final FXMLLoader loader = UIUtils.load(ScanningScreenController.class);
        Parent root = loader.getRoot();
        final Scene scene = new Scene(root);
        stage.setWidth(1024);
        stage.setHeight(768);
        scene.getStylesheets().add(App.class.getClassLoader().getResource("main.css").toExternalForm());
        scene.getStylesheets().add(CodeEditor.class.getResource("java-keywords.css").toExternalForm());
        stage.setScene(scene);
        stage.getIcons().addAll(new Image("/Java-support-16x16.png"), new Image("/Java-support-32x32.png"));
        stage.setTitle("Erik's Tools");
        stage.show();
    }

    @Override
    public void stop() {
        EXECUTOR.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static final ExecutorService getEXECUTOR() {
        return EXECUTOR;
    }
}
