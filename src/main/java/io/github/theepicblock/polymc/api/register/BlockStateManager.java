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
                BlockState t = block.getStateManager().getStates().get(current);
                BlockStateIdIndex.put(block,current+1);
                if (filter.test(t)){
                    return t;
                }
            } catch (IndexOutOfBoundsException e) {
                throw new OutOfBoundsException("Tried to access more BlockStates then block has: " + block.getTranslationKey());
            }
        }
    }

    /**
     * Request a blockstate value to be allocated for any of a list of blocks.
     * @param blocks the blocks which can be used to pull blockstates from.
     * @param filter limits the blockstates that this function can return. A blockstate can only be used if {@link Predicate#test(Object)} returns true. A blockstate that was rejected can't be used anymore, even when using a different filter. It is advised to use the same filter per block.
     * @param onFirstRegister this will be called if this block is first used. Useful for registering a poly for it.
     * @throws OutOfBoundsException if the limit of BlockStates is reached
     * @return the value you can use.
     */
    public BlockState requestBlockState(Block[] blocks, PolyRegistry registry, Predicate<BlockState> filter, BiConsumer<Block,PolyRegistry> onFirstRegister) throws OutOfBoundsException{
        for (Block block : blocks) {
            try {
                return requestBlockState(block,registry,filter,onFirstRegister);
            } catch (OutOfBoundsException ignored) {}
        }
        throw new OutOfBoundsException("Tried to access more BlockStates then block has: " + blocks[blocks.length-1].getTranslationKey() + " after iterating through others");
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

        List<BlockState> ret = new ArrayList<>(amount);
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
     * Request multiple Blockstates for any of a list of blocks
     * @param blocks the blocks which can be used to pull blockstates from.
     * @param amount the amount of BlockStates you need
     * @param filter limits the blockstates that this function can return. A blockstate can only be used if {@link Predicate#test(Object)} returns true. A blockstate that was rejected can't be used anymore, even when using a different filter. It is advised to use the same filter per block.
     * @param onFirstRegister this will be called if this block is first used. Useful for registering a poly for it.
     * @throws OutOfBoundsException if the limit of BlockStates is reached
     * @return the BlockStates you can do
     */
    public List<BlockState> requestBlockState(Block[] blocks, int amount, PolyRegistry registry, Predicate<BlockState> filter, BiConsumer<Block,PolyRegistry> onFirstRegister) throws OutOfBoundsException {
        List<BlockState> ret = new ArrayList<>(amount);
        int left = amount;
        for (Block block : blocks) {
            for (int i = 0; i < left; i++) {
                try {
                    ret.add(requestBlockState(block, registry,filter,onFirstRegister));
                } catch (OutOfBoundsException e) {
                    left -= (left - 1);
                }
            }
        }
        if (left != 0) {
            //We didn't reach the needed amount. We need to hack this in to un register the blockstates.
            for (BlockState state : ret) {
                BlockStateIdIndex.put(state.getBlock(),BlockStateIdIndex.getInt(state.getBlock())-1);
            }
            throw new OutOfBoundsException("Tried to access more BlockStates then block has: " + blocks[blocks.length-1].getTranslationKey() + " after iterating through others. Blockstates have now been wasted.");
        }
        return ret;
    }

    /**
     * Checks how many blockstates are available for the specified block and compares that with the amount specified
     * @param block the blocks which can be used to pull blockstates from.
     * @param filter limits which blockstates you're looking for. A blockstate can only be used if {@link Predicate#test(Object)} returns true. It is advised to use the same filter per block.
     * @param amount how many blockstates you need
     * @return true if that amount of blockstates are available
     */
    public boolean isAvailable(Block block, int amount, Predicate<BlockState> filter) {
        int current = BlockStateIdIndex.getOrDefault(block,0); //this is the current blockstateId that we're at for this item/

        int goodBlocks = 0;
        while (true) {
            current++;
            try {
                BlockState t = block.getStateManager().getStates().get(current);
                if (filter.test(t)) {
                    goodBlocks++;
                    if (goodBlocks == amount) return true;
                }
            } catch (IndexOutOfBoundsException e) {
                return false;
            }
        }
    }

    /**
     * Checks how many blockstates are available for a list of blocks and compares that with the amount specified
     * @param blocks for which block to check
     * @param filter limits which blockstates you're looking for. A blockstate can only be used if {@link Predicate#test(Object)} returns true. It is advised to use the same filter per block.
     * @param amount how many blockstates you need
     * @return true if that amount of blockstates are available
     */
    public boolean isAvailable(Block[] blocks, int amount, Predicate<BlockState> filter) {
        int goodBlocks = 0;
        for (Block block : blocks) {
            int current = BlockStateIdIndex.getOrDefault(block,0); //this is the current blockstateId that we're at for this item/

            while (true) {
                current++;
                try {
                    BlockState t = block.getStateManager().getStates().get(current);
                    if (filter.test(t)) {
                        goodBlocks++;
                        if (goodBlocks == amount) return true;
                    }
                } catch (IndexOutOfBoundsException ignored) {
                    break;
                }
            }
        }
        return false;
    }

    private int getBlockStateIdIndex(Block block, PolyRegistry registry, BiConsumer<Block,PolyRegistry> onFirstRegister) {
        if (!BlockStateIdIndex.containsKey(block)) {
            onFirstRegister.accept(block,registry);
            BlockStateIdIndex.put(block,0);
        }
        return BlockStateIdIndex.getInt(block);
    }
}
