package io.github.theepicblock.polymc.mixins.item.codec;

import com.mojang.serialization.Codec;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.ItemLocationStaticHack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.function.Supplier;


@Mixin(ItemStack.class)
public class ItemStackImplementationMixin {
    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;lazyInitialized(Ljava/util/function/Supplier;)Lcom/mojang/serialization/Codec;"))
    private static Supplier<Codec<ItemStack>> patchCodec(Supplier<Codec<ItemStack>> codec) {
        return () -> codec.get().xmap(content -> { // Decode
            if (PolymerCommonUtils.isServerNetworkingThread()) {
                var ctx = PacketContext.get();
                var map = Util.tryGetPolyMap(ctx);
                return map.reverseClientItem(content, ctx.getPlayer());
            }
            return content;
        }, content -> { // Encode
            if (PolymerCommonUtils.isServerNetworkingThreadWithContext()) {
                var ctx = PacketContext.get();
                if (ctx.getPacketListener() == null) {
                    return content;
                }
                var map = Util.tryGetPolyMap(ctx);
                return map.getClientItem(content, ctx.getPlayer(), ItemLocationStaticHack.location.get());
            }
            return content;
        });
    }
}
