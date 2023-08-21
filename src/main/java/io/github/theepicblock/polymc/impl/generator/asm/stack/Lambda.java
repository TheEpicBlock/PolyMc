package io.github.theepicblock.polymc.impl.generator.asm.stack;

import com.google.gson.JsonElement;
import io.github.theepicblock.polymc.impl.generator.asm.InternalName;
import io.github.theepicblock.polymc.impl.generator.asm.StackEntryTable;
import net.minecraft.network.PacketByteBuf;
import org.objectweb.asm.Handle;

public record Lambda(Handle method, String name, @InternalName String type, StackEntry[] extraArguments) implements StackEntry {
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
    public void write(PacketByteBuf buf, StackEntryTable table) {
        buf.writeVarInt(method.getTag());
        buf.writeString(method.getOwner());
        buf.writeString(method.getName());
        buf.writeString(method.getDesc());
        buf.writeBoolean(method.isInterface());
        buf.writeString(name);
        buf.writeString(type);
        buf.writeVarInt(extraArguments.length);
        for (var entry : extraArguments) {
            buf.writeNullable(entry, (buf2, e) -> e.writeWithTag(buf2, table));
        }
    }

    public static StackEntry read(PacketByteBuf buf, StackEntryTable table) {
        var handle = new Handle(
                buf.readVarInt(),
                buf.readString(),
                buf.readString(),
                buf.readString(),
                buf.readBoolean()
        );
        var name = buf.readString();
        var desc = buf.readString();
        var length = buf.readVarInt();
        var extraArguments = new StackEntry[length];
        for(int i = 0; i < length; i++) {
            extraArguments[i] = buf.readNullable(buf2 -> StackEntry.readWithTag(buf2, table));
        }
        return new Lambda(handle, name, desc, extraArguments);
    }

    @Override
    public boolean isConcrete() {
        return true;
    }
}