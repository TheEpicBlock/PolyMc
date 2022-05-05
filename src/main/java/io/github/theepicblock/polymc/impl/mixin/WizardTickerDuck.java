package io.github.theepicblock.polymc.impl.mixin;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.wizard.Wizard;
import net.minecraft.util.math.ChunkPos;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface WizardTickerDuck {
    void polymc$addBlockTicker(PolyMap polyMap, ChunkPos pos, Wizard wizard);
    void polymc$removeBlockTicker(PolyMap polyMap, ChunkPos pos, Wizard wizard);
    Map<PolyMap, Map<ChunkPos, List<Wizard>>> polymc$getBlockTickers();

    // Entity tickers are only used in threaded mode
    void polymc$addEntityTicker(PolyMap polyMap, Wizard wizard);
    void polymc$removeEntityTicker(PolyMap polyMap, Wizard wizard);
    Map<PolyMap, Set<Wizard>> polymc$getEntityTickers();
}
