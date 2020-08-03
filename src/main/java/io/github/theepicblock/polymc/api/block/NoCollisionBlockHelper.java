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
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.enums.WireConnection;
import net.minecraft.state.property.Property;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Helper to make block polys without collision.
 */
public class NoCollisionBlockHelper {
    private static final Block[] NO_COLLISION_BLOCKS = {Blocks.SUGAR_CANE,
            Blocks.ACACIA_SAPLING,Blocks.BIRCH_SAPLING,Blocks.DARK_OAK_SAPLING,Blocks.JUNGLE_SAPLING,Blocks.OAK_SAPLING,
            Blocks.REDSTONE_WIRE};
    private static final Predicate<BlockState> NO_COLLISION_FILTER = (blockState) -> {
        if (blockState.getBlock() == Blocks.REDSTONE_WIRE) {
            return isRedstoneStateUsable(blockState);
        }
        return blockState != blockState.getBlock().getDefaultState();
    };
    private static final BiConsumer<Block, PolyRegistry> NO_COLLISION_ON_FIRST_REGISTER = (block, polyRegistry) -> {
        if (block == Blocks.REDSTONE_WIRE) {
            polyRegistry.registerBlockPoly(block, new RedstonePoly());
        } else {
            polyRegistry.registerBlockPoly(block, new SimpleReplacementPoly(block.getDefaultState()));
        }
    };
    private static final Property<?>[] REDSTONE_PROPERTIES = {RedstoneWireBlock.WIRE_CONNECTION_NORTH,RedstoneWireBlock.WIRE_CONNECTION_WEST,RedstoneWireBlock.WIRE_CONNECTION_SOUTH,RedstoneWireBlock.WIRE_CONNECTION_EAST};

    private static boolean isRedstoneStateUsable(BlockState block){
        int amountConnected = 0;
        for (Property<?> p : REDSTONE_PROPERTIES) {
            if (((WireConnection) block.get(p)).isConnected()) amountConnected++;
        }
        return amountConnected == 1;
    }

    public static BlockPoly getPoly(Block block, PolyRegistry builder) throws OutOfBoundsException {
        return new UnusedBlockStatePoly(block,NO_COLLISION_BLOCKS,builder,NO_COLLISION_FILTER,NO_COLLISION_ON_FIRST_REGISTER);
    }

    private static class RedstonePoly implements BlockPoly {
        @Override
        public BlockState getClientBlock(BlockState input) {
            return isRedstoneStateUsable(input) ? Blocks.REDSTONE_WIRE.getDefaultState() : input;
        }

        @Override
        public void AddToResourcePack(Block block, ResourcePackMaker pack) {}
    }
}
