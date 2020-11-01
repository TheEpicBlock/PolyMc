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
package io.github.theepicblock.polymc.generator;

import io.github.theepicblock.polymc.Util;
import io.github.theepicblock.polymc.api.item.*;
import io.github.theepicblock.polymc.api.register.CustomModelDataManager;
import io.github.theepicblock.polymc.api.register.PolyRegistry;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;

/**
 * Class to automatically generate ItemPolys for Items
 */
public class ItemPolyGenerator {
    /**
     * Automatically generates all {@link ItemPoly}s that are missing in the specified builder
     * @param builder builder to add the {@link ItemPoly}s to
     */
    public static void generateMissing(PolyRegistry builder) {
        for (Item item : getItemRegistry()) {
            if (builder.hasItemPoly(item)) continue;
            Identifier id = getItemRegistry().getId(item);
            if (!Util.isVanilla(id)) {
                //this is a modded item and should have a Poly
                addItemToBuilder(item,builder);
            }
        }
    }

    /**
     * Generates the most suitable ItemPoly for a given item
     */
    public static ItemPoly generatePoly(Item item, PolyRegistry builder) {
        if (item instanceof ShieldItem) {
            return new ShieldPoly(builder.getCMDManager(), item);
        }
        if (item instanceof CompassItem) {
            return new CustomModelDataPoly(builder.getCMDManager(), item, Items.COMPASS);
        }
        if (item instanceof CrossbowItem) {
            return new PredicateBasedDamageableItem(builder.getCMDManager(), item, Items.CROSSBOW);
        }
        if (item instanceof RangedWeaponItem) {
            return new BowPoly(builder.getCMDManager(), item);
        }
        if (item.isDamageable()) {
            if (item instanceof DyeableItem) {
                return new DamageableItemPoly(builder.getCMDManager(), item, Items.LEATHER_HELMET);
            }
            return new DamageableItemPoly(builder.getCMDManager(), item);
        }
        if (item.isFood()) {
            return new CustomModelDataPoly(builder.getCMDManager(), item, CustomModelDataManager.FOOD_ITEMS);
        }
        if (item instanceof DyeableItem) {
            return new CustomModelDataPoly(builder.getCMDManager(), item, Items.LEATHER_HORSE_ARMOR);
        }
        if (item instanceof BlockItem) {
            return new CustomModelDataPoly(builder.getCMDManager(), item, CustomModelDataManager.BLOCK_ITEMS);
        }
        return new CustomModelDataPoly(builder.getCMDManager(), item);
    }

    /**
     * Generates the most suitable ItemPoly and directly adds it to the {@link PolyRegistry}
     * @see #generatePoly(Item, PolyRegistry)
     */
    private static void addItemToBuilder(Item item, PolyRegistry builder) {
        builder.registerItemPoly(item, generatePoly(item,builder));
    }

    /**
     * @return the minecraft item registry
     */
    private static DefaultedRegistry<Item> getItemRegistry() {
        return Registry.ITEM;
    }
}
