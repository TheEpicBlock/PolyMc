package io.github.theepicblock.polymc.impl.generator.asm;

import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine.Context;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownObject;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import net.fabricmc.loader.api.FabricLoader;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class AsmUtils {
    public static @Nullable MethodNode getMethod(ClassNode node, String name, String desc, Mapping mapper) {
        // This might be a client-only class, in which case it'll be in client jar, which is obfuscated
        // So we check both obfuscated and non-obfuscated names
        // TODO
        return node.methods.stream().filter(m -> m.name.equals(name) && m.desc.equals(desc)).findFirst().orElse(null);
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
}
