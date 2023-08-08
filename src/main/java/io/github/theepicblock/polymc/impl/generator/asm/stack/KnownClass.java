package io.github.theepicblock.polymc.impl.generator.asm.stack;

import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

public record KnownClass(@NotNull Type type) implements StackEntry {
    public KnownClass(@NotNull VirtualMachine.Clazz clazz) {
        this(Type.getObjectType(clazz.getNode().name));
    }

    @Override
    public <T> T extractAs(Class<T> type) {
        if (type == Type.class) return (T)this.type;
        throw new NotImplementedException();
    }

    @Override
    public boolean isConcrete() {
        return true;
    }
}