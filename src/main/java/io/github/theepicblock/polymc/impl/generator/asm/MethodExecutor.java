package io.github.theepicblock.polymc.impl.generator.asm;

import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine.Clazz;
import io.github.theepicblock.polymc.impl.generator.asm.stack.*;
import io.github.theepicblock.polymc.impl.generator.asm.stack.ops.ArrayLength;
import io.github.theepicblock.polymc.impl.generator.asm.stack.ops.BinaryOp;
import io.github.theepicblock.polymc.impl.generator.asm.stack.ops.Cast;
import io.github.theepicblock.polymc.impl.generator.asm.stack.ops.InstanceOf;
import it.unimi.dsi.fastutil.objects.AbstractObjectList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public class MethodExecutor {
    private final AbstractObjectList<@NotNull StackEntry> stack = new ObjectArrayList<>();
    private final VirtualMachine parent;
    private final StackEntry[] localVariables;
    private final String methodName;
    private AbstractInsnNode nextInstruction;
    private InsnList method;
    private Clazz owner;

    public MethodExecutor(VirtualMachine parent, StackEntry[] localVariables, String methodName) {
        this.parent = parent;
        this.localVariables = localVariables;
        this.methodName = methodName;
    }

    public MethodExecutor copy(VirtualMachine newVm) {
        var n = new MethodExecutor(newVm, new StackEntry[this.localVariables.length], this.methodName);
        n.owner = this.owner;
        n.method = this.method;
        n.nextInstruction = this.nextInstruction;
        int i = 0;
        for (var var : this.localVariables) {
            if (var != null) n.localVariables[i] = var.copy();
            i++;
        }

        for (var entry : this.stack) {
            n.stack.push(entry.copy());
        }
        return n;
    }

    public String getName() {
        return methodName;
    }
    
    public void setMethod(InsnList bytecode, Clazz owner) {
        this.owner = owner;
        this.nextInstruction = bytecode.getFirst();
    }

    public void startExecution() throws VmException {
        while (true) {
            if (nextInstruction == null) {
                // We've reached the last instruction, return void
                this.parent.onMethodReturn(null);
                return;
            }
            try {
                var continueExecution = this.execute(this.nextInstruction);
                if (!continueExecution) return;
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

    /**
     * The vm calls this after we've started another methodCall, and that method call has returned
     */
    protected void receiveReturnValue(@Nullable StackEntry returnValue) {
        pushIfNotNull(returnValue);
    }

    public boolean execute(AbstractInsnNode instruction) throws VmException, ReturnEarly {
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
                // We're going to return control back to the vm.
                // The vm will then call receiveReturnValue with the correct return value,
                // and then it'll call startExecution again
                parent.getConfig().invoke(ctx(), this.owner, inst, arguments);
                this.nextInstruction = instruction.getNext();
                return false;
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
                StackEntry length = new ArrayLength(stack.pop());
                if (length.canBeSimplified()) length = length.simplify(this.parent);

                stack.push(length);
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
                this.parent.onMethodReturn(stack.pop());
                return false;
            }
            case Opcodes.RETURN -> {
                this.parent.onMethodReturn(null);
                return false;
            }
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
                return true;
            }
            case Opcodes.LOOKUPSWITCH -> {
                var key = stack.pop();
                if (key instanceof UnknownValue) throw new VmException("Jump(LOOKUPSWITCH) based on unknown variable ("+key+")", null);
                
                var inst = (LookupSwitchInsnNode)instruction;
                var keyValue = key.extractAs(Integer.class);
                var labelIndex = inst.keys.indexOf(keyValue);
                if (labelIndex == -1) {
                    this.nextInstruction = inst.dflt;
                } else {
                    this.nextInstruction = inst.labels.get(labelIndex);
                }
            }
            case Opcodes.IF_ACMPEQ -> {
                var a = stack.pop();
                var b = stack.pop();
                if (a instanceof UnknownValue || b instanceof UnknownValue) throw new VmException("Jump(IF_ACMPEQ) based on unknown variables ("+a+","+b+")", null);
                if (a == b) {
                    this.nextInstruction = ((JumpInsnNode)instruction).label;
                    return true;
                }
            }
            case Opcodes.IF_ACMPNE -> {
                var a = stack.pop();
                var b = stack.pop();
                if (a instanceof UnknownValue || b instanceof UnknownValue) throw new VmException("Jump(IF_ACMPNE) based on unknown variables ("+a+","+b+")", null);
                if (a != b) {
                    this.nextInstruction = ((JumpInsnNode)instruction).label;
                    return true;
                }
            }
            case Opcodes.IFNULL -> {
                var a = stack.pop();
                if (a.canBeSimplified()) a = a.simplify(this.parent);
                if (!a.isConcrete()) {
                    this.parent.getConfig().handleUnknownJump(ctx(), a, null, Opcodes.IFNULL, ((JumpInsnNode)instruction).label);
                    return true;
                }

                if (a instanceof KnownObject o && o.i() == null) {
                    this.nextInstruction = ((JumpInsnNode)instruction).label;
                    return true;
                }
            }
            case Opcodes.IFNONNULL -> {
                var a = stack.pop();
                if (a.canBeSimplified()) a = a.simplify(this.parent);
                if (!a.isConcrete()) {
                    this.parent.getConfig().handleUnknownJump(ctx(), a, null, Opcodes.IFNONNULL, ((JumpInsnNode)instruction).label);
                    return true;
                }

                if (!(a instanceof KnownObject o && o.i() == null)) {
                    this.nextInstruction = ((JumpInsnNode)instruction).label;
                    return true;
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
            case Opcodes.IADD  -> binOp(BinaryOp.Type.INT, BinaryOp.Op.ADD);
            case Opcodes.ISUB  -> binOp(BinaryOp.Type.INT, BinaryOp.Op.SUB);
            case Opcodes.IMUL  -> binOp(BinaryOp.Type.INT, BinaryOp.Op.MUL);
            case Opcodes.IDIV  -> binOp(BinaryOp.Type.INT, BinaryOp.Op.DIV);
            case Opcodes.IAND  -> binOp(BinaryOp.Type.INT, BinaryOp.Op.AND);
            case Opcodes.IOR   -> binOp(BinaryOp.Type.INT, BinaryOp.Op.OR);
            case Opcodes.IXOR  -> binOp(BinaryOp.Type.INT, BinaryOp.Op.XOR);
            case Opcodes.IREM  -> binOp(BinaryOp.Type.INT, BinaryOp.Op.REM);
            case Opcodes.IUSHR -> binOp(BinaryOp.Type.INT, BinaryOp.Op.USHR);
            case Opcodes.ISHR  -> binOp(BinaryOp.Type.INT, BinaryOp.Op.SHR);
            case Opcodes.ISHL  -> binOp(BinaryOp.Type.INT, BinaryOp.Op.SHL);
            case Opcodes.LADD  -> binOp(BinaryOp.Type.LONG, BinaryOp.Op.ADD);
            case Opcodes.LSUB  -> binOp(BinaryOp.Type.LONG, BinaryOp.Op.SUB);
            case Opcodes.LMUL  -> binOp(BinaryOp.Type.LONG, BinaryOp.Op.MUL);
            case Opcodes.LDIV  -> binOp(BinaryOp.Type.LONG, BinaryOp.Op.DIV);
            case Opcodes.LAND  -> binOp(BinaryOp.Type.LONG, BinaryOp.Op.AND);
            case Opcodes.LOR   -> binOp(BinaryOp.Type.LONG, BinaryOp.Op.OR);
            case Opcodes.LXOR  -> binOp(BinaryOp.Type.LONG, BinaryOp.Op.XOR);
            case Opcodes.LREM  -> binOp(BinaryOp.Type.LONG, BinaryOp.Op.REM);
            case Opcodes.LUSHR -> binOp(BinaryOp.Type.LONG, BinaryOp.Op.USHR);
            case Opcodes.LSHR  -> binOp(BinaryOp.Type.LONG, BinaryOp.Op.SHR);
            case Opcodes.LSHL  -> binOp(BinaryOp.Type.LONG, BinaryOp.Op.SHL);
            case Opcodes.FADD  -> binOp(BinaryOp.Type.FLOAT, BinaryOp.Op.ADD);
            case Opcodes.FSUB  -> binOp(BinaryOp.Type.FLOAT, BinaryOp.Op.SUB);
            case Opcodes.FMUL  -> binOp(BinaryOp.Type.FLOAT, BinaryOp.Op.MUL);
            case Opcodes.FDIV  -> binOp(BinaryOp.Type.FLOAT, BinaryOp.Op.DIV);
            case Opcodes.FREM  -> binOp(BinaryOp.Type.FLOAT, BinaryOp.Op.REM);
            case Opcodes.FCMPL -> binOp(BinaryOp.Type.FLOAT, BinaryOp.Op.CMPL);
            case Opcodes.FCMPG -> binOp(BinaryOp.Type.FLOAT, BinaryOp.Op.CMPG);
            case Opcodes.DADD  -> binOp(BinaryOp.Type.DOUBLE, BinaryOp.Op.ADD);
            case Opcodes.DSUB  -> binOp(BinaryOp.Type.DOUBLE, BinaryOp.Op.SUB);
            case Opcodes.DMUL  -> binOp(BinaryOp.Type.DOUBLE, BinaryOp.Op.MUL);
            case Opcodes.DDIV  -> binOp(BinaryOp.Type.DOUBLE, BinaryOp.Op.DIV);
            case Opcodes.DREM  -> binOp(BinaryOp.Type.DOUBLE, BinaryOp.Op.REM);
            case Opcodes.DCMPL -> binOp(BinaryOp.Type.DOUBLE, BinaryOp.Op.CMPL);
            case Opcodes.DCMPG -> binOp(BinaryOp.Type.DOUBLE, BinaryOp.Op.CMPG);
            case Opcodes.IINC -> {
                var inst = (IincInsnNode)instruction;
                var localVar = this.localVariables[inst.var];
                try {
                    var i = localVar.extractAs(Integer.class);
                    this.localVariables[inst.var] = new KnownInteger(i + inst.incr);
                } catch (Exception e) {
                    this.localVariables[inst.var] = new UnknownValue("Can't increment "+localVar+" because of "+e);
                }

            }
            case Opcodes.I2F -> castOp(Cast.Type.INTEGER, Cast.Type.FLOAT);
            case Opcodes.I2L -> castOp(Cast.Type.INTEGER, Cast.Type.LONG);
            case Opcodes.I2D -> castOp(Cast.Type.INTEGER, Cast.Type.DOUBLE);
            case Opcodes.F2I -> castOp(Cast.Type.FLOAT, Cast.Type.INTEGER);
            case Opcodes.F2L -> castOp(Cast.Type.FLOAT, Cast.Type.LONG);
            case Opcodes.F2D -> castOp(Cast.Type.FLOAT, Cast.Type.DOUBLE);
            case Opcodes.L2I -> castOp(Cast.Type.LONG, Cast.Type.INTEGER);
            case Opcodes.L2F -> castOp(Cast.Type.LONG, Cast.Type.FLOAT);
            case Opcodes.L2D -> castOp(Cast.Type.LONG, Cast.Type.DOUBLE);
            case Opcodes.D2I -> castOp(Cast.Type.DOUBLE, Cast.Type.INTEGER);
            case Opcodes.D2F -> castOp(Cast.Type.DOUBLE, Cast.Type.FLOAT);
            case Opcodes.D2L -> castOp(Cast.Type.DOUBLE, Cast.Type.LONG);
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
                // We assume that it can always be cast (exceptions aren't implemented yet)
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
        return true;
    }

    private void castOp(Cast.Type in, Cast.Type out) throws VmException {
        StackEntry c = new Cast(stack.pop(), in, out);
        if (c.canBeSimplified()) c = c.simplify(this.parent);

        stack.push(c);
    }

    private void binOp(BinaryOp.Type type, BinaryOp.Op op) throws VmException {
        var value2 = stack.pop();
        var value1 = stack.pop();
        StackEntry binOp = new BinaryOp(value1, value2, op, type);
        if (binOp.canBeSimplified()) binOp = binOp.simplify(this.parent);

        stack.push(binOp);
    }

    private boolean icmp0(AbstractInsnNode inst, BiIntPredicate predicate) throws VmException {
        var value1 = stack.pop();
        return icmp(inst, value1, new KnownInteger(0), predicate);
    }

    private boolean icmp(AbstractInsnNode inst, BiIntPredicate predicate) throws VmException {
        var value2 = stack.pop();
        var value1 = stack.pop();
        return icmp(inst, value1, value2, predicate);
    }

    private boolean icmp(AbstractInsnNode inst, StackEntry value1, StackEntry value2, BiIntPredicate predicate) throws VmException {
        if (value1 instanceof UnknownValue || value2 instanceof UnknownValue) throw new VmException("Int jump based on unknown variables ("+value1+","+value2+")", null);
        
        var int1 = value1.extractAs(Integer.class);
        var int2 = value2.extractAs(Integer.class);

        if (predicate.compute(int1, int2)) {
            this.nextInstruction = ((JumpInsnNode)inst).label;
        } else {
            this.nextInstruction = inst.getNext();
        }
        return true;
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
    public interface BiIntPredicate {
        boolean compute(int value1, int value2);
    }

    @ApiStatus.Internal
    public AbstractInsnNode inspectCurrentInsn() {
        return this.nextInstruction;
    }

    @ApiStatus.Internal
    public void overrideNextInsn(AbstractInsnNode target) {
        this.nextInstruction = target;
    }
}
