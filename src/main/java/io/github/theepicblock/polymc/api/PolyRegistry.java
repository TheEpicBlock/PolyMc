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
package io.github.theepicblock.polymc.api;

import com.google.common.collect.ImmutableMap;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.block.BlockStateManager;
import io.github.theepicblock.polymc.api.gui.GuiPoly;
import io.github.theepicblock.polymc.api.item.CustomModelDataManager;
import io.github.theepicblock.polymc.api.item.ItemPoly;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.screen.ScreenHandlerType;

import java.util.HashMap;
import java.util.Map;

/**
 * A class to register Polys.
 * Also contains helper stuff to help with the registration of Polys and help lower conflicts.
 * This eventually gets transformed to an {@link PolyMap}.
 */
public class PolyRegistry {
    private final CustomModelDataManager CMDManager = new CustomModelDataManager();
    private final BlockStateManager blockStateManager = new BlockStateManager(this);

    private final Map<Item,ItemPoly> itemPolys = new HashMap<>();
    private final Map<Block,BlockPoly> blockPolys = new HashMap<>();
    private final Map<ScreenHandlerType<?>,GuiPoly> guiPolys = new HashMap<>();

    /**
     * Register a poly for an item.
     * @param item item to associate poly with.
     * @param poly poly to register.
     */
    public void registerItemPoly(Item item, ItemPoly poly) {
        itemPolys.put(item, poly);
    }

    /**
     * Register a poly for a block.
     * @param block block to associate poly with.
     * @param poly  poly to register.
     */
    public void registerBlockPoly(Block block, BlockPoly poly) {
        blockPolys.put(block, poly);
    }

    /**
     * Register a poly for a gui.
     * @param screenHandler screen handler to associate poly with.
     * @param poly          poly to register.
     */
    public void registerGuiPoly(ScreenHandlerType<?> screenHandler, GuiPoly poly) {
        guiPolys.put(screenHandler, poly);
    }

    /**
     * Checks if the item has a registered {@link ItemPoly}.
     * @param item item to check.
     * @return True if a {@link ItemPoly} exists for the given item.
     */
    public boolean hasItemPoly(Item item) {
        return itemPolys.containsKey(item);
    }

    /**
     * Checks if the block has a registered {@link BlockPoly}.
     * @param block block to check.
     * @return True if an {@link BlockPoly} exists for the given block
     */
    public boolean hasBlockPoly(Block block) {
        return blockPolys.containsKey(block);
    }

    /**
     * Checks if the screen handler has a registered {@link GuiPoly}.
     * @param screenHandler screen handler to check.
     * @return True if a {@link GuiPoly} exists for the given screen handler.
     */
    public boolean hasGuiPoly(ScreenHandlerType<?> screenHandler) {
        return guiPolys.containsKey(screenHandler);
    }

    /**
     * Gets the {@link CustomModelDataManager} allocated to assist during registration
     */
    public CustomModelDataManager getCMDManager() {
        return CMDManager;
    }

    /**
     * Gets the {@link BlockStateManager} allocated to assist during registration
     */
    public BlockStateManager getBlockStateManager() {
        return blockStateManager;
    }

    /**
     * Creates an immutable {@link PolyMap} containing all of the registered polys
     */
    public PolyMap build() {
        ImmutableMap<Item,ItemPoly> itemMap = ImmutableMap.copyOf(itemPolys);
        ImmutableMap<Block,BlockPoly> blockMap = ImmutableMap.copyOf(blockPolys);
        ImmutableMap<ScreenHandlerType<?>,GuiPoly> guiMap = ImmutableMap.copyOf(guiPolys);
        return new PolyMap(itemMap, blockMap, guiMap);
    }
}
