/*
 * PolyMc
 * Copyright (C) 2020-2020 TheEpicBlock_TEB
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package io.github.theepicblock.polymc.api.item;

import io.github.theepicblock.polymc.Util;
import io.github.theepicblock.polymc.api.register.CustomModelDataManager;
import io.github.theepicblock.polymc.resource.JsonModel;
import io.github.theepicblock.polymc.resource.ResourcePackMaker;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;

/**
 * The most standard ItemPoly implementation
 */
public class CustomModelDataPoly implements ItemPoly {
    protected final ItemStack defaultServerItem;
    protected final int CMDvalue;

    public CustomModelDataPoly(CustomModelDataManager registerManager, Item base) {
        Pair<Item,Integer> pair = registerManager.requestCMDwithItem();
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
     * @param target          the serverside item will be of this type
     */
    public CustomModelDataPoly(CustomModelDataManager registerManager, Item base, Item target) {
        CMDvalue = registerManager.requestCMDValue(target);
        defaultServerItem = new ItemStack(target);
        CompoundTag tag = new CompoundTag();
        tag.putInt("CustomModelData", CMDvalue);
        defaultServerItem.setTag(tag);
        defaultServerItem.setCustomName(new TranslatableText(base.getTranslationKey()).setStyle(Style.EMPTY.withItalic(false)));
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public ItemStack getClientItem(ItemStack input) {
        ItemStack serverItem = defaultServerItem;
        if (input.hasTag()) {
            serverItem = defaultServerItem.copy();
            serverItem.setTag(input.getTag().copy());
            //doing this removes the CMD, so we should add that again
            serverItem.getTag().putInt("CustomModelData", CMDvalue);
            if (!input.hasCustomName()) { //It might be that the tags didn't include the name, so we should add them back in
                serverItem.setCustomName(defaultServerItem.getName());
            }
        }
        serverItem.setCount(input.getCount());
        serverItem.setCooldown(input.getCooldown());
        return serverItem;
    }

    @Override
    public void AddToResourcePack(Item item, ResourcePackMaker pack) {
        //TODO this can be cleaner
        pack.copyItemModel(item);
        JsonModel itemModel = pack.getOrDefaultPendingItemModel(Registry.ITEM.getId(defaultServerItem.getItem()).getPath());
        JsonModel.Override override = new JsonModel.Override();
        override.predicate = new HashMap<>();
        override.predicate.put("custom_model_data", (double)CMDvalue);
        Identifier modelId = Registry.ITEM.getId(item);
        override.model = modelId.getNamespace() + ":item/" + modelId.getPath();
        itemModel.addOverride(override);
    }

    @Override
    public String getDebugInfo(Item item) {
        return "CMD: " + Util.expandTo(CMDvalue, 3) + ", item:" + defaultServerItem.getTranslationKey();
    }
}
