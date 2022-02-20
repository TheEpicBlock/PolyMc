package io.github.theepicblock.polymc.impl.poly.wizard;

import io.github.theepicblock.polymc.mixins.wizards.EntityAccessor;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;

import java.util.ArrayList;
import java.util.List;

public class EntityUtil {
    public static int getNewEntityId() {
        return EntityAccessor.getEntityIdCounter().incrementAndGet();
    }

    public static EntityPositionS2CPacket createEntityPositionPacket(
            int id, double x, double y, double z, byte yaw, byte pitch, boolean onGround) {
        PacketByteBuf byteBuf = PacketByteBufs.create();
        byteBuf.writeVarInt(id);
        byteBuf.writeDouble(x);
        byteBuf.writeDouble(y);
        byteBuf.writeDouble(z);
        byteBuf.writeByte(yaw);
        byteBuf.writeByte(pitch);
        byteBuf.writeBoolean(onGround);

        return new EntityPositionS2CPacket(byteBuf);
    }

    public static EntityVelocityUpdateS2CPacket createEntityVelocityUpdate(int id, int x, int y, int z) {
        PacketByteBuf byteBuf = PacketByteBufs.create();
        byteBuf.writeVarInt(id);
        byteBuf.writeShort(x);
        byteBuf.writeShort(y);
        byteBuf.writeShort(z);

        return new EntityVelocityUpdateS2CPacket(byteBuf);
    }

    public static <T> EntityTrackerUpdateS2CPacket createDataTrackerUpdate(int id, TrackedData<T> tracker, T value) {
        return new EntityTrackerUpdateS2CPacket(id,
                new DataTracker(null) {
                    @Override
                    public List<Entry<?>> getDirtyEntries() {
                        List<Entry<?>> list = new ArrayList<>(1);
                        list.add(new Entry<>(tracker, value));
                        return list;
                    }
                },
                false);
    }
}
