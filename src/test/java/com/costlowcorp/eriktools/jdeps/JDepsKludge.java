/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.jdeps;

import com.costlowcorp.eriktools.toolentry.JDepsGrabber;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author ecostlow
 */
public class JDepsKludge {
    
    private Path jdkRoot;
    
    @BeforeClass
    public void setUp(){
        final String jdkHome = System.getProperty("java.home");
        jdkRoot = Paths.get(jdkHome).getParent();
    }
    
    @Test
    public void testDoIt(){
        final String s = JDepsGrabber.INSTANCE.run(Paths.get("C:\\Apps\\titan\\lib\\groovy-shaded-asm-1.8.9.jar"));
        System.out.println(s);
    }
    
    public void testAsCommand() throws IOException{
        final Path command = Files.exists(jdkRoot.resolve("bin/jdeps")) ? jdkRoot.resolve("bin/jdeps") : jdkRoot.resolve("bin/jdeps.exe");
        final String n = command.toString();
        System.out.println(n);
        final String[] args = new String[]{n, "-jdkinternals", "C:\\Apps\\titan\\lib\\groovy-shaded-asm-1.8.9.jar"};
        final ProcessBuilder pb = new ProcessBuilder(args);
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
        final Process p = pb.start();
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try(final InputStream in = p.getInputStream();){
            final byte[] bytes = new byte[2048];
            for(int length=in.read(bytes); length>0; length=in.read(bytes)){
                bout.write(bytes, 0, length);
            }
        }
        System.out.println("The output is...");
        System.out.println(bout.toString());
    }

    //@Test
    public void testHook() throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, Throwable {
        final Path tools = jdkRoot.resolve("lib/tools.jar");
        Assert.assertTrue(Files.exists(tools), "No tools");

        final StringWriter sw = new StringWriter();
        final PrintWriter writer = new PrintWriter(sw);

        final URLClassLoader loader = new URLClassLoader(new URL[]{tools.toUri().toURL()});
        final Class<?> clazz = loader.loadClass("com.sun.tools.jdeps.JdepsTask");
        final Lookup lookup = MethodHandles.lookup();
        final Constructor ctr = clazz.getDeclaredConstructor();
        ctr.setAccessible(true);

        final Object obj = ctr.newInstance();
        System.out.println("constructed is " + obj);

        final Method mthd = clazz.getDeclaredMethod("setLog", PrintWriter.class);
        mthd.setAccessible(true);
        mthd.invoke(obj, writer);

        final Method doit = clazz.getDeclaredMethod("run", String[].class);
        doit.setAccessible(true);
        final MethodHandle handle = lookup.unreflect(doit);
        handle.invoke(obj, 
            new String[]{"-jdkinternals", "C:\\Apps\\titan\\lib\\groovy-shaded-asm-1.8.9.jar"}
        );
        System.out.println("OUTPUT");
        System.out.println(sw.toString());
    }
}
