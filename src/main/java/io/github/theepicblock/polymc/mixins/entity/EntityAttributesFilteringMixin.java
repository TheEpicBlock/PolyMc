package io.github.theepicblock.polymc.mixins.entity;


import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(EntityAttributesS2CPacket.class)
public abstract class EntityAttributesFilteringMixin {

    @SuppressWarnings("unchecked")
    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeCollection(Ljava/util/Collection;Lnet/minecraft/network/PacketByteBuf$PacketWriter;)V", ordinal = 0))
    private Collection<EntityAttributesS2CPacket.Entry> polymer_replaceWithPolymer(Collection<EntityAttributesS2CPacket.Entry> value) {
        var map = Util.tryGetPolyMap(PacketContext.get().getTarget());
        List<EntityAttributesS2CPacket.Entry> list = new ArrayList<>();
        for (EntityAttributesS2CPacket.Entry entry : value) {
            if (map.canReceiveEntityAttribute(entry.getId())) {
                list.add(entry);
            }
        }

        return list;
    }
}
