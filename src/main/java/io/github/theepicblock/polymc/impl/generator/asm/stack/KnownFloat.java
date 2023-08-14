package io.github.theepicblock.polymc.impl.generator.asm.stack;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.network.PacketByteBuf;

public record KnownFloat(float i) implements StackEntry {
    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(i);
    }

    @Override
    public <T> T extractAs(Class<T> type) {
        if (type == float.class) {
            return (T)(Float)i;
        }
        return StackEntry.super.extractAs(type);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeFloat(this.i);
    }

    public static StackEntry read(PacketByteBuf buf) {
        return new KnownFloat(buf.readFloat());
    }

    @Override
    public boolean isConcrete() {
        return true;
    }
}