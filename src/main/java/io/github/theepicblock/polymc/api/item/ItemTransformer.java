package io.github.theepicblock.polymc.api.item;

import io.github.theepicblock.polymc.api.DebugInfoProvider;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Transforms an item from the serverside to the clientside.
 * Used for global item polys.
 * @see io.github.theepicblock.polymc.api.PolyRegistry#registerGlobalItemPoly(ItemTransformer)
 */
public interface ItemTransformer {
    /**
     * Transforms an ItemStack to its clientside version.
     *
     * @apiNote this method should never edit the incoming ItemStack. As that might have unspecified consequences for the actual serverside representation of the item.
     * @param input the original {@link ItemStack} that's used serverside.
     * @return The {@link ItemStack} that should be sent to the client.
     */
    ItemStack transform(ItemStack input);
}
