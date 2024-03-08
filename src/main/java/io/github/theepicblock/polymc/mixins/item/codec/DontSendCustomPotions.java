package io.github.theepicblock.polymc.mixins.item.codec;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Optional;

import static io.github.theepicblock.polymc.impl.Util.isVanillaAndRegistered;

@Mixin(PotionContentsComponent.class)
public class DontSendCustomPotions {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/PacketCodec;tuple(Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Lcom/mojang/datafixers/util/Function3;)Lnet/minecraft/network/codec/PacketCodec;"))
    private static PacketCodec<RegistryByteBuf, PotionContentsComponent> polyMcWrapCodec(PacketCodec<RegistryByteBuf, PotionContentsComponent> codec) {
        return codec.xmap(
                component -> component,
                component -> {
                    if (component.potion().isPresent() && !isVanillaAndRegistered(component.potion().get()) ||
                        component.customEffects().stream().anyMatch(effect -> !isVanillaAndRegistered(effect.getEffectType()))) {
                        // The client doesn't really need to know anything but colour anyway

                        // There should be no risk of data corruption; the value of this goes into
                        // PacketCodecs#registryEntry which writes a registry index. So no mod should be using this
                        // for persistent storage
                        return new PotionContentsComponent(Optional.empty(), component.customColor(), List.of());
                    }
                    return component;
                });
    }
}
