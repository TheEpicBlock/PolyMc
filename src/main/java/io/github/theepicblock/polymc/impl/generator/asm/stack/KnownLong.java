package io.github.theepicblock.polymc.impl.generator.asm.stack;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.network.PacketByteBuf;

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
    public void write(PacketByteBuf buf) {
        buf.writeVarLong(this.i);
    }

    public static StackEntry read(PacketByteBuf buf) {
        return new KnownLong(buf.readVarLong());
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