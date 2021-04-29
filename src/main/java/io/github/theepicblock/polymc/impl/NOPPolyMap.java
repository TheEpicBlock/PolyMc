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

public class NOPPolyMap implements PolyMap {
	@Override
	public ItemStack getClientItem(ItemStack serverItem) {
		return serverItem;
	}

	@Override
	public BlockState getClientBlock(BlockState serverBlock) {
		return serverBlock;
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
		return null;
	}

	@Override
	public ImmutableMap<Block,BlockPoly> getBlockPolys() {
		return null;
	}

	@Override
	public ItemStack reverseClientItem(ItemStack clientItem) {
		return clientItem;
	}

	@Override
	public boolean isVanillaLikeMap() {
		return false; //This disables patches meant for vanilla clients
	}
}
