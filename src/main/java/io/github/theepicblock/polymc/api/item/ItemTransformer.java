package io.github.theepicblock.polymc.api.item;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Transforms an item from the serverside to the clientside.
 * Used for global item polys.
 * @see io.github.theepicblock.polymc.api.PolyRegistry#registerGlobalItemPoly(ItemTransformer)
 */
public interface ItemTransformer {
    /**
     * Transforms an ItemStack to its clientside version.
     * @param input the original {@link ItemStack} that's used serverside.
     * @param player the player this item is being sent to
     * @param location the location this item is sent from
     * @return The {@link ItemStack} that should be sent to the client.
     * @apiNote this method should never edit the incoming ItemStack. As that might have unspecified consequences for the actual serverside representation of the item.
     */
    ItemStack transform(ItemStack input, @Nullable ServerPlayerEntity player, @Nullable ItemLocation location);
}
