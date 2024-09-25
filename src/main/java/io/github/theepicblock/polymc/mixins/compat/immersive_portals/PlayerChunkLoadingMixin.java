package io.github.theepicblock.polymc.mixins.compat.immersive_portals;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import io.github.theepicblock.polymc.impl.misc.WatchListener;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import qouteall.imm_ptl.core.chunk_loading.PlayerChunkLoading;

@Pseudo
@Mixin(value = PlayerChunkLoading.class)
public class PlayerChunkLoadingMixin {
    @WrapMethod(method = "sendChunkPacket")
    private static void onSendChunkPacket(ServerPlayNetworkHandler serverGamePacketListenerImpl, ServerWorld serverLevel, WorldChunk levelChunk, Operation<Void> original) {
        PolymerCommonUtils.executeWithNetworkingLogic(serverGamePacketListenerImpl, () -> original.call(serverGamePacketListenerImpl, serverLevel, levelChunk));
        ((WatchListener) levelChunk).polymc$addPlayer(serverGamePacketListenerImpl.player);
    }
}
