package io.github.theepicblock.polymc.impl.generator.asm;

import io.github.theepicblock.polymc.impl.generator.asm.stack.*;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public class MethodExecutor {
    private Stack<@NotNull StackEntry> stack = new ObjectArrayList<>();
    private VirtualMachine parent;
    private StackEntry[] localVariables;
    private String methodName;

    public MethodExecutor(VirtualMachine parent, StackEntry[] localVariables, String methodName) {
        this.parent = parent;
        this.localVariables = localVariables;
        this.methodName = methodName;
    }
    
    public @NotNull StackEntry run(InsnList bytecode) throws VmException {
        var lineNumber = -1;
        for (var inst : bytecode) {
            if (inst instanceof LineNumberNode lineNumberNode) {
                lineNumber = lineNumberNode.line;
                continue;
            }

            try {
                var ret = this.execute(inst);
                if (ret != null) return ret;
            } catch (ReturnEarly e) {
                return null;
            } catch (Exception e) {
                throw new VmException("Error executing code on line "+lineNumber+" of "+methodName, e);
            }
        }
        return null;
    }

    public @Nullable StackEntry execute(AbstractInsnNode instruction) throws VmException, ReturnEarly {
        switch (instruction.getOpcode()) {
            case Opcodes.ICONST_0 -> stack.push(new KnownInteger(0));
            case Opcodes.ICONST_1 -> stack.push(new KnownInteger(1));
            case Opcodes.ICONST_2 -> stack.push(new KnownInteger(2));
            case Opcodes.ICONST_3 -> stack.push(new KnownInteger(3));
            case Opcodes.ICONST_4 -> stack.push(new KnownInteger(4));
            case Opcodes.ICONST_5 -> stack.push(new KnownInteger(5));
            case Opcodes.ICONST_M1 -> stack.push(new KnownInteger(-1));
            case Opcodes.FCONST_0 -> stack.push(new KnownFloat(0));
            case Opcodes.FCONST_1 -> stack.push(new KnownFloat(1));
            case Opcodes.FCONST_2 -> stack.push(new KnownFloat(2));
            case Opcodes.ACONST_NULL -> stack.push(new KnownObject(null));
            case Opcodes.GETSTATIC -> {
                var inst = (FieldInsnNode)instruction;
                stack.push(parent.getConfig().loadStaticField(ctx(), inst));
            }
            case Opcodes.PUTSTATIC -> {
                var inst = (FieldInsnNode)instruction;
                parent.getConfig().putStaticField(ctx(), inst, stack.pop());
            }
            case Opcodes.INVOKESTATIC, Opcodes.INVOKEVIRTUAL, Opcodes.INVOKEINTERFACE, Opcodes.INVOKESPECIAL -> {
                var inst = (MethodInsnNode)instruction;
                var descriptor = Type.getType(inst.desc);

                int i = descriptor.getArgumentTypes().length;
                if (inst.getOpcode() != Opcodes.INVOKESTATIC) {
                    i += 1;
                }
                
                var arguments = new StackEntry[i];
                for (Type argumentType : descriptor.getArgumentTypes()) {
                    i--;
                    arguments[i] = stack.pop(); // pop all the arguments
                }
                
                if (inst.getOpcode() != Opcodes.INVOKESTATIC) {
                    arguments[0] = stack.pop(); // objectref
                }
                pushIfNotNull(parent.getConfig().invoke(ctx(), inst, arguments)); // push the result
            }
            case Opcodes.INVOKEDYNAMIC -> {
                var inst = (InvokeDynamicInsnNode)instruction;
                var descriptor = Type.getType(inst.desc);
                int i = descriptor.getArgumentTypes().length;
                var args = new StackEntry[i];
                for (Type argumentType : descriptor.getArgumentTypes()) {
                    i--;
                    args[i] = stack.pop(); // pop all the arguments
                }
                stack.push(new Lambda((Handle)inst.bsmArgs[1], args));
            }
            case Opcodes.NEW -> {
                var inst = (TypeInsnNode)instruction;
                stack.push(parent.getConfig().newObject(ctx(), inst));
            }
            case Opcodes.PUTFIELD -> {
                var inst = (FieldInsnNode)instruction;
                var value = stack.pop();
                var objectRef = stack.pop();
                objectRef.setField(inst.name, value);
            }
            case Opcodes.GETFIELD -> {
                var inst = (FieldInsnNode)instruction;
                stack.push(stack.pop().getField(inst.name));
            }
            case Opcodes.ALOAD, Opcodes.DLOAD, Opcodes.FLOAD, Opcodes.ILOAD, Opcodes.LLOAD -> {
                var variable = localVariables[((VarInsnNode)instruction).var];
                if (variable == null) variable = new UnknownValue("Uninitialized local variable");
                stack.push(variable);
            }
            case Opcodes.ASTORE, Opcodes.DSTORE, Opcodes.FSTORE, Opcodes.ISTORE, Opcodes.LSTORE -> {
                localVariables[((VarInsnNode)instruction).var] = stack.pop();
            }
            case Opcodes.ARETURN, Opcodes.DRETURN, Opcodes.FRETURN, Opcodes.IRETURN, Opcodes.LRETURN -> {
                return stack.pop();
            }
            case Opcodes.RETURN -> { throw new ReturnEarly(); }
            case Opcodes.LDC -> {
                var inst = (LdcInsnNode)instruction;
                stack.push(StackEntry.knownStackValue(inst.cst));
            }
            case Opcodes.CHECKCAST -> {
                var inst = (TypeInsnNode)instruction;
                var obj = stack.pop();
                if (obj instanceof KnownVmObject o) {
                    // Set obj to new type (doesn't do much)
                    obj = new KnownVmObject(parent.getClass(inst.desc), o.fields());
                }
                stack.push(obj);
            }
            case Opcodes.NOP -> {}
            case Opcodes.DUP -> stack.push(stack.top());
            case Opcodes.DUP2 -> {
                var a = stack.peek(1);
                var b = stack.peek(0);
                stack.push(a);
                stack.push(b);
            }
            case Opcodes.POP -> stack.pop();
            case Opcodes.POP2 -> { stack.pop(); stack.pop(); }
            case -1 -> {} // This is a virtual opcodes defined by asm, can be safely ignored
            default -> {
                parent.getConfig().handleUnknownInstruction(ctx(), instruction, 0); // TODO
            }
        }
        return null;
    }

    private void pushIfNotNull(@Nullable StackEntry e) {
        if (e != null) {
            this.stack.push(e);
        }
    }

    private VirtualMachine.Context ctx() {
        return new VirtualMachine.Context(parent);
    }


    ///

    public static class ReturnEarly extends Exception {

    }

    public static class VmException extends Exception {
        public VmException(String message, Throwable cause) {
            super(message, cause);
        }

        public String createFancyErrorMessage() {
            var err = new StringBuilder();
            err.append("[");
            err.append(this.getMessage());
            err.append("]");

            var cause = this.getCause();
            while (cause != null) {
                err.append(" caused by ");
                err.append("[");
                if (cause instanceof VmException e) {
                    err.append(e.getMessage());
                } else {
                    err.append(cause.toString());
                }
                err.append("]");
                cause = cause.getCause();
            }

            return err.toString();
        }
    }
}
