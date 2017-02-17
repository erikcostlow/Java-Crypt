/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.files;

import com.costlowcorp.eriktools.ErikUtils;
import com.costlowcorp.eriktools.back.DoWithNestedInputStream;
import com.costlowcorp.eriktools.classdetails.ClassDetailsController;
import com.costlowcorp.eriktools.code.XMLEditor;
import com.costlowcorp.eriktools.files.image.ImageShowerController;
import com.costlowcorp.eriktools.jardetails.JarNavigationController;
import com.costlowcorp.eriktools.toolentry.JavaPGrabber;
import com.costlowcorp.fx.utils.UIUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.objectweb.asm.ClassReader;
import static org.objectweb.asm.Opcodes.ASM5;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;
import org.openide.util.Exceptions;

/**
 *
 * @author erik
 */
public class FileTypeTask extends Task<Node> {

    private final String[] filename;

    private final Pane inHere;

    private Node end;

    private final Path path;

    public FileTypeTask(Path path, String[] filename, Pane inHere) {
        this.filename = filename;
        this.inHere = inHere;
        this.path = path;
    }

    @Override
    protected Node call() throws Exception {
        final String extension = ErikUtils.getExtension(filename[filename.length - 1]);
        switch (extension) {
            case "png":
            case "jpg":
            case "jpeg":
            case "gif":
                final FXMLLoader loader = UIUtils.load(ImageShowerController.class);
                final ImageShowerController imageCtrl = loader.getController();
                final Consumer<InputStream> populateImage = (InputStream i) -> {
                    final Image image = new Image(i);
                    imageCtrl.populateWith(image);
                };

                blah(populateImage);
                end = loader.getRoot();
                break;
            case "properties":
            case "mf":
            case "txt":
            case "css":
                final TextArea txt = new TextArea();
                end = txt;
                final Consumer<InputStream> readText = i -> {
                    final ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    final byte[] bytes = new byte[2048];
                    try {
                        for (int length = i.read(bytes); length > 0; length = i.read(bytes)) {
                            bout.write(bytes, 0, length);
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    txt.setText(bout.toString());
                };
                blah(readText);
                break;
            case "xml":
            case "fxml":
                final Consumer<InputStream> readXml = i -> {
                    final ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    final byte[] bytes = new byte[2048];
                    try {
                        for (int length = i.read(bytes); length > 0; length = i.read(bytes)) {
                            bout.write(bytes, 0, length);
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    end = XMLEditor.make(bout.toString());
                };
                blah(readXml);
                break;
            case "class":
                final FXMLLoader classDetailsLoader = UIUtils.load(ClassDetailsController.class);
                final ClassDetailsController classDetails = classDetailsLoader.getController();
                end = classDetailsLoader.getRoot();
                final ClassNode node = new ClassNode(ASM5);
                final Consumer<InputStream> readClass = i -> {
                    try {
                        final ClassReader reader = new ClassReader(i);
                        final Printer asmifier = new Textifier();////ASMifier();
                        final ByteArrayOutputStream bout = new ByteArrayOutputStream();

                        final TraceClassVisitor tracer = new TraceClassVisitor(node, asmifier, new PrintWriter(bout));
                        reader.accept(tracer, ClassReader.EXPAND_FRAMES);
                        final String tempName = filename[filename.length - 1];
                        final String className = tempName.replace('/', '.').substring(0, tempName.length() - ".class".length());

                        classDetails.populateClassSummary(node.name, tempName, node.sourceFile, node.version, node.superName, node.interfaces);
                        classDetails.populateAsmifierTab(bout.toString());

                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                };
                blah(readClass);
                UIUtils.setAnchors(end, 0, 0, 0, 0);
                break;
            default:
                end = new Label("Unable to load reader for " + String.join("->", filename));
        }
        UIUtils.setAnchors(end, 0, 0, 0, 0);

        return end;
    }

    private void blah(Consumer<InputStream> doMe) {
        try (InputStream in = Files.newInputStream(path)) {
            final DoWithNestedInputStream doer = new DoWithNestedInputStream(filename, doMe);
            doer.blah(in);
        } catch (IOException ex) {
            Logger.getLogger(JarNavigationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        if (end != null) {
            Platform.runLater(() -> inHere.getChildren().setAll(end));
        }
    }

}
