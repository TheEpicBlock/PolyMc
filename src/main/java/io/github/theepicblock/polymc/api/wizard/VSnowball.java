package io.github.theepicblock.polymc.api.wizard;

import io.github.theepicblock.polymc.impl.poly.wizard.VThrownItemEntity;
import net.minecraft.entity.EntityType;

public class VSnowball extends VThrownItemEntity {
    @Override
    public EntityType<?> getEntityType() {
        return EntityType.SNOWBALL;
    }
}
