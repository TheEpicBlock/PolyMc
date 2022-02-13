package io.github.theepicblock.polymc.mixins.entity;

import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.impl.misc.PolyMapMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
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

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerMixin {
    @Shadow @Final private Entity entity;
    @Shadow @Final private ServerWorld world;

    @SuppressWarnings("unchecked")
    @Unique
    private final PolyMapMap<Wizard> wizards = new PolyMapMap<>(polyMap -> {
        var poly = polyMap.getEntityPoly((EntityType<Entity>)this.entity.getType());
        if (poly == null) return null;
        return poly.createWizard(this.world, this.entity.getPos(), this.entity);
    });

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        wizards.forEach((polyMap, wizard) -> {
            if (wizard != null) wizard.updatePosition(this.entity.getPos());
        });
    }

    @Inject(method = "startTracking", at = @At("HEAD"))
    private void onStartTracking(ServerPlayerEntity player, CallbackInfo ci) {
        var polymap = PolyMapProvider.getPolyMap(player);
        var wizard = wizards.get(polymap);
        if (wizard != null) {
            wizard.addPlayer(player);
        }
    }

    @Inject(method = "stopTracking", at = @At("HEAD"))
    private void onStopTracking(ServerPlayerEntity player, CallbackInfo ci) {
        var polymap = PolyMapProvider.getPolyMap(player);
        var wizard = wizards.get(polymap);
        if (wizard != null) {
            wizard.removePlayer(player);
        }
    }
}
