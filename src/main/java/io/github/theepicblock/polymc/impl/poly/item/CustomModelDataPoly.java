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
package io.github.theepicblock.polymc.impl.poly.item;

import io.github.theepicblock.polymc.api.item.CustomModelDataManager;
import io.github.theepicblock.polymc.api.item.ItemPoly;
import io.github.theepicblock.polymc.api.resource.JsonModel;
import io.github.theepicblock.polymc.api.resource.ResourcePackMaker;
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.*;
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
        this(registerManager, base, CustomModelDataManager.DEFAULT_ITEMS);
    }

    /**
     * Makes a poly that generates the specified item with a custom model data value
     * If the item used doesn't matter it is recommended to use the more generic method instead
     * @param registerManager manager used to generate the CMD value
     * @param target          the serverside item will be of this type
     */
    public CustomModelDataPoly(CustomModelDataManager registerManager, Item base, Item target) {
        this(registerManager, base, new Item[]{target});
    }

    /**
     * Makes a poly that generates the specified item with a custom model data value
     * If the item used doesn't matter it is recommended to use the more generic method instead
     * @param registerManager manager used to generate the CMD value
     * @param targets         the serverside items that can be chosen from
     */
    public CustomModelDataPoly(CustomModelDataManager registerManager, Item base, Item[] targets) {
        Pair<Item,Integer> pair = registerManager.requestCMD(targets);
        CMDvalue = pair.getRight();
        defaultServerItem = new ItemStack(pair.getLeft());
        NbtCompound tag = new NbtCompound();
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
        }

        // Always set the name again in case the item can change its name based on NBT data
        if (!input.hasCustomName()) {

            BaseText name = (BaseText) input.getName();

            // Override the style to make sure the client does not render
            // the custom name in italics
            name.setStyle(name.getStyle().withItalic(false));

            serverItem.setCustomName(name);
        }

        serverItem.setCount(input.getCount());
        serverItem.setCooldown(input.getCooldown());
        return serverItem;
    }

    @Override
    public void addToResourcePack(Item item, ResourcePackMaker pack) {
        pack.copyItemModel(item);

        Identifier modelId = Registry.ITEM.getId(item);
        addOverride(
                pack,
                defaultServerItem.getItem(),
                CMDvalue,
                String.format("%s:item/%s", modelId.getNamespace(), modelId.getPath())
        );
    }

    /**
     * Adds a cmd override to a vanilla item.
     * Note: the {@link ResourcePackMaker#getOrDefaultPendingItemModel(String)} may not produce a correct json model for the vanilla item.
     * @param pack pack to register to
     * @param vanillaItem vanilla item which should receive the override
     * @param cmdValue the cmd value to override
     * @param modelPath the model to link this override to
     */
    public static void addOverride(ResourcePackMaker pack, Item vanillaItem, int cmdValue, String modelPath) {
        JsonModel itemModel = pack.getOrDefaultPendingItemModel(Registry.ITEM.getId(vanillaItem));

        JsonModel.Override override = new JsonModel.Override();
        override.predicate = new HashMap<>();
        override.predicate.put("custom_model_data", (double)cmdValue);
        override.model = modelPath;
        itemModel.addOverride(override);
    }

    @Override
    public String getDebugInfo(Item item) {
        return "CMD: " + Util.expandTo(CMDvalue, 3) + ", item:" + defaultServerItem.getTranslationKey();
    }
}
