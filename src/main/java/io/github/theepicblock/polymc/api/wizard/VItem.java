package io.github.theepicblock.polymc.api.wizard;

import io.github.theepicblock.polymc.impl.poly.wizard.AbstractVirtualEntity;
import io.github.theepicblock.polymc.impl.poly.wizard.EntityUtil;
import io.github.theepicblock.polymc.mixins.wizards.ItemEntityAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public class VItem extends AbstractVirtualEntity {
    public VItem() {
        super();
    }

    public VItem(UUID uuid, int id) {
        super(uuid, id);
    }

    @Override
    public EntityType<?> getEntityType() {
        return EntityType.ITEM;
    }

    public void sendItem(PacketConsumer player, ItemStack item) {
        player.sendPacket(EntityUtil.createDataTrackerUpdate(
                this.id,
                ItemEntityAccessor.getStackTracker(),
                item
        ));
    }
}
