package io.github.theepicblock.polymc.api.item;

import io.github.theepicblock.polymc.api.register.CustomModelDataManager;
import io.github.theepicblock.polymc.resource.JsonModel;
import io.github.theepicblock.polymc.resource.ResourcePackMaker;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;

/**
 * Replaces an item with a random vanilla item with a custom model data value.
 */
public class CustomModelDataPoly implements ItemPoly{
    private final ItemStack defaultServerItem;
    private final int CMDvalue;

    public CustomModelDataPoly(CustomModelDataManager registerManager, Item base) {
        Pair<Item, Integer> pair = registerManager.RequestCMDwithItem();
        CMDvalue = pair.getRight();
        defaultServerItem = new ItemStack(pair.getLeft());
        CompoundTag tag = new CompoundTag();
        tag.putInt("CustomModelData", CMDvalue);
        defaultServerItem.setTag(tag);
        defaultServerItem.setCustomName(new TranslatableText(base.getTranslationKey()).setStyle(Style.EMPTY.withItalic(false)));
    }

    /**
     * Makes a poly that generates the specified item with a custom model data value
     * If the item used doesn't matter it is recommended to use the more generic method instead
     * @param registerManager manager used to generate the CMD value
     * @param target the serverside item will be of this type
     */
    public CustomModelDataPoly(CustomModelDataManager registerManager, Item base, Item target) {
        CMDvalue = registerManager.RequestCMDValue(target);
        defaultServerItem = new ItemStack(target);
        CompoundTag tag = new CompoundTag();
        tag.putInt("CustomModelData", CMDvalue);
        defaultServerItem.setTag(tag);
        defaultServerItem.setCustomName(new TranslatableText(base.getTranslationKey()).setStyle(Style.EMPTY.withItalic(false)));
    }

    @Override
    public ItemStack getClientItem(ItemStack input) {
        ItemStack serverItem = defaultServerItem;
        if (input.hasTag()) {
            serverItem = defaultServerItem.copy();
            serverItem.setTag(input.getTag());
            //doing this removes the CMD, so we should add that again
            serverItem.getTag().putInt("CustomModelData",CMDvalue);
        }
        serverItem.setCount(input.getCount());
        serverItem.setCooldown(input.getCooldown());
        return serverItem;
    }

    @Override
    public void AddToResourcePack(Item item, ResourcePackMaker pack) {
        //TODO this can be cleaner
        pack.copyItemModel(item);
        JsonModel itemMod = pack.copyMinecraftItemModel(Registry.ITEM.getId(defaultServerItem.getItem()).getPath());
        JsonModel.Override override = new JsonModel.Override();
        override.predicate = new HashMap<>();
        override.predicate.put("custom_model_data",CMDvalue);
        Identifier modelId = Registry.ITEM.getId(item);
        override.model = modelId.getNamespace()+":item/"+modelId.getPath();
        itemMod.addOverride(override);
    }
}
