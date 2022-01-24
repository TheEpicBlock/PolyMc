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
package io.github.theepicblock.polymc.impl.poly.block;

import io.github.theepicblock.polymc.api.block.BlockPoly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

/**
 * This poly simply replaces the block with another block
 */
public class SimpleReplacementPoly implements BlockPoly {
    protected final BlockState state;

    public SimpleReplacementPoly(BlockState state) {
        this.state = state;
    }

    public SimpleReplacementPoly(Block block) {
        this(block.getDefaultState());
    }

    @Override
    public BlockState getClientBlock(BlockState input) {
        return state;
    }

    @Override
    public String getDebugInfo(Block obj) {
        return state.toString();
    }
}
