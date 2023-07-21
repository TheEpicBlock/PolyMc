package io.github.theepicblock.polymc.impl.generator.asm.stack;

import com.google.gson.JsonElement;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Represents and object that exists outside of the vm
 */
public record KnownObject(Object i, @NotNull HashMap<Object, StackEntry> mutations) implements StackEntry {
    public static KnownObject NULL = new KnownObject(null, new HashMap<>());

    public KnownObject(Object i) {
        this(i, new HashMap<>());
    }

    @Override
    public JsonElement toJson() {
        if (!mutations.isEmpty()) throw new NotImplementedException("Known object extraction does not yet factor in mutations");
        return StackEntry.GSON.toJsonTree(i);
    }

    @Override
    public <T> T extractAs(Class<T> type) {
        if (!mutations.isEmpty()) throw new NotImplementedException("Known object extraction does not yet factor in mutations");
        if (type.isAssignableFrom(i.getClass())) {
            return (T)i;
        }
        return StackEntry.super.extractAs(type);
    }

    @Override
    public @NotNull StackEntry getField(String name) throws VmException {
        if (mutations.containsKey(name)) {
            return mutations.get(name);
        }
        try {
            var clazz = i.getClass();
            var field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return new KnownObject(field.get(i));
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new VmException("Couldn't get field "+name, e);
        }
    }

    @Override
    public void setField(String name, StackEntry e) throws VmException {
        if (this.i == null) {
            throw new VmException("Can't set property on null value", null);
        }
        mutations.put(name, e);
    }

    @Override
    public @NotNull StackEntry arrayAccess(int index) throws VmException {
        if (i instanceof Object[] arr) {
            if (mutations.containsKey(index)) {
                return mutations.get(index);
            }
            if (arr[index] == null) {
                return KnownObject.NULL;
            }
            return new KnownObject(arr[index]);
        } else {
            throw new VmException("Attempted to use "+this+" as an array", null);
        }
    }

    @Override
    public void arraySet(int index, @NotNull StackEntry entry) throws VmException {
        mutations.put(index, entry);
    }

    @Override
    public boolean isConcrete() {
        return true;
    }
}