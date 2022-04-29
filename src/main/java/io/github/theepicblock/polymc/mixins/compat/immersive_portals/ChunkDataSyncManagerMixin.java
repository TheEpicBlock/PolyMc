package io.github.theepicblock.polymc.mixins.compat.immersive_portals;

import io.github.theepicblock.polymc.impl.misc.WatchListener;
import io.github.theepicblock.polymc.impl.mixin.ChunkPacketStaticHack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.chunk_loading.ChunkDataSyncManager;
import qouteall.imm_ptl.core.chunk_loading.DimensionalChunkPos;
import qouteall.imm_ptl.core.chunk_loading.NewChunkTrackingGraph;
import qouteall.imm_ptl.core.ducks.IEThreadedAnvilChunkStorage;

/**
 * Replaces {@link io.github.theepicblock.polymc.mixins.block.implementations.ChunkDataPlayerProvider} and {@link io.github.theepicblock.polymc.mixins.wizards.block.WatchProviderMixin} when immersive portals is active
 */
@Mixin(ChunkDataSyncManager.class)
public abstract class ChunkDataSyncManagerMixin {
    @Shadow protected abstract void sendChunkDataPacketNow(ServerPlayerEntity player, DimensionalChunkPos chunkPos, IEThreadedAnvilChunkStorage ieStorage);

    @Inject(method = "sendChunkDataPacketNow(Lnet/minecraft/server/network/ServerPlayerEntity;Lqouteall/imm_ptl/core/chunk_loading/DimensionalChunkPos;Lqouteall/imm_ptl/core/ducks/IEThreadedAnvilChunkStorage;)V",
            at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "net/minecraft/network/packet/s2c/play/ChunkDataS2CPacket.<init> (Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/world/chunk/light/LightingProvider;Ljava/util/BitSet;Ljava/util/BitSet;Z)V"))
    public void chunkDataPacketInitInject(ServerPlayerEntity player, DimensionalChunkPos chunkPos, IEThreadedAnvilChunkStorage ieStorage, CallbackInfo ci) {
        ChunkPacketStaticHack.player.set(player);
    }

    @Inject(method = "sendChunkDataPacketNow(Lnet/minecraft/server/network/ServerPlayerEntity;Lqouteall/imm_ptl/core/chunk_loading/DimensionalChunkPos;Lqouteall/imm_ptl/core/ducks/IEThreadedAnvilChunkStorage;)V",
            at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "net/minecraft/network/packet/s2c/play/ChunkDataS2CPacket.<init> (Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/world/chunk/light/LightingProvider;Ljava/util/BitSet;Ljava/util/BitSet;Z)V"))
    public void chunkDataPacketInitPostInject(ServerPlayerEntity player, DimensionalChunkPos chunkPos, IEThreadedAnvilChunkStorage ieStorage, CallbackInfo ci) {
        ChunkPacketStaticHack.player.set(null);
    }

    @Inject(method = "onBeginWatch", at = @At("HEAD"))
    public void onBeginWatch(ServerPlayerEntity player, DimensionalChunkPos chunkPos, CallbackInfo ci) {
        if (chunkPos.dimension == player.getWorld().getRegistryKey()) {
            var pos = chunkPos.getChunkPos();
            var chunk = player.getWorld().getChunk(pos.x, pos.z);
            ((WatchListener)chunk).polymc$addPlayer(player);
        }
    }

    @Inject(method = "onEndWatch", at = @At("HEAD"))
    public void onEndWatch(ServerPlayerEntity player, DimensionalChunkPos chunkPos, CallbackInfo ci) {
        if (chunkPos.dimension == player.getWorld().getRegistryKey()) {
            var pos = chunkPos.getChunkPos();
            var chunk = player.getWorld().getChunk(pos.x, pos.z);
            ((WatchListener)chunk).polymc$removePlayer(player);
        }
    }

    @Inject(method = "onChunkProvidedDeferred", at = @At("HEAD"), cancellable = true)
    public void yeahIKnowThisIsHeckingBad(WorldChunk chunk, CallbackInfo ci) {
        var dimension = chunk.getWorld().getRegistryKey();
        var tacs = ((ServerWorld)chunk.getWorld()).getChunkManager().threadedAnvilChunkStorage;
        var pos = new DimensionalChunkPos(dimension, chunk.getPos());

        NewChunkTrackingGraph.getPlayersViewingChunk(
                dimension, chunk.getPos().x, chunk.getPos().z
        ).forEach(playerEntity -> {
            this.sendChunkDataPacketNow(playerEntity, pos, (IEThreadedAnvilChunkStorage)tacs);
        });
        ci.cancel();
    }
}
