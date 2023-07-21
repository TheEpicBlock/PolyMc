package io.github.theepicblock.polymc.impl.generator.asm.stack;

import com.google.gson.JsonElement;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

public record KnownClass(@NotNull Type type) implements StackEntry {
    public KnownClass(@NotNull VirtualMachine.Clazz clazz) {
        this(Type.getObjectType(clazz.getNode().name));
    }

    @Override
    public JsonElement toJson() {
        throw new NotImplementedException();
    }

    @Override
    public <T> T extractAs(Class<T> type) {
        throw new NotImplementedException();
    }

    @Override
    public boolean isConcrete() {
        return true;
    }
}