package io.github.theepicblock.polymc.impl.generator.asm;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine.Context;
import io.github.theepicblock.polymc.impl.generator.asm.stack.MockedObject;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class AsmUtils {
    /**
     * @deprecated This method is really slow, only use it in non-critical sections
     */
    @Deprecated
    public static @Nullable MethodNode getMethod(ClassNode node, String name, String desc) {
        return node.methods
            .stream()
            .filter(m -> (m.name.equals(name) && m.desc.equals(desc)))
            .findFirst()
            .orElse(null);
    }

    /**
     * Converts an internal name (as returned by {@link org.objectweb.asm.Type#getInternalName})
     * to a binary name (like the one {@link ClassLoader#loadClass(String)} expects)
     */
    public static String toBinary(String internalName) {
        return internalName.replace("/", ".");
    }

    /**
     * @implNote Does not account for static fields declared in interfaces
     */
    public static Stream<FieldNode> getFields(VirtualMachine.Clazz rootClass) {
        return getInheritanceChain(rootClass).stream().flatMap(classNode -> classNode.fields.stream());
    }

    /**
     * @return a list containing the rootclass and all of its parents
     */
    public static List<ClassNode> getInheritanceChain(@NotNull VirtualMachine.Clazz rootClass) {
        ClassNode clazz = rootClass.getNode();
        var classes = new ArrayList<ClassNode>();
        while (true) {
            classes.add(clazz);
            if (clazz.superName != null) {
                // Get parent class
                try {
                    clazz = rootClass.getLoader().getClass(clazz.superName).getNode();
                } catch (VmException e) {
                    PolyMc.LOGGER.warn("Couldn't get all fields of "+rootClass.getNode().name+": "+e.createFancyErrorMessage());
                    break;
                }
            } else {
                break;
            }
        }
        return classes;
    }

    public static <T> @Nullable T forEachInterface(@NotNull VirtualMachine.Clazz root, Function<VirtualMachine.Clazz, @Nullable T> func) throws VmException {
        var vm = root.getLoader();
        for (var interface_ : root.getNode().interfaces) {
            var interfaceClass = vm.getClass(interface_);
            var result = func.apply(interfaceClass);
            if (result != null) return result;
            var recursiveResult = forEachInterface(interfaceClass, func);
            if (recursiveResult != null) return recursiveResult;
        }
        return null;
    }

    @Contract(pure = true)
    public static @NotNull Field getFieldRecursive(@NotNull Class<?> root, @NotNull String name) throws NoSuchFieldException {
        try {
            return root.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            if (root == Object.class) throw e;
            return getFieldRecursive(root.getSuperclass(), name);
        }
    }

    /**
     * Checks the *actual* jvm to see if the static field referenced by {@code inst} is already loaded
     */
    public static @Nullable StackEntry tryGetStaticFieldFromEnvironment(Context ctx, @NotNull @InternalName String className, @NotNull String fieldName) {
        try {
            var clazz = Class.forName(className.replace("/", "."));
            var field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);

            return StackEntry.known(field.get(null));
        } catch (Throwable e) {}
        return null;
    }

    public static boolean hasFlag(ClassNode node, int flag) {
        return hasFlag(node.access, flag);
    }

    public static boolean hasFlag(MethodNode node, int flag) {
        return hasFlag(node.access, flag);
    }

    public static boolean hasFlag(FieldNode node, int flag) {
        return hasFlag(node.access, flag);
    }

    public static boolean hasFlag(int bitfield, int flag) {
        return (bitfield & flag) == flag;
    }

    public static @NotNull MappedFunction map(String className, String methodName, String descriptor) {
        var result = map(FabricLoader.getInstance().getMappingResolver(), className, methodName, descriptor);
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            if (result.clazz.equals(className) || result.method.equals(methodName)) {
                PolyMc.LOGGER.warn("no change between "+className+"#"+methodName+descriptor+" and "+result.clazz+"#"+result.method+result.desc+" this might be wrong (unless this isn't a MC class)");
            }
        }
        return result;
    }

    public static MappedFunction map(MappingResolver resolver, @BinaryName String className, String methodName, String descriptor) {
        var newClassname = resolver.mapClassName("intermediary", className);
        var newMethodName = resolver.mapMethodName("intermediary", className, methodName, descriptor);
        var newDesc = Mapping.remapDescriptor(s -> resolver.mapClassName("intermediary", s), descriptor);
        return new MappedFunction(newClassname.replace(".", "/"), newMethodName, newDesc);
    }

    public static String mapField(Class<?> owner, String intermediaryName, String descriptor) {
        var resolver = FabricLoader.getInstance().getMappingResolver();
        return resolver.mapFieldName("intermediary", owner.getName(), intermediaryName, descriptor);
    }

    public static @InternalName String mapClass(@BinaryName String intermediaryName) {
        var resolver = FabricLoader.getInstance().getMappingResolver();
        return resolver.mapClassName("intermediary", intermediaryName).replace(".", "/");
    }

    public static void simplifyAll(Context ctx, @Nullable StackEntry[] entries) throws VmException {
        for (var i = 0; i < entries.length; i++) {
            if (entries[i] != null && entries[i].canBeSimplified()) entries[i] = entries[i].simplify(ctx.machine());
        }
    }

    public record MappedFunction(@InternalName String clazz, String method, String desc) {
    }

    public static StackEntry mockVmObjectRemap(VirtualMachine vm, @BinaryName String className, String name) throws VmException {
        var runtimeName = FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary", className);
        return mockVmObject(vm, runtimeName.replace(".", "/"), name);
    }

    public static StackEntry mockVmObject(VirtualMachine vm, @InternalName String className, String name) throws VmException {
        return new MockedObject(new MockedObject.Root(name), vm.getClass(className));
//        return mockVmObject(vm, className, 4);
    }
    public static Stream<AbstractInsnNode> insnStream(AbstractInsnNode start) {
        return Stream.iterate(start, s -> s.getNext() != null, AbstractInsnNode::getNext);
    }
}
