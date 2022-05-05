package io.github.theepicblock.polymc.impl.mixin;

import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.impl.misc.PolyMapMap;

public interface EntityTrackerEntryDuck {
    PolyMapMap<Wizard> polymc$getWizards();
}
