package io.github.theepicblock.polymc.impl.mixin;

import io.github.theepicblock.polymc.api.wizard.Wizard;

import java.util.List;

public interface WizardTickerDuck {
    void polymc$addTicker(Wizard wizard);
    void polymc$removeTicker(Wizard wizard);
    List<Wizard> polymc$getTickers();
}
