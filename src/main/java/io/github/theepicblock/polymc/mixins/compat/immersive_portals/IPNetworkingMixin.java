package io.github.theepicblock.polymc.mixins.compat.immersive_portals;

import io.github.theepicblock.polymc.impl.mixin.PlayerContextContainer;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.platform_specific.IPNetworking;

@Mixin(IPNetworking.class)
public class IPNetworkingMixin {
    @Inject(method = "sendRedirectedMessage", at = @At("HEAD"))
    private static void onCreateRedirectedMessage(ServerPlayerEntity player, RegistryKey<World> dimension, Packet packet, CallbackInfo ci) {
        if (packet instanceof PlayerContextContainer container) {
            container.setPolyMcProvidedPlayer(player);
        }
    }
}
