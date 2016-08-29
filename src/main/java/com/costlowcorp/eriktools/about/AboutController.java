/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.about;

import com.costlowcorp.eriktools.App;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.security.CodeSource;
import java.util.ResourceBundle;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;

/**
 * FXML Controller class
 *
 * @author ecostlow
 */
public class AboutController implements Initializable {

    @FXML
    private Hyperlink homepage;

    @FXML
    private Label version;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        String when = "unknown";
        try {
            final String page = homepage.getText();
            homepage.setOnAction(event -> App.getSELF().getHostServices().showDocument(page));

            final CodeSource source = AboutController.class.getProtectionDomain().getCodeSource();
            final URL self = source.getLocation();
            final File tempFile = new File(self.toURI());
            
            if (tempFile.isDirectory()) {
                final String className = AboutController.class.getName().replace(".", "/");
                final Path path = tempFile.toPath().resolve(Paths.get(className + ".class"));
                when = String.valueOf(Files.getLastModifiedTime(path));
            } else {
                try (JarFile file = new JarFile(tempFile)) {
                    final JarEntry entry = file.getJarEntry("META-INF/MANIFEST.MF");
                    when = String.valueOf(entry.getLastModifiedTime());
                } catch (IOException ex) {
                    Logger.getLogger(AboutController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(AboutController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AboutController.class.getName()).log(Level.SEVERE, null, ex);
        }
        version.setText(String.valueOf(when));
    }

}
