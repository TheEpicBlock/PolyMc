package io.github.theepicblock.polymc.impl.generator.asm.stack;

import org.apache.commons.lang3.NotImplementedException;

import com.google.gson.JsonElement;

import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;

public record KnownObject(Object i) implements StackEntry {
    @Override
    public JsonElement toJson() {
        return StackEntry.GSON.toJsonTree(i);
    }

    @Override
    public <T> T cast(Class<T> type) {
        if (type.isAssignableFrom(i.getClass())) {
            return (T)i;
        }
        return StackEntry.super.cast(type);
    }

    @Override
    public StackEntry getField(String name) throws VmException {
        try {
            return new KnownObject(i.getClass().getField(name).get(i));
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new VmException("Couldn't get field "+name, e);
        }
    }

    @Override
    public void setField(String name, StackEntry e) {
        // We do *not* want to set this field on the actual object, because we don't know where the object originated from
        // and we don't want the virtual machine to have any side effect on the actual jvm
        throw new NotImplementedException("Can't set fields of jvm objects");
    }
}