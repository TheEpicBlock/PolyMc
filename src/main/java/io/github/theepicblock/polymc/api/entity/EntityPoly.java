package io.github.theepicblock.polymc.api.entity;

import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.api.wizard.WizardInfo;
import net.minecraft.entity.Entity;

public interface EntityPoly<T extends Entity> {
    Wizard createWizard(WizardInfo info, T entity);
}
