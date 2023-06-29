package io.github.theepicblock.polymc.impl.generator.asm.stack;

import com.google.gson.JsonElement;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

/**
 * Represents and object that exists outside of the vm
 */
public record KnownObject(Object i) implements StackEntry {
    public static KnownObject NULL = new KnownObject(null);

    @Override
    public JsonElement toJson() {
        return StackEntry.GSON.toJsonTree(i);
    }

    @Override
    public <T> T extractAs(Class<T> type) {
        if (type.isAssignableFrom(i.getClass())) {
            return (T)i;
        }
        return StackEntry.super.extractAs(type);
    }

    @Override
    public StackEntry getField(String name) throws VmException {
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
    public void setField(String name, StackEntry e) {
        // We do *not* want to set this field on the actual object, because we don't know where the object originated from
        // and we don't want the virtual machine to have any side effect on the actual jvm
        throw new NotImplementedException("Can't set fields of jvm objects");
    }

    @Override
    public @NotNull StackEntry arrayAccess(int index) throws VmException {
        if (i instanceof Object[] arr) {
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
        // We do *not* want to set this index on the actual array object, because we don't know where the array object originated from
        // and we don't want the virtual machine to have any side effect on the actual jvm
        throw new NotImplementedException("Can't set fields of jvm objects");
    }

    @Override
    public boolean isConcrete() {
        return true;
    }
}