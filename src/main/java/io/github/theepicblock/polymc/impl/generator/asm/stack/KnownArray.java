package io.github.theepicblock.polymc.impl.generator.asm.stack;

import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonElement;

import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;

public record KnownArray(StackEntry[] data) implements StackEntry {
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
}
