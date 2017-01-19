/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools;

import com.costlowcorp.eriktools.code.CodeEditor;
import com.costlowcorp.fx.utils.UIUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.controlsfx.control.MasterDetailPane;
import org.controlsfx.control.TaskProgressView;

/**
 *
 * @author ecostlow
 */
public class App extends Application {

    private static final ErikThreadExecutor EXECUTOR = ErikThreadExecutor.INSTANCE;

    private static Application SELF;

    public static Application getSELF() {
        return SELF;
    }

    private static final TaskProgressView taskView = makeDetailPane();
    private static final MasterDetailPane masterDetail = new MasterDetailPane(Side.BOTTOM, new Label("Not set yet"), taskView, true);

    @Override
    public void start(Stage stage) throws Exception {
        if (System.getProperty("os.name").contains("mac")) {
            //AquaFx.style();
        }
        SELF = this;
        final FXMLLoader loader = UIUtils.load(HomeController.class);
        //final FXMLLoader loader = UIUtils.load(ScanningScreenController.class);
        Parent root = loader.getRoot();
        masterDetail.setMasterNode(root);
        masterDetail.setShowDetailNode(false);
        final Scene scene = new Scene(masterDetail);
        EXECUTOR.setWhenNoJobsLeft(() -> Platform.runLater(() -> masterDetail.setShowDetailNode(false)));
        stage.setWidth(1024);
        stage.setHeight(768);
        scene.getStylesheets().add(App.class.getClassLoader().getResource("main.css").toExternalForm());
        scene.getStylesheets().add(CodeEditor.class.getResource("java-keywords.css").toExternalForm());
        stage.setScene(scene);
        stage.getIcons().addAll(new Image("/Java-support-16x16.png"), new Image("/Java-support-32x32.png"));
        stage.setTitle("Erik's Tools");
        stage.show();
    }

    private static TaskProgressView makeDetailPane() {
        final TaskProgressView view = new TaskProgressView();

        return view;
    }

    @Override
    public void stop() {
        EXECUTOR.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static final void submitVisible(Task<?> task) {
        Platform.runLater(() -> {
            if(!masterDetail.isShowDetailNode()){
                masterDetail.setShowDetailNode(true);
            }
            taskView.getTasks().add(task);
            EXECUTOR.submit(task);
        });
    }
    
    public static final void submitInvisible(Task task) {
        EXECUTOR.submit(task);
    }
}
