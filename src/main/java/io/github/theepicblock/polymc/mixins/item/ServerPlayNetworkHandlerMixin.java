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

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    private ItemStack polyMCrecentlyVoided;
    /**
     * @reason Minecraft checks to see if the inventories are out of sync here. But when using PolyMC a desync is intentional. So we check here if the desync is actually a good desync or a bad desync.
     */
    @Redirect(method = "onClickWindow(Lnet/minecraft/network/packet/c2s/play/ClickWindowC2SPacket;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;areEqual(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z"))
    public boolean areEqualRedirect(ItemStack left, ItemStack right) {
        return ItemStack.areEqual(left, PolyMc.getMap().getClientItem(right));
    }

    /*
        When items are moved around by a creative mode player, the client just tells the server to set a stack to a specific item.
        This means that if the client thinks it's holding a stick, it will instruct the server to set the slot to a stick.
        Even if the stick is supposed to represent another item.

        My hacky solution:
        when a packet is sent to void a slot. The item previously in there gets set in "polyMCrecentlyVoided".
        Then when it tries to set a slot to an item. It first get's checked to see if the item it tries to set could be the poly of
        We also check if the client tries to set a slot to it's polyd version.
        TODO add a config to disable this mess
     */

    /**
     * @reason see comment block above
     */
    @Redirect(method = "onCreativeInventoryAction(Lnet/minecraft/network/packet/c2s/play/CreativeInventoryActionC2SPacket;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/PlayerScreenHandler;setStackInSlot(ILnet/minecraft/item/ItemStack;)V"))
    public void creativemodeSetSlotRedirect(PlayerScreenHandler screenHandler, int slot, ItemStack setStack) {
        ItemStack slotStack = screenHandler.getSlot(slot).getStack();
        if (!slotStack.isEmpty()) {
            if (setStack.isEmpty()) {
                polyMCrecentlyVoided = slotStack;
            } else {
                if (ItemStack.areEqual(setStack,PolyMc.getMap().getClientItem(slotStack))) {
                    //the item the client is trying to set is actually a the polyd version of the item in the same slot.
                    return;
                }
            }
        }

        screenHandler.setStackInSlot(slot, setStack);
    }

    @Redirect(method = "onCreativeInventoryAction(Lnet/minecraft/network/packet/c2s/play/CreativeInventoryActionC2SPacket;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/c2s/play/CreativeInventoryActionC2SPacket;getItemStack()Lnet/minecraft/item/ItemStack;"))
    public ItemStack getItemStackRedirect(CreativeInventoryActionC2SPacket creativeInventoryActionC2SPacket) {
        ItemStack original = creativeInventoryActionC2SPacket.getItemStack();

        if (polyMCrecentlyVoided == null) return original;

        if (ItemStack.areEqual(original,PolyMc.getMap().getClientItem(polyMCrecentlyVoided))) {
            //the item the client is trying to set is actually a polyd version of polyMCrecentlyVoided.
            return polyMCrecentlyVoided;
        }
        return original;
    }
}
