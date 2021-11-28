package io.github.theepicblock.polymc.mixins;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ThreadedAnvilChunkStorage.class)
public interface TACSAccessor {
    @Accessor
    int getWatchDistance();

    @Invoker
    static boolean callIsOnDistanceEdge(ChunkPos chunkPos, ServerPlayerEntity player, boolean usePlayerWatchedSection, int distance) {
        throw new IllegalStateException();
    }
}
