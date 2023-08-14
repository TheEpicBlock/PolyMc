package io.github.theepicblock.polymc.impl.generator.asm.stack;

import com.google.gson.JsonElement;
import net.minecraft.network.PacketByteBuf;
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
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(method.getTag());
        buf.writeString(method.getOwner());
        buf.writeString(method.getName());
        buf.writeString(method.getDesc());
        buf.writeBoolean(method.isInterface());
        buf.writeVarInt(extraArguments.length);
        for (var entry : extraArguments) {
            buf.writeNullable(entry, (buf2, e) -> e.writeWithTag(buf2));
        }
    }

    public static StackEntry read(PacketByteBuf buf) {
        var handle = new Handle(
                buf.readVarInt(),
                buf.readString(),
                buf.readString(),
                buf.readString(),
                buf.readBoolean()
        );
        var length = buf.readVarInt();
        var extraArguments = new StackEntry[length];
        for(int i = 0; i < length; i++) {
            extraArguments[i] = buf.readNullable(StackEntry::readWithTag);
        }
        return new Lambda(handle, extraArguments);
    }

    @Override
    public boolean isConcrete() {
        return true;
    }
}