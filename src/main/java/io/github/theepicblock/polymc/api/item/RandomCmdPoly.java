package io.github.theepicblock.polymc.api.item;

import io.github.theepicblock.polymc.api.register.CustomModelDataManager;
import io.github.theepicblock.polymc.resource.JsonModel;
import io.github.theepicblock.polymc.resource.ResourcePackMaker;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;

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
        JsonModel itemMod = pack.copyMinecraftItemModel(Registry.ITEM.getId(replaceItem.getItem()).getPath());
        JsonModel.Override override = new JsonModel.Override();
        override.predicate = new HashMap<>();
        int cmdValue = replaceItem.getTag().getInt("CustomModelData");
        override.predicate.put("custom_model_data",cmdValue);
        Identifier modelId = Registry.ITEM.getId(item);
        override.model = modelId.getNamespace()+":item/"+modelId.getPath();
        itemMod.addOverride(override);
    }
}
