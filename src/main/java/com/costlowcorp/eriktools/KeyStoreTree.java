package com.costlowcorp.eriktools;

import com.costlowcorp.eriktools.back.JavaFinder;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;

/**
 * Created by ecostlow on 4/24/14.
 */
public class KeyStoreTree extends TreeView<KeyStoreTree.KnownKeyStore> {

    public KeyStoreTree(){
        final TreeItem<KnownKeyStore> root = new TreeItem<>(new KnownKeyStore("All known keystores", null));
        setRoot(root);

        setCellFactory(p -> new KnownKeyStoreTreeCellImpl());

        final TreeItem<KnownKeyStore> java = new TreeItem<>(new KnownKeyStore("Bundled with Java installations", null));
        buildJavaSpecific().stream().filter(known -> !known.getChildren().isEmpty()).forEach(known -> java.getChildren().add(known));
        java.setGraphic(new ImageView("/JavaCup16.png"));
        final TreeItem<KnownKeyStore> specificToUser = new TreeItem<>(new KnownKeyStore("Specific to " + System.getProperty("user.name"), null));
        specificToUser.getChildren().setAll(lookAtUserHome());

        root.getChildren().addAll(java, specificToUser);
        forceExpansion(root);
    }

    private List<TreeItem<KnownKeyStore>> buildJavaSpecific() {
        final TreeItem<KnownKeyStore> cacerts = new TreeItem<>(new KnownKeyStore("Certificate Authorities", null));
        cacerts.setGraphic(new ImageView("com/costlowcorp/eriktools/lock.png"));
        cacerts.setExpanded(true);
        final TreeItem<KnownKeyStore> jssecerts = new TreeItem<>(new KnownKeyStore("JSSE CA Certs", null));
        final JavaFinder finder = new JavaFinder();
        finder.findJavaInstallations().stream().forEach((location) -> {
            final Path jreDir = Paths.get(location);
            final Path dir = Paths.get(location, "lib", "security");
            blah(cacerts, jreDir, dir, "cacerts");
            blah(jssecerts, jreDir, dir, "jssecacerts");
        });

        return Arrays.asList(cacerts, jssecerts);
    }

    private void blah(TreeItem<KnownKeyStore> section, Path jreDir, Path dir, String file){
        final Path check1 = dir.resolve(file);
        if (Files.exists(check1)) {
            final String label = jreDir.getParent().toFile().getName().startsWith("jdk") ? jreDir.getParent().toFile().getName() : jreDir.toFile().getName();
            final TreeItem<KnownKeyStore> sub = new TreeItem<>(new KnownKeyStore(label, check1));
            section.getChildren().add(sub);
        }
    }

    private List<TreeItem<KnownKeyStore>> lookAtUserHome() {
//        final List<TreeItem<KnownKeyStore>> retval = new ArrayList<>();
        final Path userHome = Paths.get(System.getProperty("user.home"));
        final List<Path> files = findFilesMatching(userHome, ".*\\.jks", ".keystore", ".*\\.pkcs12");

        Collections.sort(files);

        final Path securityDir = figureOutStorageDirectory();
        files.addAll(findFilesMatching(securityDir, "trusted.cacerts", "trusted.jssecacerts", "trusted.certs", "trusted.jssecerts"));

        final List<TreeItem<KnownKeyStore>> retval = files.stream().map(file -> new TreeItem<>(new KnownKeyStore(file.toFile().getName(), file))).collect(Collectors.toList());

        return retval;
    }

    private Path figureOutStorageDirectory() {
        final JavaFinder javaFinder = new JavaFinder();
        return javaFinder.findUserSpecificJavaConfigurationArea();
    }

    private List<Path> findFilesMatching(Path location, String... patterns) {
        final List<Path> retval = new ArrayList<>(0);
        final List<Pattern> pattenList = Arrays.stream(patterns).map(s -> Pattern.compile(s)).collect(Collectors.toList());
        final DirectoryStream.Filter<Path> filter = entry -> {
            return pattenList.stream().anyMatch(p -> p.matcher(entry.getFileName().toString()).matches());
            
        };
        try (final DirectoryStream<Path> dir = Files.newDirectoryStream(location, filter)) {
            for (Path file : dir) {
                retval.add(file);
            }
        } catch (IOException e) {
            //Unable to find the path or match. Oh well.
        }
        return retval;
    }

    private static void forceExpansion(TreeItem<KnownKeyStore> item){
        item.setExpanded(true);
        item.getChildren().forEach(KeyStoreTree::forceExpansion);
    }

    protected static class KnownKeyStore{
        private final String term;
        private final Path path;

        public KnownKeyStore(String term, Path path){
            this.term = term;
            this.path = path;
        }

        public String getTerm(){
            return term;
        }

        public Path getPath(){
            return path;
        }
    }

    private class KnownKeyStoreTreeCellImpl extends TreeCell<KnownKeyStore> {
        @Override
        public void updateItem(KnownKeyStore item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            }else{
                setText(item.getTerm());
                setGraphic(getTreeItem().getGraphic());
            }
        }
    }
}
