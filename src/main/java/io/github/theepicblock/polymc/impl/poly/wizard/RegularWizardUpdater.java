package io.github.theepicblock.polymc.impl.poly.wizard;

import io.github.theepicblock.polymc.impl.mixin.WizardTickerDuck;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.world.ServerWorld;

public class RegularWizardUpdater {
    public static void registerEvents() {
        ServerTickEvents.END_WORLD_TICK.register(RegularWizardUpdater::tick);
    }

    private static void tick(ServerWorld world) {
        ((WizardTickerDuck)world).polymc$getTickers()
                .forEach((polyMap, wizardsPerPos) -> {
                    wizardsPerPos.forEach((pos, wizards) -> {
                        var playerView = new CachedPolyMapFilteredPlayerView(PolyMapFilteredPlayerView.getAll(world, pos), polyMap);
                        wizards.forEach(wizard -> {
                            wizard.onTick(playerView);
                        });
                        playerView.sendBatched();
                    });
                });
    }
}
