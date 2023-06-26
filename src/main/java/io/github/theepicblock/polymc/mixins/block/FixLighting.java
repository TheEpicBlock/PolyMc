package io.github.theepicblock.polymc.mixins.block;

import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.mixins.TACSAccessor;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(ChunkHolder.class)
public abstract class FixLighting {

    @Shadow @Final private ChunkHolder.PlayersWatchingChunkProvider playersWatchingChunkProvider;

    @Shadow public abstract ChunkPos getPos();

    @Shadow @Final
    ChunkPos pos;

    /**
     * Minecraft usually only sends lighting packets when a chunk is on the watch distance edge.
     * This mixin forces lighting packets to be sent regardless, to make sure vanilla clients are kept in sync.
     */
    @Redirect(method = "flushUpdates", at = @At(value="INVOKE", target = "Lnet/minecraft/server/world/ChunkHolder$PlayersWatchingChunkProvider;getPlayersWatchingChunk(Lnet/minecraft/util/math/ChunkPos;Z)Ljava/util/List;"))
    private List<ServerPlayerEntity> onGetPlayersWatchingChunk(ChunkHolder.PlayersWatchingChunkProvider chunkHolder, ChunkPos chunkPos, boolean onlyOnWatchDistanceEdge) {

        // Get all the watchers anyway
        List<ServerPlayerEntity> watchers = this.playersWatchingChunkProvider.getPlayersWatchingChunk(this.pos, false);

        if (onlyOnWatchDistanceEdge == false) {
            // This will be sent to everyone regardless. Just use the normal method
            return watchers;
        }

        var watchDistance = ((TACSAccessor)this.playersWatchingChunkProvider).getWatchDistance();

        return watchers.stream()
                .filter(watcher -> {
                    var polymap = Util.tryGetPolyMap(watcher);
                    var isVanilla = polymap.isVanillaLikeMap();
                    var watcherChunk = watcher.getWatchedSection();

                    var isOnEdge = TACSAccessor.callIsOnDistanceEdge(this.getPos().x, this.getPos().z, watcherChunk.getSectionX(), watcherChunk.getSectionZ(), watchDistance);

                    return isVanilla || isOnEdge;
                })
                .toList();
    }
}
