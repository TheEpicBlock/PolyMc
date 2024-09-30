package io.github.theepicblock.polymc.mixins.enchantment;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.ItemLocationStaticHack;
import net.minecraft.enchantment.effect.AllOfEnchantmentEffects;
import net.minecraft.enchantment.effect.EnchantmentLocationBasedEffect;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(EnchantmentLocationBasedEffect.class)
public interface EnchantmentLocationBasedEffectMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;dispatch(Ljava/util/function/Function;Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"))
    private static Codec<EnchantmentLocationBasedEffect> patchCodec(Codec<EnchantmentLocationBasedEffect> codec) {
        return codec.xmap(Function.identity(), content -> { // Encode
            if (PolymerCommonUtils.isServerNetworkingThreadWithContext()) {
                var ctx = PacketContext.get();
                if (ctx.getPacketListener() == null) {
                    return content;
                }
                var map = Util.tryGetPolyMap(ctx);
                return map.canReceiveEnchantmentLocationBasedEffect(content) ? content : new AllOfEnchantmentEffects.LocationBasedEffects(List.of());
            }
            return content;
        });
    }
}
