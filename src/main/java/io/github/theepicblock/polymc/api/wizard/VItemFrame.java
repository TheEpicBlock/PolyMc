package io.github.theepicblock.polymc.api.wizard;

import io.github.theepicblock.polymc.impl.poly.wizard.AbstractVirtualEntity;
import io.github.theepicblock.polymc.impl.poly.wizard.EntityUtil;
import io.github.theepicblock.polymc.mixins.wizards.ItemFrameEntityAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class VItemFrame extends AbstractVirtualEntity {
    public void spawn(PacketConsumer player, Vec3d pos, Direction facing) {
        player.sendPacket(new EntitySpawnS2CPacket(
                id,
                MathHelper.randomUuid(),
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                0,
                0,
                this.getEntityType(),
                facing.ordinal(),
                Vec3d.ZERO,
                0
        ));
    }

    public void sendItemStack(PacketConsumer player, ItemStack stack) {
        player.sendPacket(EntityUtil.createDataTrackerUpdate(
                this.id,
                ItemFrameEntityAccessor.getItemStackTracker(),
                stack.copy()
        ));
    }

    public void makeInvisible(PacketConsumer playerEntity) {
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
