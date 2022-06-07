package io.github.theepicblock.polymc.mixins.entity;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.world.EntityTrackingListener;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(ThreadedAnvilChunkStorage.EntityTracker.class)
public interface EntityTrackerAccessor {
    @Accessor
    EntityTrackerEntry getEntry();

    @Accessor
    Set<EntityTrackingListener> getListeners();

    @Accessor
    Entity getEntity();
}
