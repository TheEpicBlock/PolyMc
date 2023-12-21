package io.github.theepicblock.polymc.mixins;

import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryFixedCodec;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(RegistryFixedCodec.class)
public class RegistryFixedCodecMixin {
    @ModifyVariable(
            method = "encode(Lnet/minecraft/registry/entry/RegistryEntry;Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;",
            at = @At("HEAD")
    )
    private RegistryEntry<?> swapEntry(RegistryEntry<?> entry) {
        var ctx = PacketContext.get();
        if (ctx.getClientConnection() != null) {
            try {
                var map = Util.tryGetPolyMap(ctx.getClientConnection());

                if (entry.value() instanceof Item item) {
                    return Registries.ITEM.getEntry(map.getClientItem(item.getDefaultStack(), ctx.getPlayer(), null).getItem());
                } else if (entry.value() instanceof Block item && map.getBlockPoly(item) != null) {
                    return Registries.BLOCK.getEntry(map.getBlockPoly(item).getClientBlock(item.getDefaultState()).getBlock());
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        return entry;
    }
}
