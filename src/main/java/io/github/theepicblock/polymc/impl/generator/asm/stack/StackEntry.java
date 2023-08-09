package io.github.theepicblock.polymc.impl.generator.asm.stack;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.stack.ops.StaticFieldValue;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

public interface StackEntry {
    static final Gson GSON = new Gson();

    static @NotNull StackEntry known(Object o) {
        if (o instanceof Integer i) {
            return new KnownInteger(i);
        }
        if (o instanceof Boolean i) {
            return new KnownInteger(i);
        }
        if (o instanceof Byte i) {
            return new KnownInteger(i);
        }
        if (o instanceof Character i) {
            return new KnownInteger(i);
        }
        if (o instanceof Long l) {
            return new KnownLong(l);
        }
        if (o instanceof Float f) {
            return new KnownFloat(f);
        }
        if (o instanceof Double d) {
            return new KnownDouble(d);
        }
        if (o instanceof Type t) {
            return new KnownClass(t);
        }
        return new KnownObject(o);
    }

    static @NotNull StackEntry known(int i) {
        return new KnownInteger(i);
    }

    static @NotNull StackEntry known(boolean b) {
        return new KnownInteger(b);
    }

    static @NotNull StackEntry known(long l) {
        return new KnownLong(l);
    }

    static @NotNull StackEntry known(float f) {
        return new KnownFloat(f);
    }

    static @NotNull StackEntry known(double d) {
        return new KnownDouble(d);
    }

    default void setField(String name, StackEntry e) throws VmException {
        throw new NotImplementedException("Can't set field "+name+" on "+this);
    }

    default @NotNull StackEntry getField(String name) throws VmException {
        throw new NotImplementedException("Can't get field "+name+" from "+this);
    }

    default @NotNull StackEntry arrayAccess(int index) throws VmException {
        throw new NotImplementedException("Can't load an array index ("+index+") from "+this);
    }

    default void arraySet(int index, @NotNull StackEntry entry) throws VmException {
        throw new NotImplementedException("Can't load an array index ("+index+") from "+this);
    }

    default boolean canBeSimplified() {
        return false;
    }

    default StackEntry simplify(VirtualMachine vm) throws VmException {
        return simplify(vm, new Reference2ReferenceOpenHashMap<>());
    }

    /**
     * For stack entries that represent delayed instructions, such as {@link StaticFieldValue}
     * @param simplificationCache A mutable map that can be used to cache a {@link StackEntry} to its simplified value
     *                            This prevents infinite recursion when an entry contains a reference to itself.
     *                            This cache isn't used in most {@link StackEntry}'s, except {@link KnownVmObject}
     */
    default StackEntry simplify(VirtualMachine vm, Reference2ReferenceOpenHashMap<StackEntry,StackEntry> simplificationCache) throws VmException {
        return this;
    }

    default JsonElement toJson() {
        throw new NotImplementedException();
    };

    /**
     * @return Whether this stack value represents a concrete value that's ready to be extracted
     */
    default boolean isConcrete() {
        return false;
    }

    /**
     * Extracts the value of this entry into a POJO
     */
    default <T> T extractAs(Class<T> type) {
        return GSON.fromJson(this.toJson(), type);
    }

    default StackEntry[] asKnownArray() {
        throw new NotImplementedException(this+" is not an array");
    }

    default StackEntry copy() {
        return copy(new Reference2ReferenceOpenHashMap<>());
    }

    default StackEntry copy(Reference2ReferenceOpenHashMap<StackEntry,StackEntry> simplificationCache) {
        return this;
    }

    /**
     * Equivalent to the computational type category as defined in
     * <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-2.html#jvms-2.11.1-320">this table</a>
     */
    default int getWidth() {
        return 1;
    }
}