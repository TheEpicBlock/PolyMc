package io.github.theepicblock.polymc.impl.generator.asm.stack;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor;
import io.github.theepicblock.polymc.impl.generator.asm.StackEntryTable;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.NotNull;

public record KnownLong(long i) implements StackEntry {
    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(i);
    }

    @Override
    public <T> T extractAs(Class<T> type) {
        if (type == long.class) {
            return (T)(Long)i;
        }
        return StackEntry.super.extractAs(type);
    }

    @Override
    public void write(PacketByteBuf buf, StackEntryTable table) {
        buf.writeVarLong(this.i);
    }

    public static StackEntry read(PacketByteBuf buf, StackEntryTable table) {
        return new KnownLong(buf.readVarLong());
    }

    @Override
    public @NotNull StackEntry getField(String name) throws MethodExecutor.VmException {
        if (name.equals("value")) return this; // I like dealing with boxing in dumb ways
        return StackEntry.super.getField(name);
    }

    @Override
    public boolean isConcrete() {
        return true;
    }

    @Override
    public int getWidth() {
        return 2;
    }
}