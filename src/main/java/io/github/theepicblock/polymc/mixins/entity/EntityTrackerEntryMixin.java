package io.github.theepicblock.polymc.mixins.entity;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.impl.misc.PolyMapMap;
import io.github.theepicblock.polymc.impl.mixin.EntityTrackerEntryDuck;
import io.github.theepicblock.polymc.impl.mixin.WizardTickerDuck;
import io.github.theepicblock.polymc.impl.poly.entity.EntityWizard;
import io.github.theepicblock.polymc.impl.poly.wizard.EntityWizardInfo;
import io.github.theepicblock.polymc.impl.poly.wizard.PolyMapFilteredPlayerView;
import io.github.theepicblock.polymc.impl.poly.wizard.SinglePlayerView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin implements EntityTrackerEntryDuck {
    @Shadow @Final private Entity entity;
    @Shadow @Final private ServerWorld world;

    @SuppressWarnings("unchecked")
    @Unique
    private final PolyMapMap<Wizard> wizards = new PolyMapMap<>(polyMap -> {
        var poly = polyMap.getEntityPoly((EntityType<Entity>)this.entity.getType());
        if (poly == null) return null;
        try {
            var wizard = poly.createWizard(new EntityWizardInfo(this.entity), this.entity);
            if (wizard != null) ((WizardTickerDuck)this.world).polymc$addEntityTicker(polyMap, wizard);
            return wizard;
        } catch (Throwable t) {
            PolyMc.LOGGER.error("Failed to create block wizard for "+this.entity+" | "+poly);
            t.printStackTrace();
            return null;
        }
    });

    @Override
    public PolyMapMap<Wizard> polymc$getWizards() {
        return wizards;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        // FIXME, use the list of listener inside ThreadedAnvilChunkStorage$EntityTracker
        var allPlayers = PolyMapFilteredPlayerView.getAll(world, this.entity.getBlockPos());
        wizards.forEach((polyMap, wizard) -> {
            if (wizard == null) return;
            var filteredView = new PolyMapFilteredPlayerView(allPlayers, polyMap);
            try {
                wizard.onMove(filteredView); // TODO check if the entity actually moved
                wizard.onTick(filteredView);
            } catch (Throwable t) {
                PolyMc.LOGGER.error("Error ticking entity wizard");
                t.printStackTrace();
            }
            filteredView.sendBatched();
        });
    }

    @Inject(method = "startTracking", at = @At("HEAD"))
    private void onStartTracking(ServerPlayerEntity player, CallbackInfo ci) {
        var polymap = PolyMapProvider.getPolyMap(player);
        var wizard = wizards.get(polymap);
        if (wizard != null) {
            try {
                var view = new SinglePlayerView(player);
                wizard.addPlayer(view);
                view.sendBatched();
            } catch (Throwable t) {
                PolyMc.LOGGER.error("Error adding player to entity wizard");
                t.printStackTrace();
            }
        }
    }

    @Inject(method = "stopTracking", at = @At("HEAD"))
    private void onStopTracking(ServerPlayerEntity player, CallbackInfo ci) {
        var polymap = PolyMapProvider.getPolyMap(player);
        var wizard = wizards.get(polymap);
        if (wizard != null) {
            try {
                var view = new SinglePlayerView(player);
                wizard.removePlayer(view);
                view.sendBatched();
            } catch (Throwable t) {
                PolyMc.LOGGER.error("Error removing player from entity wizard");
                t.printStackTrace();
            }
        }
    }

    @Inject(method = "syncEntityData", at = @At("HEAD"), cancellable = true)
    private void preventSyncEntityData(CallbackInfo ci) {

        if (wizards.isEmpty()) return;

        AtomicBoolean hasValidWizard = new AtomicBoolean(false);
        wizards.forEach((polyMap, wizard) -> {

            if (!(wizard instanceof EntityWizard<?> entityWizard)) return;

            hasValidWizard.set(true);
            var allPlayers = PolyMapFilteredPlayerView.getAll(world, this.entity.getBlockPos());
            var filteredView = new PolyMapFilteredPlayerView(allPlayers, polyMap);

            try {
                entityWizard.syncEntityData(filteredView);
            } catch (Throwable t) {
                PolyMc.LOGGER.error("Error syncing entity wizard");
                t.printStackTrace();
            }
            filteredView.sendBatched();
        });

        if (!hasValidWizard.get()) return;

        if (this.entity instanceof LivingEntity livingEntity) {
            Set<EntityAttributeInstance> set = ((LivingEntity)this.entity).getAttributes().getTracked();
            set.clear();
        }

        ci.cancel();
    }
}
