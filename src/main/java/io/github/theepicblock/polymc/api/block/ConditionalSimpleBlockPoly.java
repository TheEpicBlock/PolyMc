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

import io.github.theepicblock.polymc.resource.ResourcePackMaker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import java.util.function.Predicate;

/**
 * This block poly replaces the the block it's registered to with another blockstate, but only if exempts returns false
 */
public class ConditionalSimpleBlockPoly extends SimpleReplacementPoly {
    private final Predicate<BlockState> exempts;

    public ConditionalSimpleBlockPoly(BlockState state, Predicate<BlockState> exempts) {
        super(state);
        this.exempts = exempts;
    }

    @Override
    public BlockState getClientBlock(BlockState input) {
        return exempts.test(input) ? input : state;
    }

    @Override
    public void AddToResourcePack(Block block, ResourcePackMaker pack) {
    }
}
