package io.github.theepicblock.polymc.impl.generator.asm.stack;

import com.google.gson.JsonElement;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record KnownArray(@Nullable StackEntry[] data) implements StackEntry {
    @Override
    public JsonElement toJson() {
        return StackEntry.GSON.toJsonTree(data);
    }

    public static KnownArray withLength(int length) {
        return new KnownArray(new StackEntry[length]);
    }

    @Override
    public @NotNull StackEntry arrayAccess(int index) throws VmException {
        if (data[index] == null) {
            return KnownObject.NULL;
        }
        return data[index];
    }

    @Override
    public void arraySet(int index, @NotNull StackEntry entry) throws VmException {
        data[index] = entry;
    }

    @Override
    public <T> T extractAs(Class<T> type) {
        if (type == Object[].class) {
            return (T)data;
        }
        return StackEntry.super.extractAs(type);
    }

    @Override
    public boolean isConcrete() {
        return true;
    }

    @Override
    public StackEntry copy() {
        var newArr = new StackEntry[this.data.length];
        int i = 0;
        for (var v : this.data) {
            if (v != null) newArr[i] = v.copy();
            i++;
        }
        return new KnownArray(newArr);
    }

    public StackEntry shallowCopy() {
        return new KnownArray(this.data.clone());
    }
}
