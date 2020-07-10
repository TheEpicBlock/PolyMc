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
package io.github.theepicblock.polymc.api.register;

import com.google.common.collect.ImmutableMap;
import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.item.ItemPoly;
import net.minecraft.item.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * A class to register Polys.
 * Also contains helper stuff to help with the registration of Polys and help lower conflicts.
 * This eventually get's transformed to an {@link PolyMap}
 */
public class PolyRegistry {
    private final CustomModelDataManager CMDManager = new CustomModelDataManager();
    private final Map<Item, ItemPoly> itemMap = new HashMap<>();

    /**
     * Register a poly for an item
     * @param item item to associate poly with
     * @param poly poly to register
     */
    public void registerItem(Item item, ItemPoly poly) {
        itemMap.put(item, poly);
    }

    /**
     * Checks if the item has a registered itempoly
     * @param item item to check
     * @return true if the itempoly exists for the given item
     */
    public boolean hasItemPoly(Item item) {
        return itemMap.containsKey(item);
    }

    public CustomModelDataManager getCMDManager() {
        return CMDManager;
    }

    public PolyMap build() {
        ImmutableMap<Item,ItemPoly> itemMapIm = ImmutableMap.copyOf(itemMap);
        return new PolyMap(itemMapIm);
    }
}
