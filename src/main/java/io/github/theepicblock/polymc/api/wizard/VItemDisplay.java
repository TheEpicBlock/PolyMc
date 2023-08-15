package io.github.theepicblock.polymc.api.wizard;

import io.github.theepicblock.polymc.impl.poly.wizard.EntityUtil;
import io.github.theepicblock.polymc.mixins.ItemDisplayEntityAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public class VItemDisplay extends AbstractVDisplay {
    public VItemDisplay() {
        super();
    }

    public VItemDisplay(UUID uuid, int id) {
        super(uuid, id);
    }

    @Override
    public EntityType<?> getEntityType() {
        return EntityType.ITEM_DISPLAY;
    }

    public void sendItem(PacketConsumer player, ItemStack item) {
        player.sendPacket(EntityUtil.createDataTrackerUpdate(
                this.id,
                ItemDisplayEntityAccessor.getITEM(),
                item
        ));
    }
}
