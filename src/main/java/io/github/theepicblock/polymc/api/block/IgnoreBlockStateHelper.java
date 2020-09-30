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
package io.github.theepicblock.polymc.api.block;

import io.github.theepicblock.polymc.api.OutOfBoundsException;
import io.github.theepicblock.polymc.api.register.PolyRegistry;
import io.github.theepicblock.polymc.resource.ResourcePackMaker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import java.util.function.Predicate;

/**
 * Helper to create {@link UnusedBlockStatePoly}s that ignore certain blockstates.
 */
public class IgnoreBlockStateHelper {
    /**
     *
     * @param moddedBlock the block this poly represents
     * @param clientSideBlock the block used to display this block on the client
     * @param registry registry used to register this poly
     * @param ignored return true for all blockstates you want to ignore.
     * @return An {@link UnusedBlockStatePoly} that ignores certain blockstates
     */
    public static UnusedBlockStatePoly of(Block moddedBlock, Block clientSideBlock, PolyRegistry registry, Predicate<BlockState> ignored) throws OutOfBoundsException {
        return new UnusedBlockStatePoly(moddedBlock,clientSideBlock,registry,ignored.negate(),
                (block, onFirstRegisterRegistry) -> onFirstRegisterRegistry.registerBlockPoly(block, new ConditionalSimpleBlockPoly(ignored, clientSideBlock.getDefaultState())));
    }

    /**
     * This block poly replaces the the block it's registered to with another blockstate, but only if exempts returns false
     */
    public static class ConditionalSimpleBlockPoly extends SimpleReplacementPoly{
        private final Predicate<BlockState> exempts;

        public ConditionalSimpleBlockPoly(Predicate<BlockState> exempts, BlockState state) {
            super(state);
            this.exempts = exempts;
        }

        @Override
        public BlockState getClientBlock(BlockState input) {
            return exempts.test(input) ? input : state;
        }

        @Override
        public void AddToResourcePack(Block block, ResourcePackMaker pack) {}
    }
}
