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

    @Invoker
    static boolean callIsOnDistanceEdge(int sourceChunkX, int sourceChunkZ, int playerChunkX, int playerChunkZ, int watchDistance) {
        throw new IllegalStateException();
    }

    @Invoker
    static boolean callIsWithinDistance(int sourceChunkX, int sourceChunkZ, int playerChunkX, int playerChunkZ, int watchDistance) {
        throw new IllegalStateException();
    }

    @Accessor
    Int2ObjectMap<ThreadedAnvilChunkStorage.EntityTracker> getEntityTrackers();
}
