package io.github.theepicblock.polymc.api.entity;

import io.github.theepicblock.polymc.api.wizard.Wizard;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;

public interface EntityPoly<T extends Entity> {
    Wizard createWizard(ServerWorld world, T entity);
}
