package io.github.theepicblock.polymc.api.wizard;

import net.minecraft.entity.EntityType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * Represents nothing more then an entity id. You can instruct packets to be sent with this id.
 * No other state about the entity is stored
 */
public interface VirtualEntity {
    EntityType<?> getEntityType();

    int getId();

    void spawn(ServerPlayerEntity playerEntity, Vec3d pos);

    void remove(ServerPlayerEntity playerEntity);
}
