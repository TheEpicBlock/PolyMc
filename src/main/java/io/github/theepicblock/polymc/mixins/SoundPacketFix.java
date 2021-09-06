package io.github.theepicblock.polymc.mixins;

import io.github.theepicblock.polymc.impl.Util;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundIdS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class SoundPacketFix {
    private static final String MINECRAFT = "minecraft";

    @Shadow public ServerPlayerEntity player;

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("HEAD"), cancellable = true)
    public void sendPacketInject(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> listener, CallbackInfo ci) {
        if (packet instanceof PlaySoundS2CPacket soundPacket && Util.isPolyMapVanillaLike(this.player)) {

            Identifier soundId = soundPacket.getSound().getId();

            // Allow the Minecraft vanilla sounds
            if (soundId.getNamespace().equals(MINECRAFT)) {
                return;
            }

            // Cancel sending the original packet
            ci.cancel();

            // Create a new SoundId packet
            Vec3d pos = new Vec3d(soundPacket.getX(), soundPacket.getY(), soundPacket.getZ());
            PlaySoundIdS2CPacket soundIdPacket = new PlaySoundIdS2CPacket(soundId, soundPacket.getCategory(), pos, soundPacket.getVolume(), soundPacket.getPitch());

            this.player.networkHandler.sendPacket(soundIdPacket);
        }
    }
}