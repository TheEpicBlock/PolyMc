package io.github.theepicblock.polymc.impl.generator.asm;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class MethodExecutor {
    private Stack<StackEntry> stack = new ObjectArrayList<>();
    
    public void run(InsnList bytecode) throws VmException {
        var lineNumber = -1;
        for (var inst : bytecode) {
            if (inst instanceof LineNumberNode lineNumberNode) {
                lineNumber = lineNumberNode.line;
                continue;
            }

            try {
                this.execute(inst);
            } catch (Exception e) {
                throw new VmException("Error executing code on line "+lineNumber, e);
            }
        }
    }

    public void execute(AbstractInsnNode instruction) {
        switch (instruction.getOpcode()) {
            case Opcodes.ICONST_0 -> stack.push(new KnownInteger(0));
            case Opcodes.ICONST_1 -> stack.push(new KnownInteger(1));
            case Opcodes.ICONST_2 -> stack.push(new KnownInteger(2));
            case Opcodes.ICONST_3 -> stack.push(new KnownInteger(3));
            case Opcodes.ICONST_4 -> stack.push(new KnownInteger(4));
            case Opcodes.ICONST_5 -> stack.push(new KnownInteger(5));
            case Opcodes.ICONST_M1 -> stack.push(new KnownInteger(-1));
            case Opcodes.GETSTATIC -> {
                var inst = (FieldInsnNode)instruction;
                stack.push(loadStaticField(inst));
            }
            case Opcodes.INVOKESTATIC -> {
                var inst = (MethodInsnNode)instruction;
                var descriptor = Type.getType(inst.desc);
                var arguments = new ArrayList<Pair<Type, StackEntry>>();
                for (Type argumentType : descriptor.getArgumentTypes()) {
                    arguments.add(Pair.of(argumentType, stack.pop())); // pop all the arguments
                }
                stack.push(invokeStatic(inst, arguments)); // push the result
            }
            case Opcodes.INVOKEDYNAMIC -> {
                var inst = (InvokeDynamicInsnNode)instruction;
                stack.push(new Lambda((Handle)inst.bsmArgs[1]));
            }
            case AbstractInsnNode.LINE -> {}
            default -> {

            }
        }
    }

    public StackEntry loadStaticField(FieldInsnNode inst) {
        return new UnknownValue();
    }

    public StackEntry invokeStatic(MethodInsnNode inst, List<Pair<Type, StackEntry>> arguments) {
        // We don't know what this method returns
        return new UnknownValue();
    }

    public void handleUnknownInstruction(AbstractInsnNode instruction, int lineNumber) {
        throw new NotImplementedException("Unimplemented instruction "+instruction.getOpcode());
    }


    ///

    

    public interface StackEntry {

    }

    public static StackEntry knownStackValue(Object o) {
        if (o instanceof Integer i) {
            return new KnownInteger(i);
        }
        return new KnownObject(o);
    }

    public record KnownInteger(int i) implements StackEntry {

    }

    public record KnownObject(Object i) implements StackEntry {
    }

    public record Lambda(Handle method) implements StackEntry {
    }

    public record KnownVoid() implements StackEntry {
        public String toString() {
            return "void";
        }

    }

    public record UnknownValue() implements StackEntry {
        public String toString() {
            return "unknown";
        }
    }

    ///

    public class VmException extends Exception {
        public VmException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
