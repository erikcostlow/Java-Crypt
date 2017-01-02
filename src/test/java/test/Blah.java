/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
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
import org.openide.util.Lookup;

/**
 *
 * @author ecostlow
 */
public class Blah {

    private static GraphModel graphModel;
    private static DirectedGraph directedGraph;
    
    private static int extension;

    public static void main(String[] args) throws MalformedURLException, IOException {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        //Get a graph model - it exists because we have a workspace
        graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
        extension = graphModel.addEdgeType("extends");
        directedGraph = graphModel.getDirectedGraph();

        final Path path = Paths.get("C:\\Apps\\apache-maven-3.2.1\\lib\\guava-14.0.1.jar");
        try (InputStream in = Files.newInputStream(path);
                ZipInputStream zis = new ZipInputStream(in)) {
            for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
                //System.out.println(entry.getName());
                if (entry.getName().toLowerCase().endsWith(".class")) {
                    processClass(entry, zis);
                }
            }
        }
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        //GraphExporter exporter = (GraphExporter) ec.getExporter("gexf");
        try {
            ec.exportFile(new File("io_gexf.gexf"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void processClass(ZipEntry entry, ZipInputStream zis) throws IOException {
        final ClassReader reader = new ClassReader(zis);
        final ClassVisitor v = new ClassVisitor(Opcodes.ASM5) {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                final StringBuilder sb = new StringBuilder(entry.getName());
                final String printName;
                if (interfaces != null && interfaces.length > 0) {
                    printName = superName + ", " + String.join(", ", interfaces);
                } else {
                    printName = superName;
                }
                System.out.println(name + " : " + printName);
                final Node self = findOrCreate(name);
                final Node parent = findOrCreate(superName);
                if (directedGraph.getEdge(self, parent) == null) {
                    final Edge edge = graphModel.factory().newEdge(self, parent, extension, true);
                    directedGraph.addEdge(edge);
                }
                super.visit(version, access, name, signature, superName, interfaces);
            }

        };
        reader.accept(v, ClassReader.SKIP_CODE);
    }

    private static Node findOrCreate(String id) {
        if (directedGraph.hasNode(id)) {
            return directedGraph.getNode(id);
        }
        Node node = graphModel.factory().newNode(id);
        node.setLabel(id);
        directedGraph.addNode(node);
        return node;
    }
}
