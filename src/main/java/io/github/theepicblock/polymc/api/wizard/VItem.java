package io.github.theepicblock.polymc.api.wizard;

import io.github.theepicblock.polymc.impl.poly.wizard.AbstractVirtualEntity;
import io.github.theepicblock.polymc.impl.poly.wizard.EntityUtil;
import io.github.theepicblock.polymc.mixins.wizards.ItemEntityAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public class VItem extends AbstractVirtualEntity {
    @Override
    public EntityType<?> getEntityType() {
        return EntityType.ITEM;
    }

    public void sendItem(ServerPlayerEntity playerEntity, ItemStack item) {
        playerEntity.networkHandler.sendPacket(EntityUtil.createDataTrackerUpdate(
                this.id,
                ItemEntityAccessor.getStackTracker(),
                item
        ));
    }
}
