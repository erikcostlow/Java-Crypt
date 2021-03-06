/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.wardetails;

import com.costlowcorp.fx.utils.ImageGrabber;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author ecostlow
 */
public class ArchiveEntryTextFieldTreeCellImpl extends TreeCell<ArchiveEntryTreeViewer> {

    private TextField textField;

    public ArchiveEntryTextFieldTreeCellImpl() {
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
    public void updateItem(ArchiveEntryTreeViewer item, boolean empty) {
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
            final String s = getString();
            setText(s);
            Node node = getTreeItem().getGraphic();
            if(node==null){
                final Image img = ImageGrabber.imageFor(s);
                if(img!=null){
                    final ImageView temp = new ImageView(img);
                    temp.setFitHeight(16);
                    temp.setFitWidth(16);
                    node = temp;
                    setGraphic(node);
                }
            }
            
            
            setGraphic(node);
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
