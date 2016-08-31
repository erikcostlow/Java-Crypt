/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.wardetails;

import com.costlowcorp.eriktools.back.ArchiveWalker;
import com.costlowcorp.eriktools.back.ArchiveWalkerRecipient;
import com.costlowcorp.eriktools.jardetails.ClassFileMetaVisitor;
import com.costlowcorp.eriktools.jardetails.IdentifiedURL;
import com.costlowcorp.eriktools.jardetails.URLIdentificationVisitor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
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
public class BasicBytecodeScan extends Task<Void> {

    private final Path path;
    private final double maxClasses;
    private final Consumer<Set<String>> onCompletion;
    private final Set<String> ownedPackages = new TreeSet<>();
    private final Map<String, TreeItem> items = new HashMap<>();
    private TreeItem root;
    private TreeItem<IdentifiedURL> urlRoot;
    private final Consumer<TreeItem> updateTree;
    private final Consumer<TreeItem<IdentifiedURL>> urlUpdate;

    public BasicBytecodeScan(String title, Path path, long maxClasses, Consumer<Set<String>> onCompletion, Consumer<TreeItem> updateTree, Consumer<TreeItem<IdentifiedURL>> urlUpdate) {
        updateTitle(title);
        this.path = path;
        this.maxClasses = maxClasses;
        this.onCompletion = onCompletion;
        this.updateTree = updateTree;
        this.urlUpdate=urlUpdate;
    }

    @Override
    protected Void call() throws Exception {
        final LongAdder classCount = new LongAdder();
        final Double upMaxClasses = maxClasses;
        urlRoot = new TreeItem<>(new IdentifiedURL("/", "", "", ""));
        urlRoot.setExpanded(true);

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
                        final URLIdentificationVisitor urlIdentificationVisitor = new URLIdentificationVisitor(Opcodes.ASM5, null);
                        final ClassFileMetaVisitor v = new ClassFileMetaVisitor(Opcodes.ASM5, urlIdentificationVisitor);
                        final ClassReader reader = new ClassReader(bytes);
                        reader.accept(v, ClassReader.SKIP_CODE);
                        final String pkg = v.getPackage();
                        final boolean foundJar = t.stream().filter(archive -> archive.toLowerCase().contains(".jar")).findAny().isPresent();
                        if (!foundJar && !"".equals(pkg)) {
                            ownedPackages.add(pkg);
                        }
                        final List<String> names = new ArrayList<>(t.size());
                        names.addAll(t);
                        names.set(names.size() - 1, pkg);
                        if (!items.containsKey(makeName(names))) {
                            final TreeItem<ArchiveOwnershipEntry> item = nest(items, names);
                            if (entry.getTime() != 0) {
                                item.getValue().setWhenMade(new Date(entry.getTime()));
                            }
                        }
                        
                        if(!urlIdentificationVisitor.getIdentifiedURLs().isEmpty()){
                            urlIdentificationVisitor.getIdentifiedURLs().stream()
                                    .map(id -> new TreeItem<>(id))
                                    .forEach(urlRoot.getChildren()::add);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(BasicBytecodeScan.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            };
            final ArchiveWalker walker = new ArchiveWalker(path.toString(), zin, eachFile);
            walker.walk();
        } catch (Exception ex) {
            Logger.getLogger(WarDetailsController.class.getName()).log(Level.SEVERE, "The task failed", ex);
        }
        done();
        return null;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        onCompletion.accept(ownedPackages);
        updateTree.accept(root);
        urlUpdate.accept(urlRoot);
    }

    private TreeItem nest(Map<String, TreeItem> items, List<String> names) {
        final String currentName = makeName(names);
        if (items.isEmpty() && names.size() == 1) {
            final TreeItem item = new TreeItem(new ArchiveOwnershipEntry(names.get(names.size() - 1), new Date()));
            items.put(currentName, item);
            root = item;
            item.setExpanded(true);
            return item;
        } else if (items.containsKey(currentName)) {
            return items.get(currentName);
        } else {
            final TreeItem item = new TreeItem(new ArchiveOwnershipEntry(names.get(names.size() - 1), new Date()));
            item.setExpanded(true);
            final List<String> subList = names.subList(0, names.size() - 1);
            nest(items, subList).getChildren().add(item);
            items.put(currentName, item);
            return item;
        }
    }

    private static String makeName(List<String> names) {
        return String.join("->", names);
    }
}
