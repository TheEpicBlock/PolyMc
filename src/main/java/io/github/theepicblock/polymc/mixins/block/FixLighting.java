package io.github.theepicblock.polymc.mixins.block;

import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.mixins.TACSAccessor;
import net.minecraft.network.Packet;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkHolder.class)
public abstract class FixLighting {

    @Shadow protected abstract void sendPacketToPlayersWatching(Packet<?> packet, boolean onlyOnWatchDistanceEdge);

    @Shadow @Final private ChunkHolder.PlayersWatchingChunkProvider playersWatchingChunkProvider;

    @Shadow public abstract ChunkPos getPos();

    /**
     * Minecraft usually only sends lighting packets when a chunk is far-away.
     * This mixin forces lighting packets to be sent regardless, to make sure vanilla clients are kept in sync.
     * This replaces the {@code ChunkHolder#sendPacketToPlayersWatching} method.
     *
     * @see net.minecraft.server.world.ThreadedAnvilChunkStorage#getPlayersWatchingChunk(ChunkPos, boolean)
     */
    @Redirect(method = "flushUpdates(Lnet/minecraft/world/chunk/WorldChunk;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkHolder;sendPacketToPlayersWatching(Lnet/minecraft/network/Packet;Z)V"))
    private void onSendLightUpdates(ChunkHolder chunkHolder, Packet<?> packet, boolean onlyOnWatchDistanceEdge) {
        if (onlyOnWatchDistanceEdge == false) {
            // This will be sent to everyone regardless. Just use the normal method
            this.sendPacketToPlayersWatching(packet, false);
        }

        // Used to determine if a chunk is on the watchDistance edge.
        // Assumes that playersWatchingChunkProvider is an instance of TACS. Which should always be the case in vanilla
        var watchDistance = ((TACSAccessor)this.playersWatchingChunkProvider).getWatchDistance();

        var watchers = this.playersWatchingChunkProvider.getPlayersWatchingChunk(this.getPos(), false);
        watchers.forEach((watcher) -> {
            var polymap = PolyMapProvider.getPolyMap(watcher);
            var isVanilla = polymap.isVanillaLikeMap();

            var isOnEdge = TACSAccessor.callIsOnDistanceEdge(this.getPos(), watcher, true, watchDistance);

            if (isVanilla || isOnEdge) {
                watcher.networkHandler.sendPacket(packet);
            }
        });
    }
}
