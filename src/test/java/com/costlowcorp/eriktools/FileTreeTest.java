/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javafx.application.Application;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author ecostlow
 */
public class FileTreeTest extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        VBox root = new VBox();
        root.getChildren().add(new Label("BLah"));
        final String file = "C:\\Users\\erikc_000\\Downloads\\temp\\CryptoUI-1.0-SNAPSHOT.jar";
        try (ZipFile zip = new ZipFile(file)) {
            root.getChildren().add(treeFromZip(zip));
        }

        final Scene scene = new Scene(root);
        stage.setWidth(1024);
        stage.setHeight(768);
        scene.getStylesheets().add(App.class.getClassLoader().getResource("main.css").toExternalForm());
        stage.setScene(scene);
        stage.getIcons().addAll(new Image("/Java-support-16x16.png"), new Image("/Java-support-32x32.png"));
        stage.setTitle("Text");
        stage.show();
    }

    private TreeView treeFromZip(ZipFile zip) {
        final TreeView<BasicTreeThing> retval = new TreeView();
        final Image folder = new Image("/folder.png");
        retval.setCellFactory((TreeView<BasicTreeThing> param) -> new TextFieldTreeCellImpl());
        final TreeItem<BasicTreeThing> root = new TreeItem<>(new BasicTreeThing(zip.getName()), new ImageView(folder));
        final TreeItem<BasicTreeThing> a = new TreeItem<>(new BasicTreeThing("A"));
        final TreeItem<BasicTreeThing> b = new TreeItem<>(new BasicTreeThing("B"), new ImageView(folder));
        final TreeItem<BasicTreeThing> b2 = new TreeItem<>(new BasicTreeThing("C"));
        b.getChildren().add(b2);
        b.setExpanded(true);
        final TreeItem<BasicTreeThing> c = new TreeItem<>(new BasicTreeThing("D"));

        final Map<String, TreeItem> map = new HashMap<>();
        final List<TreeItem<BasicTreeThing>> innerClassList = new ArrayList<>();
        final AtomicLong innerClassDepth = new AtomicLong();
        zip.stream().sorted((first, second) -> first.getName().compareToIgnoreCase(second.getName()))
                .forEach(entry -> {
                    final String entryName = entry.getName();
                    final String parentDir = parentDir(entry);

                    final TreeItem<BasicTreeThing> item = new TreeItem<>(new BasicTreeThing(entryName));
                    if (entry.isDirectory()) {
                        item.setGraphic(new ImageView(folder));
                    }

                    final boolean parentFound = map.containsKey(parentDir);
                    //System.out.println(entryName + " has parent " + parentDir + " and found " + parentFound );
                    if (parentFound) {
                        System.out.println(entryName + " has parent " + parentDir);

                        final String currentFilename = item.getValue().showNameProperty().get();
                        final long currentInnerClassDepth = currentFilename.chars().filter(ch -> ch == '$').count();
                        if (currentInnerClassDepth == 0) {
                            final TreeItem parent = map.get(parentDir);
                            parent.getChildren().add(item);
                            item.getChildren().addAll(innerClassList);
                            innerClassList.clear();;
                        } else if (currentInnerClassDepth > innerClassDepth.get()) {
                            innerClassList.add(item);
                        } else {
                            if (currentInnerClassDepth != innerClassDepth.get()) {
                                item.getChildren().addAll(innerClassList);
                                innerClassList.clear();
                            }
                            innerClassList.add(item);
                        }
                        innerClassDepth.set(currentInnerClassDepth);

                    } else {
                        root.getChildren().add(item);
                    }
                    item.setExpanded(true);
                    map.put(entryName, item);
                });

        a.setGraphic(new ImageView(folder));
        root.getChildren().addAll(a, b, c);
        retval.setRoot(root);
        root.setExpanded(true);

        return retval;
    }

    private String parentDir(ZipEntry entry) {
        final String name = entry.getName();
        if (!name.contains("/")) {
            return name;
        }
        final int end = entry.isDirectory() ? name.lastIndexOf('/', name.length() - 2) : name.lastIndexOf('/');
        return end < 0 ? "/" : name.substring(0, end + 1);

    }

    private TreeTableView treeTableFromZip(ZipFile zip) {
        final Image folder = new Image("/folder.png");
        final ImageView iv = new ImageView(folder);
        final TreeItem<TreeThing> childNode1 = new TreeItem<>(new TreeThing("Child Node 1", "orig", 20));
        final TreeItem<TreeThing> childNode2 = new TreeItem<>(new TreeThing("Child Node 2", "blah", 9999));
        final TreeItem<TreeThing> childNode3 = new TreeItem<>(new TreeThing("Child Node 3", "junk", 84));
        childNode3.getChildren().add(new TreeItem<>(new TreeThing("ABC", "DEF", 123), iv));
        childNode3.setExpanded(true);
        final TreeItem<TreeThing> root = new TreeItem<>(new TreeThing("Blah", "root", 0), new ImageView(folder));
        root.setExpanded(true);

        //Adding tree items to the root
        root.getChildren().setAll(childNode1, childNode2, childNode3);

        //Creating a column
        TreeTableColumn<TreeThing, String> fileCol = new TreeTableColumn<>("File");
        fileCol.setPrefWidth(150);
        fileCol.setCellValueFactory(new TreeItemPropertyValueFactory("filename")
        //(TreeTableColumn.CellDataFeatures<TreeThing, String> param) -> 
        //param.getValue().getValue().filenameProperty()      
        );

        TreeTableColumn<TreeThing, String> typeCol = new TreeTableColumn<>("Type");
        typeCol.setCellValueFactory(new TreeItemPropertyValueFactory("type"));
        typeCol.setPrefWidth(150);

        TreeTableColumn<TreeThing, Long> sizeCol = new TreeTableColumn<>("Size");
        sizeCol.setCellValueFactory(new TreeItemPropertyValueFactory("size"));
        sizeCol.setPrefWidth(150);

        //Creating a tree table view
        final TreeTableView<TreeThing> treeTableView = new TreeTableView<>(root);
        treeTableView.getColumns().addAll(fileCol, typeCol, sizeCol);
        treeTableView.setPrefWidth(152);
        treeTableView.setShowRoot(true);

        return treeTableView;
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class TreeThing {

        private final StringProperty filename;
        private final StringProperty type;
        private final LongProperty size;

        public TreeThing(String filename, String type, long size) {
            this.filename = new SimpleStringProperty(filename);
            this.type = new SimpleStringProperty(type);
            this.size = new SimpleLongProperty(size);
        }

        public StringProperty filenameProperty() {
            return filename;
        }

        public LongProperty sizeProperty() {
            return size;
        }

        public StringProperty typeProperty() {
            return type;
        }
    }

    private final class TextFieldTreeCellImpl extends TreeCell<BasicTreeThing> {

        private TextField textField;

        public TextFieldTreeCellImpl() {
        }

        @Override
        public void startEdit() {
            super.startEdit();

            if (textField == null) {
                createTextField();
            }
            setText(null);
            setGraphic(textField);
            textField.selectAll();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getItem().actualFileProperty().get());
            setGraphic(getTreeItem().getGraphic());
        }

        @Override
        public void updateItem(BasicTreeThing item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else if (isEditing()) {
                if (textField != null) {
                    textField.setText(getString());
                }
                setText(null);
                setGraphic(textField);
            } else {
                setText(getString());
                setGraphic(getTreeItem().getGraphic());
            }
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setOnKeyReleased((KeyEvent t) -> {
                if (t.getCode() == KeyCode.ENTER) {
                    //commitEdit(textField.getText());
                } else if (t.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                }
            });
        }

        private String getString() {
            return getItem() == null ? "" : getItem().showNameProperty().get();
        }
    }
}
