package io.github.theepicblock.polymc.impl.generator.asm.stack;

import com.google.gson.JsonElement;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record UnknownValue(@Nullable Object reason) implements StackEntry {
    public UnknownValue() {
        this(null);
    }

    @Override
    public JsonElement toJson() {
        throw new NotImplementedException();
    }

    @Override
    public <T> T cast(Class<T> type) {
        var reasonStr = reason instanceof MethodExecutor.VmException e ? e.createFancyErrorMessage() : Objects.toString(reason);
        throw new NotImplementedException("Can't cast an unknown value ("+reasonStr+")");
    }
}