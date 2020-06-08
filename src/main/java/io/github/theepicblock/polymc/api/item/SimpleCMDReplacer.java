package io.github.theepicblock.polymc.api.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Pair;

/**
 * An ItemPoly implementation that replaces the selected item with a vanilla item with a customModelData value.
 */
public class SimpleCMDReplacer implements ItemPoly{
    final ItemStack replaceItem;
    public SimpleCMDReplacer(CustomModelDataManager registerManager) {
        Pair<Item, Integer> pair = registerManager.RequestItem();
        replaceItem = new ItemStack(pair.getLeft());
        CompoundTag tag = new CompoundTag();
        tag.putInt("CustomModelData", pair.getRight());
        replaceItem.setTag(tag);
    }

    @Override
    public ItemStack getPoly(ItemStack input) {
        return replaceItem;
    }
}
