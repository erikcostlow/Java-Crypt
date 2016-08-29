/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.jardetails;

import com.costlowcorp.eriktools.ErikUtils;
import com.costlowcorp.eriktools.classdetails.ClassDetailsController;
import com.costlowcorp.eriktools.code.XMLEditor;
import com.costlowcorp.eriktools.files.image.ImageShowerController;
import com.costlowcorp.eriktools.toolentry.JavaPGrabber;
import com.costlowcorp.fx.utils.UIUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicLong;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.objectweb.asm.ClassReader;
import static org.objectweb.asm.Opcodes.ASM5;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.TraceClassVisitor;

/**
 * FXML Controller class
 *
 * @author ecostlow
 */
public class JarNavigationController implements Initializable {

    @FXML
    private TreeView<JarEntryTreeViewer> jarNavigation;

    @FXML
    private AnchorPane detailsPane;

    private File file;

    private WeakReference<Node> detailsNode;

    private ClassDetailsController classDetails;
    private Node classDetailsNode;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        final FXMLLoader classDetailsLoader = UIUtils.load(ClassDetailsController.class);
        this.classDetails = classDetailsLoader.getController();
        this.classDetailsNode = classDetailsLoader.getRoot();
        UIUtils.setAnchors(classDetailsNode, 0, 0, 0, 0);

        jarNavigation.setCellFactory((TreeView<JarEntryTreeViewer> param) -> new TextFieldTreeCellImpl());
        jarNavigation.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends TreeItem<JarEntryTreeViewer>> observable, TreeItem<JarEntryTreeViewer> oldValue, TreeItem<JarEntryTreeViewer> newValue) -> {
            if (newValue == null) {
                return;
            }
            if (newValue == jarNavigation.getRoot() || newValue.getValue().actualFileProperty().getValue().endsWith("/")) {
                if (detailsNode.get() == null) {
                    getDetails();
                } else {
                    detailsPane.getChildren().setAll(detailsNode.get());
                }
            } else {
                final String filename = newValue.getValue().actualFileProperty().get();
                final String extension = ErikUtils.getExtension(filename);
                final Node end;
                switch (extension) {
                    case "class":
                        end = classDetailsNode;
                        new Thread(() -> readClass(filename)).start();
                        break;
                    case "png":
                    case "jpg":
                    case "jpeg":
                    case "gif":
                        final FXMLLoader loader = UIUtils.load(ImageShowerController.class);
                        final ImageShowerController imageCtrl = loader.getController();
                        try (ZipFile zip = new ZipFile(file)) {
                            final ZipEntry entry = zip.getEntry(filename);
                            final InputStream in = zip.getInputStream(entry);
                            final Image image = new Image(in);
                            imageCtrl.populateWith(image);
                        } catch (IOException ex) {
                            Logger.getLogger(JarNavigationController.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        end = loader.getRoot();
                        break;
                    case "properties":
                    case "mf":
                    case "txt":
                    case "css":
                        final TextArea txt = new TextArea();
                        end = txt;
                        new Thread(() -> plainText(filename, txt)).start();
                        break;
                    case "xml":
                    case "fxml":
                        end = XMLEditor.make(readPlainText(filename));
                        break;
                    default:
                        end = new Label("Unable to load reader for " + filename);
                }
                UIUtils.setAnchors(end, 0, 0, 0, 0);
                Platform.runLater(() -> detailsPane.getChildren().setAll(end));

            }
        });
    }

    public void populateWith(File file) {
        this.file = file;
        new Thread(() -> populate(file)).start();
        getDetails();
    }

    private void populate(File file) {
        try (ZipFile zip = new ZipFile(file)) {
            final Image folder = new Image("/folder.png");

            final TreeItem<JarEntryTreeViewer> root = new TreeItem<>(new JarEntryTreeViewer(zip.getName()), new ImageView(folder));

            final Map<String, TreeItem> map = new HashMap<>();
            final List<TreeItem<JarEntryTreeViewer>> innerClassList = new ArrayList<>();
            final AtomicLong innerClassDepth = new AtomicLong();
            zip.stream().sorted((first, second) -> first.getName().compareToIgnoreCase(second.getName()))
                    .forEach(entry -> {
                        final String entryName = entry.getName();
                        final String parentDir = parentDir(entry);

                        final TreeItem<JarEntryTreeViewer> item = new TreeItem<>(new JarEntryTreeViewer(entryName));
                        if (entry.isDirectory()) {
                            item.setGraphic(new ImageView(folder));
                        }

                        final boolean parentFound = map.containsKey(parentDir);
                        if (parentFound) {
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
            root.setExpanded(true);

            Platform.runLater(() -> jarNavigation.setRoot(root));
        } catch (IOException ex) {
            Logger.getLogger(JarNavigationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static String parentDir(ZipEntry entry) {
        final String name = entry.getName();
        if (!name.contains("/")) {
            return name;
        }
        final int end = entry.isDirectory() ? name.lastIndexOf('/', name.length() - 2) : name.lastIndexOf('/');
        return end < 0 ? "/" : name.substring(0, end + 1);

    }

    private void getDetails() {
        new Thread(() -> {
            final FXMLLoader loader = UIUtils.load(JarDetailsController.class);
            final JarDetailsController controller = loader.getController();
            //controller.populateWith(file.);
            controller.populateWith(file);
            final Node details = loader.getRoot();
            this.detailsNode = new WeakReference<>(details);
            UIUtils.setAnchors(details, 0, 0, 0, 0);
            Platform.runLater(() -> detailsPane.getChildren().setAll(details));
        }).start();
    }

    private void readClass(String filename) {
        try (JarFile jar = new JarFile(this.file)) {
            final JarEntry entry = jar.getJarEntry(filename);
            final ClassNode node = new ClassNode(ASM5);
            final InputStream in = jar.getInputStream(entry);
            final ClassReader reader = new ClassReader(in);
            //reader.accept(node, ClassReader.EXPAND_FRAMES);
            
            final Printer asmifier = new ASMifier();
            //final InputStream in2 = jar.getInputStream(entry);
            final ByteArrayOutputStream bout = new ByteArrayOutputStream();
            
            final TraceClassVisitor tracer = new TraceClassVisitor(node, asmifier, new PrintWriter(bout));
            reader.accept(tracer, ClassReader.EXPAND_FRAMES);
            final String className = filename.replace('/', '.').substring(0, filename.length()-".class".length());
            final String javap = JavaPGrabber.INSTANCE.run(file.toPath(), className);
            Platform.runLater(() -> {
                classDetails.populateClassSummary(node.name, filename, node.sourceFile, node.version, node.superName, node.interfaces);
                classDetails.populateAsmifierTab(bout.toString());
                classDetails.populateJavaPTab(javap);
            });
        } catch (IOException ex) {
            Logger.getLogger(JarNavigationController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void plainText(String filename, TextArea node) {
        final String txt = readPlainText(filename);
        Platform.runLater(() -> node.setText(txt));
    }
    
    private String readPlainText(String filename) {
        try (JarFile jar = new JarFile(this.file)) {
            final JarEntry entry = jar.getJarEntry(filename);
            final byte[] bytes = new byte[2048];
            final ByteArrayOutputStream bout = new ByteArrayOutputStream();
            try(InputStream in = jar.getInputStream(entry)){
                for(int length=in.read(bytes); length>0; length = in.read(bytes)){
                    bout.write(bytes, 0, length);
                }
            }
            return bout.toString();
        } catch (IOException ex) {
            Logger.getLogger(JarNavigationController.class.getName()).log(Level.SEVERE, null, ex);
            return ex.getMessage();
        }
    }
}
