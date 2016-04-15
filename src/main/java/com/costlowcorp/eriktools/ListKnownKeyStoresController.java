/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools;

import com.costlowcorp.fx.utils.UIUtils;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.AnchorPane;

/**
 *
 * @author ecostlow
 */
public class ListKnownKeyStoresController implements Initializable {

    private static final String DEFAULT_PASSWORD = "changeit";

    @FXML
    private TextField filename;

    @FXML
    private ScrollPane keystores;

    @FXML
    private ScrollPane detailArea;

    @FXML
    private AnchorPane keystoreContents;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        populateKeyStores();
    }

    private void populateKeyStores() {
        new Thread(() -> {
            final KeyStoreTree list = new KeyStoreTree();
            list.getSelectionModel().selectedItemProperty().addListener((observableValue, previous, current) -> change(list.getSelectionModel().getSelectedItems()));
            Platform.runLater(() -> keystores.setContent(list));
        }).start();
    }

    private void change(ObservableList<TreeItem<KeyStoreTree.KnownKeyStore>> known) {
        final List<Path> found = known.stream().map(k -> k.getValue().getPath()).filter(p -> p != null).collect(Collectors.toList());
        switch (found.size()) {
            case 0:
                break;
            case 1:
                change(found.get(0));
                break;
            default:
            //TODO
        }
    }

    private void change(Path path) {
        keystoreContents.getChildren().clear();
        final ProgressIndicator indicator = new ProgressIndicator();
        keystoreContents.getChildren().add(indicator);
        new Thread(() -> {
            try {
                filename.setText(path.toAbsolutePath().toString());
                final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(Files.newInputStream(path), DEFAULT_PASSWORD.toCharArray());

                final GroupedListKeyStore grouped = new GroupedListKeyStore(keyStore);
                
                final FXMLLoader stats = UIUtils.load(GroupedKeyStoreStatisticsController.class);
                final GroupedKeyStoreStatisticsController statsController = stats.getController();
                statsController.initialize(grouped);
                
                UIUtils.setAnchors(grouped, 0,0,0,0);
                Platform.runLater(() -> {
                    detailArea.setContent(stats.getRoot());
                    keystoreContents.getChildren().clear();
                    keystoreContents.getChildren().add(grouped);
                });
            } catch (Exception e) {

            }
        }).start();
    }
}
