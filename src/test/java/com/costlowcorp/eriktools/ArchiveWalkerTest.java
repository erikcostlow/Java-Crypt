/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools;

import com.costlowcorp.eriktools.back.ArchiveWalker;
import com.costlowcorp.eriktools.back.ArchiveWalkerRecipient;
import com.costlowcorp.eriktools.jardetails.ClassFileMetaVisitor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipInputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.testng.annotations.Test;

/**
 *
 * @author ecostlow
 */
public class ArchiveWalkerTest {

    @Test
    public void testWalkWar() throws IOException {
        final String war = "C:\\apps\\thing.ear";
        final Path path = Paths.get(war);
        final LongAdder fileCount = new LongAdder();
        final Set<String> ownedPackages = new TreeSet<>();
        final StringBuilder sb = new StringBuilder();
        try (InputStream in = Files.newInputStream(path);
                ZipInputStream zin = new ZipInputStream(in)) {

            final ArchiveWalkerRecipient eachFile = (t, entry, u) -> {
                fileCount.increment();
                final String lastName = t.get(t.size() - 1);
                if (lastName.endsWith(".class")) {
                    sb.append("Class File: " + String.join("->", t) + "\n");
                    final ClassFileMetaVisitor v = new ClassFileMetaVisitor(Opcodes.ASM5, null);
                    try {
                        final byte[] bytes = ArchiveWalker.currentEntry(u);
                        final ClassReader reader = new ClassReader(bytes);
                        reader.accept(v, ClassReader.SKIP_CODE);
                        final String packageName = v.getName().contains("/") ? v.getName().substring(0, v.getName().lastIndexOf('/')) : "EmptyPackage";
                        sb.append(" JAVA " + v.getJava() + " PKG " + packageName + "\n");
                    } catch (IOException ex) {
                        Logger.getLogger(ArchiveWalkerTest.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    //System.out.println();
                }
            };
            final ArchiveWalker walker = new ArchiveWalker(war, zin, eachFile);
            
            walker.walk();
        }
        System.out.println(sb.toString());
        System.out.println("There are " + fileCount.longValue() + " files.");
    }
}
