package io.github.theepicblock.polymc.api.block;

import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.api.wizard.WizardInfo;

@FunctionalInterface
public interface WizardConstructor {
    Wizard construct(WizardInfo info);
}
