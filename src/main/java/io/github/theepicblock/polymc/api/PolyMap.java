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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;

public interface PolyMap {
	/**
	 * Converts a serverside item into a clientside one using the corresponding {@link ItemPoly}.
	 */
	ItemStack getClientItem(ItemStack serverItem);

	/**
	 * Converts a serverside block into a clientside one using the corresponding {@link BlockPoly}.
	 */
	BlockState getClientBlock(BlockState serverBlock);

	/**
	 * Converts a serverside gui into a clientside one using the corresponding {@link GuiPoly}.
	 * Currently experimental
	 */
	GuiPoly getGuiPoly(ScreenHandlerType<?> serverGuiType);

	BlockPoly getBlockPoly(Block block);

	/**
	 * gets a map containing all itempolys in this map
	 */
	ImmutableMap<Item,ItemPoly> getItemPolys();

	/**
	 * gets a map containing all blockpolys in this map
	 */
	ImmutableMap<Block,BlockPoly> getBlockPolys();

	/**
	 * Specifies if this map is meant for vanilla-like clients
	 * This is used to disable/enable miscellaneous patches
	 */
	boolean isVanillaLikeMap();
}
