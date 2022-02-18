package io.github.theepicblock.polymc.api.wizard;

import io.github.theepicblock.polymc.impl.poly.wizard.AbstractVirtualEntity;
import io.github.theepicblock.polymc.impl.poly.wizard.EntityUtil;
import io.github.theepicblock.polymc.mixins.wizards.ItemFrameEntityAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class VItemFrame extends AbstractVirtualEntity {
    public void spawn(ServerPlayerEntity playerEntity, Vec3d pos, Direction facing) {
        playerEntity.networkHandler.sendPacket(new EntitySpawnS2CPacket(
                id,
                MathHelper.randomUuid(),
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                0,
                0,
                this.getEntityType(),
                facing.ordinal(),
                Vec3d.ZERO
        ));
    }

    public void sendItemStack(ServerPlayerEntity playerEntity, ItemStack stack) {
        playerEntity.networkHandler.sendPacket(EntityUtil.createDataTrackerUpdate(
                this.id,
                ItemFrameEntityAccessor.getItemStackTracker(),
                stack.copy()
        ));
    }

    public void makeInvisible(ServerPlayerEntity playerEntity) {
        this.sendFlags(playerEntity,
                false,
                false,
                false,
                false,
                true,
                false,
                false);
    }

    @Override
    public EntityType<?> getEntityType() {
        return EntityType.ITEM_FRAME;
    }
}
