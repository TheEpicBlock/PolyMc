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

import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine.VmConfig;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownInteger;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownVoid;
import io.github.theepicblock.polymc.impl.generator.asm.stack.Lambda;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import io.github.theepicblock.polymc.impl.generator.asm.stack.UnknownValue;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class MethodExecutor {
    private Stack<StackEntry> stack = new ObjectArrayList<>();
    private VirtualMachine parent;

    public MethodExecutor(VirtualMachine parent) {
        this.parent = parent;
    }
    
    public StackEntry run(InsnList bytecode) throws VmException {
        var lineNumber = -1;
        for (var inst : bytecode) {
            if (inst instanceof LineNumberNode lineNumberNode) {
                lineNumber = lineNumberNode.line;
                continue;
            }

            try {
                var ret = this.execute(inst);
                if (ret != null) return ret;
            } catch (Exception e) {
                throw new VmException("Error executing code on line "+lineNumber, e);
            }
        }
        return new KnownVoid();
    }

    public @Nullable StackEntry execute(AbstractInsnNode instruction) throws VmException {
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
                stack.push(parent.getConfig().loadStaticField(ctx(), inst));
            }
            case Opcodes.INVOKESTATIC -> {
                var inst = (MethodInsnNode)instruction;
                var descriptor = Type.getType(inst.desc);
                var arguments = new ArrayList<Pair<Type, StackEntry>>();
                for (Type argumentType : descriptor.getArgumentTypes()) {
                    arguments.add(Pair.of(argumentType, stack.pop())); // pop all the arguments
                }
                stack.push(parent.getConfig().invokeStatic(ctx(), inst, arguments)); // push the result
            }
            case Opcodes.INVOKEDYNAMIC -> {
                var inst = (InvokeDynamicInsnNode)instruction;
                stack.push(new Lambda((Handle)inst.bsmArgs[1]));
            }
            case -1 -> {} // This is a virtual opcodes defined by asm, can be safely ignored
            default -> {
                parent.getConfig().handleUnknownInstruction(ctx(), instruction, 0); // TODO
            }
        }
        return null;
    }

    private VirtualMachine.Context ctx() {
        return new VirtualMachine.Context(parent);
    }


    ///

    public static class VmException extends Exception {
        public VmException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
