package io.github.theepicblock.polymc.mixins.entity;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.TransformingPacketCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

@Mixin(EntityAttributesS2CPacket.class)
public abstract class EntityAttributesFilteringMixin {
    @SuppressWarnings("UnreachableCode")
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/PacketCodec;tuple(Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Ljava/util/function/BiFunction;)Lnet/minecraft/network/codec/PacketCodec;"))
    private static PacketCodec<RegistryByteBuf, EntityAttributesS2CPacket> removeUnsupportedAttributes(PacketCodec<RegistryByteBuf, EntityAttributesS2CPacket> original) {
        return TransformingPacketCodec.encodeOnly(original, (buf, packet) -> {
            var map = Util.tryGetPolyMap(PacketContext.get().getClientConnection());
            var p = new EntityAttributesS2CPacket(packet.getEntityId(), List.of());
            var list = p.getEntries();
            for (EntityAttributesS2CPacket.Entry entry : packet.getEntries()) {
                if (map.canReceiveRegistryEntry(Registries.ATTRIBUTE, entry.attribute())) {
                    list.add(entry);
                }
            }

            return p;
        });
    }
}
