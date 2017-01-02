/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools;

import com.costlowcorp.eriktools.about.AboutController;
import com.costlowcorp.eriktools.checksum.ChecksumFileController;
import com.costlowcorp.eriktools.eardetails.EarNavigationController;
import com.costlowcorp.eriktools.jardetails.JarDetailsController;
import com.costlowcorp.eriktools.jardetails.JarNavigationController;
import com.costlowcorp.eriktools.wardetails.WarDetailsController;
import com.costlowcorp.eriktools.wardetails.WarNavigationController;
import com.costlowcorp.fx.utils.UIUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author ecostlow
 */
public class HomeController implements Initializable {

    @FXML
    private TabPane tabs;

    private File lastFile = new File(System.getProperty("user.home"));

    private FileChooser.ExtensionFilter lastFilter = new FileChooser.ExtensionFilter("Anything", "*.jar", "*.cer", "*.der", "*.war", "*.ear");

    private final List<FileChooser.ExtensionFilter> filters = Arrays.asList(
            lastFilter,
            new FileChooser.ExtensionFilter("JARs only", "*.jar"),
            new FileChooser.ExtensionFilter("KeyStores", "*.jks", "*.pkcs12"),
            new FileChooser.ExtensionFilter("Web Applications", "*.war"),
            new FileChooser.ExtensionFilter("Enterprise Archives", "*.ear")
    );

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public void scanSystem(ActionEvent event) {
        final FXMLLoader loader = UIUtils.load(ListKnownKeyStoresController.class);
        final Pane root = loader.getRoot();

        final Tab newTab = new Tab("System", root);
        tabs.getTabs().add(newTab);
    }

    public void openChecksum(ActionEvent e) {
        final FXMLLoader loader = UIUtils.load(ChecksumFileController.class);
        final Pane root = loader.getRoot();
        final ChecksumFileController controller = loader.getController();
        final Tab tab = new Tab("Checksum", root);
        tabs.getTabs().add(tab);
    }

    public void openFile(ActionEvent event) {
        final FileChooser chooser = new FileChooser();
        if (lastFile != null) {
            final boolean isDir = lastFile.isDirectory();
            chooser.setInitialDirectory(isDir ? lastFile : lastFile.getParentFile());
            chooser.setInitialFileName(isDir ? null : lastFile.getName());
        }

        chooser.getExtensionFilters().addAll(filters);
        chooser.setSelectedExtensionFilter(lastFilter);
        final File check = chooser.showOpenDialog(UIUtils.getWindowFor(tabs));
        lastFilter = chooser.getSelectedExtensionFilter();

        if (check != null) {
            lastFile = check;
            Node newTabRoot;
            try {
                final String filename = check.getName().toLowerCase();
                final int dot = filename.lastIndexOf(".");
                final String extension = dot > 0 ? filename.substring(dot) : filename;

                switch (extension) {
                    case ".cer":
                    case ".der":
                        newTabRoot = loadCertificatesFrom(check);
                        break;
                    case ".jar":
                        newTabRoot = loadJar(check);
                        break;
                    case ".war":
                        newTabRoot = loadWar(check);
                        break;
                    case ".ear":
                        newTabRoot = loadEar(check);
                        break;
                    default:
                        newTabRoot = new Label("Unable to open " + check.getName());
                }

            } catch (CertificateException | IOException ex) {
                Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
                newTabRoot = new Label(ex.getMessage() + " :: " + check.getName());
            }
            final Tab newTab = new Tab(check.getName(), newTabRoot);
            tabs.getTabs().add(newTab);
            tabs.getSelectionModel().select(newTab);
        }
    }

    public void openAbout(ActionEvent event) {
        Stage stage = new Stage();
        final FXMLLoader loader = UIUtils.load(AboutController.class);
        Parent root = loader.getRoot();
        stage.setScene(new Scene(root));
        stage.setTitle("About");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(
                tabs.getScene().getWindow());
        stage.show();
    }

    private Node loadCertificatesFrom(File file) throws CertificateException, IOException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final VBox retval = new VBox();
        retval.getChildren().add(new Label("Provider: " + cf.getProvider()));
        retval.getChildren().add(new Label("Type: " + cf.getType()));
        try (InputStream in = new FileInputStream(file)) {
            Collection<? extends Certificate> certs = cf.generateCertificates(in);
            certs.stream().forEach(certificate -> {
                final FXMLLoader loader = UIUtils.load(ShowCertificateController.class);
                final ShowCertificateController controller = loader.getController();
                controller.initilize(certificate, "N/A opened as file");
                retval.getChildren().add(loader.getRoot());
            });
        }

        return retval;
    }

    private Node loadJar(File check) {
        final FXMLLoader loader = UIUtils.load(JarNavigationController.class);
        //final JarDetailsController controller = loader.getController();
        //controller.populateWith(check);
        final JarNavigationController controller = loader.getController();
        controller.populateWith(check);
        final Node root = loader.getRoot();
        return root;
    }

    private Node loadWar(File check) {
        final FXMLLoader loader = UIUtils.load(WarNavigationController.class);
        final WarNavigationController controller = loader.getController();
        controller.populateWith(check.toPath());
        final Node root = loader.getRoot();
        return root;
    }
    
    private Node loadEar(File check) {
        final FXMLLoader loader = UIUtils.load(EarNavigationController.class);
        final EarNavigationController controller = loader.getController();
        controller.populateWith(check.toPath());
        final Node root = loader.getRoot();
        return root;
    }
}
