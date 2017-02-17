/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.tests;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.TypePath;

/**
 *
 * @author erik
 */
public class InstructionToStringVisitor extends MethodVisitor{
    
    private final StringBuilder sb = new StringBuilder();
    
    public InstructionToStringVisitor(int i, MethodVisitor mv) {
        super(i, mv);
    }
    
    private void reset(){
        if(sb.length()>0){
        sb.delete(0, sb.length());
        }
    }

    @Override
    public void visitEnd() {
        reset();
        sb.append("end");
        super.visitEnd(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void visitMaxs(int i, int i1) {
        super.visitMaxs(i, i1); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void visitLineNumber(int i, Label label) {
        reset();
        sb.append("Line ");
        sb.append(i);
        super.visitLineNumber(i, label); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int i, TypePath tp, Label[] labels, Label[] labels1, int[] ints, String string, boolean bln) {
        return super.visitLocalVariableAnnotation(i, tp, labels, labels1, ints, string, bln); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void visitLocalVariable(String string, String string1, String string2, Label label, Label label1, int i) {
        super.visitLocalVariable(string, string1, string2, label, label1, i); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int i, TypePath tp, String string, boolean bln) {
        return super.visitTryCatchAnnotation(i, tp, string, bln); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void visitTryCatchBlock(Label label, Label label1, Label label2, String string) {
        super.visitTryCatchBlock(label, label1, label2, string); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int i, TypePath tp, String string, boolean bln) {
        reset();
        return super.visitInsnAnnotation(i, tp, string, bln); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void visitMultiANewArrayInsn(String string, int i) {
        reset();
        sb.append("MultiANewArray ");
        sb.append(string);
        sb.append(' ');
        sb.append(i);
        super.visitMultiANewArrayInsn(string, i); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void visitLookupSwitchInsn(Label label, int[] ints, Label[] labels) {
        super.visitLookupSwitchInsn(label, ints, labels); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void visitTableSwitchInsn(int i, int i1, Label label, Label... labels) {
        super.visitTableSwitchInsn(i, i1, label, labels); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void visitIincInsn(int i, int i1) {
        super.visitIincInsn(i, i1); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void visitLdcInsn(Object o) {
        reset();
        sb.append("LDC ");
        sb.append(String.valueOf(o));
        super.visitLdcInsn(o); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void visitLabel(Label label) {
        super.visitLabel(label); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void visitJumpInsn(int i, Label label) {
        super.visitJumpInsn(i, label); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void visitInvokeDynamicInsn(String string, String string1, Handle handle, Object... os) {
        super.visitInvokeDynamicInsn(string, string1, handle, os); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void visitMethodInsn(int i, String string, String string1, String string2, boolean bln) {
        super.visitMethodInsn(i, string, string1, string2, bln); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void visitMethodInsn(int i, String string, String string1, String string2) {
        super.visitMethodInsn(i, string, string1, string2); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void visitFieldInsn(int i, String string, String string1, String string2) {
        super.visitFieldInsn(i, string, string1, string2); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void visitTypeInsn(int i, String string) {
        super.visitTypeInsn(i, string); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void visitVarInsn(int i, int i1) {
        super.visitVarInsn(i, i1); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void visitIntInsn(int i, int i1) {
        super.visitIntInsn(i, i1); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void visitInsn(int i) {
        super.visitInsn(i); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void visitFrame(int i, int i1, Object[] os, int i2, Object[] os1) {
        super.visitFrame(i, i1, os, i2, os1); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void visitCode() {
        super.visitCode(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void visitAttribute(Attribute atrbt) {
        super.visitAttribute(atrbt); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int i, String string, boolean bln) {
        return super.visitParameterAnnotation(i, string, bln); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int i, TypePath tp, String string, boolean bln) {
        return super.visitTypeAnnotation(i, tp, string, bln); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AnnotationVisitor visitAnnotation(String string, boolean bln) {
        return super.visitAnnotation(string, bln); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        return super.visitAnnotationDefault(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void visitParameter(String string, int i) {
        super.visitParameter(string, i); //To change body of generated methods, choose Tools | Templates.
    }

    String getConvertedString() {
        return sb.toString();
    }
    
}
