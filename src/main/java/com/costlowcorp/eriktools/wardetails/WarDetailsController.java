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
import com.costlowcorp.eriktools.scanners.BuildHierarchyTask;
import com.costlowcorp.fx.utils.DateApproximator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.scene.input.MouseEvent;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.controlsfx.control.Notifications;

/**
 * FXML Controller class
 *
 * @author ecostlow
 */
public class WarDetailsController implements Initializable {

    private Path path;
    private long totalFilesWithFolders;

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

    public void populateWith(Path path) {
        filename.setText(String.valueOf(path.getFileName()));
        this.path = path;
        new Thread(() -> count()).start();
    }

    private void count() {
        final SimpleFileCountTask countFiles = new SimpleFileCountTask(path, this::afterCount);
        executeTasks(countFiles);
    }

    private void afterCount(long withFolders, long justFiles) {
        this.totalFilesWithFolders=withFolders;
        final Consumer<Set<String>> afterDone = set -> Platform.runLater(() -> updateOwnedPackages(set));
        final Consumer<TreeItem> blah = f -> Platform.runLater(() -> updateTree(f));
        final Consumer<TreeItem<IdentifiedURL>> urlActor = f -> Platform.runLater(() -> updateUrlTree(f));
        final BasicBytecodeScan identifyTask = new BasicBytecodeScan("Identify owned Java packages", path, withFolders, afterDone, blah, urlActor);
        final CountFileIntrospectTypesTask countFileTypes = new CountFileIntrospectTypesTask("Identify files", path, withFolders, chart, requiredJava);

        executeTasks(identifyTask, countFileTypes);
        String runsOn = "Any Java web server";
        try (InputStream in = Files.newInputStream(path);
                ZipInputStream zis = new ZipInputStream(in)) {
            FileTime highest = null;
            for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
                final FileTime lastModified = entry.getLastModifiedTime();
                if (highest == null || (lastModified != null && lastModified.compareTo(highest) > 0)) {
                    highest = entry.getLastModifiedTime();
                }
                final String entryName = entry.getName();
                if ("WEB-INF/web.xml".equals(entryName)) {
                    final byte[] bytes = ArchiveWalker.currentEntry(zis);
                    final String str = new String(bytes);
                    Platform.runLater(() -> webXml.setText(str));
                } else if ("WEB-INF/weblogic.xml".equals(entryName)) {
                    runsOn = "WebLogic";
                } else if ("WEB-INF/jboss-web.xml".equals(entryName)) {
                    runsOn = "JBoss";
                }
            }
            final FileTime t = highest;
            final String runText = runsOn;
            Platform.runLater(() -> {
                final String builtDate = String.valueOf(t);
                final String builtDateEnglish = DateApproximator.between(LocalDateTime.ofInstant(t.toInstant(), ZoneId.systemDefault()), LocalDateTime.now()) + " ago.";
                builtOn.setText(builtDateEnglish);
                builtOn.setOnMouseClicked((MouseEvent event) -> {
                    if (builtOn.getText().equals(builtDateEnglish)) {
                        builtOn.setText(builtDate);
                    } else {
                        builtOn.setText(builtDateEnglish);
                    }
                });
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

    private MadeBy update(Set<String> ownedPackageNames, TreeItem<ArchiveOwnershipEntry> item) {
        MadeBy temp = MadeBy.THIRD_PARTY;
        if (!item.getChildren().isEmpty()) {
            final Set<MadeBy> checkers = item.getChildren().stream().map(child -> update(ownedPackageNames, child)).filter(madeBy -> !madeBy.equals(MadeBy.THIRD_PARTY)).collect(Collectors.toSet());
            if (checkers.contains(MadeBy.BOTH)) {
                temp = MadeBy.BOTH;
            } else if (checkers.contains(MadeBy.SELF)) {
                temp = MadeBy.SELF;
            }
        }
        final String name = item.getValue().getName();
        final Optional<String> checkIfSelf = ownedPackageNames.stream().filter(pkg -> name.startsWith(pkg)).findAny();
        final MadeBy current = checkIfSelf.isPresent() || temp == MadeBy.SELF ? MadeBy.SELF : temp;

        checkIfSelf.ifPresent(str -> item.getValue().setMadeBy(current));

        return current;
    }

    public void outputHierarchy(ActionEvent e) {
        //Ask for otuput file
        //Open all JAR files
        //For each class:
        //1. GetOrCreate a node. ID should be classname
        //Attribute: archive
        //Attribute: havebytecode
        //2. GetOrCreate node of superclass and all interfaces
        //If create, havebytecode should be false, archive should be null
        //3. Walk through methods
        //Same for method annotation classes
        //Output gexf
        final Graph graph = App.makeGraph();
        final BuildHierarchyTask task = new BuildHierarchyTask(path, graph, totalFilesWithFolders);
        task.stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State oldState, Worker.State newState) {
                if (newState == Worker.State.SUCCEEDED) {
                    Notifications.create().title("Hierarchy done").text("Finished").showInformation();
                    try {
                        graph.io(IoCore.graphson()).writeGraph("tinkerpop-modern.json");
                        graph.io(IoCore.gryo()).writeGraph("tinkerpop-modern.kryo");
                    } catch (IOException ex) {
                        Logger.getLogger(WarDetailsController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        App.submitVisible(task);

    }

    public void outputControlFlow(ActionEvent e) {
        final Notifications notification = Notifications.create().title("TODO").text("Replace control flow with Tinkerpop");
        notification.showError();
    }
}
