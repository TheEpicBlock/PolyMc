package io.github.theepicblock.polymc.impl.generator.asm.stack;

import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import net.minecraft.network.PacketByteBuf;
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
    public void write(PacketByteBuf buf) {
        buf.writeString(type.getDescriptor());
    }

    public static StackEntry read(PacketByteBuf buf) {
        return new KnownClass(Type.getType(buf.readString()));
    }

    @Override
    public boolean isConcrete() {
        return true;
    }
}