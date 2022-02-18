package io.github.theepicblock.polymc.mixins;

import io.github.theepicblock.polymc.impl.Util;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class DisableCustomParticles {
    @Shadow public ServerPlayerEntity player;

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("HEAD"), cancellable = true)
    private void sendPacketInject(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> listener, CallbackInfo ci) {
        if (packet instanceof ParticleS2CPacket particlePacket && Util.isPolyMapVanillaLike(this.player)) {
            var effect = particlePacket.getParameters();
            if (!Util.isVanilla(Registry.PARTICLE_TYPE.getId(effect.getType()))) {
                ci.cancel();
            }
        }
    }
}
