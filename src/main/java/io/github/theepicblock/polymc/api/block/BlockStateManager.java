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

import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.SharedValuesKey;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Manages which blockstates are allocated to which polys.
 */
public class BlockStateManager {
    public static final SharedValuesKey<BlockStateManager> KEY = new SharedValuesKey<>(BlockStateManager::new, null);

    private final Map<Block, List<BlockState>> availableBlockStates = new HashMap<>();
    private final PolyRegistry polyRegistry;

    public BlockStateManager(PolyRegistry polyRegistry) {
        this.polyRegistry = polyRegistry;
    }

    public BlockState requestBlockState(BlockStateProfile profile) throws StateLimitReachedException {
        try {
            return requestBlockState(profile.filter, profile.blocks, profile.onFirstRegister);
        } catch (StateLimitReachedException e) {
            throw new StateLimitReachedException("No states found in profile: " + profile.name);
        }
    }

    public BlockState requestBlockState(Predicate<BlockState> blockStatePredicate, Block[] searchSpace, BiConsumer<Block,PolyRegistry> onFirstRegister) throws StateLimitReachedException {
        for (var block : searchSpace) {
            var availableStates = availableBlockStates.computeIfAbsent(block, (b) -> {
                onFirstRegister.accept(b, this.polyRegistry);
                return new LinkedList<>(b.getStateManager().getStates());
            });

            // Return first block state that matches `blockStatePredicate`
            var iterator = availableStates.iterator();
            while (iterator.hasNext()) {
                BlockState next = iterator.next();
                if (blockStatePredicate.test(next)) {
                    iterator.remove();
                    return next;
                }
            }
        }
        throw new StateLimitReachedException("No states found in " + Arrays.toString(searchSpace));
    }

    public static class StateLimitReachedException extends Exception {
        public StateLimitReachedException(String s) {
            super(s);
        }
    }
}
