package io.github.theepicblock.polymc.impl.generator.asm;

import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownObject;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownVmObject;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import io.github.theepicblock.polymc.impl.generator.asm.stack.UnknownValue;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class VirtualMachine {
    private final HashMap<String, Clazz> classes = new HashMap<>();
    private final ClientClassLoader classResolver;
    private final VmConfig config;
    private final Stack<@NotNull MethodExecutor> stack = new ObjectArrayList<>();

    public VirtualMachine(ClientClassLoader classResolver, VmConfig config) {
        this.classResolver = classResolver;
        this.config = config;
    }

    public StackEntry runMethod(String clazz, String method, String desc) throws VmException {
        var clazzNode = getClass(clazz);
        return runMethod(clazzNode, method, desc, null);
    }

    public StackEntry runMethod(Clazz clazz, String method, String desc, @Nullable StackEntry[] arguments) throws VmException {
        var meth = AsmUtils.getMethod(clazz.node, method, desc);
        if (meth == null) {
            throw new VmException("Couldn't find method `"+method+"` with desc `"+desc+"` in class `"+clazz.node.name+"`", null);
        }
        return runMethod(new MethodRef(clazz, meth), arguments);
    }

    public StackEntry runMethod(MethodRef methRef, @Nullable StackEntry[] arguments) throws VmException {
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
        
        var executor = new MethodExecutor(this, localVariables, methRef.clazz().node.name+"#"+meth.name+meth.desc);
        stack.push(executor);
        var ret = executor.run(meth.instructions);
        stack.pop();
        return ret;
    }
    
    public Clazz getClass(String name) throws VmException {
        var clazz = this.classes.get(name);
        if (clazz == null) {

            // Load class using ASM
            var node = new ClassNode(Opcodes.ASM9);
            try {
                var stream = classResolver.getClass(name);
                new ClassReader(stream).accept(node, 0);
            } catch (IOException e) {
                throw new VmException("Error loading " + name, e);
            }
            clazz = new Clazz(node);
            this.classes.put(name, clazz);
            return clazz;
        } else {
            return clazz;
        }
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
            runMethod(node, "<clinit>", "()V", null);
        }
    }

    /**
     * @return null if the {@code objectRef} is of an unknown type, and the method can't be resolved due to that.
     */
    public @Nullable MethodRef resolveMethod(MethodInsnNode inst, @Nullable StackEntry objectRef, boolean tryResolve) throws VmException {
        switch (inst.getOpcode()) {
            case Opcodes.INVOKESTATIC -> {
                var clazz = this.getClass(inst.owner);
                var method = AsmUtils.getMethod(clazz.node, inst.name, inst.desc);
                // This is a hard-error. Static methods shouldn't be hard to find and something's wrong here. So no returning null in this case
                if (method == null) throw new VmException("Couldn't find static method "+inst.name+inst.desc+" in "+inst.owner, null);
                return new MethodRef(clazz, method);
            }
            case Opcodes.INVOKEINTERFACE, Opcodes.INVOKEVIRTUAL, Opcodes.INVOKESPECIAL -> {
                if (objectRef == null) throw new IllegalArgumentException("objectRef can't be null for method invokation ("+inst.getOpcode()+")");

                // Find the root class from which to start looking for the method
                Clazz rootClass = switch (inst.getOpcode()) {
                    case Opcodes.INVOKESPECIAL -> {
                        // See https://docs.oracle.com/javase/specs/jvms/se10/html/jvms-6.html#jvms-6.5.invokespecial
                        var clazz = this.getClass(inst.owner);
                        if (!inst.name.startsWith("<init>") &&
                                !AsmUtils.hasFlag(clazz.node.access, Opcodes.ACC_INTERFACE) &&
                                AsmUtils.hasFlag(clazz.node.access, Opcodes.ACC_SUPER)) {
                            clazz = this.getClass(clazz.node.superName);
                        }
                        yield clazz;
                    }
                    case Opcodes.INVOKEINTERFACE, Opcodes.INVOKEVIRTUAL -> {
                        if (tryResolve) {
                            objectRef = objectRef.resolve(this);
                        }
                        if (objectRef instanceof KnownObject o) {
                            yield this.getClass(o.getClass().getCanonicalName());
                        } else if (objectRef instanceof KnownVmObject o) {
                            yield o.type();
                        } else {
                            yield null;
                        }
                    }
                    default -> throw new IllegalStateException();
                };

                if (rootClass == null) return null;

                var clazz = rootClass;
                while (true) {
                    var method = AsmUtils.getMethod(clazz.node, inst.name, inst.desc);
                    if (method != null) {
                        return new MethodRef(clazz, method);
                    } else {
                        // Check super class
                        if (clazz.node.superName == null) throw new VmException("Can't find method "+inst.name+inst.desc+" in "+rootClass.node.name+" ("+inst.getOpcode()+", "+inst.owner+")", null);
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

        default void putStaticField(Context ctx, FieldInsnNode inst, StackEntry value) throws VmException {
            var clazz = ctx.machine.getClass(inst.owner);
            ctx.machine.ensureClinit(clazz);
            clazz.setStatic(inst.name, value);
        }
    
        /**
         * @param arguments If this instruction is not invokeStatic, the first element will represent the object this was called on.
         */
        default @Nullable StackEntry invoke(Context ctx, MethodInsnNode inst, StackEntry[] arguments) throws VmException {
            if (inst.owner.equals(Type.getInternalName(LoggerFactory.class))) {
                return new UnknownValue("Refusing to invoke LoggerFactory for optimization reasons");
            }
            // Tmp hacks until the virtual machine is advanced enough to handle this function
            if (inst.owner.equals(Type.getInternalName(Identifier.class)) && 
                (inst.name.equals("validateNamespace") || inst.name.equals("validatePath"))) {
                return inst.name.equals("validateNamespace") ? arguments[0] : arguments[1];
            }
            if (inst.owner.equals(Type.getInternalName(Objects.class)) && inst.name.equals("requireNonNull")) {
                return arguments[0];
            }
            // I'm pretty sure we're supposed to run clinit at this point, but let's delay it as much as possible
            var method = ctx.machine().resolveMethod(inst, Util.first(arguments), true);
            if (method == null) {
                // Can't be resolved, return an unknown value
                return Type.getReturnType(inst.desc) == Type.VOID_TYPE ? null : new UnknownValue("Can't resolve method "+inst.owner+"#"+inst.name+inst.desc);
            }
            return ctx.machine.runMethod(method, arguments);
        }

        default @NotNull StackEntry newObject(Context ctx, TypeInsnNode inst) throws VmException {
            var clazz = ctx.machine.getClass(inst.desc);
            return new KnownVmObject(clazz, new HashMap<>());
        }

        default void handleUnknownInstruction(Context ctx, AbstractInsnNode instruction, int lineNumber) throws VmException {
            throw new NotImplementedException("Unimplemented instruction "+instruction.getOpcode());
        }
    }

    public record Context(VirtualMachine machine) {
    }

    public record MethodRef(Clazz clazz, MethodNode meth) {};

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
    }
}
