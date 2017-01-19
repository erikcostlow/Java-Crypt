/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import com.costlowcorp.eriktools.scanners.BuildHierarchyTask;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

/**
 *
 * @author ecostlow
 */
public class Blah extends Application {

    public static void main(String[] args) throws MalformedURLException, IOException, InterruptedException, ExecutionException {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final Label go = new Label("lkdjlkjdf");
        final Scene scene = new Scene(go);
        primaryStage.setScene(scene);
        primaryStage.show();

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();
        final GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
        final ExecutorService exec = Executors.newSingleThreadExecutor();
        final int extension = graphModel.addEdgeType("extends");
        final DirectedGraph directedGraph = graphModel.getDirectedGraph();

        if (graphModel.getNodeTable().getColumn("haveCode") == null) {
            graphModel.getNodeTable().addColumn("haveCode", Boolean.class);
        }

        final String javaHome = System.getProperty("java.home");
        final Path rtJar = Paths.get(javaHome, "lib", "rt.jar");
        System.out.println("Processing " + rtJar);
        System.out.println("Building JRE hierarchy");
        final BuildHierarchyTask task1 = new BuildHierarchyTask(rtJar, directedGraph);
        exec.submit(task1);
        System.out.println("Done");

        final Path tomcatPath = Paths.get("/home/erik/devel/apache-tomcat-8.0.27/lib", "tomcat-websocket.jar");
        final BuildHierarchyTask task2 = new BuildHierarchyTask(tomcatPath, directedGraph);
        exec.submit(task2);

        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        //GraphExporter exporter = (GraphExporter) ec.getExporter("gexf");
        DirectedGraph check = task1.get();
        check = task2.get();
        try {
            ec.exportFile(new File("io_gexf.gexf"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        final LongAdder classes = new LongAdder();
        final LongAdder edges = new LongAdder();
        final Set<String> orphans = new HashSet<>();

        final Path mxGraph = Paths.get("mxGraph.js");
        final Path cytoscape = Paths.get("cytoscape.js");
        try (final OutputStream mxOut = Files.newOutputStream(mxGraph);
                final OutputStream cytoOut = Files.newOutputStream(cytoscape);
                final PrintWriter mxPrint = new PrintWriter(mxOut);
            final PrintWriter cytoPrint = new PrintWriter(cytoOut);) {
            
            cytoPrint.println("nodes: [");
            directedGraph.getNodes().iterator().forEachRemaining(node -> {
                final String s = String.valueOf(node.getId());
                mxPrint.println("erik['" + s + "'] = graph.insertVertex(parent, null, '" + s + "', 0, 0, 80, 30);");
                cytoPrint.println("{ data: { id: '" + s + "' } },");
                final AtomicBoolean hasOutgoing = new AtomicBoolean();
                directedGraph.getEdges(node).forEach(edge -> {
                    if (!hasOutgoing.get()) {
                        hasOutgoing.set(edge.getTarget() != node);
                    }
                });

                if (!hasOutgoing.get()) {
                    orphans.add(String.valueOf(node.getId()));
                }
                classes.increment();
            });
            cytoPrint.println("],\nedges: [");
            directedGraph.getEdges().forEach(edge -> {
                final String source = String.valueOf(edge.getSource().getId());
                final String target = String.valueOf(edge.getTarget().getId());
                mxPrint.println("graph.insertEdge(parent, null, '', erik['" + source + "'], erik['" + target + "']);");
                final String cyto = "{ data: { source: '" + source + "', target: '" + target + "' } },";
                cytoPrint.println(cyto);
                edges.increment();
            });
            cytoPrint.println("]");
            System.out.println("done");
        }

        System.out.println();
        System.out.println("------------");
        System.out.println("\tClasses: " + classes.longValue() + "\tEdges: " + edges.longValue());
        System.out.println("\tOrphans:");
        orphans.stream().forEach(o -> System.out.println("\t " + o));
        exec.shutdown();
        System.exit(0);
    }
}
