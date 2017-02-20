/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.wardetails;

import com.costlowcorp.eriktools.App;
import com.costlowcorp.eriktools.files.FileTypeTask;
import com.costlowcorp.fx.utils.UIUtils;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import org.controlsfx.control.Notifications;

/**
 * FXML Controller class
 *
 * @author ecostlow
 */
public class WarNavigationController implements Initializable {

    @FXML
    private TreeView<ArchiveEntryTreeViewer> fileNavigation;

    @FXML
    private TextField fileSearch;

    @FXML
    private AnchorPane detailsPane;

    private Path path;

    private WeakReference<Node> detailsNode;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fileNavigation.setCellFactory((TreeView<ArchiveEntryTreeViewer> param) -> new ArchiveEntryTextFieldTreeCellImpl());
        fileNavigation.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends TreeItem<ArchiveEntryTreeViewer>> observable, TreeItem<ArchiveEntryTreeViewer> oldValue, TreeItem<ArchiveEntryTreeViewer> newValue) -> {
            if (newValue == null) {
                return;
            }
            if(newValue==fileNavigation.getRoot()){
                final Node check = detailsNode.get();
                if(check==null){
                    new Thread(() -> prepDetailsNode()).start();
                }else{
                    Platform.runLater(() -> detailsPane.getChildren().setAll(check));
                }
            }else{
                final String filename = newValue.getValue().actualFileProperty().get();
                Logger.getLogger(WarNavigationController.class.getSimpleName()).info("Selection is " + filename);
                final String[] split = filename.split("->");
                
                
                final FileTypeTask task = new FileTypeTask(path, split, detailsPane);
                
                App.submitInvisible(task);
            }
        });
    }

    public void populateWith(Path path) {
        this.path = path;
        new Thread(() -> prepDetailsNode()).start();
        final InputStreamMaker maker = () -> Files.newInputStream(path);

        final PopulateArchiveTreeTask task = new PopulateArchiveTreeTask(String.valueOf(path.getFileName()), maker, fileNavigation);
        App.submitVisible(task);

    }

    private void prepDetailsNode() {
        if (path == null) {
            return;
        }
        final FXMLLoader loader = UIUtils.load(WarDetailsController.class);
        final WarDetailsController ctrl = loader.getController();
        ctrl.populateWith(path);
        Node node = loader.getRoot();
        UIUtils.setAnchors(node, 0, 0, 0, 0);
        detailsNode = new WeakReference(node);
        Platform.runLater(() -> detailsPane.getChildren().setAll(node));
    }
    
    public void extractSelection(ActionEvent event){
        final ArchiveEntryTreeViewer f = fileNavigation.getSelectionModel().getSelectedItem().getValue();
        final Notifications n = Notifications.create().title("Extraction (not implemented yet)").text("Extracting " + f.actualFileProperty().get());
        n.showInformation();
    }
}
