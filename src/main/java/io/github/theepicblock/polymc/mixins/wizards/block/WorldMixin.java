package io.github.theepicblock.polymc.mixins.wizards.block;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.impl.ConfigManager;
import io.github.theepicblock.polymc.impl.mixin.WizardTickerDuck;
import io.github.theepicblock.polymc.impl.poly.wizard.ThreadedWizardUpdater;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * See {@link io.github.theepicblock.polymc.PolyMc} and {@link ThreadedWizardUpdater} for where these are ticked from
 * See {@link WorldChunkMixin} for where the wizards are put into the list
 */
@Mixin(ServerWorld.class)
public class WorldMixin implements WizardTickerDuck {
    @Unique
    private final Map<PolyMap, Map<ChunkPos, List<Wizard>>> tickingWizards = new Reference2ObjectArrayMap<>();

    @Override
    public void polymc$addTicker(PolyMap polyMap, ChunkPos pos, Wizard wizard) {
        if (ConfigManager.getConfig().enableWizardThreading) {
            this.tickingWizards
                    .computeIfAbsent(polyMap, v -> new ConcurrentHashMap<>())
                    .computeIfAbsent(pos, v -> ObjectLists.synchronize(new ObjectArrayList<>()))
                    .add(wizard);
        } else {
            this.tickingWizards
                    .computeIfAbsent(polyMap, v -> new HashMap<>())
                    .computeIfAbsent(pos, v -> new ArrayList<>())
                    .add(wizard);
        }
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
