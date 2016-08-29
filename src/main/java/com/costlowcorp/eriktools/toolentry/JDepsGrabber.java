/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.toolentry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ecostlow
 */
public enum JDepsGrabber {
    INSTANCE;

    private boolean preferDirect = true;
    private final Path command;

    private Constructor ctr;
    private MethodHandle setLog;
    private MethodHandle run;

    private JDepsGrabber() {
        final String jdkHome = System.getProperty("java.home");
        final Path jdkRoot = Paths.get(jdkHome).getParent();
        command = Files.exists(jdkRoot.resolve("bin/jdeps")) ? jdkRoot.resolve("bin/jdeps") : jdkRoot.resolve("bin/jdeps.exe");

        try {
            prep(jdkRoot);
        } catch (ClassNotFoundException | MalformedURLException | NoSuchMethodException | IllegalAccessException ex) {
            preferDirect();
        }
    }

    private void prep(Path jdkRoot) throws ClassNotFoundException, MalformedURLException, NoSuchMethodException, IllegalAccessException {
        final Path tools = jdkRoot.resolve("lib/tools.jar");
        final URLClassLoader loader = new URLClassLoader(new URL[]{tools.toUri().toURL()});
        final Class<?> clazz = loader.loadClass("com.sun.tools.jdeps.JdepsTask");
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        ctr = clazz.getDeclaredConstructor();
        ctr.setAccessible(true);

        final Method mthd = clazz.getDeclaredMethod("setLog", PrintWriter.class);
        mthd.setAccessible(true);
        setLog = lookup.unreflect(mthd);

        final Method doit = clazz.getDeclaredMethod("run", String[].class);
        doit.setAccessible(true);
        run = lookup.unreflect(doit);
    }

    private void preferDirect() {
        preferDirect = false;
        ctr = null;
        setLog = null;
        run = null;
    }

    public String run(Path jarFile) {
        String retval;

        if (preferDirect) {
            try {
                retval = doDirect(jarFile);
            } catch (Throwable ex) {
                preferDirect();
                retval = doByCall(jarFile);
            }
        } else {
            retval = doByCall(jarFile);
        }

        return retval;
    }

    private String doDirect(Path jarFile) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, Throwable {
        final StringWriter sw = new StringWriter();
        final PrintWriter writer = new PrintWriter(sw);
        final Object obj = ctr.newInstance();
        setLog.invoke(obj, writer);
        final String name = jarFile.toString();
        run.invoke(obj,
                new String[]{"-jdkinternals", name}
        );
        final String retval = sw.toString();
        return retval;
    }

    private String doByCall(Path jarFile) {
        final String[] args = new String[]{command.toString(), "-jdkinternals", jarFile.toString()};
        final ProcessBuilder pb = new ProcessBuilder(args);
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
        String retval;
        try {
            final Process p = pb.start();
            final ByteArrayOutputStream bout = new ByteArrayOutputStream();
            try (final InputStream in = p.getInputStream();) {
                final byte[] bytes = new byte[2048];
                for (int length = in.read(bytes); length > 0; length = in.read(bytes)) {
                    bout.write(bytes, 0, length);
                }
            } catch (IOException ex) {
                Logger.getLogger(JDepsGrabber.class.getName()).log(Level.SEVERE, "Unable to use jdeps", ex);
            }
            retval = bout.toString();
        } catch (IOException e) {
            retval = "ERROR: Unable to call " + command;
        }
        
        return retval;
    }
}
