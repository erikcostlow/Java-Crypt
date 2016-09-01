/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.back;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Recursively walk through archives and view everything.
 *
 * @author ecostlow
 */
public class ArchiveWalker {

    private static final Set<String> ARCHIVE_FORMATS;

    static {
        final Set<String> temp = new HashSet<>();
        temp.add("war");
        temp.add("zip");
        temp.add("jar");
        temp.add("ear");
        ARCHIVE_FORMATS = Collections.unmodifiableSet(temp);
    }

    private final ArchiveWalkerRecipient eachFile;
    private final List<String> names;
    private final ZipInputStream zis;

    /**
     *
     * @param name The original filename; will be included in your paths.
     * @param zis An opened stream that you will close.
     * @param eachFile What to do on each file, will receive a list of the
     * nested archive and the current stream to read.
     */
    public ArchiveWalker(String name, ZipInputStream zis, ArchiveWalkerRecipient eachFile) {
        this.names = new ArrayList<>(1);
        names.add(name);
        this.zis = zis;
        this.eachFile = eachFile;
    }

    public ArchiveWalker(List<String> names, ZipInputStream zis, ArchiveWalkerRecipient eachFile) {
        this.names = names;
        this.zis = zis;
        this.eachFile = eachFile;
    }
    
    /**
     * For a base archive, to not include its filename.
     * @param zis
     * @param eachFile 
     */
    public ArchiveWalker(ZipInputStream zis, ArchiveWalkerRecipient eachFile) {
        this.names = new ArrayList<>(1);
        this.zis = zis;
        this.eachFile = eachFile;
    }

    /**
     * Begin walking through the archive.
     *
     * @throws IOException
     */
    public void walk() throws IOException {
        for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
            final String entryName = entry.getName();
            names.add(entryName);
            eachFile.accept(names, entry, zis);
            final int lastDot = entryName.lastIndexOf('.');
            if (lastDot > 0 && lastDot != entryName.length() - 1) {
                final String extension = entryName.substring(lastDot + 1);
                if (ARCHIVE_FORMATS.contains(extension)) {
                    try (InputStream in = new NestedInputStream(zis);
                            ZipInputStream zis1 = new ZipInputStream(in)) {
                        final ArchiveWalker walker = new ArchiveWalker(names, zis1, eachFile);
                        walker.walk();
                    }
                }
            }
            names.remove(names.size() - 1);
        }
    }

    /**
     * 
     * @param zis
     * @return The current entry's bytes
     * @throws IOException 
     */
    public static byte[] currentEntry(ZipInputStream zis) throws IOException {
        final byte[] readBytes = new byte[2048];
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        for (int length = zis.read(readBytes); length > 0; length = zis.read(readBytes)) {
            bout.write(readBytes, 0, length);
        }
        final byte[] bytes = bout.toByteArray();
        return bytes;
    }
}
