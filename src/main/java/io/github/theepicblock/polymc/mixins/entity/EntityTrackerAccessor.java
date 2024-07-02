package io.github.theepicblock.polymc.mixins.entity;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.world.ServerChunkLoadingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(ServerChunkLoadingManager.EntityTracker.class)
public interface EntityTrackerAccessor {
    @Accessor
    EntityTrackerEntry getEntry();

    @Accessor
    Set<PlayerAssociatedNetworkHandler> getListeners();

    @Accessor
    Entity getEntity();
}
