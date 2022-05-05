package io.github.theepicblock.polymc.impl.poly.wizard;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.api.wizard.PacketConsumer;
import io.github.theepicblock.polymc.impl.mixin.EntityTrackerEntryDuck;
import io.github.theepicblock.polymc.impl.mixin.WizardTickerDuck;
import io.github.theepicblock.polymc.mixins.TACSAccessor;
import io.github.theepicblock.polymc.mixins.entity.EntityTrackerAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.server.world.EntityTrackingListener;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RegularWizardUpdater {
    public static void registerEvents() {
        ServerTickEvents.END_WORLD_TICK.register(RegularWizardUpdater::tick);
    }

    private static void tick(ServerWorld world) {
        var tick = world.getServer().getTicks();
        var updateInfo = new ThreadedWizardUpdater.UpdateInfoImpl(tick, 1); // Called at the end of the tick, therefore delta is 1

        var entityTrackers = ((TACSAccessor)world.getChunkManager().threadedAnvilChunkStorage).getEntityTrackers();
        for (var tracker : entityTrackers.values()) {
            var trackerEntry = (EntityTrackerEntryDuck)((EntityTrackerAccessor)tracker).getEntry();
            var wizards = trackerEntry.polymc$getWizards();
            var listeners = ((EntityTrackerAccessor)tracker).getListeners();
            wizards.forEach((polyMap, wizard) -> {
                if (wizard == null) return;
                var playerView = new EntityListenerPlayerView(listeners, polyMap);
                wizard.update(playerView, updateInfo);
            });
        }

        ((WizardTickerDuck)world).polymc$getBlockTickers()
                .forEach((polyMap, wizardsPerPos) -> {
                    wizardsPerPos.forEach((pos, wizards) -> {
                        var playerView = new CachedPolyMapFilteredPlayerView(PolyMapFilteredPlayerView.getAll(world, pos), polyMap);
                        wizards.forEach(wizard -> {
                            wizard.update(playerView, updateInfo);
                            wizard.onTick(playerView);
                        });
                        playerView.sendBatched();
                    });
                });
    }

    public static class EntityListenerPlayerView implements PacketConsumer {
        private final List<EntityTrackingListener> listeners;

        public EntityListenerPlayerView(Set<EntityTrackingListener> listeners, PolyMap filter) {
            this.listeners = new ArrayList<>();
            for (var listener : listeners) {
                if (PolyMapProvider.getPolyMap(listener.getPlayer()) == filter) {
                    this.listeners.add(listener);
                }
            }
        }

        @Override
        public void sendPacket(Packet<?> packet) {
            for (var listener : listeners) {
                listener.sendPacket(packet);
            }
        }

        @Override
        public void sendDeathPacket(int id) {
            this.sendPacket(new EntitiesDestroyS2CPacket(id));
        }

        @Override
        public void sendBatched() {
        }
    }
}
