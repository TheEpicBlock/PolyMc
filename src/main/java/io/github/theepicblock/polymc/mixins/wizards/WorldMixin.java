package io.github.theepicblock.polymc.mixins.wizards;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.impl.ConfigManager;
import io.github.theepicblock.polymc.impl.mixin.WizardTickerDuck;
import io.github.theepicblock.polymc.impl.poly.wizard.ThreadedWizardUpdater;
import io.github.theepicblock.polymc.mixins.wizards.block.WorldChunkMixin;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * See {@link io.github.theepicblock.polymc.PolyMc} and {@link ThreadedWizardUpdater} for where these are ticked from
 * See {@link WorldChunkMixin} for where the wizards are put into the list
 */
@Mixin(ServerWorld.class)
public class WorldMixin implements WizardTickerDuck {
    @Unique
    private final Map<PolyMap, Map<ChunkPos, List<Wizard>>> blockTickers = new Reference2ObjectArrayMap<>();
    @Unique // This field is only used in threaded mode
    private final Map<PolyMap, Set<Wizard>> entityTickers = new Reference2ObjectArrayMap<>();

    @Override
    public void polymc$addBlockTicker(PolyMap polyMap, ChunkPos pos, Wizard wizard) {
        if (ConfigManager.getConfig().enableWizardThreading) {
            this.blockTickers
                    .computeIfAbsent(polyMap, v -> new ConcurrentHashMap<>())
                    .computeIfAbsent(pos, v -> ObjectLists.synchronize(new ObjectArrayList<>()))
                    .add(wizard);
        } else {
            this.blockTickers
                    .computeIfAbsent(polyMap, v -> new HashMap<>())
                    .computeIfAbsent(pos, v -> new ArrayList<>())
                    .add(wizard);
        }
    }

    @Override
    public void polymc$removeBlockTicker(PolyMap polyMap, ChunkPos pos, Wizard wizard) {
        var wizardsPerPos = this.blockTickers.get(polyMap);
        if (wizardsPerPos == null) return;

        var wizardList = wizardsPerPos.get(pos);
        if (wizardList == null) return;
        wizardList.remove(wizard);

        if (wizardList.isEmpty()) wizardsPerPos.remove(pos);
    }

    @Override
    public Map<PolyMap, Map<ChunkPos, List<Wizard>>> polymc$getBlockTickers() {
        return blockTickers;
    }

    @Override
    public void polymc$addEntityTicker(PolyMap map, Wizard wizard) {
        if (ConfigManager.getConfig().enableWizardThreading) {
            entityTickers.computeIfAbsent(map, v -> Collections.synchronizedSet(new HashSet<>()))
                    .add(wizard);
        }
    }

    @Override
    public void polymc$removeEntityTicker(PolyMap map, Wizard wizard) {
        if (ConfigManager.getConfig().enableWizardThreading) {
            var set = entityTickers.get(map);
            if (set != null) {
                set.remove(wizard);
            }
        }
    }

    @Override
    public Map<PolyMap, Set<Wizard>> polymc$getEntityTickers() {
        return entityTickers;
    }
}
