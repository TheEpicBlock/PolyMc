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
import io.github.theepicblock.polymc.impl.poly.block.ListOfSlabs;
import io.github.theepicblock.polymc.impl.poly.block.PropertyRetainingReplacementPoly;
import io.github.theepicblock.polymc.impl.poly.block.SimpleReplacementPoly;
import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.SculkSensorPhase;
import net.minecraft.block.enums.SlabType;
import net.minecraft.block.enums.WallShape;
import net.minecraft.item.HoneycombItem;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * A BlockStateProfile defines a group of states which may be used on the client for other purposes.
 * The {@link #onFirstRegister} field should register a poly which removes these states from regular use.
 *
 * PolyMc has a whole host of built-in profiles which make use of various properties that don't affect the client.
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
    private static final Block[] LEAVES_BLOCKS = {Blocks.ACACIA_LEAVES, Blocks.BIRCH_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.AZALEA_LEAVES, Blocks.FLOWERING_AZALEA_LEAVES};
    private static final Block[] SAPLING_BLOCKS = {Blocks.ACACIA_SAPLING, Blocks.BIRCH_SAPLING, Blocks.DARK_OAK_SAPLING, Blocks.JUNGLE_SAPLING, Blocks.OAK_SAPLING, Blocks.SPRUCE_SAPLING};
    private static final Block[] DOOR_BLOCKS = {Blocks.ACACIA_DOOR, Blocks.BIRCH_DOOR, Blocks.DARK_OAK_DOOR, Blocks.JUNGLE_DOOR, Blocks.OAK_DOOR, Blocks.SPRUCE_DOOR, Blocks.CRIMSON_DOOR, Blocks.WARPED_DOOR};
    private static final Block[] TRAPDOOR_BLOCKS = {Blocks.ACACIA_TRAPDOOR, Blocks.BIRCH_TRAPDOOR, Blocks.DARK_OAK_TRAPDOOR, Blocks.JUNGLE_TRAPDOOR, Blocks.OAK_TRAPDOOR, Blocks.SPRUCE_TRAPDOOR, Blocks.CRIMSON_TRAPDOOR, Blocks.WARPED_TRAPDOOR};
    private static final Block[] FENCE_GATE_BLOCKS = {Blocks.ACACIA_FENCE_GATE, Blocks.BIRCH_FENCE_GATE, Blocks.DARK_OAK_FENCE_GATE, Blocks.JUNGLE_FENCE_GATE, Blocks.OAK_FENCE_GATE, Blocks.SPRUCE_FENCE_GATE, Blocks.CRIMSON_FENCE_GATE, Blocks.WARPED_FENCE_GATE};
    private static final Block[] WAXED_COPPER_STAIR_BLOCKS = {Blocks.WAXED_CUT_COPPER_STAIRS, Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS, Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS, Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS};
    private static final Block[] WAXED_COPPER_SLAB_BLOCKS = {Blocks.WAXED_CUT_COPPER_SLAB, Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB, Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB};
    private static final Block[] WAXED_COPPER_FULL_BLOCKS = {Blocks.WAXED_COPPER_BLOCK, Blocks.WAXED_EXPOSED_COPPER, Blocks.WAXED_WEATHERED_COPPER, Blocks.WAXED_OXIDIZED_COPPER, Blocks.WAXED_CUT_COPPER, Blocks.WAXED_EXPOSED_CUT_COPPER, Blocks.WAXED_WEATHERED_CUT_COPPER, Blocks.WAXED_OXIDIZED_COPPER};
    private static final Block[] INFESTED_BLOCKS = {Blocks.INFESTED_COBBLESTONE, Blocks.INFESTED_STONE, Blocks.INFESTED_CHISELED_STONE_BRICKS, Blocks.INFESTED_STONE_BRICKS, Blocks.INFESTED_CRACKED_STONE_BRICKS, Blocks.INFESTED_DEEPSLATE, Blocks.INFESTED_MOSSY_STONE_BRICKS};
    private static final Block[] DISPENSER_BLOCKS = {Blocks.DISPENSER, Blocks.DROPPER};
    private static final Block[] BEEHIVE_BLOCKS = {Blocks.BEEHIVE, Blocks.BEE_NEST};
    private static final Block[] SNOWY_GRASS_BLOCKS = {Blocks.MYCELIUM, Blocks.PODZOL}; // Snowy mycelium and podzol look the same as snowy grass
    private static final Block[] WATERLOGGED_SLABS = {Blocks.SMOOTH_STONE_SLAB}; // Smooth stone double slabs do not look like regular smooth stone blocks. Therefore, only the waterlogged double slab is available to us.
    private static final Block[] WALL_BLOCKS = {Blocks.COBBLESTONE_WALL, Blocks.MOSSY_COBBLESTONE_WALL, Blocks.BRICK_WALL, Blocks.PRISMARINE_WALL, Blocks.RED_SANDSTONE_WALL, Blocks.MOSSY_STONE_BRICK_WALL, Blocks.GRANITE_WALL, Blocks.STONE_BRICK_WALL, Blocks.NETHER_BRICK_WALL, Blocks.ANDESITE_WALL, Blocks.RED_NETHER_BRICK_WALL, Blocks.SANDSTONE_WALL, Blocks.END_STONE_BRICK_WALL, Blocks.DIORITE_WALL, Blocks.BLACKSTONE_WALL, Blocks.POLISHED_BLACKSTONE_BRICK_WALL, Blocks.POLISHED_BLACKSTONE_WALL, Blocks.COBBLED_DEEPSLATE_WALL, Blocks.POLISHED_DEEPSLATE_WALL, Blocks.DEEPSLATE_TILE_WALL, Blocks.DEEPSLATE_BRICK_WALL};
    private static final Block[] CLIMBABLE_BLOCKS = {Blocks.CAVE_VINES, Blocks.WEEPING_VINES, Blocks.TWISTING_VINES};

    //FILTERS
    private static final Predicate<BlockState> DEFAULT_FILTER = (blockState) -> blockState != blockState.getBlock().getDefaultState();
    private static final Predicate<BlockState> ALWAYS_TRUE_FILTER = (blockState) -> true;
    private static final Predicate<BlockState> WALL_FILTER = (blockState) ->
            blockState.get(WallBlock.NORTH_SHAPE) == WallShape.NONE &&
            blockState.get(WallBlock.WEST_SHAPE) == WallShape.NONE &&
            blockState.get(WallBlock.EAST_SHAPE) == WallShape.NONE &&
            blockState.get(WallBlock.SOUTH_SHAPE) == WallShape.NONE &&
            blockState.get(WallBlock.UP) == false;
    private static final Predicate<BlockState> TRIPWIRE_FILTER = BlockStateProfile::isStringUseable;
    private static final Predicate<BlockState> SMALL_DRIPLEAF_FILTER = state -> state.get(TallPlantBlock.HALF) == DoubleBlockHalf.LOWER && state.get(SmallDripleafBlock.FACING) != Direction.NORTH;
    private static final Predicate<BlockState> CAVE_VINES_FILTER = state -> state.get(AbstractPlantStemBlock.AGE) != 0;
    private static final Predicate<BlockState> FARMLAND_FILTER = (blockState) -> {
        int moisture = blockState.get(FarmlandBlock.MOISTURE);
        return moisture != 0 && moisture != 7;
    };
    private static final Predicate<BlockState> BEEHIVE_FILTER = (blockState) -> {
        int level = blockState.get(BeehiveBlock.HONEY_LEVEL);
        return level != 0 && level != BeehiveBlock.FULL_HONEY_LEVEL;
    };
    private static final Predicate<BlockState> POWERED_FILTER = (blockState) -> blockState.get(Properties.POWERED) == true;
    private static final Predicate<BlockState> TRIGGERED_FILTER = (blockState) -> blockState.get(Properties.TRIGGERED) == true;
    private static final Predicate<BlockState> OPEN_FENCE_GATE_FILTER = POWERED_FILTER.and(state -> state.get(FenceGateBlock.OPEN) == true);
    private static final Predicate<BlockState> FENCE_GATE_FILTER = POWERED_FILTER.and(state -> state.get(FenceGateBlock.OPEN) == false);
    private static final Predicate<BlockState> SCULK_FILTER = (blockState) -> blockState.get(SculkSensorBlock.POWER) != 0 && blockState.get(SculkSensorBlock.SCULK_SENSOR_PHASE) != SculkSensorPhase.ACTIVE; // Active sculk sensors have particles and emissive lighting
    private static final Predicate<BlockState> SNOWY_GRASS_FILTER = (blockState) -> blockState.get(GrassBlock.SNOWY);
    private static final Predicate<BlockState> DOUBLE_SLAB_FILTER = (blockState) -> blockState.get(SlabBlock.TYPE) == SlabType.DOUBLE;
    private static final Predicate<BlockState> WATERLOGGED_SLAB_FILTER = (blockState) -> blockState.get(SlabBlock.TYPE) == SlabType.DOUBLE && blockState.get(SlabBlock.WATERLOGGED);
    private static final Predicate<BlockState> SLAB_FILTER = (blockState) -> blockState.get(SlabBlock.TYPE) != SlabType.DOUBLE;
    private static final Predicate<BlockState> CLIMBABLE_FILTER = (blockState) -> {

        if (blockState.getBlock() == Blocks.CAVE_VINES) {

            // Don't use any state with age 0
            if (blockState.get(Properties.AGE_25) == 0) {
                return false;
            }

            // Don't use states with berries (they cause block updates on right click)
            if (blockState.get(CaveVines.BERRIES) == true) {
                return false;
            }
        }

        return DEFAULT_FILTER.test(blockState);
    };

    //ON FIRST REGISTERS
    private static final BiConsumer<Block,PolyRegistry> DEFAULT_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, new SimpleReplacementPoly(block.getDefaultState()));
    private static final BiConsumer<Block,PolyRegistry> WALL_ON_FIRST_REGISTER = (block, polyRegistry) -> {
        polyRegistry.registerBlockPoly(block, input -> {
            if (WALL_FILTER.test(input)) {
                return input.getBlock().getDefaultState();
            }
            return input;
        });
    };
    private static final BiConsumer<Block,PolyRegistry> TRIPWIRE_ON_FIRST_REGISTER = (block, polyRegistry) -> {
        polyRegistry.registerBlockPoly(block, (input) ->
                input.with(Properties.POWERED, false).with(Properties.DISARMED,false)
        );
    };
    private static final BiConsumer<Block,PolyRegistry> CLIMBABLE_ON_FIRST_REGISTER = (block, polyRegistry) -> {

        if (block == Blocks.CAVE_VINES) {
            polyRegistry.registerBlockPoly(block, (input) ->
                    // Force the age to be zero, but let the "berries" be whatever it is
                    input.with(Properties.AGE_25, 0)
            );
        } else {
            polyRegistry.registerBlockPoly(block, new SimpleReplacementPoly(block.getDefaultState()));
        }
    };
    private static final BiConsumer<Block,PolyRegistry> SMALL_DRIPLEAF_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, input -> {
        if (input.get(TallPlantBlock.HALF) == DoubleBlockHalf.LOWER) {
            return input.with(SmallDripleafBlock.FACING, Direction.NORTH);
        }
        return input;
    });
    private static final BiConsumer<Block,PolyRegistry> CAVE_VINES_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, input -> input.with(AbstractPlantStemBlock.AGE, 0));
    private static final BiConsumer<Block,PolyRegistry> SCULK_SENSOR_ON_FIRST_REGISTER = (block, polyRegistry) -> {
        polyRegistry.registerBlockPoly(block, (input) ->
                input.with(SculkSensorBlock.POWER, 0)
        );
    };
    private static final BiConsumer<Block,PolyRegistry> FARMLAND_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, new ConditionalSimpleBlockPoly(Blocks.FARMLAND.getDefaultState(), FARMLAND_FILTER));
    private static final BiConsumer<Block,PolyRegistry> BEEHIVE_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, (input) -> {
        if (BEEHIVE_FILTER.test(input)) {
            return input.with(BeehiveBlock.HONEY_LEVEL, 0);
        }
        return input;
    });
    private static final BiConsumer<Block,PolyRegistry> POWERED_BLOCK_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, (input) -> input.with(Properties.POWERED, false));
    private static final BiConsumer<Block,PolyRegistry> TRIGGERED_BLOCK_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, (input) -> input.with(Properties.TRIGGERED, false));
    private static final BiConsumer<Block,PolyRegistry> SNOWY_GRASS_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, input -> {
        if (input.get(GrassBlock.SNOWY)) {
            return Blocks.GRASS_BLOCK.getDefaultState().with(GrassBlock.SNOWY, true);
        }
        return input;
    });
    private static final BiConsumer<Block,PolyRegistry> WAXED_COPPER_ON_FIRST_REGISTER = (block, polyRegistry) -> {
        var unwaxedBlock = HoneycombItem.WAXED_TO_UNWAXED_BLOCKS.get().get(block);
        polyRegistry.registerBlockPoly(block, new PropertyRetainingReplacementPoly(unwaxedBlock));
    };
    private static final BiConsumer<Block,PolyRegistry> INFESTED_BLOCK_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, input -> ((InfestedBlock)block).toRegularState(input));
    private static final BiConsumer<Block,PolyRegistry> DOUBLESLAB_ON_FIRST_REGISTER = (block, polyRegistry) -> {
        if (!ListOfSlabs.SLAB2FULL.containsKey(block)) throw new IllegalArgumentException(block.getTranslationKey() + " isn't a registered slab");
        // We can replace (for example) oak double slabs with regular oak planks. Because double slabs can technically be waterlogged this frees up two states per slab
        var regularFullBlock = ListOfSlabs.SLAB2FULL.get(block).getDefaultState();
        if (ArrayUtils.contains(WAXED_COPPER_SLAB_BLOCKS, block) || block == Blocks.PETRIFIED_OAK_SLAB) {
            var slabReplacement = block == Blocks.PETRIFIED_OAK_SLAB ?
                    Blocks.PETRIFIED_OAK_SLAB.getDefaultState() :
                    HoneycombItem.WAXED_TO_UNWAXED_BLOCKS.get().get(block).getDefaultState();
            polyRegistry.registerBlockPoly(block, (input) -> {
                if (input.get(SlabBlock.TYPE) == SlabType.DOUBLE) {
                    return regularFullBlock;
                } else {
                    return slabReplacement
                            .with(SlabBlock.TYPE, input.get(SlabBlock.TYPE))
                            .with(SlabBlock.WATERLOGGED, input.get(SlabBlock.WATERLOGGED));
                }
            });
        } else {
            polyRegistry.registerBlockPoly(block, (input) -> {
                if (input.get(SlabBlock.TYPE) == SlabType.DOUBLE) {
                    return regularFullBlock;
                }
                return input;
            });
        }
    };
    private static final BiConsumer<Block,PolyRegistry> WATERLOGGED_SLAB_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block, (input) -> {
        if (WATERLOGGED_SLAB_FILTER.test(input)) {
            return input.with(SlabBlock.WATERLOGGED, false);
        }
        return input;
    });

    // SUB PROFILES
    public static final BlockStateProfile SAPLING_SUB_PROFILE = getProfileWithDefaultFilter("sapling", SAPLING_BLOCKS);
    public static final BlockStateProfile SUGARCANE_SUB_PROFILE = getProfileWithDefaultFilter("sugarcane", Blocks.SUGAR_CANE);
    public static final BlockStateProfile TRIPWIRE_SUB_PROFILE = new BlockStateProfile("tripwire", Blocks.TRIPWIRE, TRIPWIRE_FILTER, TRIPWIRE_ON_FIRST_REGISTER);
    public static final BlockStateProfile SMALL_DRIPLEAF_SUB_PROFILE = new BlockStateProfile("drip leaf", Blocks.SMALL_DRIPLEAF, SMALL_DRIPLEAF_FILTER, SMALL_DRIPLEAF_ON_FIRST_REGISTER);
    public static final BlockStateProfile CAVE_VINES_SUB_PROFILE = new BlockStateProfile("cave vines", Blocks.CAVE_VINES, CAVE_VINES_FILTER, CAVE_VINES_ON_FIRST_REGISTER);
    public static final BlockStateProfile KELP_SUB_PROFILE = getProfileWithDefaultFilter("kelp", Blocks.KELP);
    public static final BlockStateProfile NOTE_BLOCK_SUB_PROFILE = getProfileWithDefaultFilter("note block", Blocks.NOTE_BLOCK);
    public static final BlockStateProfile TARGET_BLOCK_SUB_PROFILE = getProfileWithDefaultFilter("target block", Blocks.TARGET);
    public static final BlockStateProfile DISPENSER_SUB_PROFILE = new BlockStateProfile("dispenser and dropper", DISPENSER_BLOCKS, TRIGGERED_FILTER, TRIGGERED_BLOCK_ON_FIRST_REGISTER);
    public static final BlockStateProfile TNT_SUB_PROFILE = getProfileWithDefaultFilter("tnt", Blocks.TNT);
    public static final BlockStateProfile JUKEBOX_SUB_PROFILE = getProfileWithDefaultFilter("jukebox", Blocks.JUKEBOX);
    public static final BlockStateProfile BEEHIVE_SUB_PROFILE = new BlockStateProfile("beehive", BEEHIVE_BLOCKS, BEEHIVE_FILTER, BEEHIVE_ON_FIRST_REGISTER);
    public static final BlockStateProfile SNOWY_GRASS_SUB_PROFILE = new BlockStateProfile("snowy grass", SNOWY_GRASS_BLOCKS, SNOWY_GRASS_FILTER, SNOWY_GRASS_ON_FIRST_REGISTER);
    public static final BlockStateProfile DOUBLE_SLAB_SUB_PROFILE = new BlockStateProfile("slabs", ListOfSlabs.SLAB2FULL.keySet().toArray(Block[]::new), DOUBLE_SLAB_FILTER, DOUBLESLAB_ON_FIRST_REGISTER);
    public static final BlockStateProfile WATERLOGGED_SLAB_SUB_PROFILE = new BlockStateProfile("waterlog only slabs", WATERLOGGED_SLABS, WATERLOGGED_SLAB_FILTER, WATERLOGGED_SLAB_ON_FIRST_REGISTER);
    public static final BlockStateProfile WAXED_COPPER_FULLBLOCK_SUB_PROFILE = new BlockStateProfile("waxed copper fullblocks", WAXED_COPPER_FULL_BLOCKS, ALWAYS_TRUE_FILTER, WAXED_COPPER_ON_FIRST_REGISTER);
    public static final BlockStateProfile INFESTED_STONE_SUB_PROFILE = new BlockStateProfile("infested stone", INFESTED_BLOCKS, ALWAYS_TRUE_FILTER, INFESTED_BLOCK_ON_FIRST_REGISTER);
    public static final BlockStateProfile PETRIFIED_OAK_SLAB_SUB_PROFILE = new BlockStateProfile("petrified oak slab", Blocks.PETRIFIED_OAK_SLAB, SLAB_FILTER, DOUBLESLAB_ON_FIRST_REGISTER); // This profile only handles top/bottom slabs. The double slabs are exposed via `DOUBLE_SLAB_SUB_PROFILE`
    public static final BlockStateProfile WAXED_COPPER_SLAB_SUB_PROFILE = new BlockStateProfile("waxed copper slab", WAXED_COPPER_SLAB_BLOCKS, SLAB_FILTER, DOUBLESLAB_ON_FIRST_REGISTER); // This profile only handles top/bottom slabs. The double slabs are exposed via `DOUBLE_SLAB_SUB_PROFILE`
    public static final BlockStateProfile OPEN_FENCE_GATE_PROFILE = new BlockStateProfile("open fence gate", FENCE_GATE_BLOCKS, OPEN_FENCE_GATE_FILTER, POWERED_BLOCK_ON_FIRST_REGISTER);
    public static final BlockStateProfile FENCE_GATE_PROFILE = new BlockStateProfile("fence gate", FENCE_GATE_BLOCKS, FENCE_GATE_FILTER, POWERED_BLOCK_ON_FIRST_REGISTER);

    //PROFILES
    public static final BlockStateProfile FULL_BLOCK_PROFILE = combine("full blocks", INFESTED_STONE_SUB_PROFILE, /*TNT_SUB_PROFILE,*/ SNOWY_GRASS_SUB_PROFILE, NOTE_BLOCK_SUB_PROFILE, DISPENSER_SUB_PROFILE, BEEHIVE_SUB_PROFILE, WAXED_COPPER_FULLBLOCK_SUB_PROFILE, JUKEBOX_SUB_PROFILE, DOUBLE_SLAB_SUB_PROFILE, TARGET_BLOCK_SUB_PROFILE, WATERLOGGED_SLAB_SUB_PROFILE);
    public static final BlockStateProfile CLIMBABLE_PROFILE = new BlockStateProfile("climbable blocks", CLIMBABLE_BLOCKS, CLIMBABLE_FILTER, CLIMBABLE_ON_FIRST_REGISTER);
    public static final BlockStateProfile LEAVES_PROFILE = getProfileWithDefaultFilter("leaves", LEAVES_BLOCKS);
    public static final BlockStateProfile NO_COLLISION_WALL_PROFILE = new BlockStateProfile("empty walls", WALL_BLOCKS, WALL_FILTER, WALL_ON_FIRST_REGISTER);
    public static final BlockStateProfile NO_COLLISION_PROFILE = combine("blocks without collisions", KELP_SUB_PROFILE, SAPLING_SUB_PROFILE, SUGARCANE_SUB_PROFILE, /*CAVE_VINES_SUB_PROFILE,*/ TRIPWIRE_SUB_PROFILE, SMALL_DRIPLEAF_SUB_PROFILE, OPEN_FENCE_GATE_PROFILE);
    public static final BlockStateProfile FARMLAND_PROFILE = new BlockStateProfile("farmland", Blocks.FARMLAND, FARMLAND_FILTER, FARMLAND_ON_FIRST_REGISTER);
    public static final BlockStateProfile CACTUS_PROFILE = getProfileWithDefaultFilter("cactus", Blocks.CACTUS);
    public static final BlockStateProfile DOOR_PROFILE = new BlockStateProfile("door", DOOR_BLOCKS, POWERED_FILTER, POWERED_BLOCK_ON_FIRST_REGISTER);
    public static final BlockStateProfile TRAPDOOR_PROFILE = new BlockStateProfile("trapdoor", TRAPDOOR_BLOCKS, POWERED_FILTER, POWERED_BLOCK_ON_FIRST_REGISTER);
    public static final BlockStateProfile METAL_DOOR_PROFILE = new BlockStateProfile("metal door", Blocks.IRON_DOOR, POWERED_FILTER, POWERED_BLOCK_ON_FIRST_REGISTER);
    public static final BlockStateProfile METAL_TRAPDOOR_PROFILE = new BlockStateProfile("metal trapdoor", Blocks.IRON_TRAPDOOR, POWERED_FILTER, POWERED_BLOCK_ON_FIRST_REGISTER);
    public static final BlockStateProfile WAXED_COPPER_STAIR_PROFILE = new BlockStateProfile("waxed copper stair", WAXED_COPPER_STAIR_BLOCKS, ALWAYS_TRUE_FILTER, WAXED_COPPER_ON_FIRST_REGISTER);
    public static final BlockStateProfile SLAB_PROFILE = combine("slab", PETRIFIED_OAK_SLAB_SUB_PROFILE, WAXED_COPPER_SLAB_SUB_PROFILE);
    public static final BlockStateProfile SCULK_SENSOR_PROFILE = new BlockStateProfile("sculk sensor", Blocks.SCULK_SENSOR, SCULK_FILTER, SCULK_SENSOR_ON_FIRST_REGISTER);
    //OTHER CODE
    public static BlockStateProfile getProfileWithDefaultFilter(String name, Block[] blocks) {
        return new BlockStateProfile(name, blocks, DEFAULT_FILTER, DEFAULT_ON_FIRST_REGISTER);
    }

    public static BlockStateProfile getProfileWithDefaultFilter(String name, Block block) {
        return new BlockStateProfile(name, block, DEFAULT_FILTER, DEFAULT_ON_FIRST_REGISTER);
    }

    public static BlockStateProfile combine(String name, BlockStateProfile... parents) {
        return new BlockStateProfile(
                name,
                // Combine blocks into one array
                Arrays.stream(parents).flatMap((v) -> Arrays.stream(v.blocks)).toArray(Block[]::new),
                // Use the correct filter for the correct block (it is assumed that no two profiles declare the same block)
                (state) -> {
                    for (var parent : parents) {
                        if (ArrayUtils.contains(parent.blocks, state.getBlock())) {
                            return parent.filter.test(state);
                        }
                    }
                    return true;
                },
                (block, registry) -> {
                    for (var parent : parents) {
                        if (ArrayUtils.contains(parent.blocks, block)) {
                            parent.onFirstRegister.accept(block, registry);
                            break;
                        }
                    }
                }
        );
    }

    private static boolean isStringUseable(BlockState state) {
        return  state.get(Properties.POWERED) == true ||
                state.get(TripwireBlock.DISARMED) == true;
    }

    public BlockStateProfile and(Predicate<BlockState> filter) {
        return new BlockStateProfile(this.name, this.blocks, this.filter.and(filter), this.onFirstRegister);
    }
}
