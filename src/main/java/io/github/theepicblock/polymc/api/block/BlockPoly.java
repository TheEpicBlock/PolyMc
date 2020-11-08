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

import io.github.theepicblock.polymc.api.DebugInfoProvider;
import io.github.theepicblock.polymc.api.resource.ResourcePackMaker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface BlockPoly extends DebugInfoProvider<Block> {
    /**
     * Transforms an BlockState to it's client version
     * @param input original BlockState
     * @return BlockState that should be sent to the client
     */
    BlockState getClientBlock(BlockState input);

    /**
     * Transforms an BlockState to it's client version. Has some extra context.
     * {@link #isNotConsistent()} must return true for this blockpoly for this method to be called (performance reasons).
     * However, this blockpoly being marked as not consistent isn't a guarantee this method will be used instead of {@link #getClientBlock(BlockState)}, there might still be situations where that method might be used.
     * @param input original BlockState
     * @param pos the position this block is in
     * @param world the world this block is in
     * @return BlockState that should be sent to the client
     */
    default BlockState getClientBlockWithContext(BlockState input, BlockPos pos, World world) {
        return getClientBlock(input);
    }

    /**
     * Marks this blockpoly as not consistent.
     * You should make this return true if the output of {@link #getClientBlock(BlockState)} relies on anything else except the BlockState it was given as input.
     * It also unlocks the {@link #getClientBlockWithContext(BlockState, BlockPos, World)} method.
     * @return true if this blockpoly isn't consistent
     */
    default boolean isNotConsistent() {
        return false;
    }

    /**
     * Callback to add all resources needed for this block to a resourcepack
     * @param block block this BlockPoly was registered to, for reference.
     * @param pack  resource pack to add to.
     */
    void AddToResourcePack(Block block, ResourcePackMaker pack);
}
