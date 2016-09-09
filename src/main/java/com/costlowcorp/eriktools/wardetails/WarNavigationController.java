/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.wardetails;

import com.costlowcorp.eriktools.App;
import com.costlowcorp.fx.utils.UIUtils;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;

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
}
