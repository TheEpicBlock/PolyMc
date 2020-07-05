package io.github.theepicblock.polymc.api.item;

import io.github.theepicblock.polymc.api.register.CustomModelDataManager;
import io.github.theepicblock.polymc.resource.ResourcePackMaker;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Pair;

/**
 * Replaces an item with a random vanilla item with a custom model data value.
 */
public class RandomCmdPoly implements ItemPoly{
    final ItemStack replaceItem;

    public RandomCmdPoly(CustomModelDataManager registerManager) {
        Pair<Item, Integer> pair = registerManager.RequestItem();
        replaceItem = new ItemStack(pair.getLeft());
        CompoundTag tag = new CompoundTag();
        tag.putInt("CustomModelData", pair.getRight());
        replaceItem.setTag(tag);
    }

    @Override
    public ItemStack getClientItem(ItemStack input) {
        return replaceItem;
    }

    @Override
    public void AddToResourcePack(Item item, ResourcePackMaker pack) {
        pack.copyItemModel(item);
    }
}
