/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.wardetails;

import com.costlowcorp.eriktools.back.ArchiveWalker;
import com.costlowcorp.eriktools.back.ArchiveWalkerRecipient;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.zip.ZipInputStream;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 *
 * @author ecostlow
 */
public class PopulateArchiveTreeTask extends Task<Void> {
    
    public static final String ARCHIVE_SPLIT_STR = "->";

    private final InputStreamMaker maker;
    private TreeItem<ArchiveEntryTreeViewer> root = new TreeItem<>();
    private final Map<String, TreeItem<ArchiveEntryTreeViewer>> map = new HashMap<>();
    private final TreeView populateMe;

    public PopulateArchiveTreeTask(String name, InputStreamMaker maker, TreeView populateMe) {
        root.setValue(new ArchiveEntryTreeViewer(name));
        //root.getValue().forceShowName(name);
        this.maker = maker;
        this.populateMe = populateMe;
    }

    @Override
    protected Void call() throws Exception {
        final AtomicLong totalFilesWithFolders = new AtomicLong();
        final ArchiveWalkerRecipient eachFile = (archiveNames, currentEntry, zin1) -> {
            final long current = totalFilesWithFolders.incrementAndGet();
            final String message = "Current count: " + current;

            nest(archiveNames);
            updateMessage(message);
        };
        try (InputStream in = maker.make();
                final ZipInputStream zis = new ZipInputStream(in)) {
            final ArchiveWalker walker = new ArchiveWalker(zis, eachFile);
            walker.walk();
        }
        expand(root);
        return null;
    }

    @Override
    protected void failed() {
        Logger.getLogger(PopulateArchiveTreeTask.class.getSimpleName()).warning("Failed to populate archive");
        super.failed(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        Platform.runLater(() -> populateMe.setRoot(root));
    }

    private TreeItem<ArchiveEntryTreeViewer> nest(List<String> names) {
        if(names.isEmpty()){
            return root;
        }
        final String joined = String.join(ARCHIVE_SPLIT_STR, names);
        if(map.containsKey(joined)){
            return map.get(joined);
        }
        final TreeItem<ArchiveEntryTreeViewer> item = new TreeItem<>(new ArchiveEntryTreeViewer(joined));
        map.put(joined, item);
        final String currentName = names.get(names.size()-1);
        int lastSlash = currentName.lastIndexOf('/');
        if(lastSlash==currentName.length()-1){
            return item;
        }
        final List subList;
        if(lastSlash<=1){
            subList = names.subList(0, names.size()-1);
        }else{
            subList = new ArrayList<>(names);
            subList.set(subList.size()-1, currentName.substring(0, lastSlash));
        }
        
        nest(subList).getChildren().add(item);
        return item;
    }
    
    private static void expand(TreeItem<?> item){
        item.setExpanded(true);
        item.getChildren().forEach(PopulateArchiveTreeTask::expand);
    }
}
