package io.github.theepicblock.polymc.mixins.compat;

import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.quiltmc.qsl.registry.impl.sync.ServerFabricRegistrySync;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerFabricRegistrySync.class)
public class QuiltFabricRegistrySyncDisabler {
    @SuppressWarnings("MixinAnnotationTarget")
    @Inject(method = "sendSyncPackets(Lnet/minecraft/network/ClientConnection;)V", at = @At("HEAD"), cancellable = true)
    private static void sendPacketInject(ClientConnection connection, CallbackInfo ci) {
        var listener = connection.getPacketListener();
        if (listener instanceof ServerPlayNetworkHandler handler && Util.isPolyMapVanillaLike(handler.player)) {
            ci.cancel();
        }
    }
}
