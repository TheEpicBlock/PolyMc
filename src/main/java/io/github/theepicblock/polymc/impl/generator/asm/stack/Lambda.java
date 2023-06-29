package io.github.theepicblock.polymc.impl.generator.asm.stack;

import com.google.gson.JsonElement;
import org.objectweb.asm.Handle;

public record Lambda(Handle method, StackEntry[] extraArguments) implements StackEntry {
    @Override
    public JsonElement toJson() {
        return StackEntry.GSON.toJsonTree(this);
    }

    @Override
    public <T> T extractAs(Class<T> type) {
        if (type == this.getClass()) {
            return (T)this;
        }
        throw new ClassCastException("Can't cast "+type+" to Lambda");
    }
    @Override
    public boolean isConcrete() {
        return true;
    }
}