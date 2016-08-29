/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.wardetails;

import com.costlowcorp.eriktools.back.NestedInputStream;
import com.costlowcorp.eriktools.jardetails.ClassFileMetaVisitor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.testng.annotations.Test;

/**
 *
 * @author ecostlow
 */
public class WarScanner {

    private Map<String, Integer> fileCount = new HashMap<>();
    final Set<String> packages = new TreeSet<>();
    int numberOfFiles=0;

    //@Test
    public void testWar() throws IOException {
        System.out.println("TESTING WAR");
        final Path path = Paths.get("C:\\Users\\ecostlow.ORADEV\\Downloads\\Java\\amc2\\JavaAMC-2_4\\webui\\amcwebui.war");
        try (InputStream in = Files.newInputStream(path);
                ZipInputStream zis = new ZipInputStream(in)) {
            for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
                parse(entry, zis);
            }
        }

        System.out.println("Known packages:");
        packages.forEach(p -> System.out.println("  " + p));
    }

    @Test
    public void testNestedJar() throws IOException {
        System.out.println("TESTING Nested JAR");
        final Path path = Paths.get("C:\\Users\\ecostlow.ORADEV\\Downloads\\Java\\amc2\\JavaAMC-2_4.ear");
        try (InputStream in = Files.newInputStream(path);
                ZipInputStream zis = new ZipInputStream(in)) {
            for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
                parse(entry, zis);
            }
        }
    }

    private void parse(ZipEntry entry, ZipInputStream zis) {
        final String entryName = entry.getName();
        final String entryNameLower = entryName.toLowerCase();
        if (entryName.startsWith("WEB-INF/classes") && entryNameLower.endsWith(".class")) {
            try {
                readClass(entry, zis);
            } catch (IOException ex) {
                Logger.getLogger(WarScanner.class.getName()).log(Level.SEVERE, "Unable to read class " + entryName, ex);
            }
        } else if (entryNameLower.endsWith(".jar") || entryNameLower.endsWith(".zip") || entryNameLower.endsWith(".war")) {
            try {
                readJar(entryName, zis);
            } catch (IOException ex) {
                Logger.getLogger(WarScanner.class.getName()).log(Level.SEVERE, "Unable to read JAR " + entryName, ex);
            }
        }
    }

    private void readClass(ZipEntry entry, ZipInputStream zis) throws IOException {
        final String entryName = entry.getName();
        final byte[] classBytes = readBytes(zis);
        final ClassReader reader = new ClassReader(classBytes);
        final ClassFileMetaVisitor v = new ClassFileMetaVisitor(Opcodes.ASM5, null);
        reader.accept(v, ClassReader.SKIP_FRAMES);
        fileCount.put(v.getLanguage(), fileCount.getOrDefault(v.getLanguage(), 0) + 1);
        if (v.getName().contains("/")) {
            final String currentPackage = v.getName().substring(0, v.getName().lastIndexOf('/'));
            packages.add(currentPackage);
        } else {
            //cannot own empty package
        }

    }

    private void readJar(String name, ZipInputStream zis) throws IOException {
        final String nameLower = name.toLowerCase();
        if (nameLower.endsWith(".class")) {
            final byte[] classBytes = readBytes(zis);
            final ClassReader reader = new ClassReader(classBytes);
            final ClassFileMetaVisitor v = new ClassFileMetaVisitor(Opcodes.ASM5, null);
            reader.accept(v, ClassReader.SKIP_CODE);
            if (v.getName().contains("/")) {
                final String currentPackage = v.getName().substring(0, v.getName().lastIndexOf('/'));
                System.out.println("  Name: " + name + " __package__ " + currentPackage);
            }
        } else if (nameLower.endsWith(".jar") || nameLower.endsWith(".war")) {
            try (InputStream in = new NestedInputStream(zis);
                    ZipInputStream zin2 = new ZipInputStream(in)) {
                for (ZipEntry entry = zin2.getNextEntry(); entry != null; entry = zin2.getNextEntry()) {
                    final String entryName = entry.getName();
                    readJar(name + " -> " + entryName, zin2);
                }
            }
        }
    }

    private static byte[] readBytes(ZipInputStream zis) throws IOException {
        final byte[] bytes = new byte[2048];
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        for (int length = zis.read(bytes); length > 0; length = zis.read(bytes)) {
            bout.write(bytes, 0, length);
        }
        return bout.toByteArray();
    }

}
