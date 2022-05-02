package io.github.theepicblock.polymc.impl.poly.wizard;

import io.github.theepicblock.polymc.api.wizard.PacketConsumer;
import io.github.theepicblock.polymc.api.wizard.VirtualEntity;
import io.github.theepicblock.polymc.mixins.wizards.EntityAccessor;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public abstract class AbstractVirtualEntity implements VirtualEntity {
    protected final UUID uuid;
    protected final int id;

    public AbstractVirtualEntity() {
        this.uuid = MathHelper.randomUuid();
        this.id = EntityUtil.getNewEntityId();
    }

    public AbstractVirtualEntity(UUID uuid, int id) {
        this.uuid = uuid;
        this.id = id;
    }

    @Override
    public void spawn(PacketConsumer player, Vec3d pos) {
        player.sendPacket(new EntitySpawnS2CPacket(
                this.id,
                MathHelper.randomUuid(),
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                0,
                0,
                this.getEntityType(),
                0,
                Vec3d.ZERO
        ));
    }

    public void spawn(PacketConsumer player, Vec3d pos, float pitch, float yaw, int entityData, Vec3d velocity) {
        player.sendPacket(new EntitySpawnS2CPacket(
                this.id,
                MathHelper.randomUuid(),
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                pitch,
                yaw,
                this.getEntityType(),
                entityData,
                velocity
        ));
    }

    public void move(PacketConsumer player, Vec3d pos, byte yaw, byte pitch, boolean onGround) {
        move(player, pos.getX(), pos.getY(), pos.getZ(), yaw, pitch, onGround);
    }

    public void move(PacketConsumer player, double x, double y, double z, byte yaw, byte pitch, boolean onGround) {
        player.sendPacket(EntityUtil.createEntityPositionPacket(
                this.id,
                x,
                y,
                z,
                yaw,
                pitch,
                onGround
        ));
    }

    public void sendVelocity(PacketConsumer player, Vec3d velocity) {
        sendVelocity(player, velocity.x, velocity.y, velocity.z);
    }

    public void sendVelocity(PacketConsumer player, double x, double y, double z) {
        player.sendPacket(EntityUtil.createEntityVelocityUpdate(
                this.id,
                (int)(MathHelper.clamp(x, -3.9, 3.9) * 8000.0),
                (int)(MathHelper.clamp(x, -3.9, 3.9) * 8000.0),
                (int)(MathHelper.clamp(x, -3.9, 3.9) * 8000.0)
        ));
    }

    @Override
    public void remove(PacketConsumer player) {
        player.sendDeathPacket(this.id);
    }

    @Override
    public int getId() {
        return this.id;
    }

    public void setSilent(PacketConsumer player, boolean isSilent) {
        player.sendPacket(EntityUtil.createDataTrackerUpdate(
                this.id,
                EntityAccessor.getSilentTracker(),
                isSilent
        ));
    }

    public void setNoGravity(PacketConsumer player, boolean noGrav) {
        player.sendPacket(EntityUtil.createDataTrackerUpdate(
                this.id,
                EntityAccessor.getNoGravityTracker(),
                noGrav
        ));
    }

    public void sendFlags(PacketConsumer player, boolean onFire, boolean sneaking, boolean sprinting, boolean swimming, boolean invisible, boolean glowing, boolean fallFlying) {
        byte flag = 0;
        if (onFire)     flag += 1 << 0;
        if (sneaking)   flag += 1 << 1;
        if (sprinting)  flag += 1 << 3;
        if (swimming)   flag += 1 << 4;
        if (invisible)  flag += 1 << 5;
        if (glowing)    flag += 1 << 6;
        if (fallFlying) flag += 1 << 7;

        player.sendPacket(EntityUtil.createDataTrackerUpdate(
                this.id,
                EntityAccessor.getFlagTracker(),
                flag
        ));
    }
}
