package io.github.theepicblock.polymc.impl.generator.asm.stack;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.NotImplementedException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;

public interface StackEntry {
    static final Gson GSON = new Gson();

    static @Nonnull StackEntry knownStackValue(Object o) {
        if (o instanceof Integer i) {
            return new KnownInteger(i);
        }
        if (o instanceof Float f) {
            return new KnownFloat(f);
        }
        return new KnownObject(o);
    }

    default void setField(String name, StackEntry e) {
        throw new NotImplementedException("Can't set field "+name+" on "+this);
    }

    default @Nonnull StackEntry getField(String name) throws VmException {
        throw new NotImplementedException("Can't get field "+name+" from "+this);
    }

    /**
     * For stack entries that represent delayed instructions, such as {@link StaticFieldValue}
     */
    default StackEntry resolve(VirtualMachine vm) throws VmException {
        return this;
    }

    JsonElement toJson();

    /**
     * Extracts the value of this entry into a POJO
     */
    default <T> T cast(Class<T> type) {
        return GSON.fromJson(this.toJson(), type);
    }
}