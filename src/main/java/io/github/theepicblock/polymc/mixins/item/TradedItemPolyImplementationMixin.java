package io.github.theepicblock.polymc.mixins.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.TransformingPacketCodec;
import io.github.theepicblock.polymc.impl.mixin.ItemLocationStaticHack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.predicate.ComponentPredicate;
import net.minecraft.village.TradedItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(TradedItem.class)
public class TradedItemPolyImplementationMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/PacketCodec;tuple(Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Lcom/mojang/datafixers/util/Function3;)Lnet/minecraft/network/codec/PacketCodec;"))
    private static PacketCodec<RegistryByteBuf, TradedItem> writeTradedItemHook(PacketCodec<RegistryByteBuf, TradedItem> original) {
        return TransformingPacketCodec.encodeOnly(original, (buf, tradedItem) -> {
            var ctx = PacketContext.get();
            var map = Util.tryGetPolyMap(ctx.getClientConnection());
            var stack = map.getClientItem(tradedItem.itemStack(), ctx.getPlayer(), ItemLocationStaticHack.location.get());
            return new TradedItem(stack.getItem().getRegistryEntry(), stack.getCount(), ComponentPredicate.of(stack.getComponents()));
        });
    }
}
