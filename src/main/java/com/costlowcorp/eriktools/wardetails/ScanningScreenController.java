/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.wardetails;

import com.costlowcorp.eriktools.App;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import org.controlsfx.control.TaskProgressView;

/**
 * FXML Controller class
 *
 * @author ecostlow
 */
public class ScanningScreenController implements Initializable {

    @FXML
    private TaskProgressView taskProgress;
    
    @FXML
    private PieChart chart;
    
    @FXML
    private TextArea ownedPackages;
    
    @FXML
    private TreeTableView<ArchiveOwnershipEntry> archiveTable;
    
    @FXML
    private TreeTableColumn<ArchiveOwnershipEntry, String> archiveNameCol;
    
    @FXML
    private TreeTableColumn archiveDateCol;
    
    @FXML
    private TreeTableColumn ownershipCol;
    
    private Path path;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        archiveNameCol.setCellValueFactory(
            (TreeTableColumn.CellDataFeatures<ArchiveOwnershipEntry, String> param) -> 
            new ReadOnlyStringWrapper(param.getValue().getValue().getName())
        );
    }

    public void startScan(ActionEvent event) {
        path = Paths.get("C:\\Users\\ecostlow.ORADEV\\Downloads\\Java\\amc2\\JavaAMC-2_4.ear");
        populateWith(path);
    }

    public void populateWith(Path path) {
        new Thread(() -> count(path)).start();
    }

    private void count(Path path) {
        final SimpleFileCountTask countFiles = new SimpleFileCountTask(path, this::afterCount);
        executeTasks(countFiles);
    }
    
    private void afterCount(long withFolders, long justFiles){
        final Consumer<Set<String>> afterDone = set -> Platform.runLater(() -> updateOwnedPackages(set));
        final IdentifyOwnedPackagesTask identifyTask = new IdentifyOwnedPackagesTask("Identify owned Java packages", path, withFolders, afterDone);
        final CountFileIntrospectTypesTask countFileTypes = new CountFileIntrospectTypesTask("Identify files", path, withFolders, chart);

        executeTasks(identifyTask, countFileTypes);
    }

    private void executeTasks(Task... tasks) {
        Platform.runLater(() -> {
            taskProgress.getTasks().addAll(Arrays.asList(tasks));
            Arrays.stream(tasks).forEach(task -> App.getEXECUTOR().submit(task));
        });
    }
    
    private void updateOwnedPackages(Set<String> packages){
        ownedPackages.setText(String.join("\n", packages));
    }
}
