package io.github.theepicblock.polymc.mixins.compat;

import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerPlayerEntity;
import org.quiltmc.qsl.registry.impl.sync.ServerRegistrySync;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerRegistrySync.class)
public class QuiltRegistrySyncDisabler {
    @SuppressWarnings("MixinAnnotationTarget")
    @Inject(method = "sendSyncPackets(Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;)V", at = @At("HEAD"), cancellable = true)
    private static void sendPacketInject(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        ((PolyMapProvider)(player)).refreshUsedPolyMap(); // Refresh it earlier
        if (Util.isPolyMapVanillaLike(player)) {
            ci.cancel();
        }
    }
}
