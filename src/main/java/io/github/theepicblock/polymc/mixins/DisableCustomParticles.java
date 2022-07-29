package io.github.theepicblock.polymc.mixins;

import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.class_7648;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class DisableCustomParticles {
    @Shadow public ServerPlayerEntity player;

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lnet/minecraft/class_7648;)V", at = @At("HEAD"), cancellable = true)
    private void sendPacketInject(Packet<?> packet, class_7648 arg, CallbackInfo ci) {
        if (packet instanceof ParticleS2CPacket particlePacket && Util.isPolyMapVanillaLike(this.player)) {
            var effect = particlePacket.getParameters();
            if (!Util.isVanilla(Registry.PARTICLE_TYPE.getId(effect.getType()))) {
                ci.cancel();
            }
        }
    }
}
