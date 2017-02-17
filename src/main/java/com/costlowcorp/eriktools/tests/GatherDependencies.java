/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.tests;

import com.costlowcorp.eriktools.back.ArchiveWalker;
import com.costlowcorp.eriktools.back.ArchiveWalkerRecipient;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author erik
 */
public class GatherDependencies {

    public static void main(String[] args) throws IOException {
        final Path path = Paths.get("/Users/erik/Downloads/turboWAR/vmturbo.war");
        try (InputStream in = Files.newInputStream(path);
                final ZipInputStream zis = new ZipInputStream(in)) {
            final Handler h = new Handler();
            final ArchiveWalker walker = new ArchiveWalker(zis, h);
            walker.walk();
            h.getEarliest().entrySet().stream()
                    .sorted(( o1,  o2) -> o1.getKey().compareTo(o2.getKey()))
                    .forEach(e -> System.out.println(e.getKey() + " was packaged on" + e.getValue()));
        }

    }

    private static class Handler implements ArchiveWalkerRecipient {

        private final Map<String, LocalDateTime> earliest = new HashMap<>();
        
        Map<String, LocalDateTime> getEarliest(){
            return Collections.unmodifiableMap(earliest);
        }
        
        @Override
        public void accept(List<String> archiveNames, ZipEntry currentEntry, ZipInputStream zin) {
            final FileTime lastModified = currentEntry.getLastModifiedTime();
            final Instant instant = lastModified.toInstant();
            final LocalDateTime when = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            storeLastModified(archiveNames, when);
            
        }

        private void storeLastModified(List<String> archiveNames, LocalDateTime when) {
            final StringBuilder sb = new StringBuilder(archiveNames.get(0));
            checkAge(sb.toString(), when);
            for(int i=1; i<archiveNames.size()-1; i++){
                sb.append("->");
                sb.append(archiveNames.get(i));
                checkAge(sb.toString(), when);
            }
        }

        private void checkAge(String toString, LocalDateTime when) {
            if(!earliest.containsKey(toString)){
                earliest.put(toString, when);
            }else if(earliest.get(toString).isAfter(when)){
                earliest.put(toString, when);
            }
        }

    }
}
