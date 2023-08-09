package io.github.theepicblock.polymc.impl.generator.asm.stack;

import com.google.gson.JsonElement;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

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
    public StackEntry[] asKnownArray() {
        return data();
    }

    @Override
    public boolean isConcrete() {
        return true;
    }

    @Override
    public StackEntry copy(Reference2ReferenceOpenHashMap<StackEntry,StackEntry> copyCache) {
        if (copyCache.containsKey(this)) return copyCache.get(this);

        var newArr = new StackEntry[this.data.length];
        int i = 0;
        for (var v : this.data) {
            if (v != null) newArr[i] = v.copy(copyCache);
            i++;
        }
        var ret = new KnownArray(newArr);
        copyCache.put(this, ret);
        return ret;
    }

    public StackEntry shallowCopy() {
        return new KnownArray(this.data.clone());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KnownArray that = (KnownArray)o;
        return Arrays.deepEquals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(data);
    }
}
