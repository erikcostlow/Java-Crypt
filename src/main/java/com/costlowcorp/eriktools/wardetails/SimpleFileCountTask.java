/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.wardetails;

import com.costlowcorp.eriktools.back.ArchiveWalker;
import com.costlowcorp.eriktools.back.ArchiveWalkerRecipient;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipInputStream;
import javafx.concurrent.Task;

/**
 *
 * @author ecostlow
 */
public class SimpleFileCountTask extends Task<Void>{
    private final Path path;
    private final AtomicLong totalFilesWithFolders = new AtomicLong();
    private final AtomicLong justFiles = new AtomicLong();
    private final BiConsumer<Long, Long> after;
    
    public SimpleFileCountTask(Path path, BiConsumer<Long, Long> after){
        this.path=path;
        this.after=after;
    }

    @Override
    protected Void call() throws Exception {
        try (InputStream in = Files.newInputStream(path);
                ZipInputStream zin = new ZipInputStream(in)) {
            final ArchiveWalkerRecipient eachFile = (archiveNames, currentEntry, zin1) -> {
                final long current = totalFilesWithFolders.incrementAndGet();
                if(!currentEntry.isDirectory()){
                    justFiles.incrementAndGet();
                }
                final String message = "Current count: " + current;
                updateMessage(message);
            };
            final ArchiveWalker walker = new ArchiveWalker(path.toString(), zin, eachFile);
            walker.walk();
        } catch (IOException ex) {
            Logger.getLogger(WarDetailsController.class.getName()).log(Level.SEVERE, "Unable to scan", ex);
        }
        return null;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        after.accept(totalFilesWithFolders.get(), justFiles.get());
    }
    
}
