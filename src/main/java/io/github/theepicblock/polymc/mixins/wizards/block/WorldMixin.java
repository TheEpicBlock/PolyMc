package io.github.theepicblock.polymc.mixins.wizards.block;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.impl.mixin.WizardTickerDuck;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * See {@link io.github.theepicblock.polymc.PolyMc} for where these are ticked from
 * See {@link WorldChunkMixin} for where the wizards are put into the list
 */
@Mixin(ServerWorld.class)
public class WorldMixin implements WizardTickerDuck {
    @Unique
    private final Map<PolyMap, Map<ChunkPos, List<Wizard>>> tickingWizards = new Reference2ObjectArrayMap<>();

    @Override
    public void polymc$addTicker(PolyMap polyMap, ChunkPos pos, Wizard wizard) {
        this.tickingWizards
                .computeIfAbsent(polyMap, v -> new Object2ObjectOpenHashMap<>())
                .computeIfAbsent(pos, v -> new ArrayList<>())
                .add(wizard);
    }

    @Override
    public void polymc$removeTicker(PolyMap polyMap, ChunkPos pos, Wizard wizard) {
        var wizardsPerPos = this.tickingWizards.get(polyMap);
        if (wizardsPerPos == null) return;

        var wizardList = wizardsPerPos.get(pos);
        if (wizardList == null) return;
        wizardList.remove(wizard);

        if (wizardList.isEmpty()) wizardsPerPos.remove(pos);
    }

    @Override
    public Map<PolyMap, Map<ChunkPos, List<Wizard>>> polymc$getTickers() {
        return tickingWizards;
    }
}
