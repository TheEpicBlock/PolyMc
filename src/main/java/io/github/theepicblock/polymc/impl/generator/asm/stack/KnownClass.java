package io.github.theepicblock.polymc.impl.generator.asm.stack;

import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

import com.google.gson.JsonElement;

public record KnownClass(@NotNull Type type) implements StackEntry {
    @Override
    public JsonElement toJson() {
        throw new NotImplementedException();
    }

    @Override
    public <T> T cast(Class<T> type) {
        throw new NotImplementedException();
    }
}