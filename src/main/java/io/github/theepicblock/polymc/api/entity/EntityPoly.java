package io.github.theepicblock.polymc.api.entity;

import io.github.theepicblock.polymc.api.wizard.Wizard;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public interface EntityPoly<T extends Entity> {
    Wizard createWizard(ServerWorld world, Vec3d pos, T entity);
}
