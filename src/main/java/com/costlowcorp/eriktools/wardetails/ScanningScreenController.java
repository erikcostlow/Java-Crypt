/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.wardetails;

import com.costlowcorp.eriktools.App;
import com.costlowcorp.eriktools.jardetails.IdentifiedURL;
import com.costlowcorp.eriktools.jardetails.PackageCount;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.util.Callback;
import org.controlsfx.control.TaskProgressView;

/**
 * FXML Controller class
 *
 * @author ecostlow
 */
public class ScanningScreenController implements Initializable {

    @FXML
    private PieChart chart;

    @FXML
    private TextArea ownedPackages;

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

    private Path path;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        archiveNameCol.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<ArchiveOwnershipEntry, String> param)
                -> new ReadOnlyStringWrapper(param.getValue().getValue().getName())
        );

        final SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
        archiveDateCol.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<ArchiveOwnershipEntry, String> param)
                -> new ReadOnlyStringWrapper(param.getValue().getValue().getWhenMade() == null ? "" : sdf.format(param.getValue().getValue().getWhenMade()))
        );

        archiveOwnershipCol.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<ArchiveOwnershipEntry, String> param)
                -> new ReadOnlyStringWrapper(String.valueOf(param.getValue().getValue().getOwnership()))
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

    public void startScan(ActionEvent event) {
        path = Paths.get("C:\\Users\\ecostlow.ORADEV\\Downloads\\Java\\amc2\\JavaAMC-2_4.ear");
//        path = Paths.get("C:\\Users\\ecostlow.ORADEV\\Downloads\\Java\\amc2\\JavaAMC-2_4\\webui\\amcwebui.war");
        populateWith(path);
    }

    public void populateWith(Path path) {
        new Thread(() -> count(path)).start();
    }

    private void count(Path path) {
        final SimpleFileCountTask countFiles = new SimpleFileCountTask(path, this::afterCount);
        executeTasks(countFiles);
    }

    private void afterCount(long withFolders, long justFiles) {
        final Consumer<Set<String>> afterDone = set -> Platform.runLater(() -> updateOwnedPackages(set));
        final Consumer<TreeItem> blah = f -> Platform.runLater(() -> updateTree(f));
        final Consumer<TreeItem<IdentifiedURL>> urlActor = f -> Platform.runLater(() -> updateUrlTree(f));

        final BasicBytecodeScan identifyTask = new BasicBytecodeScan("Identify Programming Frameworks", path, withFolders, afterDone, blah, urlActor);
        final CountFileIntrospectTypesTask countFileTypes = new CountFileIntrospectTypesTask("Identify files", path, withFolders, chart);

        executeTasks(identifyTask, countFileTypes);
    }

    private void executeTasks(Task... tasks) {
        Arrays.stream(tasks).forEach(task -> App.submit(task));
    }

    private void updateOwnedPackages(Set<String> packages) {
        ownedPackages.setText(String.join("\n", packages));
    }

    private void updateTree(TreeItem root) {
        archiveTable.setRoot(root);
    }
    
    private void updateUrlTree(TreeItem<IdentifiedURL> root) {
        urlTable.setRoot(root);
    }
}
