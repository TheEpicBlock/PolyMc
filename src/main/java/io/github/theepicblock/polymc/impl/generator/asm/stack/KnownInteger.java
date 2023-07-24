package io.github.theepicblock.polymc.impl.generator.asm.stack;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public record KnownInteger(int i) implements StackEntry {
    public KnownInteger(boolean b) {
        this(b ? 1 : 0);
    }

    public KnownInteger(char c) {
        this((int)c);
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(i);
    }

    @Override
    public <T> T extractAs(Class<T> type) {
        if (type == int.class) {
            return (T)(Integer)i;
        }
        return StackEntry.super.extractAs(type);
    }

    @Override
    public boolean isConcrete() {
        return true;
    }
}