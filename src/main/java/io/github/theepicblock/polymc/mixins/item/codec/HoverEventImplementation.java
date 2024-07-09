package io.github.theepicblock.polymc.mixins.item.codec;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import io.github.theepicblock.polymc.api.item.ItemLocation;
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.item.ItemStack;
import net.minecraft.text.HoverEvent;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(HoverEvent.ItemStackContent.class)
public class HoverEventImplementation {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC, target = "Lnet/minecraft/item/ItemStack;CODEC:Lcom/mojang/serialization/Codec;"))
    private static Codec<ItemStack> polyMcWrapCodecA(Codec<ItemStack> codec) {
        return polyMcWrapCodec(codec);
    }

    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC, target = "Lnet/minecraft/item/ItemStack;REGISTRY_ENTRY_CODEC:Lcom/mojang/serialization/Codec;"))
    private static Codec<ItemStack> polyMcWrapCodecB(Codec<ItemStack> codec) {
        return polyMcWrapCodec(codec);
    }

    @Unique
    private static Codec<ItemStack> polyMcWrapCodec(Codec<ItemStack> codec) {
        return codec.xmap(
                stack -> stack,
                stack -> {
                    // We need to be quite careful to only modify things when writing
                    // packets here, since the codec can also be used for persistent storage.
                    var ctx = PacketContext.get();
                    if (ctx.getClientConnection() != null) {
                        var map = Util.tryGetPolyMap(ctx.getClientConnection());
                        return map.getClientItem(stack, ctx.getPlayer(), ItemLocation.TEXT);
                    }
                    return stack;
                });
    }
}
