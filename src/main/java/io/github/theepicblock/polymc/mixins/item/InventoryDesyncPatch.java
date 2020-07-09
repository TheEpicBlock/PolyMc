package io.github.theepicblock.polymc.mixins.item;

import io.github.theepicblock.polymc.PolyMc;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Minecraft checks to see if the inventories are out of sync.
 * But when using PolyMC a desync is intentional.
 * So we check here if the desync is actually a good desync or a bad desync.
 */
@Mixin(ServerPlayNetworkHandler.class)
public class InventoryDesyncPatch {

    @Redirect(method = "onClickWindow(Lnet/minecraft/network/packet/c2s/play/ClickWindowC2SPacket;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;areEqual(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z"))
    public boolean areEqualRedirect(ItemStack left, ItemStack right) {
        return ItemStack.areEqual(left, PolyMc.getMap().getClientItem(right));
    }
}
