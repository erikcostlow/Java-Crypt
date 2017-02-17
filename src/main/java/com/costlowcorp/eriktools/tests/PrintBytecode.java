/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools.tests;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 *
 * @author erik
 */
public class PrintBytecode {

    public static void main(String[] args) throws IOException {
        final Path path = Paths.get("target/classes/com/costlowcorp/eriktools/App.class");
        try (InputStream in = Files.newInputStream(path)) {
            final ClassReader reader = new ClassReader(in);
            final ClassNode n = new ClassNode();
            reader.accept(n, ClassReader.EXPAND_FRAMES);

            final PrintBytecode pb = new PrintBytecode();
            pb.showIt(n);
        }
    }

    private final Map<Label, Integer> labels = new HashMap<>();

    private void showIt(ClassNode n) {
        System.out.println(n.access + " " + n.name);

        for (Object mn : n.methods) {
            blah((MethodNode) mn);
        }
        //n.methods.stream().map(m -> (MethodNode) m).forEach(PrintBytecode::blah);
    }

    private void blah(MethodNode n) {
        labels.clear();

        final AtomicInteger counter = new AtomicInteger();
        n.accept(new MethodVisitor(Opcodes.ASM5) {
            @Override
            public void visitLabel(Label label) {
                labels.put(label, counter.getAndIncrement());
                super.visitLabel(label); //To change body of generated methods, choose Tools | Templates.
            }

        });

        System.out.println(n.name + n.desc);
        final InstructionToStringVisitor v = new InstructionToStringVisitor(Opcodes.ASM5, null);
        for (AbstractInsnNode node = n.instructions.getFirst(); node.getNext() != null; node = node.getNext()) {
            node.accept(v);
            final String s;
            switch (node.getType()) {
                case AbstractInsnNode.FIELD_INSN:
                    s = fieldNode((FieldInsnNode) node);
                    break;
                case AbstractInsnNode.FRAME:
                    s = frame((FrameNode) node);
                    break;
                case AbstractInsnNode.IINC_INSN:
                    s = iinc((IincInsnNode) node);
                    break;
                case AbstractInsnNode.INSN:
                    s = insn((InsnNode) node);
                    break;
                case AbstractInsnNode.INT_INSN:
                    s = intInsn((IntInsnNode) node);
                    break;
                case AbstractInsnNode.INVOKE_DYNAMIC_INSN:
                    s = invokeDyamic((InvokeDynamicInsnNode) node);
                    break;
                case AbstractInsnNode.JUMP_INSN:
                    s = jumpInsn((JumpInsnNode) node);
                    break;
                case AbstractInsnNode.LABEL:
                    s = label((LabelNode) node);
                    break;
                case AbstractInsnNode.LDC_INSN:
                    s = ldc((LdcInsnNode) node);
                    break;
                case AbstractInsnNode.LINE:
                    s = line((LineNumberNode) node);
                    break;
                case AbstractInsnNode.LOOKUPSWITCH_INSN:
                    s = "lookup";
                    break;
                case AbstractInsnNode.METHOD_INSN:
                    s = method((MethodInsnNode) node);
                    break;
                case AbstractInsnNode.MULTIANEWARRAY_INSN:
                    s = multiANewArray((MultiANewArrayInsnNode) node);
                    break;
                case AbstractInsnNode.TABLESWITCH_INSN:
                    s = tableSwitch((TableSwitchInsnNode) node);
                    break;
                case AbstractInsnNode.TYPE_INSN:
                    s = typeInsn((TypeInsnNode) node);
                    break;
                case AbstractInsnNode.VAR_INSN:
                    s = varInsn((VarInsnNode) node);
                    break;
                default:
                    throw new RuntimeException("Unknown type");
            }
            System.out.println(s);
            
        }

        for (Object lv : n.localVariables) {
            variable((LocalVariableNode) lv);
        }

        System.out.println();
    }

    private void variable(LocalVariableNode lv) {
        final Integer startIndex = labels.get(lv.start.getLabel());
        final Integer endIndex = labels.get(lv.end.getLabel());
        System.out.println("  slot " + lv.index + " from L" + startIndex
                + " to L" + endIndex
                + " :: " + lv.desc + " " + lv.name);
    }

    private String fieldNode(FieldInsnNode fieldInsnNode) {
        return "FIELD " + fieldInsnNode.getOpcode() + " " + fieldInsnNode.desc + " " + fieldInsnNode.name;
    }

    private String frame(FrameNode frameNode) {
        return "Frame";
    }

    private String iinc(IincInsnNode iincInsnNode) {
        return "IINC " + iincInsnNode.getOpcode() + " variable " + iincInsnNode.var + " by " + iincInsnNode.incr;
    }

    private String insn(InsnNode insnNode) {
        return "INSN " + insnNode.getOpcode() + " " + insnNode;
    }

    private String intInsn(IntInsnNode intInsnNode) {
        return "IntInsn" + intInsnNode.getOpcode() + " operand " + intInsnNode.operand + intInsnNode;
    }

    private String invokeDyamic(InvokeDynamicInsnNode invokeDynamicInsnNode) {
        return "InvokeDynamic " + invokeDynamicInsnNode.desc + " name " + invokeDynamicInsnNode.name;
    }

    private String jumpInsn(JumpInsnNode jumpInsnNode) {
        final Label dest = jumpInsnNode.label.getLabel();
        return "JUMP to " + labels.get(dest);
    }

    private String label(LabelNode labelNode) {
        return "LABEL " + labels.get(labelNode.getLabel());
    }

    private String ldc(LdcInsnNode ldcInsnNode) {
        return "LDC " + ldcInsnNode.cst;
    }

    private String line(LineNumberNode lineNumberNode) {
        final Label where = lineNumberNode.start.getLabel();
        return "Line " + lineNumberNode.line + " on label " + labels.get(where);
    }

    private String method(MethodInsnNode methodInsnNode) {
        return "METHOD " + methodInsnNode.owner + "." + methodInsnNode.name + '.' + methodInsnNode.desc;
    }

    private String multiANewArray(MultiANewArrayInsnNode multiANewArrayInsnNode) {
        return "MANEWARRAY " + multiANewArrayInsnNode.desc + '[' + multiANewArrayInsnNode.dims + ']';
    }

    private String tableSwitch(TableSwitchInsnNode tableSwitchInsnNode) {
        final int defaultLabel = labels.get(tableSwitchInsnNode.dflt.getLabel());
        return "SWITCH max " + tableSwitchInsnNode.max + ", min " + tableSwitchInsnNode.min
                + " default " + defaultLabel;
    }

    private String typeInsn(TypeInsnNode typeInsnNode) {
        return "TYPE " + typeInsnNode.desc;
    }

    private String varInsn(VarInsnNode varInsnNode) {
        return "VarInsn " + varInsnNode.var;
    }
}
