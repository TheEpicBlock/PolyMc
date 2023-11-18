package io.github.theepicblock.polymc.mixins.compat.immersive_portals;

import io.github.theepicblock.polymc.impl.misc.WatchListener;
import io.github.theepicblock.polymc.impl.mixin.ChunkPacketStaticHack;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.chunk_loading.PlayerChunkLoading;

@Pseudo
@Mixin(value = PlayerChunkLoading.class)
public class PlayerChunkLoadingMixin {
    @Inject(method = "sendChunkPacket", at = @At("HEAD"), require = 0)
    private static void onSendChunkPacket(ServerPlayNetworkHandler serverGamePacketListenerImpl, ServerWorld serverLevel,
                                          WorldChunk levelChunk, CallbackInfo ci) {
        ChunkPacketStaticHack.player.set(serverGamePacketListenerImpl.player);
    }
    @Inject(method = "sendChunkPacket", at = @At("TAIL"), require = 0)
    private static void onSendChunkPacketTail(ServerPlayNetworkHandler serverGamePacketListenerImpl, ServerWorld serverLevel,
                                              WorldChunk levelChunk, CallbackInfo ci) {
        ((WatchListener) levelChunk).polymc$addPlayer(serverGamePacketListenerImpl.player);
        ChunkPacketStaticHack.player.set(null);
    }
}
