/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.wardetails;

import com.costlowcorp.eriktools.back.DoWithNestedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.openide.util.Exceptions;
import org.testng.annotations.Test;

/**
 *
 * @author erik
 */
public class NestedFileReader {
    
    @Test
    public void testReadFile() throws IOException{
        final Path path = Paths.get("/Users/erik/Downloads/turboWAR/vmturbo.war");
        //final String name = "WEB-INF/lib/org.jinteropdeps-2.0.11-SNAPSHOT.jar->rpc/pdu/RequestCoPdu$FragmentIterator.class";
        final String name = "WEB-INF/lib/org.jinteropdeps-2.0.11-SNAPSHOT.jar->META-INF/maven/org.jinterop/org.jinteropdeps/pom.xml";
        final String[] split = name.split("->");
        try(InputStream in = Files.newInputStream(path)){
            final DoWithNestedInputStream doer = new DoWithNestedInputStream(split, this::handle);
            doer.blah(in);
        }
    }
    
    private void handle(InputStream work){
        try(InputStream in = work){
            final ByteArrayOutputStream bout = new ByteArrayOutputStream();
            final byte[] bytes = new byte[2048];
            for(int length=in.read(bytes); length>0; length = in.read(bytes)){
                bout.write(bytes, 0, length);
            }
            final String s = new String(bout.toByteArray());
            System.out.println(s);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
