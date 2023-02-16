package io.github.theepicblock.polymc.impl.generator.asm.stack;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

public record KnownVoid() implements StackEntry {
    public String toString() {
        return "void";
    }

    @Override
    public JsonElement toJson() {
        return JsonNull.INSTANCE;
    }
}