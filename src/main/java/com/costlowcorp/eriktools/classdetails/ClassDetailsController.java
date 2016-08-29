/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.classdetails;

import com.costlowcorp.eriktools.code.CodeEditor;
import com.costlowcorp.fx.utils.UIUtils;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.CodeArea;

/**
 * FXML Controller class
 *
 * @author ecostlow
 */
public class ClassDetailsController implements Initializable {
    
    @FXML
    private Label className;
    
    @FXML
    private Tab summaryTab;
    
    @FXML
    private Tab asmifierTab;
    
    @FXML
    private Tab javapTab;
    
    private SummaryController sController;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        final FXMLLoader loader = UIUtils.load(SummaryController.class);
        sController = loader.getController();
        summaryTab.setContent(loader.getRoot());
    }    
    
    public void populateClassSummary(String className, String binaryFile, String debugFile, int classVersion, String superClass, List<String> ifaces) {
        this.className.setText(className);
        sController.populateWith(className, binaryFile, debugFile, classVersion, superClass, ifaces);
    }
    
    public void populateAsmifierTab(String text){
        final CodeArea field = CodeEditor.make(text);
        field.setEditable(false);
        VBox.setVgrow(field, Priority.ALWAYS);
        asmifierTab.setContent(field);
    }
    
    public void populateJavaPTab(String text){
        final TextArea field = new TextArea();
        field.setText(text);
        field.setEditable(false);
        VBox.setVgrow(field, Priority.ALWAYS);
        javapTab.setContent(field);
    }
}
