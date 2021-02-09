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
package io.github.theepicblock.polymc.impl;

import com.google.common.collect.ImmutableMap;
import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.gui.GuiPoly;
import io.github.theepicblock.polymc.api.item.ItemPoly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A map containing different types of polys
 */
public class PolyMapImpl implements PolyMap {
    private final ImmutableMap<Item,ItemPoly> itemPolys;
    private final ImmutableMap<Block,BlockPoly> blockPolys;
    private final ImmutableMap<ScreenHandlerType<?>,GuiPoly> guiPolys;

    public PolyMapImpl(ImmutableMap<Item,ItemPoly> itemPolys, ImmutableMap<Block,BlockPoly> blockPolys, ImmutableMap<ScreenHandlerType<?>,GuiPoly> guiPolys) {
        this.itemPolys = itemPolys;
        this.blockPolys = blockPolys;
        this.guiPolys = guiPolys;
    }

    /**
     * Converts a serverside item into a clientside one using the corresponding {@link ItemPoly}.
     */
    @Override
    public ItemStack getClientItem(ItemStack serverItem) {
        ItemStack ret = serverItem;

        ItemPoly poly = itemPolys.get(serverItem.getItem());
        if (poly != null) ret = poly.getClientItem(serverItem);

        ret = Util.portEnchantmentsToLore(ret);

        return ret;
    }

    /**
     * Converts a serverside block into a clientside one using the corresponding {@link BlockPoly}.
     */
    @Override
    public BlockState getClientBlock(BlockState serverBlock) {
        BlockPoly poly = blockPolys.get(serverBlock.getBlock());
        if (poly == null) return serverBlock;

        return poly.getClientBlock(serverBlock);
    }

    @Override
    public BlockState getClientBlockWithContext(BlockState serverBlock, BlockPos pos, World world) {
        BlockPoly poly = blockPolys.get(serverBlock.getBlock());
        if (poly == null) return serverBlock;

        return poly.getClientBlockWithContext(serverBlock, pos, world);
    }

    /**
     * Converts a serverside gui into a clientside one using the corresponding {@link GuiPoly}.
     * Currently experimental
     */
    @Override
    public GuiPoly getGuiPoly(ScreenHandlerType<?> serverGuiType) {
        return guiPolys.get(serverGuiType);
    }

    @Override
    public BlockPoly getBlockPoly(Block block) {
        return blockPolys.get(block);
    }

    /**
     * gets a map containing all itempolys in this map
     */
    @Override
    public ImmutableMap<Item,ItemPoly> getItemPolys() {
        return itemPolys;
    }

    /**
     * gets a map containing all blockpolys in this map
     */
    @Override
    public ImmutableMap<Block,BlockPoly> getBlockPolys() {
        return blockPolys;
    }

    @Override
    public boolean isVanillaLikeMap() {
        return true;
    }
}
