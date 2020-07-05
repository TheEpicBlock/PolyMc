package io.github.theepicblock.polymc.api.item;

import io.github.theepicblock.polymc.resource.ResourcePackMaker;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface ItemPoly {

    /**
     * Transforms an ItemStack to it's client version
     * @param input original ItemStack
     * @return ItemStack that should be sent to the client
     */
    ItemStack getClientItem(ItemStack input);

    /**
     * Callback to add all resources needed for this item to a resourcepack
     * @param item item this ItemPoly was registered to, for reference.
     * @param pack resourcepack to add to.
     */
    void AddToResourcePack(Item item, ResourcePackMaker pack);
}
