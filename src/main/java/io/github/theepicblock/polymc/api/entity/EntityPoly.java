package io.github.theepicblock.polymc.api.entity;

import io.github.theepicblock.polymc.api.DebugInfoProvider;
import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.api.wizard.WizardInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

public interface EntityPoly<T extends Entity> extends DebugInfoProvider<EntityType<?>> {
    Wizard createWizard(WizardInfo info, T entity);
}
