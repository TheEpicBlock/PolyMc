package io.github.theepicblock.polymc.mixins.tag;

import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.PlayerContextContainer;
import net.minecraft.network.packet.s2c.play.SynchronizeTagsS2CPacket;
import net.minecraft.tag.TagPacketSerializer;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.HashMap;
import java.util.Map;

@Mixin(SynchronizeTagsS2CPacket.class)
public class SynchronizeTagsMixin {
    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeMap(Ljava/util/Map;Ljava/util/function/BiConsumer;Ljava/util/function/BiConsumer;)V"))
    public Map<RegistryKey<? extends Registry<?>>, TagPacketSerializer.Serialized> editTagMap(Map<RegistryKey<? extends Registry<?>>, TagPacketSerializer.Serialized> in) {
        var player = PlayerContextContainer.retrieve(this);
        if (Util.isPolyMapVanillaLike(player)) {
            // Vanilla doesn't like it if it receives tags for registries that don't exist
            var newMap = new HashMap<RegistryKey<? extends Registry<?>>, TagPacketSerializer.Serialized>();
            in.forEach((key, tags) -> {
                if (Util.isVanilla(key.getValue())) {
                    newMap.put(key, tags);
                }
            });
            return newMap;
        } else {
            return in;
        }
    }
}
