package io.github.theepicblock.polymc.mixins.wizards.block;

import io.github.theepicblock.polymc.impl.misc.WatchListener;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class WatchProviderMixin {
    @Shadow protected abstract ChunkHolder getChunkHolder(long pos);

    @Inject(method = "sendChunkDataPackets(Lnet/minecraft/server/network/ServerPlayerEntity;Lorg/apache/commons/lang3/mutable/MutableObject;Lnet/minecraft/world/chunk/WorldChunk;)V",
            at = @At("HEAD"))
    private void onSendChunkData(ServerPlayerEntity player, MutableObject<ChunkDataS2CPacket> cachedDataPacket, WorldChunk chunk, CallbackInfo ci) {
        ((WatchListener)chunk).polymc$addPlayer(player);
    }

    @Inject(method = "sendWatchPackets(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/math/ChunkPos;Lorg/apache/commons/lang3/mutable/MutableObject;ZZ)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;sendUnloadChunkPacket(Lnet/minecraft/util/math/ChunkPos;)V"))
    private void onSendUnloadPacket(ServerPlayerEntity player, ChunkPos pos, MutableObject<ChunkDataS2CPacket> mutableObject, boolean oldWithinViewDistance, boolean newWithinViewDistance, CallbackInfo ci) {
        var chunkHolder = this.getChunkHolder(pos.toLong());
        if (chunkHolder == null) return;

        var chunk = chunkHolder.getWorldChunk();
        if (chunk == null) return;

        ((WatchListener)chunk).polymc$removePlayer(player);
    }
}
