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
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

/**
 * Class to automatically generate {@link BlockPoly}s for {@link Block}s
 */
public class BlockPolyGenerator {
    /**
     * Generates the most suitable {@link BlockPoly} for a given {@link Block}
     */
    public static BlockPoly generatePoly(Block block, PolyRegistry builder) {
        BlockState state = block.getDefaultState();
        FakedWorld fakeWorld = new FakedWorld(state);

        //Get the block's collision shape.
        VoxelShape collisionShape;
        try {
            collisionShape = state.getCollisionShape(fakeWorld, BlockPos.ORIGIN);
        } catch (Exception e) {
            PolyMc.LOGGER.warn("Failed to get collision shape for " + block.getTranslationKey());
            e.printStackTrace();
            collisionShape = VoxelShapes.UNBOUNDED;
        }

        //=== INVISIBLE BLOCKS ===
        if (block.getRenderType(state) == BlockRenderType.INVISIBLE) {
            //This block is supposed to be invisible anyway

            if (Block.isShapeFullCube(collisionShape)) {
                return new SimpleReplacementPoly(Blocks.BARRIER);
            }

            if (collisionShape.isEmpty()) {
                //Try to get its selection shape so we can decide between a structure void (which has a selection box) and air (which doesn't)
                try {
                    VoxelShape outlineShape = state.getOutlineShape(fakeWorld, BlockPos.ORIGIN);

                    if (outlineShape.isEmpty()) {
                        return new SimpleReplacementPoly(Blocks.VOID_AIR);
                    } else {
                        return new SimpleReplacementPoly(Blocks.STRUCTURE_VOID);
                    }
                } catch (Exception e) {
                    PolyMc.LOGGER.warn("Failed to get outline shape for " + block.getTranslationKey());
                    e.printStackTrace();
                }
            }

            //This is neither full not empty, yet it's invisible. So the other strategies won't work.
            //Default to stone
            return new SimpleReplacementPoly(Blocks.STONE);
        }

        //=== FLUIDS ===
        if (block instanceof FluidBlock) {
            return new PropertyRetainingReplacementPoly(Blocks.WATER);
        }

        //=== LEAVES ===
        if (block instanceof LeavesBlock || BlockTags.LEAVES.contains(block)) { //TODO I don't like that leaves can be set tags in datapacks, it might cause issues. However, as not every leaf block extends LeavesBlock I can't see much of a better option. Except to maybe check the id if it ends on "_leaves"
            try {
                return new SingleUnusedBlockStatePoly(builder, BlockStateProfile.LEAVES_PROFILE);
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }

        //=== (TRAP)DOORS ===
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

        //=== FULL BLOCKS ===
        if (Block.isShapeFullCube(collisionShape)) {
            try {
                return new UnusedBlockStatePoly(block, builder, BlockStateProfile.NOTE_BLOCK_PROFILE);
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }

        //=== NO COLLISION BLOCKS ===
        if (collisionShape.isEmpty()) {
            if (block instanceof SaplingBlock) {
                try { //prevents saplings using 2 states when it isn't needed
                    return new SingleUnusedBlockStatePoly(builder, BlockStateProfile.NO_COLLISION_PROFILE);
                } catch (BlockStateManager.StateLimitReachedException ignored) {}
            }
            try {
                return new SwitchingUnusedBlockStatePoly(block, builder,
                        BlockStateProfile.KELP_PROFILE, // Chosen if the block state has a fluid state. (For waterlogable blocks)
                        BlockStateProfile.NO_COLLISION_PROFILE, // Chosen if the block state doesn't have a fluid state
                        (blockState) -> ((BlockState)blockState).getFluidState().isEmpty());
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }

        //=== SLABS ===
        if (block instanceof SlabBlock) {
            try {
                return new UnusedBlockStatePoly(block, builder, BlockStateProfile.PETRIFIED_OAK_SLAB_PROFILE);
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }

        //=== FARMLAND-LIKE BLOCKS ===
        if (Util.areEqual(collisionShape, Blocks.FARMLAND.getCollisionShape(Blocks.FARMLAND.getDefaultState(), fakeWorld, BlockPos.ORIGIN, ShapeContext.absent()))) {
            try {
                return new UnusedBlockStatePoly(block, builder, BlockStateProfile.FARMLAND_PROFILE);
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }

        //=== CACTUS-LIKE BLOCKS ===
        if (Util.areEqual(collisionShape, Blocks.CACTUS.getCollisionShape(Blocks.CACTUS.getDefaultState(), fakeWorld, BlockPos.ORIGIN, ShapeContext.absent()))) {
            try {
                return new SingleUnusedBlockStatePoly(builder, BlockStateProfile.CACTUS_PROFILE);
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }

        //=== DEFAULT ===
        //PolyMc can't handle this block. TODO implement more general polys to more of these cases
        return new SimpleReplacementPoly(Blocks.STONE);
    }

    /**
     * Generates the most suitable {@link BlockPoly} and directly adds it to the {@link PolyRegistry}
     * @see #generatePoly(Block, PolyRegistry)
     */
    public static void addBlockToBuilder(Block block, PolyRegistry builder) {
        try {
            builder.registerBlockPoly(block, generatePoly(block, builder));
        } catch (Exception e) {
            PolyMc.LOGGER.error("Failed to generate a poly for block " + block.getTranslationKey());
            e.printStackTrace();
            PolyMc.LOGGER.error("Attempting to recover by using a default poly. Please report this");
            builder.registerBlockPoly(block, new SimpleReplacementPoly(Blocks.RED_STAINED_GLASS));
        }
    }

    /**
     * A world filled with air except for a single block at 0,0,0.
     */
    public static class FakedWorld implements BlockView {
        public final BlockState blockState;
        public final BlockEntity blockEntity;

        /**
         * Initializes a new fake world. This world is filled with air except for 0,0,0
         * @param block The block that will be used at 0,0,0
         */
        public FakedWorld(BlockState block) {
            blockState = block;

            if (blockState.getBlock() instanceof BlockEntityProvider beProvider) {
                blockEntity = beProvider.createBlockEntity(BlockPos.ORIGIN, blockState);
            } else {
                blockEntity = null;
            }
        }

        @Override
        public BlockEntity getBlockEntity(BlockPos pos) {
            if (pos.equals(BlockPos.ORIGIN)) {
                return blockEntity;
            }
            return null;
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            if (pos.equals(BlockPos.ORIGIN)) {
                return blockState;
            }
            return null;
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            if (pos.equals(BlockPos.ORIGIN)) {
                return blockState.getFluidState();
            }
            return null;
        }

        @Override
        public int getHeight() {
            return 255;
        }

        @Override
        public int getBottomY() {
            return 0;
        }
    }
}
