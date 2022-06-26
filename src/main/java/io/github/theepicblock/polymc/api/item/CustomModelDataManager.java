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

import io.github.theepicblock.polymc.api.SharedValuesKey;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.ApiStatus;

/**
 * Helper class to prevent CustomModelData values from conflicting.
 * For example, a mod can request 100 CustomModelData values for a specific item. Then those will be reserved and another mod will get different values.
 */
public class CustomModelDataManager {
    public final static SharedValuesKey<CustomModelDataManager> KEY = new SharedValuesKey<>(registry -> new CustomModelDataManager(), null);

    public final static Item[] DEFAULT_ITEMS = {
            Items.STICK,
            Items.FEATHER,
            Items.FLINT,
            Items.LEATHER,
            Items.RABBIT_HIDE,
            Items.RABBIT_FOOT,
            Items.SCUTE,
            Items.BRICK,
            Items.CLAY_BALL,
            Items.GHAST_TEAR,
            Items.FERMENTED_SPIDER_EYE,
            Items.MAGMA_CREAM,
            Items.GLISTERING_MELON_SLICE,
            Items.EMERALD,
            Items.QUARTZ,
            Items.IRON_NUGGET,
            Items.IRON_INGOT,
            Items.COPPER_INGOT,
            Items.GOLD_NUGGET,
            Items.GOLD_INGOT,
            Items.DIAMOND,
            Items.NETHERITE_SCRAP,
            Items.NETHERITE_INGOT,
            Items.HEART_OF_THE_SEA,
            Items.NAUTILUS_SHELL,
            Items.PHANTOM_MEMBRANE,
            Items.ECHO_SHARD,
            Items.GUNPOWDER,
            Items.SUGAR,
            Items.BLAZE_ROD,
            Items.PAPER,
    };
    public final static Item[] FUEL_ITEMS = {
            Items.COAL,
            Items.CHARCOAL,
            Items.BLAZE_ROD,
    };
    public final static Item[] FOOD_ITEMS = {
            Items.COOKED_BEEF,
            Items.COOKED_CHICKEN,
            Items.COOKED_COD,
            Items.COOKED_MUTTON,
            Items.COOKED_PORKCHOP,
            Items.COOKED_RABBIT,
            Items.COOKED_SALMON,
            Items.BEEF,
            Items.CHICKEN,
            Items.COD,
            Items.MUTTON,
            Items.PORKCHOP,
            Items.RABBIT,
            Items.SALMON,
            Items.CARROT,
            Items.GOLDEN_CARROT,
            Items.APPLE,
            Items.BEETROOT,
            Items.POTATO,
            Items.BAKED_POTATO,
            Items.BREAD
    };
    @ApiStatus.Internal
    public final static Item[] FULL_BLOCK_ITEMS = {
            Items.STRUCTURE_VOID
    };
    @ApiStatus.Internal
    public final static Item[] BLOCK_ITEMS = {
            Items.STRUCTURE_VOID
    };

    private final Object2IntMap<Item> customModelDataCounter = new Object2IntOpenHashMap<>();
    private int roundRobin = 0;

    /**
     * Request a certain amount of CMD values from the specified item.
     * @param item   the item you need CMD for.
     * @param amount the amount of cmd values you need.
     * @return The first value you can use. Example: you passed in 5 as amount. You got 9 back as value. You can now use 9,10,11,12 and 13.
     * @deprecated it is recommended to use multiple items. As to minimize recipe weirdness.
     */
    @Deprecated
    public int requestCMD(Item item, int amount) throws ArithmeticException {
        int current = customModelDataCounter.getInt(item); //this is the current CMD that we're at for this item/
        if (current == 0) {
            current = 1; //we should start at 1. Never 0
        }
        int newValue = current + amount;
        if (newValue > 16777215) { // The amount a float can store without precision loss
            throw new OutOfCustomModelDataValuesException(amount, new Item[]{item});
        }
        customModelDataCounter.put(item, newValue);
        return current;
    }

    /**
     * Request one CMD value for a specific item.
     * @param item the item you need CMD for
     * @return The value you can use.
     * @deprecated it is recommended to use multiple items. As to minimize recipe weirdness.
     */
    @Deprecated
    public int requestCMD(Item item) {
        return requestCMD(item, 1);
    }

    /**
     * Requests a certain amount of items from the specified array.
     * @param items  the list of items to choose from.
     * @param amount the amount of cmd values you need.
     * @return The item you may use and the CMD value. The CMD value returned is the first you may use, the rest can be derived. Example: you passed in 5 as amount. You got 9 back as value. You can now use 9,10,11,12 and 13.
     */
    public Pair<Item,Integer> requestCMD(Item[] items, int amount) {
        int startingRR = roundRobin;
        do {
            roundRobin++;

            try {
                Item item = getRoundRobin(items);
                return new Pair<>(item, requestCMD(item, amount));
            } catch (OutOfCustomModelDataValuesException ignored) {}
        } while (roundRobin % items.length != startingRR % items.length);

        throw new OutOfCustomModelDataValuesException(amount, items);
    }

    /**
     * Requests a certain amount of CMD values.
     * This will use the {@link #DEFAULT_ITEMS} array.
     * @param amount the amount of cmd values you need.
     * @return The item you may use and the CMD value. The CMD value returned is the first you may use, the rest can be derived. Example: you passed in 5 as amount. You got 9 back as value. You can now use 9,10,11,12 and 13.
     */
    public Pair<Item,Integer> requestCMD(int amount) {
        return requestCMD(DEFAULT_ITEMS, amount);
    }

    /**
     * Request an item with a CustomModelData. To prevent CMD values from conflicting.
     * @param items the list of items to choose from.
     * @return The item you may use and the CMD value.
     */
    public Pair<Item,Integer> requestCMD(Item[] items) {
        return requestCMD(items, 1);
    }

    /**
     * Requests a single CMD value.
     * This will use the {@link #DEFAULT_ITEMS} array
     * @return The item you may use and the CMD value.
     */
    public Pair<Item,Integer> requestCMD() {
        return requestCMD(DEFAULT_ITEMS, 1);
    }

    private Item getRoundRobin(Item[] list) {
        return list[roundRobin % list.length];
    }

    public static class OutOfCustomModelDataValuesException extends RuntimeException {
        public OutOfCustomModelDataValuesException(int amount, Item[] items) {
            super(getString(amount, items));
        }

        private static String getString(int amount, Item[] items) {
            StringBuilder b = new StringBuilder();
            b.append("Ran out of custom model data values. ").append(amount).append(" item(s) where requested. Available items to choose from:");
            for (Item item : items) {
                b.append("\n - ");
                b.append(item.getTranslationKey());
            }
            return b.toString();
        }
    }
}
