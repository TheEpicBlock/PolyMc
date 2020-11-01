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
import io.github.theepicblock.polymc.api.gui.GuiPoly;
import io.github.theepicblock.polymc.api.item.ItemPoly;
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;

/**
 *
 */
public class PolyMap {
    private final ImmutableMap<Item,ItemPoly> itemPolys;
    private final ImmutableMap<Block,BlockPoly> blockPolys;
    private final ImmutableMap<ScreenHandlerType<?>,GuiPoly> guiPolys;

    public PolyMap(ImmutableMap<Item,ItemPoly> itemPolys, ImmutableMap<Block,BlockPoly> blockPolys, ImmutableMap<ScreenHandlerType<?>,GuiPoly> guiPolys) {
        this.itemPolys = itemPolys;
        this.blockPolys = blockPolys;
        this.guiPolys = guiPolys;
    }

    public ItemStack getClientItem(ItemStack serverItem) {
        ItemStack ret = serverItem;

        ItemPoly poly = itemPolys.get(serverItem.getItem());
        if (poly != null) ret = poly.getClientItem(serverItem);

        ret = Util.portEnchantmentsToLore(ret);

        return ret;
    }

    public BlockState getClientBlock(BlockState serverBlock) {
        BlockPoly poly = blockPolys.get(serverBlock.getBlock());
        if (poly == null) return serverBlock;

        return poly.getClientBlock(serverBlock);
    }

    public GuiPoly getGuiPoly(ScreenHandlerType<?> serverGuiType) {
        return guiPolys.get(serverGuiType);
    }

    public ImmutableMap<Item,ItemPoly> getItemPolys() {
        return itemPolys;
    }

    public ImmutableMap<Block,BlockPoly> getBlockPolys() {
        return blockPolys;
    }
}
