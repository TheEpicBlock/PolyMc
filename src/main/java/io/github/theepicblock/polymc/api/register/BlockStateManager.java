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
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

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
     * @param filter limits the blockstates that this function can return. A blockstate can only be used if {@link Predicate#test(Object)} returns true. A blockstate that was rejected can't be used anymore, even when using a different filter. It is advised to use the same filter per block.
     * @param onFirstRegister this will be called if this block is first used. Useful for registering a poly for it.
     * @throws OutOfBoundsException if the limit of BlockStates is reached
     * @return the value you can use.
     */
    public BlockState requestBlockState(Block block, PolyRegistry registry, Predicate<BlockState> filter, BiConsumer<Block,PolyRegistry> onFirstRegister) throws OutOfBoundsException{
        while (true) {
            int current = getBlockStateIdIndex(block, registry, onFirstRegister);
            try {
                BlockStateIdIndex.put(block,current+1);
                BlockState t = block.getStateManager().getStates().get(current);
                if (filter.test(t)){
                    return t;
                }
            } catch (IndexOutOfBoundsException e) {
                throw new OutOfBoundsException("Tried to access more BlockStates then block has: " + block.getTranslationKey());
            }
        }
    }

    /**
     * Request multiple Blockstates for a single block
     * @param block the block you need a block state for
     * @param amount the amount of BlockStates you need
     * @param filter limits the blockstates that this function can return. A blockstate can only be used if {@link Predicate#test(Object)} returns true. A blockstate that was rejected can't be used anymore, even when using a different filter. It is advised to use the same filter per block.
     * @param onFirstRegister this will be called if this block is first used. Useful for registering a poly for it.
     * @throws OutOfBoundsException if the limit of BlockStates is reached
     * @return the BlockStates you can do
     */
    public List<BlockState> requestBlockState(Block block, int amount, PolyRegistry registry, Predicate<BlockState> filter, BiConsumer<Block,PolyRegistry> onFirstRegister) throws OutOfBoundsException {
        int initialValue = getBlockStateIdIndex(block, registry, onFirstRegister);

        List<BlockState> ret = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            try {
                ret.add(requestBlockState(block, registry,filter,onFirstRegister));
            } catch (OutOfBoundsException e) {
                BlockStateIdIndex.put(block,initialValue);
                throw e;
            }
        }
        return ret;

    }

    /**
     * Checks how many blockstates are available for the specified block and compares that with the amount specified
     * @param block for which block to check
     * @param filter limits which blockstates you're looking for. A blockstate can only be used if {@link Predicate#test(Object)} returns true. It is advised to use the same filter per block.
     * @param amount how many blockstates you need
     * @return true if that amount of blockstates are available
     */
    public boolean isAvailable(Block block, int amount, Predicate<BlockState> filter) {
        int current = BlockStateIdIndex.getOrDefault(block,0); //this is the current blockstateId that we're at for this item/
        if (current == 0) {
            current = 1; //we should start at 1. Never 0
        }
        int i = 0;
        int goodBlocks = 0;
        while (true) {
            i++;
            try {
                BlockState t = block.getStateManager().getStates().get(i);
                if (filter.test(t)) {
                    goodBlocks++;
                    if (goodBlocks == amount) return true;
                }
            } catch (IndexOutOfBoundsException e) {
                return false;
            }
        }
    }

    private int getBlockStateIdIndex(Block block, PolyRegistry registry, BiConsumer<Block,PolyRegistry> onFirstRegister) {
        if (!BlockStateIdIndex.containsKey(block)) {
            onFirstRegister.accept(block,registry);
        }
        int v = BlockStateIdIndex.getOrDefault(block,0); //this is the current blockstateId that we're at for this item/
        BlockStateIdIndex.put(block, v + 1);
        return v;
    }
}
