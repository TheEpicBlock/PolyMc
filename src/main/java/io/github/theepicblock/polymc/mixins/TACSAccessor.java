package io.github.theepicblock.polymc.mixins;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ThreadedAnvilChunkStorage.class)
public interface TACSAccessor {
    @Accessor
    int getWatchDistance();

    @Accessor
    Int2ObjectMap<ThreadedAnvilChunkStorage.EntityTracker> getEntityTrackers();
}
