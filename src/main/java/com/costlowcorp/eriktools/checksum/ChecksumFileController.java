/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.checksum;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

/**
 * FXML Controller class
 *
 * @author ecostlow
 */
public class ChecksumFileController implements Initializable {
    
    @FXML
    private TextField filename;
    
    @FXML
    private VBox checksumCheckboxHolder;
    
    @FXML
    private TextArea checksums;
    
    @FXML
    private ProgressIndicator progress;
    
    private File lastFile;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Provider[] providers = Security.getProviders();
        
        checksumCheckboxHolder.getChildren().clear();
        Arrays.stream(providers)
                .flatMap(p -> p.getServices().stream())
                .filter(service -> "MessageDigest".equals(service.getType()))
                .forEach(this::makeCheckbox);
                ;
    }
    
    private void makeCheckbox(Provider.Service service){
        final CheckBox cb = new CheckBox(service.getAlgorithm());
        switch(service.getAlgorithm()){
            case "MD5":
            case "SHA-256":
                cb.setSelected(true);
        }
        cb.setOnAction(e -> go());
        checksumCheckboxHolder.getChildren().add(0,cb);
    }
    
    public void openFileChooser(ActionEvent actionEvent){
        final Scene scene = filename.getScene();
        final FileChooser chooser = new FileChooser();
        if(lastFile!=null){
            chooser.setInitialDirectory(lastFile.isDirectory() ? lastFile : lastFile.getParentFile());
            chooser.setInitialFileName(lastFile.getName());
        }
        chooser.setTitle("MyTitle");
        lastFile = chooser.showOpenDialog(scene.getWindow());
        if(lastFile!=null){
            try {
                filename.setText(lastFile.getCanonicalPath());
            } catch (IOException ex) {
                filename.setText(ex.getMessage());
            }
        }
        go();
    }
    
    private void go(){
        if(filename.getText()==null || filename.getText().isEmpty()){
            return;
        }
        new Thread(() -> runWhenGo()).start();
    }
    
    private void runWhenGo(){
        progress.setVisible(true);
        checksums.setText("");
        final List<MessageDigest> digests = checksumCheckboxHolder.getChildrenUnmodifiable().stream()
                .map(child -> (CheckBox) child)
                .filter(cb -> cb.isSelected())
                .map(cb -> ChecksumFileController.getDigest(cb.getText()))
                .collect(Collectors.toList());
        
        final Path path = Paths.get(filename.getText());
        if(path==null || !Files.isRegularFile(path) || !Files.isReadable(path)){
            return;
        }
        
        try(InputStream in = Files.newInputStream(path)){
            final byte[] bytes = new byte[2048];
            for(int length=in.read(bytes); length>0; length = in.read(bytes)){
                final int l =length;
                digests.stream().forEach(d -> d.update(bytes, 0, l));
            }
        } catch (IOException ex) {
            Logger.getLogger(ChecksumFileController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        final String str = digests.stream()
                .map(digest -> digest.getAlgorithm() + " : " + byteArrayToHex(digest.digest()))
                .collect(Collectors.joining(System.getProperty("line.separator")));
        Platform.runLater(() -> {
            checksums.setText(str);
            progress.setVisible(false);
        });
    }
    
    private static MessageDigest getDigest(String digest){
        try {
            return MessageDigest.getInstance(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private static String byteArrayToHex(byte[] bytes) {
        //convert the byte to hex format
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
