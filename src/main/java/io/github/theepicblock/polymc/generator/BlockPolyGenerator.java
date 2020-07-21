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
package io.github.theepicblock.polymc.generator;

import io.github.theepicblock.polymc.Util;
import io.github.theepicblock.polymc.api.OutOfBoundsException;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.block.SimpleReplacementPoly;
import io.github.theepicblock.polymc.api.block.UnusedBlockStatePoly;
import io.github.theepicblock.polymc.api.register.PolyRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;

/**
 * Class to automatically generate BlockPolys for Blocks
 */
public class BlockPolyGenerator {
    /**
     * Automatically generates all {@link BlockPoly}s that are missing in the specified builder
     * @param builder builder to add the {@link BlockPoly}s to
     */
    public static void generateMissing(PolyRegistry builder) {
        for (Block block : getItemRegistry()) {
            if (builder.hasBlockPoly(block)) continue;
            Identifier id = getItemRegistry().getId(block);
            if (!Util.isVanilla(id)) {
                //this is a modded block and should have a Poly
                addBlockToBuilder(block,builder);
            }
        }
    }

    /**
     * Generates the most suitable BlockPoly for a given block
     */
    public static BlockPoly generatePoly(Block block, PolyRegistry builder) {
        BlockState state = block.getDefaultState();
        VoxelShape collisionShape = state.getCollisionShape(null, null);

        System.out.println(block.getTranslationKey());
        if (Block.isShapeFullCube(collisionShape)) {
            try {
                return new UnusedBlockStatePoly(block,Blocks.NOTE_BLOCK,builder);
            } catch (OutOfBoundsException ignored) {}
        }
        if (collisionShape.isEmpty()) {
            System.out.println("empty");
            try {
                return new UnusedBlockStatePoly(block,Blocks.SUGAR_CANE,builder);
            } catch (OutOfBoundsException ignored) {}
        }
        return new SimpleReplacementPoly(Blocks.STONE); //TODO better implementation
    }

    /**
     * Generates the most suitable BlockPoly and directly adds it to the {@link PolyRegistry}
     * @see #generatePoly(Block, PolyRegistry)
     */
    private static void addBlockToBuilder(Block block, PolyRegistry builder) {
        builder.registerBlockPoly(block, generatePoly(block,builder));
    }

    /**
     * @return the minecraft item registry
     */
    private static DefaultedRegistry<Block> getItemRegistry() {
        return Registry.BLOCK;
    }
}
