package io.github.theepicblock.polymc.mixins.wizards.block;

import io.github.theepicblock.polymc.impl.misc.WatchListener;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class WatchProviderMixin {
    @Shadow protected abstract ChunkHolder getChunkHolder(long pos);

    @Inject(method = "sendChunkDataPackets(Lnet/minecraft/server/network/ServerPlayerEntity;[Lnet/minecraft/network/Packet;Lnet/minecraft/world/chunk/WorldChunk;)V",
            at = @At("HEAD"))
    private void onSendChunkData(ServerPlayerEntity player, Packet<?>[] packets, WorldChunk chunk, CallbackInfo ci) {
        ((WatchListener)chunk).addPlayer(player);
    }

    @Inject(method = "sendWatchPackets(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/math/ChunkPos;[Lnet/minecraft/network/Packet;ZZ)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;sendUnloadChunkPacket(Lnet/minecraft/util/math/ChunkPos;)V"))
    private void onUnloadPacket(ServerPlayerEntity player, ChunkPos pos, Packet<?>[] packets, boolean withinMaxWatchDistance, boolean withinViewDistance, CallbackInfo ci) {
        WorldChunk chunk = this.getChunkHolder(pos.toLong()).getWorldChunk();
        if (chunk != null) {
            ((WatchListener)chunk).removePlayer(player);
        }
    }
}
