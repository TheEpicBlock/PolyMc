package io.github.theepicblock.polymc.impl.poly.wizard;

import io.github.theepicblock.polymc.mixins.wizards.ThrownItemEntityAccessor;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public abstract class VThrownItemEntity extends AbstractVirtualEntity {
    public void sendItem(ServerPlayerEntity playerEntity, ItemStack item) {
        playerEntity.networkHandler.sendPacket(EntityUtil.createDataTrackerUpdate(
                this.id,
                ThrownItemEntityAccessor.polymc$getTrackedItem(),
                item
        ));
    }
}
