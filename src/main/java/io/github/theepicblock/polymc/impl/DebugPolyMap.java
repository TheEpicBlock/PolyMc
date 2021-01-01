/*
 * PolyMc
 * Copyright (C) 2020-2021 TheEpicBlock_TEB
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
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DebugPolyMap implements PolyMap {
	@Override
	public ItemStack getClientItem(ItemStack serverItem) {
		return new ItemStack(Items.STICK);
	}

	@Override
	public BlockState getClientBlock(BlockState serverBlock) {
		return Blocks.CRAFTING_TABLE.getDefaultState();
	}

	@Override
	public BlockState getClientBlockWithContext(BlockState serverBlock, BlockPos pos, World world) {
		return getClientBlock(serverBlock);
	}

	@Override
	public GuiPoly getGuiPoly(ScreenHandlerType<?> serverGuiType) {
		return null;
	}

	@Override
	public BlockPoly getBlockPoly(Block block) {
		return null;
	}

	@Override
	public ImmutableMap<Item,ItemPoly> getItemPolys() {
		return ImmutableMap.of();
	}

	@Override
	public ImmutableMap<Block,BlockPoly> getBlockPolys() {
		return ImmutableMap.of();
	}
}
