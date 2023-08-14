package io.github.theepicblock.polymc.api.wizard;

import io.github.theepicblock.polymc.impl.poly.wizard.AbstractVirtualEntity;
import net.minecraft.entity.EntityType;

import java.util.UUID;

public class VItemDisplay extends AbstractVirtualEntity {
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
}
