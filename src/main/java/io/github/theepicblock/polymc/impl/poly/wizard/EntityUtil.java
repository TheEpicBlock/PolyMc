package io.github.theepicblock.polymc.impl.poly.wizard;

import io.github.theepicblock.polymc.mixins.wizards.EntityAccessor;
import io.github.theepicblock.polymc.mixins.wizards.EntityTrackerAccessor;
import io.github.theepicblock.polymc.mixins.wizards.block.EntityPositionPacketAccessor;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;

import java.util.ArrayList;
import java.util.List;

public class EntityUtil {
    public static int getNewEntityId() {
        return EntityAccessor.getMaxEntityId().incrementAndGet();
    }

    public static EntityPositionS2CPacket createEntityPositionPacket(
            int id, double x, double y, double z, byte yaw, byte pitch, boolean onGround) {
        EntityPositionS2CPacket packet = new EntityPositionS2CPacket();
        EntityPositionPacketAccessor accessor = ((EntityPositionPacketAccessor)packet);

        accessor.setId(id);
        accessor.setX(x);
        accessor.setY(y);
        accessor.setZ(z);
        accessor.setYaw(yaw);
        accessor.setPitch(pitch);
        accessor.setOnGround(onGround);

        return packet;
    }

    public static <T> EntityTrackerUpdateS2CPacket createDataTrackerUpdate(int id, TrackedData<T> tracker, T value) {
        EntityTrackerUpdateS2CPacket packet = new EntityTrackerUpdateS2CPacket();
        EntityTrackerAccessor accessor = (EntityTrackerAccessor)packet;
        accessor.setId(id);

        List<DataTracker.Entry<?>> list = new ArrayList<>(1);
        list.add(new DataTracker.Entry<>(tracker, value));
        accessor.setTrackedValues(list);

        return packet;
    }
}
