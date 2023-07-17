package io.github.theepicblock.polymc.impl.generator.asm;

import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine.Context;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownObject;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownVmObject;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import io.github.theepicblock.polymc.impl.generator.asm.stack.UnknownValue;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;

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
     * Checks the *actual* jvm to see if the static field referenced by {@code inst} is already loaded
     */
    public static @Nullable StackEntry tryGetStaticFieldFromEnvironment(Context ctx, FieldInsnNode inst) {
        try {
            /*
            // Only if the class is already loaded
            var m = AsmUtils.class.getClassLoader().getClass().getDeclaredMethod("findLoadedClassFwd", new Class[] { String.class });
            m.setAccessible(true);
            ClassLoader cl = AsmUtils.class.getClassLoader();
            var clazz = m.invoke(cl, inst.owner.replace("/", "."));
            if (clazz != null) {
                return new KnownObject(((Class<?>)clazz).getField(inst.name).get(null));
            }
            */

            // Will error if this class can't be loaded from the environment
            return new KnownObject(Class.forName(inst.owner.replace("/", ".")).getField(inst.name).get(null));
        } catch (Throwable e) {}
        return null;
    }

    public static boolean hasFlag(int bitfield, int flag) {
        return (bitfield & flag) == flag;
    }

    public static MappedFunction map(String className, String methodName, String descriptor) {
        return map(FabricLoader.getInstance().getMappingResolver(), className, methodName, descriptor);
    }

    public static MappedFunction map(MappingResolver resolver, String className, String methodName, String descriptor) {
        var newClassname = resolver.mapClassName("intermediary", className);
        var newMethodName = resolver.mapMethodName("intermediary", className, methodName, descriptor);
        var newDesc = Mapping.remapDescriptor(s -> resolver.mapClassName("intermediary", s), descriptor);
        return new MappedFunction(newClassname.replace(".", "/"), newMethodName, newDesc);
    }

    public record MappedFunction(@InternalName String clazz, String method, String desc) {
    }

    public static KnownVmObject mockVmObjectRemap(VirtualMachine vm, @BinaryName String className) throws VmException {
        var runtimeName = FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary", className);
        return mockVmObject(vm, runtimeName.replace(".", "/"));
    }


    public static KnownVmObject mockVmObject(VirtualMachine vm, @InternalName String className) throws VmException {
        return mockVmObject(vm, className, 4);
    }

    public static KnownVmObject mockVmObject(VirtualMachine vm, @InternalName String className, int recursionLimit) throws VmException {
        var clazz = vm.getClass(className);
        var fields = new HashMap<String, StackEntry>();
        fillFields(vm, fields, className, clazz.getNode(), recursionLimit);
        return new KnownVmObject(clazz, fields);
    }

    private static void fillFields(VirtualMachine vm, HashMap<String, StackEntry> fields, @InternalName String rootName, ClassNode node, int recursionLimit) throws VmException {
        if (recursionLimit == 0) return;
        for (var field : node.fields) {
            if ((field.access & Opcodes.ACC_STATIC) != 0) continue;
            if (field.desc.startsWith("[")) {
                // TODO
                continue;
            }
            try {
                fields.put(field.name, switch (field.desc) {
                    case "I", "J", "F", "D", "Z", "B", "S", "C" -> new UnknownValue("Field "+field.name+" in "+rootName+" is mocked;");
                    default -> mockVmObject(vm, field.desc.substring(1, field.desc.length()-1), recursionLimit - 1);
                });
            } catch (VmException ignored) {}
        }
        if (node.superName != null) {
            var superClass = vm.getClass(node.superName);
            fillFields(vm, fields, rootName, superClass.getNode(), recursionLimit);
        }
    }
}
