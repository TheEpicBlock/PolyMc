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

import net.minecraft.block.BlockState;

import java.util.function.Predicate;

/**
 * This block poly replaces the the block it's registered to with another blockstate, but only if condition returns true
 */
public class ConditionalSimpleBlockPoly extends SimpleReplacementPoly {
    private final Predicate<BlockState> condition;

    public ConditionalSimpleBlockPoly(BlockState state, Predicate<BlockState> condition) {
        super(state);
        this.condition = condition;
    }

    @Override
    public BlockState getClientBlock(BlockState input) {
        return condition.test(input) ? state : input;
    }
}
