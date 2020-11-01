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
import io.github.theepicblock.polymc.impl.poly.block.ConditionalSimpleBlockPoly;
import io.github.theepicblock.polymc.impl.poly.block.PropertyRetainingReplacementPoly;
import io.github.theepicblock.polymc.impl.poly.block.SimpleReplacementPoly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.state.property.Properties;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Defines a group of blocks and blockstates.
 * Is used by {@link BlockStateManager} to define which blockstates can be used and which not.
 * Also includes info on how to handle these blockstate {@link #onFirstRegister}
 */
@SuppressWarnings("PointlessBooleanExpression")
public class BlockStateProfile {
    public Block[] blocks;
    public Predicate<BlockState> filter;
    public BiConsumer<Block,PolyRegistry> onFirstRegister;
    public String name;

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
            Blocks.CAVE_AIR, Blocks.VOID_AIR, Blocks.STRUCTURE_VOID};
    private static final Block[] DOOR_BLOCKS = {Blocks.ACACIA_DOOR,Blocks.BIRCH_DOOR,Blocks.DARK_OAK_DOOR,Blocks.JUNGLE_DOOR,Blocks.OAK_DOOR,Blocks.SPRUCE_DOOR,Blocks.CRIMSON_DOOR,Blocks.WARPED_DOOR};
    private static final Block[] TRAPDOOR_BLOCKS = {Blocks.ACACIA_TRAPDOOR,Blocks.BIRCH_TRAPDOOR,Blocks.DARK_OAK_TRAPDOOR,Blocks.JUNGLE_TRAPDOOR,Blocks.OAK_TRAPDOOR,Blocks.SPRUCE_TRAPDOOR,Blocks.CRIMSON_TRAPDOOR,Blocks.WARPED_TRAPDOOR};

    //FILTERS
    private static final Predicate<BlockState> DEFAULT_FILTER = (blockState) -> blockState != blockState.getBlock().getDefaultState();
    private static final Predicate<BlockState> NO_COLLISION_FILTER = (blockState) -> {
        if (isAir(blockState.getBlock())) return true;
        return DEFAULT_FILTER.test(blockState);
    };
    private static final Predicate<BlockState> ALWAYS_TRUE_FILTER = (blockState) -> true;
    private static final Predicate<BlockState> FARMLAND_FILTER = (blockstate) -> {
        int moisture = blockstate.get(FarmlandBlock.MOISTURE);
        return moisture != 0 && moisture != 7;
    };
    private static final Predicate<BlockState> POWERED_FILTER = (blockState) -> blockState.get(Properties.POWERED) == true;

    //ON FIRST REGISTERS
    private static final BiConsumer<Block,PolyRegistry> DEFAULT_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, new SimpleReplacementPoly(block.getDefaultState()));
    private static final BiConsumer<Block,PolyRegistry> NO_COLLISION_ON_FIRST_REGISTER = (block, polyRegistry) -> {
        if (isAir(block)) {
            polyRegistry.registerBlockPoly(block, new SimpleReplacementPoly(Blocks.AIR.getDefaultState()));
            return;
        }
        polyRegistry.registerBlockPoly(block, new SimpleReplacementPoly(block.getDefaultState()));
    };
    private static final BiConsumer<Block,PolyRegistry> PETRIFIED_OAK_SLAB_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, new PropertyRetainingReplacementPoly(Blocks.OAK_SLAB));
    private static final BiConsumer<Block,PolyRegistry> FARMLAND_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, new ConditionalSimpleBlockPoly(Blocks.FARMLAND.getDefaultState(), FARMLAND_FILTER.negate()));
    private static final BiConsumer<Block,PolyRegistry> POWERED_BLOCK_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, (BlockPolyPredicate)(block2) -> block2.with(Properties.POWERED, false));

    //PROFILES
    public static BlockStateProfile NOTEBLOCK_PROFILE = getDefaultedProfileFromBlock("noteblocks", Blocks.NOTE_BLOCK);
    public static BlockStateProfile LEAVES_PROFILE = getDefaultedProfileFromBlock("leaves", LEAVES_BLOCKS);
    public static BlockStateProfile NO_COLLISION_PROFILE = new BlockStateProfile("blocks without collisions", NO_COLLISION_BLOCKS, NO_COLLISION_FILTER, NO_COLLISION_ON_FIRST_REGISTER);
    public static BlockStateProfile PETRIFIED_OAK_SLAB_PROFILE = new BlockStateProfile("petrified oak slab", Blocks.PETRIFIED_OAK_SLAB, ALWAYS_TRUE_FILTER, PETRIFIED_OAK_SLAB_ON_FIRST_REGISTER);
    public static BlockStateProfile FARMLAND_PROFILE = new BlockStateProfile("farmland", Blocks.FARMLAND, FARMLAND_FILTER, FARMLAND_ON_FIRST_REGISTER);
    public static BlockStateProfile DOOR_PROFILE = new BlockStateProfile("door", DOOR_BLOCKS, POWERED_FILTER, POWERED_BLOCK_ON_FIRST_REGISTER);
    public static BlockStateProfile TRAPDOOR_PROFILE = new BlockStateProfile("trapdoor", TRAPDOOR_BLOCKS, POWERED_FILTER, POWERED_BLOCK_ON_FIRST_REGISTER);
    public static BlockStateProfile METAL_DOOR_PROFILE = new BlockStateProfile("metal_door", Blocks.IRON_DOOR, POWERED_FILTER, POWERED_BLOCK_ON_FIRST_REGISTER);
    public static BlockStateProfile METAL_TRAPDOOR_PROFILE = new BlockStateProfile("metal_trapdoor", Blocks.IRON_TRAPDOOR, POWERED_FILTER, POWERED_BLOCK_ON_FIRST_REGISTER);

    //OTHER CODE


    @Deprecated
    public static BlockStateProfile getDefaultedProfileFromBlock(String name, Block[] blocks) {
        return new BlockStateProfile(name, blocks, DEFAULT_FILTER, DEFAULT_ON_FIRST_REGISTER);
    }

    @Deprecated
    public static BlockStateProfile getDefaultedProfileFromBlock(String name, Block block) {
        return new BlockStateProfile(name, block, DEFAULT_FILTER, DEFAULT_ON_FIRST_REGISTER);
    }

    private static boolean isAir(Block b) {
        return b == Blocks.CAVE_AIR || b == Blocks.VOID_AIR || b == Blocks.STRUCTURE_VOID;
    }
}
