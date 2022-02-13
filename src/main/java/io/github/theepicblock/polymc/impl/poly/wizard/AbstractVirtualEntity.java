package io.github.theepicblock.polymc.impl.poly.wizard;

import io.github.theepicblock.polymc.api.wizard.VirtualEntity;
import io.github.theepicblock.polymc.mixins.wizards.EntityAccessor;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
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
    public void spawn(ServerPlayerEntity playerEntity, Vec3d pos) {
        playerEntity.networkHandler.sendPacket(new EntitySpawnS2CPacket(
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

    public void move(ServerPlayerEntity playerEntity, Vec3d pos, byte yaw, byte pitch, boolean onGround) {
        move(playerEntity, pos.getX(), pos.getY(), pos.getZ(), yaw, pitch, onGround);
    }

    public void move(ServerPlayerEntity playerEntity, double x, double y, double z, byte yaw, byte pitch, boolean onGround) {
        playerEntity.networkHandler.sendPacket(EntityUtil.createEntityPositionPacket(
                this.id,
                x,
                y,
                z,
                yaw,
                pitch,
                onGround
        ));
    }

    @Override
    public void remove(ServerPlayerEntity playerEntity) {
        playerEntity.networkHandler.sendPacket(
                new EntitiesDestroyS2CPacket(this.id)
        );
    }

    public void setSilent(ServerPlayerEntity playerEntity, boolean isSilent) {
        playerEntity.networkHandler.sendPacket(EntityUtil.createDataTrackerUpdate(
                this.id,
                EntityAccessor.getSilentTracker(),
                isSilent
        ));
    }

    public void setNoGravity(ServerPlayerEntity playerEntity, boolean noGrav) {
        playerEntity.networkHandler.sendPacket(EntityUtil.createDataTrackerUpdate(
                this.id,
                EntityAccessor.getNoGravityTracker(),
                noGrav
        ));
    }

    public void sendFlags(ServerPlayerEntity playerEntity, boolean onFire, boolean sneaking, boolean sprinting, boolean swimming, boolean invisible, boolean glowing) {
        byte flag = 0;
        if (onFire)     flag += 0b00000001;
        if (sneaking)   flag += 0b00000010;
        if (sprinting)  flag += 0b00000100;
        if (swimming)   flag += 0b00001000;
        if (invisible)  flag += 0b00010000;
        if (glowing)    flag += 0b00100000;

        playerEntity.networkHandler.sendPacket(EntityUtil.createDataTrackerUpdate(
                this.id,
                EntityAccessor.getFlagTracker(),
                flag
        ));
    }
}
