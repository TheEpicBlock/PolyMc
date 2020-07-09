package io.github.theepicblock.polymc.mixins.item;

import io.github.theepicblock.polymc.PolyMc;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * This is the class responsible for replacing the serverside items with the clientside items
 */
@Mixin(PacketByteBuf.class)
public class ItemPolyImplementation {
    @ModifyVariable(method = "writeItemStack(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/network/PacketByteBuf;", at = @At("HEAD"))
    public ItemStack writeItemStackValueOverwrite(ItemStack itemStack) {
        return PolyMc.getMap().getClientItem(itemStack);
    }
}