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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Helper to make block polys without collision.
 */
public class NoCollisionBlockHelper {
    private static final Block[] NO_COLLISION_BLOCKS = {Blocks.SUGAR_CANE,
            Blocks.ACACIA_SAPLING,Blocks.BIRCH_SAPLING,Blocks.DARK_OAK_SAPLING,Blocks.JUNGLE_SAPLING,Blocks.OAK_SAPLING,
            Blocks.CAVE_AIR,Blocks.VOID_AIR,Blocks.STRUCTURE_VOID};

    private static final Predicate<BlockState> NO_COLLISION_FILTER = (blockState) -> {
        System.out.println("e"+ blockState + isAir(blockState.getBlock()));
        if (isAir(blockState.getBlock())) return true;
        return UnusedBlockStatePoly.DEFAULT_FILTER.test(blockState);
    };

    private static final BiConsumer<Block, PolyRegistry> NO_COLLISION_ON_FIRST_REGISTER = (block, polyRegistry) -> {
        if (isAir(block)) {
            polyRegistry.registerBlockPoly(block, new SimpleReplacementPoly(Blocks.AIR.getDefaultState()));
            return;
        }
        polyRegistry.registerBlockPoly(block, new SimpleReplacementPoly(block.getDefaultState()));
    };

    public static BlockPoly getPoly(Block block, PolyRegistry builder) throws OutOfBoundsException {
        return new UnusedBlockStatePoly(block,NO_COLLISION_BLOCKS,builder,NO_COLLISION_FILTER,NO_COLLISION_ON_FIRST_REGISTER);
    }

    public static boolean isAir(Block b) {
        System.out.println(b + " = " + (b == Blocks.CAVE_AIR || b == Blocks.VOID_AIR || b == Blocks.STRUCTURE_VOID));
        return b == Blocks.CAVE_AIR || b == Blocks.VOID_AIR || b == Blocks.STRUCTURE_VOID;
    }
}
