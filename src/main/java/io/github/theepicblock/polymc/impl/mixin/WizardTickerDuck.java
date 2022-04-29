package io.github.theepicblock.polymc.impl.mixin;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.wizard.Wizard;
import net.minecraft.util.math.ChunkPos;

import java.util.List;
import java.util.Map;

public interface WizardTickerDuck {
    void polymc$addTicker(PolyMap polyMap, ChunkPos pos, Wizard wizard);
    void polymc$removeTicker(PolyMap polyMap, ChunkPos pos, Wizard wizard);
    Map<PolyMap, Map<ChunkPos, List<Wizard>>> polymc$getTickers();
}
