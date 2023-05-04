package io.github.theepicblock.polymc.impl.generator.asm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine.Clazz;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownArray;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownFloat;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownInteger;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownLong;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownObject;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownVmObject;
import io.github.theepicblock.polymc.impl.generator.asm.stack.Lambda;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import io.github.theepicblock.polymc.impl.generator.asm.stack.UnknownValue;
import io.github.theepicblock.polymc.impl.generator.asm.stack.ops.Cast;
import io.github.theepicblock.polymc.impl.generator.asm.stack.ops.InstanceOf;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class MethodExecutor {
    private Stack<@NotNull StackEntry> stack = new ObjectArrayList<>();
    private VirtualMachine parent;
    private StackEntry[] localVariables;
    private String methodName;
    private AbstractInsnNode nextInstruction;
    private InsnList method;
    private Clazz owner;

    public MethodExecutor(VirtualMachine parent, StackEntry[] localVariables, String methodName) {
        this.parent = parent;
        this.localVariables = localVariables;
        this.methodName = methodName;
    }
    
    public @NotNull StackEntry run(InsnList bytecode, Clazz owner) throws VmException {
        this.owner = owner;
        this.nextInstruction = bytecode.getFirst();
        while (true) {
            if (nextInstruction == null) {
                return null; // We've reached the last instruction
            }
            try {
                var ret = this.execute(this.nextInstruction);
                if (ret != null) return ret;
            } catch (ReturnEarly e) {
                return null;
            } catch (Exception e) {
                throw new VmException("Error executing code on line "+this.getLineNumber()+" of "+methodName, e);
            }
        }
    }

    public int getLineNumber() {
        var node = this.nextInstruction;
        while (node != null) {
            if (node instanceof LineNumberNode line) {
                return line.line;
            }
            node = node.getPrevious();
        }
        return -1;
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
            case Opcodes.SIPUSH, Opcodes.BIPUSH -> stack.push(new KnownInteger(((IntInsnNode)instruction).operand));
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
                pushIfNotNull(parent.getConfig().invoke(ctx(), this.owner, inst, arguments)); // push the result
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
            case Opcodes.ANEWARRAY, Opcodes.NEWARRAY -> {
                var count = stack.pop();
                if (count instanceof KnownInteger i) {
                    stack.push(KnownArray.withLength(i.i()));
                } else {
                    throw new VmException("Tried to construct array of length "+count, null);
                }
            }
            case Opcodes.ARRAYLENGTH -> {
                var arrayref = stack.pop();
                try {
                    var length = arrayref.cast(Object[].class).length;
                    stack.push(new KnownInteger(length));
                } catch (Exception e) {
                    stack.push(new UnknownValue("Can't calculate length "+e));
                }
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
            case Opcodes.AALOAD, Opcodes.BALOAD, Opcodes.CALOAD, Opcodes.DALOAD, Opcodes.FALOAD, Opcodes.IALOAD, Opcodes.LALOAD, Opcodes.SALOAD -> {
                var index = stack.pop();
                var array = stack.pop();
                if (index instanceof KnownInteger i) {
                    stack.push(array.arrayAccess(i.i()));
                } else {
                    throw new VmException("Tried to acces array index of "+index, null);
                }
            }
            case Opcodes.AASTORE, Opcodes.BASTORE, Opcodes.CASTORE, Opcodes.DASTORE, Opcodes.FASTORE, Opcodes.IASTORE, Opcodes.LASTORE, Opcodes.SASTORE -> {
                var value = stack.pop();
                var index = stack.pop();
                var array = stack.pop();
                if (index instanceof KnownInteger i) {
                    array.arraySet(i.i(), value);
                } else {
                    throw new VmException("Tried to acces array index of "+index, null);
                }
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
            case Opcodes.RET -> {
                var inst = (VarInsnNode)instruction;
                var lvt = this.localVariables[inst.var];
                if (lvt instanceof KnownInteger i) {
                    // TODO: does this get method actually return the right thing?
                    this.nextInstruction = this.method.get(i.i());
                } else {
                    throw new VmException("Couldn't ret to "+lvt, null);
                }
            }
            case Opcodes.GOTO -> {
                this.nextInstruction = ((JumpInsnNode)instruction).label;
                return null;
            }
            case Opcodes.LOOKUPSWITCH -> {
                var key = stack.pop();
                if (key instanceof UnknownValue) throw new VmException("Jump(IFNULL) based on unknown variable ("+key+")", null);
                
                var inst = (LookupSwitchInsnNode)instruction;
                var keyValue = key.cast(Integer.class);
                var labelIndex = inst.keys.indexOf(keyValue);
                if (labelIndex == -1) {
                    this.nextInstruction = inst.dflt;
                }
                this.nextInstruction = inst.labels.get(labelIndex);
            }
            case Opcodes.IF_ACMPEQ -> {
                var a = stack.pop();
                var b = stack.pop();
                if (a instanceof UnknownValue || b instanceof UnknownValue) throw new VmException("Jump(IF_ACMPEQ) based on unknown variables ("+a+","+b+")", null);
                if (a == b) {
                    this.nextInstruction = ((JumpInsnNode)instruction).label;
                    return null;
                }
            }
            case Opcodes.IF_ACMPNE -> {
                var a = stack.pop();
                var b = stack.pop();
                if (a instanceof UnknownValue || b instanceof UnknownValue) throw new VmException("Jump(IF_ACMPNE) based on unknown variables ("+a+","+b+")", null);
                if (a != b) {
                    this.nextInstruction = ((JumpInsnNode)instruction).label;
                    return null;
                }
            }
            case Opcodes.IFNULL -> {
                var a = stack.pop();
                if (a instanceof UnknownValue) throw new VmException("Jump(IFNULL) based on unknown variable ("+a+")", null);
                if (a instanceof KnownObject o && o.i() == null) {
                    this.nextInstruction = ((JumpInsnNode)instruction).label;
                    return null;
                }
            }
            case Opcodes.IFNONNULL -> {
                var a = stack.pop();
                if (a instanceof UnknownValue) throw new VmException("Jump(IFNULL) based on unknown variable ("+a+")", null);
                if (!(a instanceof KnownObject o && o.i() == null)) {
                    this.nextInstruction = ((JumpInsnNode)instruction).label;
                    return null;
                }
            }
            case Opcodes.IF_ICMPEQ -> { return icmp(instruction, (a,b) -> a == b); }
            case Opcodes.IF_ICMPNE -> { return icmp(instruction, (a,b) -> a != b); }
            case Opcodes.IF_ICMPLT -> { return icmp(instruction, (a,b) -> a < b); }
            case Opcodes.IF_ICMPLE -> { return icmp(instruction, (a,b) -> a <= b); }
            case Opcodes.IF_ICMPGT -> { return icmp(instruction, (a,b) -> a > b); }
            case Opcodes.IF_ICMPGE -> { return icmp(instruction, (a,b) -> a >= b); }
            case Opcodes.IFEQ -> { return icmp0(instruction, (a,b) -> a == b); }
            case Opcodes.IFNE -> { return icmp0(instruction, (a,b) -> a != b); }
            case Opcodes.IFLT -> { return icmp0(instruction, (a,b) -> a < b); }
            case Opcodes.IFLE -> { return icmp0(instruction, (a,b) -> a <= b); }
            case Opcodes.IFGT -> { return icmp0(instruction, (a,b) -> a > b); }
            case Opcodes.IFGE -> { return icmp0(instruction, (a,b) -> a >= b); }
            case Opcodes.IADD  -> intOp((a,b) -> a+b);
            case Opcodes.ISUB  -> intOp((a,b) -> a-b);
            case Opcodes.IMUL  -> intOp((a,b) -> a*b);
            case Opcodes.IDIV  -> intOp((a,b) -> a/b);
            case Opcodes.IAND  -> intOp((a,b) -> a&b);
            case Opcodes.IOR   -> intOp((a,b) -> a|b);
            case Opcodes.IXOR  -> intOp((a,b) -> a^b);
            case Opcodes.IREM  -> intOp((a,b) -> a%b);
            case Opcodes.IUSHR -> intOp((a,b) -> a>>>b);
            case Opcodes.ISHR  -> intOp((a,b) -> a>>b);
            case Opcodes.ISHL  -> intOp((a,b) -> a<<b);
            case Opcodes.LADD  -> longOp((a,b) -> a+b);
            case Opcodes.LSUB  -> longOp((a,b) -> a-b);
            case Opcodes.LMUL  -> longOp((a,b) -> a*b);
            case Opcodes.LDIV  -> longOp((a,b) -> a/b);
            case Opcodes.LAND  -> longOp((a,b) -> a&b);
            case Opcodes.LOR   -> longOp((a,b) -> a|b);
            case Opcodes.LXOR  -> longOp((a,b) -> a^b);
            case Opcodes.LREM  -> longOp((a,b) -> a%b);
            case Opcodes.LUSHR -> longOp((a,b) -> a>>>b);
            case Opcodes.LSHR  -> longOp((a,b) -> a>>b);
            case Opcodes.LSHL  -> longOp((a,b) -> a<<b);
            case Opcodes.FADD  -> floatOp((a,b) -> a+b);
            case Opcodes.FSUB  -> floatOp((a,b) -> a-b);
            case Opcodes.FMUL  -> floatOp((a,b) -> a*b);
            case Opcodes.FDIV  -> floatOp((a,b) -> a/b);
            case Opcodes.FREM  -> floatOp((a,b) -> a%b);
            case Opcodes.IINC -> {
                var inst = (IincInsnNode)instruction;
                var localVar = this.localVariables[inst.var];
                try {
                    var i = localVar.cast(Integer.class);
                    this.localVariables[inst.var] = new KnownInteger(i + inst.incr);
                } catch (Exception e) {
                    this.localVariables[inst.var] = new UnknownValue("Can't increment "+localVar+" because of "+e);
                }

            }
            case Opcodes.I2F -> castOp(Cast.Type.INTEGER, Cast.Type.FLOAT);
            case Opcodes.I2L -> castOp(Cast.Type.INTEGER, Cast.Type.LONG);
            case Opcodes.F2I -> castOp(Cast.Type.FLOAT, Cast.Type.INTEGER);
            case Opcodes.F2L -> castOp(Cast.Type.FLOAT, Cast.Type.LONG);
            case Opcodes.L2I -> castOp(Cast.Type.LONG, Cast.Type.INTEGER);
            case Opcodes.L2F -> castOp(Cast.Type.LONG, Cast.Type.FLOAT);
            case Opcodes.LDC -> {
                var inst = (LdcInsnNode)instruction;
                stack.push(StackEntry.knownStackValue(inst.cst));
            }
            case Opcodes.INSTANCEOF -> {
                var inst = (TypeInsnNode)instruction;
                var objectref = stack.pop();
                try {
                    stack.push(new KnownInteger(InstanceOf.toInt(objectref, inst.desc)));
                } catch (VmException e) {
                    stack.push(new UnknownValue(e));
                }
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
            case Opcodes.DUP_X1 -> {
                var value1 = stack.pop();
                var value2 = stack.pop();
                stack.push(value1);
                stack.push(value2);
                stack.push(value1);
            }
            case Opcodes.DUP2 -> {
                var value2 = stack.peek(1);
                var value1 = stack.peek(0);
                stack.push(value2);
                stack.push(value1);
            }
            case Opcodes.POP -> stack.pop();
            case Opcodes.POP2 -> { stack.pop(); stack.pop(); }
            case -1 -> {} // This is a virtual opcodes defined by asm, can be safely ignored
            default -> {
                parent.getConfig().handleUnknownInstruction(ctx(), instruction, 0); // TODO
            }
        }

        this.nextInstruction = instruction.getNext();
        return null;
    }

    private void castOp(Cast.Type in, Cast.Type out) {
        var c = new Cast(stack.pop(), in, out).tryResolveOnce();
        if (c instanceof Cast) {
            c = new UnknownValue(c);
        }
        stack.push(c);
    }

    private void floatOp(BiFloatToFloat function) {
        var value2 = stack.pop();
        var value1 = stack.pop();
        if (value1 instanceof UnknownValue || value2 instanceof UnknownValue) stack.push(new UnknownValue("integer op unknown variables ("+value1+","+value2+")"));

        var int1 = value1.cast(Float.class);
        var int2 = value2.cast(Float.class);
        stack.push(new KnownFloat(function.compute(int1, int2)));
    }

    private void longOp(BiLongToLong function) {
        var value2 = stack.pop();
        var value1 = stack.pop();
        if (value1 instanceof UnknownValue || value2 instanceof UnknownValue) stack.push(new UnknownValue("integer op unknown variables ("+value1+","+value2+")"));

        var int1 = value1.cast(Long.class);
        var int2 = value2.cast(Long.class);
        stack.push(new KnownLong(function.compute(int1, int2)));
    }

    private void intOp(BiIntToInt function) {
        var value2 = stack.pop();
        var value1 = stack.pop();
        if (value1 instanceof UnknownValue || value2 instanceof UnknownValue) stack.push(new UnknownValue("integer op unknown variables ("+value1+","+value2+")"));

        var int1 = value1.cast(Integer.class);
        var int2 = value2.cast(Integer.class);
        stack.push(new KnownInteger(function.compute(int1, int2)));
    }

    private StackEntry icmp0(AbstractInsnNode inst, BiIntPredicate predicate) throws VmException {
        var value1 = stack.pop();
        return icmp(inst, value1, new KnownInteger(0), predicate);
    }

    private StackEntry icmp(AbstractInsnNode inst, BiIntPredicate predicate) throws VmException {
        var value2 = stack.pop();
        var value1 = stack.pop();
        return icmp(inst, value1, value2, predicate);
    }

    private StackEntry icmp(AbstractInsnNode inst, StackEntry value1, StackEntry value2, BiIntPredicate predicate) throws VmException {
        if (value1 instanceof UnknownValue || value2 instanceof UnknownValue) throw new VmException("Int jump based on unknown variables ("+value1+","+value2+")", null);
        
        var int1 = value1.cast(Integer.class);
        var int2 = value2.cast(Integer.class);

        if (predicate.compute(int1, int2)) {
            this.nextInstruction = ((JumpInsnNode)inst).label;
        } else {
            this.nextInstruction = inst.getNext();
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

        @Override
        public String toString() {
            return createFancyErrorMessage();
        }
    }

    @FunctionalInterface
    private static interface BiIntPredicate {
        boolean compute(int value1, int value2);
    }

    @FunctionalInterface
    private static interface BiIntToInt {
        int compute(int value1, int value2);
    }

    @FunctionalInterface
    private static interface BiLongToLong {
        long compute(long value1, long value2);
    }

    @FunctionalInterface
    private static interface BiFloatToFloat {
        float compute(float value1, float value2);
    }
}
