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

import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.resource.ResourcePackMaker;
import io.github.theepicblock.polymc.impl.poly.block.ConditionalSimpleBlockPoly;
import io.github.theepicblock.polymc.impl.poly.block.PropertyRetainingReplacementPoly;
import io.github.theepicblock.polymc.impl.poly.block.SimpleReplacementPoly;
import net.minecraft.block.*;
import net.minecraft.item.HoneycombItem;
import net.minecraft.state.property.Properties;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Defines a group of blocks and blockstates.
 * Is used by {@link BlockStateManager} to define which blockstates can be used and which not.
 * Also includes info on how to handle these blockstate {@link #onFirstRegister}.
 */
@SuppressWarnings("PointlessBooleanExpression")
public class BlockStateProfile {
    public final Block[] blocks;
    public final Predicate<BlockState> filter;
    public final BiConsumer<Block,PolyRegistry> onFirstRegister;
    public final String name;

    public BlockStateProfile(String name, Block[] blocks, Predicate<BlockState> filter, BiConsumer<Block,PolyRegistry> onFirstRegister) {
        this.blocks = blocks;
        this.filter = filter;
        this.onFirstRegister = onFirstRegister;
        this.name = name;
    }

    public BlockStateProfile(String name, Block block, Predicate<BlockState> filter, BiConsumer<Block,PolyRegistry> onFirstRegister) {
        this.blocks = new Block[]{block};
        this.filter = filter;
        this.onFirstRegister = onFirstRegister;
        this.name = name;
    }


    //BLOCK LISTS
    private static final Block[] LEAVES_BLOCKS = {Blocks.ACACIA_LEAVES,Blocks.BIRCH_LEAVES,Blocks.DARK_OAK_LEAVES,Blocks.JUNGLE_LEAVES,Blocks.OAK_LEAVES,Blocks.SPRUCE_LEAVES};
    private static final Block[] NO_COLLISION_BLOCKS = {Blocks.SUGAR_CANE,
            Blocks.ACACIA_SAPLING, Blocks.BIRCH_SAPLING, Blocks.DARK_OAK_SAPLING, Blocks.JUNGLE_SAPLING, Blocks.OAK_SAPLING,
            Blocks.TRIPWIRE};
    private static final Block[] DOOR_BLOCKS = {Blocks.ACACIA_DOOR,Blocks.BIRCH_DOOR,Blocks.DARK_OAK_DOOR,Blocks.JUNGLE_DOOR,Blocks.OAK_DOOR,Blocks.SPRUCE_DOOR,Blocks.CRIMSON_DOOR,Blocks.WARPED_DOOR};
    private static final Block[] TRAPDOOR_BLOCKS = {Blocks.ACACIA_TRAPDOOR,Blocks.BIRCH_TRAPDOOR,Blocks.DARK_OAK_TRAPDOOR,Blocks.JUNGLE_TRAPDOOR,Blocks.OAK_TRAPDOOR,Blocks.SPRUCE_TRAPDOOR,Blocks.CRIMSON_TRAPDOOR,Blocks.WARPED_TRAPDOOR};
    private static final Block[] WAXED_COPPER_STAIR_BLOCKS = {Blocks.WAXED_CUT_COPPER_STAIRS, Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS, Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS, Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS};
    private static final Block[] WAXED_COPPER_SLAB_BLOCKS = {Blocks.WAXED_CUT_COPPER_SLAB, Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB, Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB};

    //FILTERS
    private static final Predicate<BlockState> DEFAULT_FILTER = (blockState) -> blockState != blockState.getBlock().getDefaultState();
    private static final Predicate<BlockState> NO_COLLISION_FILTER = (blockState) -> {
        if (blockState.getBlock() == Blocks.TRIPWIRE) {
            return isStringUseable(blockState);
        } else {
            return DEFAULT_FILTER.test(blockState);
        }
    };
    private static final Predicate<BlockState> ALWAYS_TRUE_FILTER = (blockState) -> true;
    private static final Predicate<BlockState> FARMLAND_FILTER = (blockState) -> {
        int moisture = blockState.get(FarmlandBlock.MOISTURE);
        return moisture != 0 && moisture != 7;
    };
    private static final Predicate<BlockState> POWERED_FILTER = (blockState) -> blockState.get(Properties.POWERED) == true;

    //ON FIRST REGISTERS
    private static final BiConsumer<Block,PolyRegistry> DEFAULT_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, new SimpleReplacementPoly(block.getDefaultState()));
    private static final BiConsumer<Block,PolyRegistry> NO_COLLISION_ON_FIRST_REGISTER = (block, polyRegistry) -> {
        if (block == Blocks.TRIPWIRE) {
            polyRegistry.registerBlockPoly(block, new BlockPoly() {
                @Override
                public BlockState getClientBlock(BlockState input) {
                    return input.with(Properties.POWERED, false).with(Properties.DISARMED,false);
                }
                @Override public void addToResourcePack(Block block, ResourcePackMaker pack) {}
            });
        } else {
            polyRegistry.registerBlockPoly(block, new SimpleReplacementPoly(block.getDefaultState()));
        }
    };
    private static final BiConsumer<Block,PolyRegistry> PETRIFIED_OAK_SLAB_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, new PropertyRetainingReplacementPoly(Blocks.OAK_SLAB));
    private static final BiConsumer<Block,PolyRegistry> FARMLAND_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, new ConditionalSimpleBlockPoly(Blocks.FARMLAND.getDefaultState(), FARMLAND_FILTER));
    private static final BiConsumer<Block,PolyRegistry> POWERED_BLOCK_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, (BlockPolyPredicate)(block2) -> block2.with(Properties.POWERED, false));
    private static final BiConsumer<Block,PolyRegistry> WAXED_COPPER_ON_FIRST_REGISTER = (block, polyRegistry) -> {
        var unwaxedBlock = HoneycombItem.WAXED_TO_UNWAXED_BLOCKS.get().get(block);
        polyRegistry.registerBlockPoly(block, new PropertyRetainingReplacementPoly(unwaxedBlock));
    };

    //PROFILES
    public static final BlockStateProfile NOTE_BLOCK_PROFILE = getProfileWithDefaultFilter("note block", Blocks.NOTE_BLOCK);
    public static final BlockStateProfile LEAVES_PROFILE = getProfileWithDefaultFilter("leaves", LEAVES_BLOCKS);
    public static final BlockStateProfile NO_COLLISION_PROFILE = new BlockStateProfile("blocks without collisions", NO_COLLISION_BLOCKS, NO_COLLISION_FILTER, NO_COLLISION_ON_FIRST_REGISTER);
    public static final BlockStateProfile PETRIFIED_OAK_SLAB_PROFILE = new BlockStateProfile("petrified oak slab", Blocks.PETRIFIED_OAK_SLAB, ALWAYS_TRUE_FILTER, PETRIFIED_OAK_SLAB_ON_FIRST_REGISTER);
    public static final BlockStateProfile FARMLAND_PROFILE = new BlockStateProfile("farmland", Blocks.FARMLAND, FARMLAND_FILTER, FARMLAND_ON_FIRST_REGISTER);
    public static final BlockStateProfile CACTUS_PROFILE = getProfileWithDefaultFilter("cactus", Blocks.CACTUS);
    public static final BlockStateProfile KELP_PROFILE = getProfileWithDefaultFilter("kelp", Blocks.KELP);
    public static final BlockStateProfile DOOR_PROFILE = new BlockStateProfile("door", DOOR_BLOCKS, POWERED_FILTER, POWERED_BLOCK_ON_FIRST_REGISTER);
    public static final BlockStateProfile TRAPDOOR_PROFILE = new BlockStateProfile("trapdoor", TRAPDOOR_BLOCKS, POWERED_FILTER, POWERED_BLOCK_ON_FIRST_REGISTER);
    public static final BlockStateProfile METAL_DOOR_PROFILE = new BlockStateProfile("metal_door", Blocks.IRON_DOOR, POWERED_FILTER, POWERED_BLOCK_ON_FIRST_REGISTER);
    public static final BlockStateProfile METAL_TRAPDOOR_PROFILE = new BlockStateProfile("metal_trapdoor", Blocks.IRON_TRAPDOOR, POWERED_FILTER, POWERED_BLOCK_ON_FIRST_REGISTER);
    public static final BlockStateProfile WAXED_COPPER_STAIR_PROFILE = new BlockStateProfile("waxed_copper_stair", WAXED_COPPER_STAIR_BLOCKS, ALWAYS_TRUE_FILTER, WAXED_COPPER_ON_FIRST_REGISTER);
    public static final BlockStateProfile WAXED_COPPER_SLAB_PROFILE = new BlockStateProfile("waxed_copper_slab", WAXED_COPPER_SLAB_BLOCKS, ALWAYS_TRUE_FILTER, WAXED_COPPER_ON_FIRST_REGISTER);

    //OTHER CODE
    public static BlockStateProfile getProfileWithDefaultFilter(String name, Block[] blocks) {
        return new BlockStateProfile(name, blocks, DEFAULT_FILTER, DEFAULT_ON_FIRST_REGISTER);
    }

    public static BlockStateProfile getProfileWithDefaultFilter(String name, Block block) {
        return new BlockStateProfile(name, block, DEFAULT_FILTER, DEFAULT_ON_FIRST_REGISTER);
    }

    private static boolean isStringUseable(BlockState state) {
        return  state.get(Properties.POWERED) == true ||
                state.get(TripwireBlock.DISARMED) == true;
    }

    public BlockStateProfile and(Predicate<BlockState> filter) {
        return new BlockStateProfile(this.name, this.blocks, this.filter.and(filter), this.onFirstRegister);
    }
}
