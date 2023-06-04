package io.github.theepicblock.polymc.mixins;

import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.nucleoid.packettweaker.PacketContext;

/**
 * Minecraft sends sound packets in 2 different ways. Using {@link PlaySoundS2CPacket}
 * The former uses a numeric id and the latter an {@link net.minecraft.util.Identifier}.
 * We should use the latter for non-vanilla sounds. As the client does not have a numeric representation for them.
 */
@Mixin(PlaySoundS2CPacket.class)
public class SoundPacketFix {
    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeRegistryEntry(Lnet/minecraft/util/collection/IndexedIterable;Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/network/PacketByteBuf$PacketWriter;)V"))
    private RegistryEntry<SoundEvent> replaceSound(RegistryEntry<SoundEvent> entry) {
        if (entry.getType() == RegistryEntry.Type.REFERENCE&& Util.isPolyMapVanillaLike(PacketContext.get().getTarget()) && !Util.isVanilla(entry.getKey().get().getValue())) {
            return RegistryEntry.of(entry.value());
        }
        return entry;
    }
}
