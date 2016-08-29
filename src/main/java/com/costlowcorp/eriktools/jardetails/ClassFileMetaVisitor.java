/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.jardetails;

import com.costlowcorp.eriktools.ErikUtils;
import com.costlowcorp.eriktools.back.ClassFileUtils;
import org.objectweb.asm.ClassVisitor;

/**
 *
 * @author ecostlow
 */
public class ClassFileMetaVisitor extends ClassVisitor {
    private String language;
    private String java;
    private String name;

    public ClassFileMetaVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.java = ClassFileUtils.getJavaVersion(version);
        language=null;
        this.name=name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitSource(String source, String debug) {
        if (source != null && !source.isEmpty()) {
            final String checkExtension = ErikUtils.getExtension(source).toLowerCase();
            if(!"java".equalsIgnoreCase(checkExtension)){
                language = checkExtension;
            }
        }
        super.visitSource(source, debug);
    }

    @Override
    public void visitEnd() {
        language = language==null ? java : language + " (" + java + ")";
        
        super.visitEnd();
    }
    
    /**
     * Examples: groovy (Java 6), Java 8
     * @return The coded language and Java version.
     */
    public String getLanguage(){
        return language;
    }
    
    /**
     * 
     * @return "Java #"
     */
    public String getJava(){
        return java;
    }
    
    public String getName(){
        return name;
    }
    
    public String getPackage(){
        final int lastPos = name.lastIndexOf('/');
        return lastPos>0 ? name.substring(0, lastPos) : "";
    }
}
