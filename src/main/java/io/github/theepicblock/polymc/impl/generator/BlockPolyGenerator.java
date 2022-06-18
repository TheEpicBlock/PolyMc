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
import io.github.theepicblock.polymc.impl.misc.BooleanContainer;
import io.github.theepicblock.polymc.impl.poly.block.FunctionBlockStatePoly;
import io.github.theepicblock.polymc.impl.poly.block.SimpleReplacementPoly;
import io.github.theepicblock.polymc.mixins.block.MaterialAccessor;
import io.github.theepicblock.polymc.mixins.block.SlabBlockAccessor;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.property.Property;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

/**
 * Class to automatically generate {@link BlockPoly}s for {@link Block}s
 */
public class BlockPolyGenerator {
    /**
     * Generates the most suitable {@link BlockPoly} for a given {@link Block}
     */
    public static BlockPoly generatePoly(Block block, PolyRegistry registry) {
        return new FunctionBlockStatePoly(block, (state, isUniqueCallback) -> registerClientState(state, isUniqueCallback, registry.getSharedValues(BlockStateManager.KEY)));
    }

    /**
     * @param isUniqueCallback will be set to true if the return value is a unique block that'll only be used for the inputted moddedState
     * @return a client state which best matches the moddedState
     */
    public static BlockState registerClientState(BlockState moddedState, BooleanContainer isUniqueCallback, BlockStateManager manager) {
        var moddedBlock = moddedState.getBlock();
        var fakeWorld = new FakedWorld(moddedState);

        //Get the state's collision shape.
        VoxelShape collisionShape;
        try {
            collisionShape = moddedState.getCollisionShape(fakeWorld, BlockPos.ORIGIN);
        } catch (Exception e) {
            PolyMc.LOGGER.warn("Failed to get collision shape for " + moddedState.toString());
            e.printStackTrace();
            collisionShape = VoxelShapes.UNBOUNDED;
        }

        //=== INVISIBLE BLOCKS ===
        if (moddedState.getRenderType() == BlockRenderType.INVISIBLE) {
            //This block is supposed to be invisible anyway

            if (Block.isShapeFullCube(collisionShape)) {
                isUniqueCallback.set(false);
                return Blocks.BARRIER.getDefaultState();
            }

            if (collisionShape.isEmpty()) {
                //Try to get its selection shape so we can decide between a structure void (which has a selection box) and air (which doesn't)
                try {
                    VoxelShape outlineShape = moddedState.getOutlineShape(fakeWorld, BlockPos.ORIGIN);

                    if (outlineShape.isEmpty()) {
                        isUniqueCallback.set(false);
                        return Blocks.VOID_AIR.getDefaultState();
                    } else {
                        isUniqueCallback.set(false);
                        return Blocks.STRUCTURE_VOID.getDefaultState();
                    }
                } catch (Exception e) {
                    PolyMc.LOGGER.warn("Failed to get outline shape for " + moddedState);
                    e.printStackTrace();
                }
            }

            //This is neither full not empty, yet it's invisible. So the other strategies won't work.
            //Default to stone
            isUniqueCallback.set(false);
            return Blocks.STONE.getDefaultState();
        }

        //=== FLUIDS ===
        if (moddedBlock instanceof FluidBlock) {
            isUniqueCallback.set(false);
            return copyAllProperties(moddedState, Blocks.WATER);
        }

        //=== LEAVES ===
        if (moddedBlock instanceof LeavesBlock || moddedState.isIn(BlockTags.LEAVES)) { //TODO I don't like that leaves can be set tags in datapacks, it might cause issues. However, as not every leaf block extends LeavesBlock I can't see much of a better option. Except to maybe check the id if it ends on "_leaves"
            try {
                isUniqueCallback.set(true);
                return manager.requestBlockState(BlockStateProfile.LEAVES_PROFILE);
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }

        //=== FENCE GATES ===
        if (moddedBlock instanceof FenceGateBlock) {
            try {
                isUniqueCallback.set(true);
                return manager.requestBlockState((moddedState.get(FenceGateBlock.OPEN) ? BlockStateProfile.OPEN_FENCE_GATE_PROFILE : BlockStateProfile.FENCE_GATE_PROFILE)
                        .and(state -> propertyMatches(state, moddedState, FenceGateBlock.IN_WALL, HorizontalFacingBlock.FACING)));
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }

        //=== (TRAP)DOORS ===
        boolean isMetal = ((MaterialAccessor)moddedBlock).getMaterial() == Material.METAL;
        if (moddedBlock instanceof DoorBlock) {
            try {
                isUniqueCallback.set(true);
                return manager.requestBlockState((isMetal ? BlockStateProfile.METAL_DOOR_PROFILE : BlockStateProfile.DOOR_PROFILE)
                        .and((state) -> propertyMatches(state, moddedState, DoorBlock.OPEN, DoorBlock.FACING, DoorBlock.HINGE, DoorBlock.HALF)));
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }
        if (moddedBlock instanceof TrapdoorBlock) {
            try {
                isUniqueCallback.set(true);
                return manager.requestBlockState((isMetal ? BlockStateProfile.METAL_TRAPDOOR_PROFILE : BlockStateProfile.TRAPDOOR_PROFILE)
                        .and((state) -> propertyMatches(state, moddedState, TrapdoorBlock.OPEN, TrapdoorBlock.FACING, TrapdoorBlock.HALF, TrapdoorBlock.WATERLOGGED)));
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }

        //=== SLABS ===
        if (moddedBlock instanceof SlabBlock) {
            try {
                isUniqueCallback.set(true);
                return manager.requestBlockState(BlockStateProfile.SLAB_PROFILE.and(
                        state -> propertyMatches(state, moddedState, SlabBlock.WATERLOGGED, SlabBlock.TYPE)
                ));
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }

        if (Util.areEqual(collisionShape, SlabBlockAccessor.getBOTTOM_SHAPE())) {
            try {
                isUniqueCallback.set(true);
                return manager.requestBlockState(BlockStateProfile.SCULK_SENSOR_PROFILE.and(
                        state -> moddedState.getFluidState().equals(state.getFluidState())
                ));
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }

        //=== STAIRS ===
        if (moddedBlock instanceof StairsBlock) {
            try {
                isUniqueCallback.set(true);
                return manager.requestBlockState(BlockStateProfile.WAXED_COPPER_STAIR_PROFILE.and(
                        state -> propertyMatches(state, moddedState, StairsBlock.FACING, StairsBlock.HALF, StairsBlock.WATERLOGGED, StairsBlock.SHAPE)
                ));
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }

        //=== FULL BLOCKS ===
        // Blocks that have a full top face and at least something on the bottom are considered full blocks. This works better for some blocks
        if (Block.isFaceFullSquare(collisionShape, Direction.UP) && collisionShape.getMin(Direction.Axis.Y) <= 0) {

            if (!moddedState.isOpaque()) {
                // Chorus flowers are full cubes & are not opaque.
                // There are only 4 available states to reuse though
                try {
                    isUniqueCallback.set(true);
                    return manager.requestBlockState(BlockStateProfile.CHORUS_FLOWER_BLOCK_PROFILE);
                } catch (BlockStateManager.StateLimitReachedException ignored) {}

                // Each chorus plant state has a slightly different collision box.
                // But it's roughly a full cube (it's the corners that miss a few pixels of collision)
                try {
                    isUniqueCallback.set(true);
                    return manager.requestBlockState(BlockStateProfile.CHORUS_PLANT_BLOCK_PROFILE);
                } catch (BlockStateManager.StateLimitReachedException ignored) {}
            }

            try {
                isUniqueCallback.set(true);
                return manager.requestBlockState(BlockStateProfile.FULL_BLOCK_PROFILE);
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }

        //=== NO COLLISION BLOCKS ===
        if (collisionShape.isEmpty() && !(moddedState.getBlock() instanceof WallBlock)) {

            try {
                if (moddedState.isIn(BlockTags.CLIMBABLE)) {
                    isUniqueCallback.set(true);
                    return manager.requestBlockState(BlockStateProfile.CLIMBABLE_PROFILE);
                }
            } catch (BlockStateManager.StateLimitReachedException ignored) {}

            var outlineShape = moddedState.getOutlineShape(fakeWorld, BlockPos.ORIGIN);

            if (outlineShape.isEmpty()) {
                try {
                    isUniqueCallback.set(true);
                    return manager.requestBlockState(BlockStateProfile.NO_COLLISION_WALL_PROFILE.and(
                            state -> moddedState.getFluidState().equals(state.getFluidState())
                    ));
                } catch (BlockStateManager.StateLimitReachedException ignored) {}
            }

            if (outlineShape.getMax(Direction.Axis.Y) <= (1.0f / 16.0f)) {
                try {
                    isUniqueCallback.set(true);
                    return manager.requestBlockState(BlockStateProfile.PRESSURE_PLATE_PROFILE.and(
                            state -> moddedState.getFluidState().equals(state.getFluidState())
                    ));
                } catch (BlockStateManager.StateLimitReachedException ignored) {}
            }

            try {
                isUniqueCallback.set(true);
                return manager.requestBlockState(BlockStateProfile.NO_COLLISION_PROFILE.and(
                        state -> moddedState.getFluidState().equals(state.getFluidState())
                ));
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }

        //=== FARMLAND-LIKE BLOCKS ===
        if (Util.areEqual(collisionShape, Blocks.FARMLAND.getCollisionShape(Blocks.FARMLAND.getDefaultState(), fakeWorld, BlockPos.ORIGIN, ShapeContext.absent()))) {
            try {
                isUniqueCallback.set(true);
                return manager.requestBlockState(BlockStateProfile.FARMLAND_PROFILE);
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }

        //=== CACTUS-LIKE BLOCKS ===
        if (Util.areEqual(collisionShape, Blocks.CACTUS.getCollisionShape(Blocks.CACTUS.getDefaultState(), fakeWorld, BlockPos.ORIGIN, ShapeContext.absent()))) {
            try {
                isUniqueCallback.set(true);
                return manager.requestBlockState(BlockStateProfile.CACTUS_PROFILE);
            } catch (BlockStateManager.StateLimitReachedException ignored) {}
        }

        //=== DEFAULT ===
        //PolyMc can't handle this block. TODO implement more general polys to more of these cases
        isUniqueCallback.set(false);
        return Blocks.STONE.getDefaultState();
    }

    public static boolean propertyMatches(BlockState a, BlockState b, Property<?>... properties) {
        for (var property : properties) {
            if (!propertyMatches(a, b, property)) return false;
        }
        return true;
    }

    public static <T extends Comparable<T>> boolean propertyMatches(BlockState a, BlockState b, Property<T> property) {
        return a.get(property) == b.get(property);
    }

    public static BlockState copyAllProperties(BlockState input, Block output) {
        BlockState out = output.getDefaultState();
        for (Property<?> p : input.getProperties()) {
            out = copyProperty(out, input, p);
        }
        return out;
    }

    private static <T extends Comparable<T>> BlockState copyProperty(BlockState a, BlockState b, Property<T> p) {
        return a.with(p, b.get(p));
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
        public @Nullable BlockEntity blockEntity;

        /**
         * Initializes a new fake world. This world is filled with air except for 0,0,0
         * @param block The block that will be used at 0,0,0
         */
        public FakedWorld(BlockState block) {
            blockState = block;
        }

        @Override
        @Nullable
        public BlockEntity getBlockEntity(BlockPos pos) {
            if (this.blockEntity == null && blockState.getBlock() instanceof BlockEntityProvider beProvider) {
                this.blockEntity = beProvider.createBlockEntity(BlockPos.ORIGIN, blockState);
            }
            return blockEntity;
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            if (pos.equals(BlockPos.ORIGIN)) {
                return blockState;
            }
            return Blocks.AIR.getDefaultState();
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            return this.getBlockState(pos).getFluidState();
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
