package io.github.theepicblock.polymc.mixins.compat.immersive_portals;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.theepicblock.polymc.impl.misc.WatchListener;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.chunk_loading.ImmPtlChunkTracking;

import java.util.ArrayList;
import java.util.Map;

@Mixin(ImmPtlChunkTracking.class)
public class ImmPtlChunkTrackingMixin {
    @Inject(method = "lambda$purge$5", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"), require = 0)
    private static void unloadChunkOnClient(Map.Entry<ServerPlayerEntity, ImmPtlChunkTracking.PlayerWatchRecord> e, CallbackInfoReturnable<Boolean> cir) {
        removePlayer(e.getKey(), e.getValue());

    }

    @Inject(method = "lambda$forceRemovePlayer$16", at = @At(value = "INVOKE", target = "Lqouteall/imm_ptl/core/network/PacketRedirection;sendRedirectedMessage(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/network/packet/Packet;)V"), require = 0)
    private static void unloadChunkOnClient2(ServerPlayerEntity player, RegistryKey dim, Long2ObjectMap.Entry e, CallbackInfoReturnable<Boolean> cir, @Local ImmPtlChunkTracking.PlayerWatchRecord rec) {
        removePlayer(player, rec);
    }

    @Unique
    private static void removePlayer(ServerPlayerEntity player, ImmPtlChunkTracking.PlayerWatchRecord record) {
        var pos = new ChunkPos(record.chunkPos);
        ((WatchListener)player.getServer().getWorld(record.dimension)
                .getChunk(pos.x, pos.z))
                .polymc$removePlayer(player);
    }
}
