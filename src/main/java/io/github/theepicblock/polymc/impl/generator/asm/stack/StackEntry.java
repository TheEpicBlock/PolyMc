package io.github.theepicblock.polymc.impl.generator.asm.stack;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.stack.ops.StaticFieldValue;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

import java.util.HashMap;
import java.util.Map;

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
        return simplify(vm, new HashMap<>());
    }

    /**
     * For stack entries that represent delayed instructions, such as {@link StaticFieldValue}
     * @param simplificationCache A mutable map that can be used to cache a {@link StackEntry} to its simplified value
     *                            This prevents infinite recursion when an entry contains a reference to itself.
     *                            This cache isn't used in most {@link StackEntry}'s, except {@link KnownVmObject}
     */
    default StackEntry simplify(VirtualMachine vm, Map<StackEntry,StackEntry> simplificationCache) throws VmException {
        return this;
    }

    JsonElement toJson();

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

    default StackEntry copy() {
        return this;
    }
}