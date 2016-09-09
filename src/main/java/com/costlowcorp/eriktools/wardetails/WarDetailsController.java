/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.wardetails;

import com.costlowcorp.eriktools.App;
import com.costlowcorp.eriktools.back.ArchiveWalker;
import com.costlowcorp.eriktools.back.MadeBy;
import com.costlowcorp.eriktools.jardetails.IdentifiedURL;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TextFieldTreeTableCell;

/**
 * FXML Controller class
 *
 * @author ecostlow
 */
public class WarDetailsController implements Initializable {
    
    private Path path;
    
    @FXML
    private PieChart chart;
    
    @FXML
    private Label filename;
    
    @FXML
    private TextField builtOn;
    
    @FXML
    private Label requiredJava;
    
    @FXML
    private Label container;
    
    @FXML
    private TextArea ownedPackages;
    
    @FXML
    private TextArea webXml;
    
    @FXML
    private TreeTableView<ArchiveOwnershipEntry> archiveTable;
    
    @FXML
    private TreeTableColumn<ArchiveOwnershipEntry, String> archiveNameCol;
    
    @FXML
    private TreeTableColumn<ArchiveOwnershipEntry, String> archiveDateCol;
    
    @FXML
    private TreeTableColumn<ArchiveOwnershipEntry, String> archiveOwnershipCol;
    
    @FXML
    private TreeTableView<IdentifiedURL> urlTable;
    
    @FXML
    private TreeTableColumn<IdentifiedURL, String> urlCol;
    
    @FXML
    private TreeTableColumn<IdentifiedURL, String> typeCol;
    
    @FXML
    private TreeTableColumn<IdentifiedURL, String> codeCol;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        archiveNameCol.setCellValueFactory(
            (TreeTableColumn.CellDataFeatures<ArchiveOwnershipEntry, String> param) -> 
            new ReadOnlyStringWrapper(param.getValue().getValue().getName())
        );
        
        final SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
        archiveDateCol.setCellValueFactory(
            (TreeTableColumn.CellDataFeatures<ArchiveOwnershipEntry, String> param) -> 
            new ReadOnlyStringWrapper(param.getValue().getValue().getWhenMade()==null ? "" : sdf.format(param.getValue().getValue().getWhenMade()))
        );
        
        archiveOwnershipCol.setCellValueFactory(
            (TreeTableColumn.CellDataFeatures<ArchiveOwnershipEntry, String> param) -> 
            new ReadOnlyStringWrapper(String.valueOf(param.getValue().getValue().getOwnership()))
        );
        
        urlCol.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<IdentifiedURL, String> param)
                -> new ReadOnlyStringWrapper(param.getValue().getValue().getPath())
        );
        urlCol.setCellFactory((TreeTableColumn<IdentifiedURL, String> param) -> new TextFieldTreeTableCell<>());
        
        codeCol.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<IdentifiedURL, String> param)
                -> new ReadOnlyStringWrapper(param.getValue().getValue().getClassName() + '.' + param.getValue().getValue().getMethodNameAndDesc())
        );
        codeCol.setCellFactory((TreeTableColumn<IdentifiedURL, String> param) -> new TextFieldTreeTableCell<>());
        
        typeCol.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<IdentifiedURL, String> param)
                -> new ReadOnlyStringWrapper(param.getValue().getValue().getType())
        );
    }

    public void populateWith(Path path){
        filename.setText(String.valueOf(path.getFileName()));
        this.path=path;
        new Thread(() -> count()).start();
    }
    
    private void count() {
        final SimpleFileCountTask countFiles = new SimpleFileCountTask(path, this::afterCount);
        executeTasks(countFiles);
    }
    
    private void afterCount(long withFolders, long justFiles){
        final Consumer<Set<String>> afterDone = set -> Platform.runLater(() -> updateOwnedPackages(set));
        final Consumer<TreeItem> blah = f -> Platform.runLater(() -> updateTree(f));
        final Consumer<TreeItem<IdentifiedURL>> urlActor = f -> Platform.runLater(() -> updateUrlTree(f));
        final BasicBytecodeScan identifyTask = new BasicBytecodeScan("Identify owned Java packages", path, withFolders, afterDone, blah, urlActor);
        final CountFileIntrospectTypesTask countFileTypes = new CountFileIntrospectTypesTask("Identify files", path, withFolders, chart, requiredJava);

        executeTasks(identifyTask, countFileTypes);
        String runsOn = "Any Java web server";
        try(InputStream in = Files.newInputStream(path);
                ZipInputStream zis = new ZipInputStream(in)){
            FileTime highest=null;
            for(ZipEntry entry = zis.getNextEntry(); entry!=null; entry = zis.getNextEntry()){
                final FileTime lastModified = entry.getLastModifiedTime();
                if(highest==null || (lastModified!=null && lastModified.compareTo(highest)>0)){
                    highest = entry.getLastModifiedTime();
                }
                final String entryName = entry.getName();
                if("WEB-INF/web.xml".equals(entryName)){
                    final byte[] bytes = ArchiveWalker.currentEntry(zis);
                    final String str = new String(bytes);
                    Platform.runLater(() -> webXml.setText(str));
                }else if("WEB-INF/weblogic.xml".equals(entryName)){
                    runsOn = "WebLogic";
                }else if("WEB-INF/jboss-web.xml".equals(entryName)){
                    runsOn = "JBoss";
                }
            }
            final FileTime t = highest;
            final String runText = runsOn;
            Platform.runLater(() -> {
                builtOn.setText(String.valueOf(t));
                container.setText(runText);
            });
        } catch (IOException ex) {
            Logger.getLogger(WarDetailsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void updateOwnedPackages(Set<String> packages) {
        ownedPackages.setText(String.join("\n", packages));
    }

    private void updateTree(TreeItem<ArchiveOwnershipEntry> root) {
        archiveTable.setRoot(root);
        updateOwnership();
        
       
    }
    
    private void updateUrlTree(TreeItem<IdentifiedURL> root) {
        urlTable.setRoot(root);
    }
    
    private void executeTasks(Task... tasks) {
        Arrays.stream(tasks).forEach(task -> App.submitVisible(task));
    }

    private void updateOwnership() {
        final Set<String> ownedPackageNames = new HashSet<>();
        final String[] split = ownedPackages.getText().split("\n");
        Arrays.stream(split).forEach(ownedPackageNames::add);
        update(ownedPackageNames, archiveTable.getRoot());
    }
    
    private MadeBy update(Set<String> ownedPackageNames, TreeItem<ArchiveOwnershipEntry> item){
        MadeBy temp = MadeBy.THIRD_PARTY;
        if(!item.getChildren().isEmpty()){
             final Set<MadeBy> checkers =item.getChildren().stream().map(child -> update(ownedPackageNames, child)).filter(madeBy -> !madeBy.equals(MadeBy.THIRD_PARTY)).collect(Collectors.toSet());
             if(checkers.contains(MadeBy.BOTH)){
                 temp = MadeBy.BOTH;
             }else if(checkers.contains(MadeBy.SELF)){
                 temp = MadeBy.SELF;
             }
        }
        final String name = item.getValue().getName();
        final Optional<String> checkIfSelf = ownedPackageNames.stream().filter(pkg -> name.startsWith(pkg)).findAny();
        final MadeBy current = checkIfSelf.isPresent() || temp==MadeBy.SELF ? MadeBy.SELF : temp;
        
        checkIfSelf.ifPresent(str -> item.getValue().setMadeBy(current));
        
        return current;
    }
}
