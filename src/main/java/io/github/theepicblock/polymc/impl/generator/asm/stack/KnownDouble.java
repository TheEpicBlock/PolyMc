package io.github.theepicblock.polymc.impl.generator.asm.stack;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.github.theepicblock.polymc.impl.generator.asm.StackEntryTable;
import net.minecraft.network.PacketByteBuf;

public record KnownDouble(double d) implements StackEntry {
    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(d);
    }

    @Override
    public <T> T extractAs(Class<T> type) {
        if (type == double.class) {
            return (T)(Double)d;
        }
        return StackEntry.super.extractAs(type);
    }

    @Override
    public void write(PacketByteBuf buf, StackEntryTable table) {
        buf.writeDouble(this.d);
    }

    public static StackEntry read(PacketByteBuf buf, StackEntryTable table) {
        return new KnownDouble(buf.readDouble());
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
