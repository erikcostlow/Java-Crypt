/*
 * Copyright Erik Costlow.
 * Not authorized for use or view by others.
 */
package com.costlowcorp.eriktools.scanners;

import com.costlowcorp.eriktools.back.ArchiveWalker;
import com.costlowcorp.eriktools.back.ArchiveWalkerRecipient;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.zip.ZipInputStream;
import javafx.concurrent.Task;
import org.controlsfx.control.Notifications;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Erik Costlow
 */
public class BuildHierarchyTask extends Task<DirectedGraph> {

    private final Path path;

    private final DirectedGraph directedGraph;

    public BuildHierarchyTask(Path path, DirectedGraph directedGraph) {
        this.path = path;
        this.directedGraph = directedGraph;
    }

    @Override
    protected DirectedGraph call() throws Exception {
        final GraphModel graphModel = directedGraph.getModel();

        final int extension = graphModel.getEdgeType("extends");
        updateMessage("Building hierarchy from " + path);
        try (InputStream in = Files.newInputStream(path);
                ZipInputStream zin = new ZipInputStream(in)) {
            final ArchiveWalkerRecipient eachFile = (t, entry, u) -> {
                if (entry.getName().toLowerCase().endsWith(".class")) {
                    try {
                        processClass(directedGraph, extension, ArchiveWalker.currentEntry(u));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            };

            final ArchiveWalker walker = new ArchiveWalker(path.toString(), zin, eachFile);
            walker.walk();
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*
        updateMessage("Building remaining Java SE/EE hierarchy.");
        final String javaHome = System.getProperty("java.home");
        final Path rtJar = Paths.get(javaHome, "lib", "rt.jar");
        if (Files.exists(rtJar)) {
            try (InputStream in = Files.newInputStream(rtJar);
                    ZipInputStream zin = new ZipInputStream(in)) {
                final ArchiveWalkerRecipient eachFile = (t, entry, u) -> {
                    if (entry.getName().toLowerCase().endsWith(".class")) {
                        try {
                            final byte[] byted = ArchiveWalker.currentEntry(u);
                            final ClassReader reader = new ClassReader(byted);
                            final ClassVisitor v = new ClassVisitor(Opcodes.ASM5) {
                                @Override
                                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                                    final Node node;
                                    final Node check = directedGraph.getNode(name);
                                    if (check != null) {
                                        check.setAttribute("haveCode", true);
                                        node = check;
                                    } else {
                                        node = findOrCreate(directedGraph, name, true);
                                    }
                                    if (superName == null) {
                                        //System.out.println(name + " has null super");
                                        //Typically java/lang/Object
                                    } else {
                                        final Node superNode = findOrCreate(directedGraph, superName, true);
                                        if (directedGraph.getEdge(node, superNode) == null) {
                                            final Edge edge = graphModel.factory().newEdge(node, superNode, extension, true);
                                            directedGraph.addEdge(edge);
                                        }
                                        Arrays.stream(interfaces).forEach(iface -> {
                                            final Node ifaceNode = findOrCreate(directedGraph, iface, true);
                                            final Edge edge = graphModel.factory().newEdge(node, ifaceNode, extension, true);
                                            directedGraph.addEdge(edge);
                                        });
                                    }

                                }
                            };
                            reader.accept(v, ClassReader.SKIP_CODE);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }

                    };

                };
                final ArchiveWalker walker = new ArchiveWalker(zin, eachFile);
                walker.walk();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
        return directedGraph;
    }

    private static void processClass(DirectedGraph directedGraph, int extension, byte[] bytes) throws IOException {
        final ClassReader reader = new ClassReader(bytes);
        final ClassVisitor v = new ClassVisitor(Opcodes.ASM5) {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                final Node self = findOrCreate(directedGraph, name, true);
                if (superName != null) {//No superName for java/lang/Object
                    final Node parent = findOrCreate(directedGraph, superName, false);
                    if (parent != null && directedGraph.getEdge(self, parent) == null) {
                        final GraphModel graphModel = directedGraph.getModel();
                        final Edge edge = graphModel.factory().newEdge(self, parent, extension, true);
                        directedGraph.addEdge(edge);
                    }
                }
                super.visit(version, access, name, signature, superName, interfaces);
            }

        };
        reader.accept(v, ClassReader.SKIP_CODE);
    }

    private static Node findOrCreate(DirectedGraph directedGraph, String id, boolean haveCode) {
        Node retval;
        if (directedGraph.hasNode(id)) {
            retval = directedGraph.getNode(id);
        } else {
            try {
                retval = directedGraph.getModel().factory().newNode(id);
                retval.setLabel(id);
                directedGraph.addNode(retval);
            } catch (Exception e) {
                System.err.println("Error on " + id);
                e.printStackTrace();
                retval = null;
            }
        }
        if (haveCode) {
            retval.setAttribute("haveCode", true);
        }
        return retval;
    }
}
