package io.github.theepicblock.polymc.api.entity;

import io.github.theepicblock.polymc.api.DebugInfoProvider;
import io.github.theepicblock.polymc.api.resource.ModdedResources;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.api.wizard.WizardInfo;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import org.jetbrains.annotations.ApiStatus;

public interface EntityPoly<T extends Entity> extends DebugInfoProvider<EntityType<?>> {
    Wizard createWizard(WizardInfo info, T entity);


    @ApiStatus.Experimental
    default void addToResourcePack(EntityType<?> entityType, ModdedResources moddedResources, PolyMcResourcePack pack, SimpleLogger logger) {

    }
}
