/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.jardetails;

import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 *
 * @author ecostlow
 */
public class URLIdentificationVisitor extends ClassVisitor {

    private final List<IdentifiedURL> identifiedURLs = new ArrayList<>(0);
    private final List<String> prefixes = new ArrayList<>(1);
    private String className;
    private String methodNameAndDesc;

    public URLIdentificationVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    public List<IdentifiedURL> getIdentifiedURLs() {
        return identifiedURLs;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String arg3, String[] arg4) {
        methodNameAndDesc = name + desc;
        final MethodVisitor superV = super.visitMethod(access, name, desc, arg3, arg4); //To change body of generated methods, choose Tools | Templates.
        final URLMethodIdentificationVisitor v = new URLMethodIdentificationVisitor(api, superV, className, methodNameAndDesc, prefixes, identifiedURLs);
        return v;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        final AnnotationVisitor superV = super.visitAnnotation(desc, visible);
        final URLIdentificationAnnotationVisitor v = new URLIdentificationAnnotationVisitor(className, methodNameAndDesc, desc, prefixes, identifiedURLs, api, superV);
        return v;
    }

    private static class URLMethodIdentificationVisitor extends MethodVisitor {

        private final String className;
        private final String methodNameAndDesc;
        private final List<String> prefixes;
        private final List<IdentifiedURL> addToMe;

        public URLMethodIdentificationVisitor(int api, MethodVisitor mv, String className, String methodNameAndDesc, List<String> prefixes, List<IdentifiedURL> addToMe) {
            super(api, mv);
            this.className = className;
            this.methodNameAndDesc = methodNameAndDesc;
            this.prefixes=prefixes;
            this.addToMe = addToMe;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            final AnnotationVisitor superV = super.visitAnnotation(desc, visible);
            final URLIdentificationAnnotationVisitor v = new URLIdentificationAnnotationVisitor(className, methodNameAndDesc, desc, prefixes, addToMe, api, superV);
            //System.out.println(" Annotation " + desc);
            return v;
        }

        @Override
        public AnnotationVisitor visitAnnotationDefault() {
            final AnnotationVisitor superV = super.visitAnnotationDefault();
            final URLIdentificationAnnotationVisitor v = new URLIdentificationAnnotationVisitor(className, methodNameAndDesc, "", prefixes, addToMe, api, superV);
            //System.out.println("AnnotationDefault");
            return v;
        }

    }

    private static class URLIdentificationAnnotationVisitor extends AnnotationVisitor {

        private final String className;
        private final String methodNameAndDesc;
        private final String annotationClass;
        private final List<IdentifiedURL> addToMe;
        private final List<String> prefixes;

        public URLIdentificationAnnotationVisitor(String className, String methodNameAndDesc, String annotationClass, List<String> prefixes, List<IdentifiedURL> addToMe, int api, AnnotationVisitor av) {
            super(api, av);
            this.className = className;
            this.methodNameAndDesc = methodNameAndDesc;
            this.annotationClass = annotationClass;
            this.addToMe = addToMe;
            this.prefixes=prefixes;
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            //System.out.println("  visitArray(" + name + ")");
            return super.visitArray(name);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String desc) {
            //System.out.println("  visitAnnotation(" + name + ", " + desc + ")");
            return super.visitAnnotation(name, desc); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void visitEnum(String name, String desc, String value) {
            //System.out.println("  visitEnum(" + name + ", " + desc + ", " + value + ")");
            super.visitEnum(name, desc, value); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void visit(String name, Object value) {
            //System.out.println("  " + className + "." + methodNameAndDesc + "_-_visit(" + name + ", " + value + ")");
            if ("Ljavax/ws/rs/Path;".equals(annotationClass) && "value".equals(name)) {
                if(methodNameAndDesc==null){
                    //It's on a class
                    final String valueStr = String.valueOf(value);
                    if(!"/".equals(valueStr)){
                        prefixes.add(String.valueOf(valueStr));
                    }
                }else{
                    if(prefixes.isEmpty()){
                    final IdentifiedURL url = new IdentifiedURL(String.valueOf(value), className, methodNameAndDesc, "REST");
                        addToMe.add(url);
                    }else{
                        prefixes.stream()
                                .map(pfx -> pfx + '/' + value)
                                .map(url -> new IdentifiedURL(url, className, methodNameAndDesc, "REST"))
                                .forEach(addToMe::add);
                    }
                }
                //System.out.println("    Found a REST URL: " + value);
            } else if ("Ljavax/servlet/annotation/WebServlet;".equals(annotationClass)) {
                final IdentifiedURL url = new IdentifiedURL(String.valueOf(value), className, methodNameAndDesc, "Servlet");
                addToMe.add(url);
                System.out.println("    Found a WebServlet: " + value);
            } else if ("Ljavax/servlet/annotation/WebFilter;".equals(annotationClass)) {
                final IdentifiedURL url = new IdentifiedURL(String.valueOf(value), className, methodNameAndDesc, "Servlet Filter");
                addToMe.add(url);
                System.out.println("    Found a WebFilter: " + value);
            } else if ("Ljavax/websocket/server/ServerEndpoint;".equals(annotationClass)) {
                final IdentifiedURL url = new IdentifiedURL(String.valueOf(value), className, methodNameAndDesc, "WebSocket Endpoint");
                addToMe.add(url);
            } else if ("Lorg/springframework/web/bind/annotation/RequestMapping;".equals(annotationClass)) {
                final IdentifiedURL url = new IdentifiedURL(String.valueOf(value), className, methodNameAndDesc, "WebSocket Endpoint");
                addToMe.add(url);
            }
            super.visit(name, value);
        }

    }
}
