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
package io.github.theepicblock.polymc.impl.generator;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.block.BlockStateManager;
import io.github.theepicblock.polymc.api.block.BlockStateProfile;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.poly.block.*;
import io.github.theepicblock.polymc.mixins.block.MaterialAccessor;
import net.minecraft.block.*;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

/**
 * Class to automatically generate BlockPolys for Blocks
 */
public class BlockPolyGenerator {

    /**
     * Automatically generates all {@link BlockPoly}s that are missing in the specified builder
     * @param builder builder to add the {@link BlockPoly}s to
     */
    public static void generateMissing(PolyRegistry builder) {
        for (Block block : getBlockRegistry()) {
            if (builder.hasBlockPoly(block)) continue;
            Identifier id = getBlockRegistry().getId(block);
            if (!Util.isVanilla(id)) {
                //this is a modded block and should have a Poly
                addBlockToBuilder(block, builder);
            }
        }
    }

    /**
     * Generates the most suitable BlockPoly for a given block
     */
    public static BlockPoly generatePoly(Block block, PolyRegistry builder) {
        BlockState state = block.getDefaultState();
        VoxelShape collisionShape;
        try {
            collisionShape = state.getCollisionShape(null, null);
        } catch (Exception e) {
            PolyMc.LOGGER.warn("Failed to get collision shape for " + block.getTranslationKey() + ": " + e.getMessage());
            collisionShape = VoxelShapes.UNBOUNDED;
        }

        //Handle fluids
        if (block instanceof FluidBlock) {
            return new PropertyRetainingReplacementPoly(Blocks.WATER);
        }
        //Handle leaves
        if (block instanceof LeavesBlock || block.isIn(BlockTags.LEAVES)) { //TODO I don't like that leaves can be set tags in datapacks, it might cause issues. However, as not every leaf block extends LeavesBlock I can't see much of a better option. Except to maybe check the id if it ends on "_leaves"
            try {
                return new SingleUnusedBlockStatePoly(builder, BlockStateProfile.LEAVES_PROFILE);
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }
        //Handle doors/trapdoors
        boolean isMetal = ((MaterialAccessor)block).getMaterial() == Material.METAL;
        if (block instanceof DoorBlock) {
            try {
                return new PoweredStateBlockPoly(builder, isMetal ? BlockStateProfile.METAL_DOOR_PROFILE : BlockStateProfile.DOOR_PROFILE);
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }
        if (block instanceof TrapdoorBlock) {
            try {
                return new PoweredStateBlockPoly(builder, isMetal ? BlockStateProfile.METAL_TRAPDOOR_PROFILE : BlockStateProfile.TRAPDOOR_PROFILE);
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }
        //Handle full blocks
        if (Block.isShapeFullCube(collisionShape)) {
            try {
                return new UnusedBlockStatePoly(block, builder, BlockStateProfile.NOTE_BLOCK_PROFILE);
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }
        //Handle blocks without collision
        if (collisionShape.isEmpty()) {
            if (block instanceof SaplingBlock) {
                try {
                    return new SingleUnusedBlockStatePoly(builder, BlockStateProfile.NO_COLLISION_PROFILE);
                } catch (BlockStateManager.StateLimitReachedException ignored) {}
            }
            try {
                return new UnusedBlockStatePoly(block, builder, BlockStateProfile.NO_COLLISION_PROFILE);
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }
        //Handle slabs
        if (block instanceof SlabBlock) {
            try {
                return new UnusedBlockStatePoly(block, builder, BlockStateProfile.PETRIFIED_OAK_SLAB_PROFILE);
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }
        //Handle blocks with same collision as farmland
        if (collisionShape == Blocks.FARMLAND.getOutlineShape(null,null,null,null)) {
            try {
                return new UnusedBlockStatePoly(block, builder, BlockStateProfile.FARMLAND_PROFILE);
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }
        //Odd cases
        return new SimpleReplacementPoly(Blocks.STONE); //TODO better implementation
    }

    /**
     * Generates the most suitable BlockPoly and directly adds it to the {@link PolyRegistry}
     * @see #generatePoly(Block, PolyRegistry)
     */
    private static void addBlockToBuilder(Block block, PolyRegistry builder) {
        builder.registerBlockPoly(block, generatePoly(block, builder));
    }

    /**
     * @return the minecraft item registry
     */
    private static DefaultedRegistry<Block> getBlockRegistry() {
        return Registry.BLOCK;
    }
}
