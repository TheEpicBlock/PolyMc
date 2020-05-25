package io.github.theepicblock.polymc.api;

import net.minecraft.item.ItemStack;

public interface ItemPoly {

    /**
     * Transforms an ItemStack to it's client version
     * @param input original ItemStack
     * @return ItemStack that should be sent to the client
     */
    ItemStack getPoly(ItemStack input);
}
