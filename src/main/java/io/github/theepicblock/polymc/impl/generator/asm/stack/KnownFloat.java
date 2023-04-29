package io.github.theepicblock.polymc.impl.generator.asm.stack;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public record KnownFloat(float i) implements StackEntry {
    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(i);
    }

    @Override
    public <T> T cast(Class<T> type) {
        if (type == Float.TYPE) {
            return (T)(Float)i;
        }
        return StackEntry.super.cast(type);
    }
}