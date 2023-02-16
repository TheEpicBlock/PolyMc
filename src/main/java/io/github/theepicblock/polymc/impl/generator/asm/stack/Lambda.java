package io.github.theepicblock.polymc.impl.generator.asm.stack;

import org.objectweb.asm.Handle;

import com.google.gson.JsonElement;

public record Lambda(Handle method) implements StackEntry {
    @Override
    public JsonElement toJson() {
        return StackEntry.GSON.toJsonTree(this);
    }

    @Override
    public <T> T cast(Class<T> type) {
        if (type == this.getClass()) {
            return (T)this;
        }
        throw new ClassCastException("Can't cast "+type+" to Lambda");
    }
}