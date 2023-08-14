package io.github.theepicblock.polymc.api.wizard;

import io.github.theepicblock.polymc.impl.poly.wizard.VThrownItemEntity;
import net.minecraft.entity.EntityType;

import java.util.UUID;

public class VSnowball extends VThrownItemEntity {
    public VSnowball() {
        super();
    }

    public VSnowball(UUID uuid, int id) {
        super(uuid, id);
    }

    @Override
    public EntityType<?> getEntityType() {
        return EntityType.SNOWBALL;
    }
}
