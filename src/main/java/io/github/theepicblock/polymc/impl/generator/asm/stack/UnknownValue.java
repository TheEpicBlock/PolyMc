package io.github.theepicblock.polymc.impl.generator.asm.stack;

import javax.annotation.Nullable;

import org.apache.commons.lang3.NotImplementedException;

import com.google.gson.JsonElement;

public record UnknownValue(@Nullable Object reason) implements StackEntry {
    public UnknownValue() {
        this(null);
    }

    @Override
    public JsonElement toJson() {
        throw new NotImplementedException();
    }
}