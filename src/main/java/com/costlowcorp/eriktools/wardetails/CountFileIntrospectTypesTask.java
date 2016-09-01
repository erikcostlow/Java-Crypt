/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.wardetails;

import com.costlowcorp.eriktools.ErikUtils;
import com.costlowcorp.eriktools.back.ArchiveWalker;
import com.costlowcorp.eriktools.back.ArchiveWalkerRecipient;
import com.costlowcorp.eriktools.back.ClassFileUtils;
import com.costlowcorp.eriktools.jardetails.ClassFileMetaVisitor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipInputStream;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import jdk.internal.org.objectweb.asm.Opcodes;
import org.objectweb.asm.ClassReader;

/**
 *
 * @author ecostlow
 */
public class CountFileIntrospectTypesTask extends Task<Void> {
    
    private final Path path;
    private final double totalFiles;
    private final PieChart chart;
    private final Label minJava;
    private final Map<String, Double> chartData;
    
    public CountFileIntrospectTypesTask(String title, Path path, long totalFiles, PieChart chart, Label minJava) {
        updateTitle(title);
        this.path = path;
        this.totalFiles = totalFiles;
        this.chart = chart;
        this.minJava = minJava;
        chartData = new HashMap<>();
        chart.getData().forEach(data -> chartData.put(data.getName(), data.getPieValue()));
    }
    
    @Override
    protected Void call() throws Exception {
        final DoubleAdder fileCount = new DoubleAdder();
        
        try (InputStream in = Files.newInputStream(path);
                ZipInputStream zin = new ZipInputStream(in)) {
            final Double upTotalFiles = totalFiles;
            final ArchiveWalkerRecipient eachFile = (t, entry, u) -> {
                fileCount.add(1.0);
                final double currentClass = fileCount.doubleValue();
                //final String joinedFile = String.join("->", t);
                final String message = String.format("File %1.0f of %1.0f", currentClass, upTotalFiles);
                updateMessage(message);
                updateProgress(currentClass, totalFiles);
                final String justFilename = t.get(t.size() - 1);
                if (entry.isDirectory()) {
                    return;
                }
                final String extension = ErikUtils.getExtension(justFilename);
                String key = "unknown";
                switch (extension) {
                    case "class":
                        try {
                            final byte[] bytes = ArchiveWalker.currentEntry(u);
                            final ClassReader reader = new ClassReader(bytes);
                            final ClassFileMetaVisitor v = new ClassFileMetaVisitor(Opcodes.ASM5, null);
                            reader.accept(v, ClassReader.SKIP_CODE);
                            key = v.getLanguage();
                        } catch (IOException ex) {
                            Logger.getLogger(CountFileIntrospectTypesTask.class.getName()).log(Level.SEVERE, "Unable to read entry " + String.join("->", t), ex);
                            key = "Invalid Java";
                        }
                        break;
                    default:
                        key = extension;
                }
                final double currentCount = chartData.getOrDefault(key, 0.0);
                chartData.put(key, currentCount + 1);
            };
            final ArchiveWalker walker = new ArchiveWalker(path.toString(), zin, eachFile);
            walker.walk();
        } catch (IOException ex) {
            Logger.getLogger(WarDetailsController.class.getName()).log(Level.SEVERE, "The task failed", ex);
        }
        
        return null;
    }
    
    @Override
    protected void failed() {
        super.failed();
        System.out.println("Failed count file");
    }
    
    @Override
    protected void cancelled() {
        super.cancelled();
        System.out.println("Cancelled count file");
    }
    
    @Override
    protected void succeeded() {
        super.succeeded();
        updateChart();
    }
    
    private void updateChart() {
        final Map<String, PieChart.Data> pieData = new HashMap<>();
        chart.getData().forEach(data -> pieData.put(data.getName(), data));
        
        final Optional<String> javaVer = chartData.keySet().stream()
                .filter(name -> name.contains("Java"))
                .max((a, b) -> ClassFileUtils.latestVersion(a, b));
        Platform.runLater(()
                -> {
            javaVer.ifPresent(str -> minJava.setText(str));
            chartData.forEach((key, value) -> {
                if (pieData.containsKey(key)) {
                    pieData.get(key).setPieValue(value);
                } else {
                    final PieChart.Data data = new PieChart.Data(key, value);
                    chart.getData().add(data);
                }
            });
        });
    }
    
}
