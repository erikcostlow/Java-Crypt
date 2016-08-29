/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.jdeps;

import com.costlowcorp.eriktools.toolentry.JavaPGrabber;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.testng.annotations.Test;

/**
 *
 * @author ecostlow
 */
public class JavaPKludge {
    
    @Test
    public void testRun(){
        final Path jarFile = Paths.get("C:\\Program Files\\Java\\jdk1.8.0_102\\lib\\tools.jar");
        final String r = JavaPGrabber.INSTANCE.run(jarFile, "com.sun.tools.javap.JavapTask");
        System.out.println(r);
    }
}
