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
import io.github.theepicblock.polymc.api.item.ItemLocation;
import io.github.theepicblock.polymc.api.item.ItemPoly;
import io.github.theepicblock.polymc.api.resource.ModdedResources;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.api.resource.json.JModelOverride;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import io.github.theepicblock.polymc.impl.resource.ResourceConstants;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.TreeMap;

/**
 * The most standard ItemPoly implementation
 */
public class CustomModelDataPoly implements ItemPoly {
    protected final int cmdValue;
    protected final CustomModelDataComponent cmdComponent;
    protected final Item clientItem;

    public CustomModelDataPoly(CustomModelDataManager registerManager, Item moddedBase) {
        this(registerManager, moddedBase, CustomModelDataManager.DEFAULT_ITEMS);
    }

    /**
     * Makes a poly that generates the specified item with a custom model data value
     * If the item used doesn't matter it is recommended to use the more generic method instead
     * @param registerManager manager used to generate the CMD value
     * @param target          the serverside item will be of this type
     */
    public CustomModelDataPoly(CustomModelDataManager registerManager, Item moddedBase, Item target) {
        this(registerManager, moddedBase, new Item[]{target});
    }

    /**
     * Makes a poly that generates the specified item with a custom model data value
     * If the item used doesn't matter it is recommended to use the more generic method instead
     * @param registerManager manager used to generate the CMD value
     * @param targets         the serverside items that can be chosen from
     */
    public CustomModelDataPoly(CustomModelDataManager registerManager, Item moddedBase, Item[] targets) {
        Pair<Item,Integer> pair = registerManager.requestCMD(targets);
        this.clientItem = pair.getLeft();
        this.cmdValue = pair.getRight();

        this.cmdComponent = new CustomModelDataComponent(this.cmdValue);
    }

    /**
     * Adds PolyMc specific tags to the item to display correctly on the client.
     * These shouldn't change depending on the stack as this method will be cached.
     * For un-cached tags, use {@link #getClientItem(ItemStack, ServerPlayerEntity, ItemLocation)}
     */
    protected void addCustomTagsToItem(ItemStack stack) {
        stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, this.cmdComponent);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public ItemStack getClientItem(ItemStack input, @Nullable ServerPlayerEntity player, @Nullable ItemLocation location) {
        var output = Util.copyWithItem(input, clientItem, player);

        this.addCustomTagsToItem(output);
        if (!output.contains(DataComponentTypes.ITEM_NAME)) {
            output.set(DataComponentTypes.ITEM_NAME, input.getItem().getName(input));
        }

        if (input.contains(DataComponentTypes.TOOL)) {
            output.set(DataComponentTypes.TOOL, input.get(DataComponentTypes.TOOL));
        }

        return output;
    }

    @Override
    public void addToResourcePack(Item item, ModdedResources moddedResources, PolyMcResourcePack pack, SimpleLogger logger) {
        // We need to copy over the modded item model into the pack (including all of the textures it references)
        // Then we need to include an override into a vanilla item model that links to that modded item model
        var moddedItemId = Registries.ITEM.getId(item);
        var moddedItemModel = moddedResources.getItemModel(moddedItemId.getNamespace(), moddedItemId.getPath());
        if (moddedItemModel == null) {
            logger.error("Can't find item model for "+moddedItemId+", can't generate resources for it");
            // Set the override to have the barrier model to signify it's missing
            moddedItemId = Registries.ITEM.getId(Items.BARRIER);
        } else {
            pack.setItemModel(moddedItemId.getNamespace(), moddedItemId.getPath(), moddedItemModel);
            pack.importRequirements(moddedResources, moddedItemModel, logger);
        }

        var clientItemId = Registries.ITEM.getId(this.clientItem);

        // Copy and retrieve the vanilla item's model
        var clientItemModel = pack.getOrDefaultVanillaItemModel(moddedResources, clientItemId.getNamespace(), clientItemId.getPath(), logger);
        // Add an override into the vanilla item's model that references the modded one
        clientItemModel.getOverrides().add(JModelOverride.ofCMD(cmdValue, ResourceConstants.itemLocation(moddedItemId)));

        // Check if the modded item model has overrides
        if (moddedItemModel != null && !moddedItemModel.getOverridesReadOnly().isEmpty()) {
            // The modded item has overrides of its own. The correct behaviour here is for PolyMc to move those overrides
            // Into the vanilla item, adding the custom model data as an additional predicate for each override.
            for (var override : moddedItemModel.getOverridesReadOnly()) {
                var predicates = new TreeMap<>(override.predicates());
                predicates.put("custom_model_data", (float)cmdValue);
                clientItemModel.getOverrides().add(new JModelOverride(predicates, override.model()));
            }
            moddedItemModel.getOverrides().clear();
        }
    }

    @Override
    public String getDebugInfo(Item item) {
        return "CMD: " + Util.expandTo(cmdValue, 3) + ", item:" + clientItem.getTranslationKey();
    }
}
