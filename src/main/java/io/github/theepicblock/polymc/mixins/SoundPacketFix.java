package io.github.theepicblock.polymc.mixins;

import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.PlaySoundIdS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Minecraft sends sound packets in 2 different ways. Using {@link PlaySoundS2CPacket} & {@link net.minecraft.network.packet.s2c.play.PlaySoundIdS2CPacket}.
 * The former uses a numeric id and the latter an {@link net.minecraft.util.Identifier}.
 * We should use the latter for non-vanilla sounds. As the client does not have a numeric representation for them.
 */
@Mixin(ServerPlayNetworkHandler.class)
public class SoundPacketFix {
    @Shadow public ServerPlayerEntity player;

    @ModifyVariable(method = "sendPacket(Lnet/minecraft/network/Packet;Lnet/minecraft/class_7648;)V", at = @At("HEAD"), argsOnly = true)
    private Packet<?> replacePacket(Packet<?> input) {
        if (input instanceof PlaySoundS2CPacket soundPacket && Util.isPolyMapVanillaLike(this.player)) {
            var soundId = soundPacket.getSound().getId();

            if (!Util.isVanilla(soundId)) {
                return new PlaySoundIdS2CPacket(
                        soundId,
                        soundPacket.getCategory(),
                        new Vec3d(soundPacket.getX(), soundPacket.getY(), soundPacket.getZ()),
                        soundPacket.getVolume(),
                        soundPacket.getPitch(),
                        soundPacket.getSeed()
                );
            }
        }
        return input;
    }
}
