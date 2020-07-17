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

import io.github.theepicblock.polymc.api.OutOfBoundsException;
import io.github.theepicblock.polymc.api.block.SimpleReplacementPoly;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * We can use (unused) vanilla blockstates to display different modded blocks.
 * This manager allocates blockstates to prevent them from conflicting.
 * It will also register a poly to prevent the blockstate from being used normally.
 */
public class BlockStateManager {
    /**
     * The blockstateId we are currently at for a specified block
     */
    private final Object2IntMap<Block> BlockStateIdIndex = new Object2IntOpenHashMap<>();

    /**
     * Request a blockstate value to be allocated for a specific block.
     * @param block the block you need a BlockState for
     * @throws OutOfBoundsException if the limit of BlockStates is reached
     * @return the value you can use.
     */
    public BlockState requestBlockState(Block block, PolyRegistry registry) throws OutOfBoundsException{
        int current = BlockStateIdIndex.getInt(block); //this is the current blockstateId that we're at for this item/
        if (current == 0) {
            current = 1; //we should start at 1. Never 0
            registry.registerBlockPoly(block, new SimpleReplacementPoly(block.getStateManager().getDefaultState()));
        }
        BlockStateIdIndex.put(block, current + 1);
        try {
            return block.getStateManager().getStates().get(current);
        } catch (IndexOutOfBoundsException e) {
            throw new OutOfBoundsException("Tried to access more BlockStates then block has: " + block.getTranslationKey());
        }
    }

    /**
     * Request multiple Blockstates for a single block
     * @param block the block you need a block state for
     * @param amount the amount of BlockStates you need
     * @throws OutOfBoundsException if the limit of BlockStates is reached
     * @return the BlockStates you can do
     */
    public List<BlockState> requestBlockState(Block block, int amount, PolyRegistry registry) throws OutOfBoundsException {
        List<BlockState> ret = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            ret.add(requestBlockState(block, registry));
        }
        return ret;
    }
}
