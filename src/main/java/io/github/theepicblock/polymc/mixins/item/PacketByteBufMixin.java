package io.github.theepicblock.polymc.mixins.item;

import io.github.theepicblock.polymc.PolyMc;
import net.minecraft.item.ItemStack;
import net.minecraft.util.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PacketByteBuf.class)
public class PacketByteBufMixin {
    @ModifyVariable(method = "writeItemStack(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/util/PacketByteBuf;", at = @At("HEAD"))
    public ItemStack writeItemStackValueOverwrite(ItemStack itemStack) {
        return PolyMc.getMap().getClientItem(itemStack);
    }
}