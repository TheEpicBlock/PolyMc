package io.github.theepicblock.polymc.impl.generator.asm;

import com.google.common.collect.Streams;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import io.github.theepicblock.polymc.impl.generator.asm.stack.*;
import it.unimi.dsi.fastutil.objects.AbstractObjectList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class VirtualMachine {
    private final HashMap<String, Clazz> classes = new HashMap<>();
    private final ClientClassLoader classResolver;
    private VmConfig config;
    private AbstractObjectList<@NotNull MethodExecutor> stack = new ObjectArrayList<>();
    /**
     * This is here to map from runtime names back to obfuscated, which is needed to
     * resolve methods in the client jar.
     */
    public final Mapping mappings;
    private StackEntry lastReturnedValue;

    public VirtualMachine(ClientClassLoader classResolver, VmConfig config) {
        this.classResolver = classResolver;
        this.config = config;
        this.mappings = Mapping.runtimeToObfFromClasspath();
    }

    public void changeConfig(VmConfig config) {
        this.config = config;
    }

    public VirtualMachine copy() {
        var n = new VirtualMachine(this.classResolver, config);
        n.lastReturnedValue = this.lastReturnedValue;
        for (var meth : this.stack) {
            n.stack.add(meth.copy());
        }
        return n;
    }

    public AbstractObjectList<@NotNull MethodExecutor> switchStack(@Nullable AbstractObjectList<@NotNull MethodExecutor> newStack) {
        if (newStack == null) {
            newStack = new ObjectArrayList<>();
        }
        var oldStack = this.stack;
        this.stack = newStack;
        return oldStack;
    }

    @ApiStatus.Internal
    public MethodExecutor inspectRunningMethod() {
        return this.stack.top();
    }

    /**
     * Should be called by the {@link MethodExecutor} that's currently on top of the stack
     */
    @ApiStatus.Internal
    protected void onMethodReturn(@Nullable StackEntry returnValue) {
        this.lastReturnedValue = returnValue;
        stack.pop(); // The top method returned a value now, so it shall be yeeted

        if (!stack.isEmpty()) {
            stack.top().receiveReturnValue(returnValue);
        }
    }

    public void addMethodToStack(Lambda lambda, StackEntry[] arguments) throws VmException {
        var method = lambda.method();
        var clazz = getClass(method.getOwner());
        if (method.getTag() == 8) {
            var newO = new StackEntry[] { new KnownVmObject(clazz, new HashMap<>()) };
            StackEntry[] args = Streams
                    .concat(Arrays.stream(newO), Arrays.stream(arguments), Arrays.stream(lambda.extraArguments()))
                    .toArray(StackEntry[]::new);
            addMethodToStack(clazz, method.getName(), method.getDesc(), args);
            onMethodReturn(newO[0]);
        } else {
            StackEntry[] args = Streams.concat(Arrays.stream(arguments), Arrays.stream(lambda.extraArguments()))
                    .toArray(StackEntry[]::new);
            addMethodToStack(clazz, method.getName(), method.getDesc(), args);
        }
    }

    public void addMethodToStack(String clazz, String method, String desc) throws VmException {
        var clazzNode = getClass(clazz);
        addMethodToStack(clazzNode, method, desc, null);
    }

    public void addMethodToStack(Clazz clazz, String method, String desc, @Nullable StackEntry[] arguments)
            throws VmException {
        var meth = AsmUtils.getMethod(clazz.node, method, desc, mappings);
        if (meth == null) {
            throw new VmException(
                    "Couldn't find method `" + method + "` with desc `" + desc + "` in class `" + clazz.node.name + "`",
                    null);
        }
        addMethodToStack(new MethodRef(clazz, meth), arguments);
    }

    public void addMethodToStack(MethodRef methRef, @Nullable StackEntry[] arguments) throws VmException {
        var meth = methRef.meth();
        var a = arguments == null ? -1 : arguments.length;
        var localVariables = new StackEntry[Math.max(meth.maxLocals, a)];

        // Fill in arguments
        if (arguments != null) {
            int i = 0;
            for (var pair : arguments) {
                localVariables[i] = pair;
                i++;
            }
        }

        var executor = new MethodExecutor(this, localVariables,
                methRef.clazz().node.name + "#" + meth.name + meth.desc);
        stack.push(executor);
        executor.setMethod(meth.instructions, methRef.clazz());
    }

    public StackEntry runToCompletion() throws VmException {
        while (!this.stack.isEmpty()) {
            try {
                this.stack.top().startExecution();
            } catch (VmException e) {
                var handledVal = this.config.onVmError(this.stack.top().getName(), this.stack.top().getName().endsWith("v"), e);
                this.onMethodReturn(handledVal);
            }
        }
        return this.lastReturnedValue;
    }

    public Clazz getClass(String name) throws VmException {
        var clazz = this.classes.get(name);
        if (clazz == null) {

            // Load class using ASM
            var node = new ClassNode(Opcodes.ASM9);
            try {
                var stream = classResolver.getClass(name, this.mappings);
                new ClassReader(stream).accept(node, 0);
            } catch (IOException e) {
                throw new VmException("Error loading " + name, e);
            }
            clazz = new Clazz(node);
            this.classes.put(name, clazz);
        }
        return clazz;
    }

    public VmConfig getConfig() {
        return config;
    }

    public ClientClassLoader getClassResolver() {
        return classResolver;
    }

    public void ensureClinit(Clazz node) throws VmException {
        // This isn't spec-compliant
        if (!node.hasInitted) {
            node.hasInitted = true;
            var stack = this.switchStack(null); // Run this in a new, fresh state
            addMethodToStack(node, "<clinit>", "()V", null);
            runToCompletion();
            this.switchStack(stack); // Restore old state
        }
    }

    /**
     * @param currentClass The class the method is in
     * @return null if the {@code objectRef} is of an unknown type, and the method
     *         can't be resolved due to that.
     */
    public @Nullable MethodRef resolveMethod(Clazz currentClass, MethodInsnNode inst,
            @Nullable StackEntry objectRef) throws VmException {
        switch (inst.getOpcode()) {
            case Opcodes.INVOKESTATIC -> {
                var clazz = this.getClass(inst.owner);
                var method = AsmUtils.getMethod(clazz.node, inst.name, inst.desc, mappings);
                // This is a hard-error. Static methods shouldn't be hard to find and
                // something's wrong here. So no returning null in this case
                if (method == null)
                    throw new VmException("Couldn't find static method " + inst.name + inst.desc + " in " + inst.owner,
                            null);
                return new MethodRef(clazz, method);
            }
            case Opcodes.INVOKEINTERFACE, Opcodes.INVOKEVIRTUAL, Opcodes.INVOKESPECIAL -> {
                if (objectRef == null)
                    throw new IllegalArgumentException(
                            "objectRef can't be null for method invocation (" + inst.getOpcode() + ")");

                // Find the root class from which to start looking for the method
                Clazz rootClass = switch (inst.getOpcode()) {
                    case Opcodes.INVOKESPECIAL -> {
                        // See
                        // https://docs.oracle.com/javase/specs/jvms/se10/html/jvms-6.html#jvms-6.5.invokespecial
                        if (!inst.name.startsWith("<init>") &&
                                !AsmUtils.hasFlag(currentClass.node.access, Opcodes.ACC_INTERFACE) &&
                                AsmUtils.hasFlag(currentClass.node.access, Opcodes.ACC_SUPER)) {
                            yield this.getClass(currentClass.node.superName);
                        } else {
                            yield this.getClass(inst.owner);
                        }
                    }
                    case Opcodes.INVOKEINTERFACE, Opcodes.INVOKEVIRTUAL -> {
                        if (objectRef instanceof KnownObject o) {
                            yield this.getClass(o.i().getClass().getCanonicalName());
                        } else if (objectRef instanceof KnownVmObject o) {
                            yield o.type();
                        } else {
                            yield null;
                        }
                    }
                    default -> throw new IllegalStateException();
                };

                if (rootClass == null)
                    return null;

                var clazz = rootClass;
                while (true) {
                    var method = AsmUtils.getMethod(clazz.node, inst.name, inst.desc, mappings);
                    if (method != null) {
                        if ((method.access & Opcodes.ACC_ABSTRACT) != 0) {
                            throw new VmException("Method " + inst.name + inst.desc + " in "
                                    + rootClass.node.name + " (" + inst.getOpcode() + ", " + inst.owner + ") resolved to an abstract method", null);
                        }
                        return new MethodRef(clazz, method);
                    } else {
                        // Check super class
                        if (clazz.node.superName == null)
                            throw new VmException("Can't find method " + inst.name + inst.desc + " in "
                                    + rootClass.node.name + " (" + inst.getOpcode() + ", " + inst.owner + ")", null);
                        clazz = this.getClass(clazz.node.superName);
                    }
                }
            }
        }
        return null;
    }

    public interface VmConfig {
        default @NotNull StackEntry loadStaticField(Context ctx, FieldInsnNode inst) throws VmException {
            var clazz = ctx.machine.getClass(inst.owner);
            ctx.machine.ensureClinit(clazz);
            return clazz.getStatic(inst.name);
        }

        default StackEntry onVmError(String method, boolean returnsVoid, VmException e) throws VmException {
            throw e;
        }

        default void putStaticField(Context ctx, FieldInsnNode inst, StackEntry value) throws VmException {
            var clazz = ctx.machine.getClass(inst.owner);
            ctx.machine.ensureClinit(clazz);
            clazz.setStatic(inst.name, value);
        }

        /**
         * @param currentClass Class the invocation is coming from
         * @param arguments    If this instruction is not invokeStatic, the first element
         *                     will represent the object this was called on.
         */
        default void invoke(Context ctx, Clazz currentClass, MethodInsnNode inst, StackEntry[] arguments)
                throws VmException {
            if (inst.owner.equals(Type.getInternalName(LoggerFactory.class))) {
                ret(ctx, new UnknownValue("Refusing to invoke LoggerFactory for optimization reasons"));
                return;
            }
            // Hardcoded functions
            if (inst.owner.equals(Type.getInternalName(String.class)) && arguments[0] instanceof KnownObject o && o.i() instanceof String str) {
                // String is quite strict with its fields, we can't reflect into them
                // This causes the jvm to be unable to execute many methods inside String
                if (inst.name.equals("isEmpty")) {
                    ret(ctx, new KnownInteger(str.isEmpty()));
                    return;
                }
                if (inst.name.equals("length")) {
                    ret(ctx, new KnownInteger(str.length()));
                    return;
                }
                if (inst.name.equals("charAt")) {
                    if (arguments[1].canBeSimplified()) arguments[1].simplify(ctx.machine());
                    if (arguments[1].isConcrete()) {
                        ret(ctx, new KnownInteger(str.charAt(arguments[1].extractAs(Integer.class))));
                        return;
                    }
                }
                if (inst.name.equals("indexOf") && inst.desc.equals("(II)v")) {
                    if (arguments[1].canBeSimplified()) arguments[1].simplify(ctx.machine());
                    if (arguments[2].canBeSimplified()) arguments[2].simplify(ctx.machine());
                    if (arguments[1].isConcrete() && arguments[2].isConcrete()) {
                        ret(ctx, new KnownInteger(str.indexOf(arguments[1].extractAs(Integer.class), arguments[2].extractAs(Integer.class))));
                        return;
                    }
                }
            }
            if (inst.owner.equals(Type.getInternalName(Objects.class)) && inst.name.equals("requireNonNull")) {
                ret(ctx, arguments[0]);
                return;
            }
            if (inst.owner.equals(Type.getInternalName(Arrays.class)) && inst.name.equals("copyOf")
                    && inst.desc.equals("([Ljava/lang/Object;I)[Ljava/lang/Object;")
                    && arguments[0] instanceof KnownArray a) {
                ret(ctx, new KnownArray(Arrays.copyOf(a.data(), arguments[1].extractAs(Integer.class))));
                return;
            }

            if (inst.getOpcode() != Opcodes.INVOKESTATIC) {
                // TODO figure out what to do with this
                if (arguments[0].canBeSimplified()) arguments[0] = arguments[0].simplify(ctx.machine());
            }

            // I'm pretty sure we're supposed to run clinit at this point, but let's delay
            // it as much as possible
            var method = ctx.machine().resolveMethod(currentClass, inst, Util.first(arguments));
            if (method == null) {
                // This method is part of Object and wouldn't be found otherwise
                if (inst.name.equals("hashCode") && inst.desc.equals("()I")
                        && Util.first(arguments) instanceof KnownObject obj) {
                    ret(ctx, new KnownInteger(obj.i().hashCode())); // It's no problem to do this in the outer vm
                    return;
                }

                // Can't be resolved, return an unknown value
                ret(ctx, Type.getReturnType(inst.desc) == Type.VOID_TYPE ? null
                        : new UnknownValue("Can't resolve method " + inst.owner + "#" + inst.name + inst.desc));
                return;
            }
            ctx.machine.addMethodToStack(method, arguments);
        }

        default @NotNull StackEntry newObject(Context ctx, TypeInsnNode inst) throws VmException {
            var clazz = ctx.machine.getClass(inst.desc);
            return new KnownVmObject(clazz, new HashMap<>());
        }

        default void handleUnknownInstruction(Context ctx, AbstractInsnNode instruction, int lineNumber)
                throws VmException {
            throw new NotImplementedException("Unimplemented instruction " + instruction.getOpcode());
        }

        default void handleUnknownJump(Context ctx, StackEntry compA, @Nullable StackEntry compB, int opcode, LabelNode target) throws VmException {
            throw new VmException("Jump on unknown value(s)", null);
        }

        /**
         * Just a convenience method. Probably shouldn't override this one since it doesn't receive all returns
         */
        default void ret(Context ctx, StackEntry e) {
            // We're going to create a "method" and act like that one returned the value
            ctx.machine.stack.push(new MethodExecutor(ctx.machine(), new StackEntry[0], "Fake method"));
            ctx.machine.onMethodReturn(e);
        }
    }

    public record Context(VirtualMachine machine) {
    }

    public record MethodRef(@NotNull Clazz clazz, @NotNull MethodNode meth) {
    };

    public static class Clazz {
        private ClassNode node;
        private boolean hasInitted;
        private Map<String, @NotNull StackEntry> staticFields = new HashMap<>();

        public Clazz(ClassNode node) {
            this.node = node;
        }

        public @NotNull StackEntry getStatic(String name) {
            return staticFields.getOrDefault(name, new UnknownValue("Unknown static value"));
        }

        public void setStatic(String name, @NotNull StackEntry v) {
            staticFields.put(name, v);
        }

        @Override
        public String toString() {
            return node.name;
        }

        public ClassNode getNode() {
            return node;
        }
    }
}