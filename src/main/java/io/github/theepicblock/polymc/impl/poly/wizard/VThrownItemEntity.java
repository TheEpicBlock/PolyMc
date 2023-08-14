package io.github.theepicblock.polymc.impl.poly.wizard;

import io.github.theepicblock.polymc.api.wizard.PacketConsumer;
import io.github.theepicblock.polymc.mixins.wizards.ThrownItemEntityAccessor;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public abstract class VThrownItemEntity extends AbstractVirtualEntity {
    public VThrownItemEntity() {
        super();
    }

    public VThrownItemEntity(UUID uuid, int id) {
        super(uuid, id);
    }

    public void sendItem(PacketConsumer player, ItemStack item) {
        player.sendPacket(EntityUtil.createDataTrackerUpdate(
                this.id,
                ThrownItemEntityAccessor.polymc$getTrackedItem(),
                item
        ));
    }
}
