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
package io.github.theepicblock.polymc.impl.generator;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.item.CustomModelDataManager;
import io.github.theepicblock.polymc.api.item.ItemPoly;
import io.github.theepicblock.polymc.impl.poly.item.ArmorItemPoly;
import io.github.theepicblock.polymc.impl.poly.item.CustomModelDataPoly;
import io.github.theepicblock.polymc.impl.poly.item.DamageableItemPoly;
import net.minecraft.item.*;

/**
 * Class to automatically generate {@link ItemPoly}s for {@link Item}s
 */
public class ItemPolyGenerator {
    /**
     * Generates the most suitable {@link ItemPoly} for a given {@link Item}
     */
    public static ItemPoly generatePoly(Item item, PolyRegistry builder) {
        if (item instanceof ArmorItem armorItem) {
            return new ArmorItemPoly(builder, armorItem);
        }
        if (item instanceof ShieldItem) {
            return new DamageableItemPoly(builder.getCMDManager(), item, Items.SHIELD);
        }
        if (item instanceof CompassItem) {
            return new CustomModelDataPoly(builder.getCMDManager(), item, Items.COMPASS);
        }
        if (item instanceof CrossbowItem) {
            return new DamageableItemPoly(builder.getCMDManager(), item, Items.CROSSBOW);
        }
        if (item instanceof RangedWeaponItem) {
            return new DamageableItemPoly(builder.getCMDManager(), item, Items.BOW);
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
     * Generates the most suitable {@link ItemPoly} and directly adds it to the {@link PolyRegistry}
     * @see #generatePoly(Item, PolyRegistry)
     */
    public static void addItemToBuilder(Item item, PolyRegistry builder) {
        try {
            builder.registerItemPoly(item, generatePoly(item, builder));
        } catch (Exception e) {
            PolyMc.LOGGER.error("Failed to generate a poly for item " + item.getTranslationKey());
            e.printStackTrace();
            PolyMc.LOGGER.error("Attempting to recover by using a default poly. Please report this");
            builder.registerItemPoly(item, (input, location) -> new ItemStack(Items.BARRIER));
        }
    }
}
