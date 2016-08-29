/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.classdetails;

import com.costlowcorp.eriktools.ErikUtils;
import com.costlowcorp.eriktools.back.ClassFileUtils;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.controlsfx.control.HyperlinkLabel;

/**
 * FXML Controller class
 *
 * @author ecostlow
 */
public class SummaryController implements Initializable {
    
    @FXML
    private HyperlinkLabel classNameDetails;
    
    @FXML
    private Label versionAndLanguage;
    
    @FXML
    private TextField binaryFile;
    
    @FXML
    private TextField sourceFile;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }    

    void populateWith(String className, String binaryFile, String debugFile, int classVersion, String superClass, List<String> interfaces) {
        final String java = ClassFileUtils.getJavaVersion(classVersion);
        final String language = debugFile==null ? "unknown language" : ErikUtils.getExtension(debugFile);
        final String formatted = String.format("%s written in %s", java, language);
        versionAndLanguage.setText(formatted);
        this.binaryFile.setText(binaryFile);
        sourceFile.setText(debugFile);
        final String extendsStr = superClass==null || "java/lang/Object".equals(superClass) ? "" : " extends [" + superClass + "]";
        final String ifaces = interfaces==null || interfaces.isEmpty() ? "" : " implements " + interfaces.stream().map(i -> "[" + i + "]").collect(Collectors.joining(", "));
        final String s = String.format("%s%s%s", className, extendsStr, ifaces);
        classNameDetails.setText(s);
    }
    
}
