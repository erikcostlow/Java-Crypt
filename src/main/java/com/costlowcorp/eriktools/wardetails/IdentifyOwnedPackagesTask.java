/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.wardetails;

import com.costlowcorp.eriktools.back.ArchiveWalker;
import com.costlowcorp.eriktools.back.ArchiveWalkerRecipient;
import com.costlowcorp.eriktools.jardetails.ClassFileMetaVisitor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipInputStream;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author ecostlow
 */
public class IdentifyOwnedPackagesTask extends Task<Void> {

    private final Path path;
    private final double maxClasses;
    private final Consumer<Set<String>> onCompletion;
    private final Set<String> ownedPackages = new TreeSet<>();

    public IdentifyOwnedPackagesTask(String title, Path path, long maxClasses, Consumer<Set<String>> onCompletion) {
        updateTitle(title);
        this.path = path;
        this.maxClasses = maxClasses;
        this.onCompletion = onCompletion;
    }

    @Override
    protected Void call() throws Exception {
        final LongAdder classCount = new LongAdder();
        final Double upMaxClasses = maxClasses;
        final Map<String, TreeItem> items = new HashMap<>();
        try (InputStream in = Files.newInputStream(path);
                ZipInputStream zin = new ZipInputStream(in)) {
            final ArchiveWalkerRecipient eachFile = (t, entry, u) -> {
                classCount.increment();
                final double currentClass = classCount.doubleValue();
                final String message = String.format("Class %1.0f of %1.0f", currentClass, upMaxClasses);
                updateMessage(message);
                updateProgress(currentClass, maxClasses);
                if (entry.getName().toLowerCase().endsWith(".class")) {

                    try {
                        final byte[] bytes = ArchiveWalker.currentEntry(u);
                        final ClassFileMetaVisitor v = new ClassFileMetaVisitor(Opcodes.ASM5, null);
                        final ClassReader reader = new ClassReader(bytes);
                        reader.accept(v, ClassReader.SKIP_CODE);
                        final String pkg = v.getPackage();
                        final boolean foundJar = t.stream().filter(archive -> archive.toLowerCase().contains(".jar")).findAny().isPresent();
                        if (!foundJar && !"".equals(pkg)) {
                            ownedPackages.add(pkg);
                        }
                        
                        nest(items, t, pkg);
                    } catch (IOException ex) {
                        Logger.getLogger(IdentifyOwnedPackagesTask.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            };
            final ArchiveWalker walker = new ArchiveWalker(path.toString(), zin, eachFile);
            walker.walk();
        } catch (IOException ex) {
            Logger.getLogger(WarDetailsController.class.getName()).log(Level.SEVERE, "The task failed", ex);
        }
        done();
        return null;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        onCompletion.accept(ownedPackages);
    }

    private void nest(Map<String, TreeItem> items, List<String> t, String pkg) {
        
    }
}
