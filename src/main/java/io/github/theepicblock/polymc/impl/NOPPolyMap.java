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
	public BlockState getClientBlockWithContext(BlockState serverBlock, BlockPos pos, World world) {
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
}
