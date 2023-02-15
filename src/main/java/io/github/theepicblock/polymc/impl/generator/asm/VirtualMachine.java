package io.github.theepicblock.polymc.impl.generator.asm;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.slf4j.LoggerFactory;

import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import io.github.theepicblock.polymc.impl.generator.asm.stack.UnknownValue;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class VirtualMachine {
    private final HashMap<String, Clazz> classes = new HashMap<>();
    private final ClassLoader classResolver;
    private final VmConfig config;
    private final Stack<MethodExecutor> stack = new ObjectArrayList();

    public VirtualMachine(ClassLoader classResolver, VmConfig config) {
        this.classResolver = classResolver;
        this.config = config;
    }

    public StackEntry runMethod(String clazz, String method, String desc) throws VmException {
        var clazzNode = getClass(clazz);
        return runMethod(clazzNode, method, desc, null);
    }

    public StackEntry runMethod(Clazz clazz, String method, String desc, @Nullable Pair<Type, StackEntry>[] arguments) throws VmException {
        var meth = AsmUtils.getMethod(clazz.node, method, desc);
        var localVariables = new StackEntry[meth.maxLocals];
        
        // Fill in arguments
        if (arguments != null) {
            int i = 0;
            for (var pair : arguments) {
                localVariables[i] = pair.getRight();
                i++;
            }
        }
        
        var executor = new MethodExecutor(this, localVariables, clazz.node.name+"#"+method);
        stack.push(executor);
        var ret = executor.run(meth.instructions);
        stack.pop();
        return ret;
    }
    
    public Clazz getClass(String name) throws VmException {
        var clazz = this.classes.get(name);
        if (clazz == null) {
            var stream = classResolver.getResourceAsStream(name.replace(".", "/") + ".class");

            // Load class using ASM
            var node = new ClassNode(Opcodes.ASM9);
            try {
                new ClassReader(stream).accept(node, 0);
            } catch (IOException e) {
                throw new VmException("Error loading" + name, e);
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

    public void ensureClinit(Clazz node) throws VmException {
        // This isn't spec-compliant
        if (!node.hasInitted) {
            node.hasInitted = true;
            runMethod(node, "<clinit>", "()V", null);
        }
    }

    public interface VmConfig {
        default StackEntry loadStaticField(Context ctx, FieldInsnNode inst) throws VmException {
            var clazz = ctx.machine.getClass(inst.owner);
            ctx.machine.ensureClinit(clazz);
            return clazz.getStatic(inst.name);
        }

        default void putStaticField(Context ctx, FieldInsnNode inst, StackEntry value) throws VmException {
            var clazz = ctx.machine.getClass(inst.owner);
            ctx.machine.ensureClinit(clazz);
            clazz.setStatic(inst.name, value);
        }
    
        default StackEntry invokeStatic(Context ctx, MethodInsnNode inst, Pair<Type, StackEntry>[] arguments) throws VmException {
            if (inst.owner.equals(Type.getInternalName(LoggerFactory.class))) {
                return new UnknownValue();
            }
            var clazz = ctx.machine.getClass(inst.owner);
            // I'm pretty sure we're supposed to run clinit at this point, but let's delay it as much as possible
            return ctx.machine.runMethod(clazz, inst.name, inst.desc, arguments);
        }

        default void handleUnknownInstruction(Context ctx, AbstractInsnNode instruction, int lineNumber) throws VmException {
            throw new NotImplementedException("Unimplemented instruction "+instruction.getOpcode());
        }
    }

    public record Context(VirtualMachine machine) {
    }

    public class Clazz {
        private ClassNode node;
        private boolean hasInitted;
        private Map<String, StackEntry> staticFields = new HashMap<>();

        public Clazz(ClassNode node) {
            this.node = node;
        }

        public StackEntry getStatic(String name) {
            return staticFields.getOrDefault(name, new UnknownValue());
        }

        public void setStatic(String name, StackEntry v) {
            staticFields.put(name, v);
        }
    }
}
