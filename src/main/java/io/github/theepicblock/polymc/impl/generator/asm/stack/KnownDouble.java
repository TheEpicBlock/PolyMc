package io.github.theepicblock.polymc.impl.generator.asm.stack;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public record KnownDouble(double d) implements StackEntry {
    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(d);
    }

    @Override
    public <T> T extractAs(Class<T> type) {
        if (type == double.class) {
            return (T)(Double)d;
        }
        return StackEntry.super.extractAs(type);
    }

    @Override
    public boolean isConcrete() {
        return true;
    }

    @Override
    public int getWidth() {
        return 2;
    }
}
