package io.github.theepicblock.polymc.impl.generator.asm.stack;

import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor;
import net.minecraft.network.PacketByteBuf;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record UnknownValue(@Nullable Object reason) implements StackEntry {
    public UnknownValue() {
        this(null);
    }

    @Override
    public <T> T extractAs(Class<T> type) {
        var reasonStr = reason instanceof MethodExecutor.VmException e ? e.createFancyErrorMessage() : Objects.toString(reason);
        throw new NotImplementedException("Can't cast an unknown value ("+reasonStr+")");
    }

    @Override
    public void write(PacketByteBuf buf) {
    }

    public static StackEntry read(PacketByteBuf buf) {
        return new UnknownValue("<deserialized>");
    }
}