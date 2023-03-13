package io.github.theepicblock.polymc.impl.poly.block;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;

import java.util.HashMap;
import java.util.Map;

public class ListOfSlabs {
    /**
     * This is a map of all slabs to their base block
     * It only contains slabs that look alike to their base block. So smooth stone slabs are excluded.
     * Please keep this up to date.
     */
    public static final Map<SlabBlock, Block> SLAB2FULL = new HashMap<>();

    static {
        // Woods
        SLAB2FULL.put((SlabBlock)Blocks.ACACIA_SLAB, Blocks.ACACIA_PLANKS);
        SLAB2FULL.put((SlabBlock)Blocks.BIRCH_SLAB, Blocks.BIRCH_PLANKS);
        SLAB2FULL.put((SlabBlock)Blocks.DARK_OAK_SLAB, Blocks.DARK_OAK_PLANKS);
        SLAB2FULL.put((SlabBlock)Blocks.JUNGLE_SLAB, Blocks.JUNGLE_PLANKS);
        SLAB2FULL.put((SlabBlock)Blocks.SPRUCE_SLAB, Blocks.SPRUCE_PLANKS);
        SLAB2FULL.put((SlabBlock)Blocks.OAK_SLAB, Blocks.OAK_PLANKS);
        SLAB2FULL.put((SlabBlock)Blocks.PETRIFIED_OAK_SLAB, Blocks.OAK_PLANKS);
        SLAB2FULL.put((SlabBlock)Blocks.WARPED_SLAB, Blocks.WARPED_PLANKS);
        SLAB2FULL.put((SlabBlock)Blocks.CRIMSON_SLAB, Blocks.CRIMSON_PLANKS);
        SLAB2FULL.put((SlabBlock)Blocks.MANGROVE_SLAB, Blocks.MANGROVE_PLANKS);
        // Stones
        SLAB2FULL.put((SlabBlock)Blocks.STONE_SLAB, Blocks.STONE);
        SLAB2FULL.put((SlabBlock)Blocks.BLACKSTONE_SLAB, Blocks.BLACKSTONE);
        SLAB2FULL.put((SlabBlock)Blocks.COBBLESTONE_SLAB, Blocks.COBBLESTONE);
        SLAB2FULL.put((SlabBlock)Blocks.MOSSY_COBBLESTONE_SLAB, Blocks.MOSSY_COBBLESTONE);
        SLAB2FULL.put((SlabBlock)Blocks.COBBLED_DEEPSLATE_SLAB, Blocks.COBBLED_DEEPSLATE);
        SLAB2FULL.put((SlabBlock)Blocks.ANDESITE_SLAB, Blocks.ANDESITE);
        SLAB2FULL.put((SlabBlock)Blocks.DIORITE_SLAB, Blocks.DIORITE);
        SLAB2FULL.put((SlabBlock)Blocks.GRANITE_SLAB, Blocks.GRANITE);
        // Polished stones
        SLAB2FULL.put((SlabBlock)Blocks.POLISHED_ANDESITE_SLAB, Blocks.POLISHED_ANDESITE);
        SLAB2FULL.put((SlabBlock)Blocks.POLISHED_DEEPSLATE_SLAB, Blocks.POLISHED_DEEPSLATE);
        SLAB2FULL.put((SlabBlock)Blocks.POLISHED_DIORITE_SLAB, Blocks.POLISHED_DIORITE);
        SLAB2FULL.put((SlabBlock)Blocks.POLISHED_GRANITE_SLAB, Blocks.POLISHED_GRANITE);
        SLAB2FULL.put((SlabBlock)Blocks.POLISHED_BLACKSTONE_SLAB, Blocks.POLISHED_BLACKSTONE);
        // Bricks
        SLAB2FULL.put((SlabBlock)Blocks.BRICK_SLAB, Blocks.BRICKS);
        SLAB2FULL.put((SlabBlock)Blocks.END_STONE_BRICK_SLAB, Blocks.END_STONE_BRICKS);
        SLAB2FULL.put((SlabBlock)Blocks.STONE_BRICK_SLAB, Blocks.STONE_BRICKS);
        SLAB2FULL.put((SlabBlock)Blocks.MOSSY_STONE_BRICK_SLAB, Blocks.MOSSY_STONE_BRICKS);
        SLAB2FULL.put((SlabBlock)Blocks.NETHER_BRICK_SLAB, Blocks.NETHER_BRICKS);
        SLAB2FULL.put((SlabBlock)Blocks.RED_NETHER_BRICK_SLAB, Blocks.RED_NETHER_BRICKS);
        SLAB2FULL.put((SlabBlock)Blocks.DEEPSLATE_BRICK_SLAB, Blocks.DEEPSLATE_BRICKS);
        SLAB2FULL.put((SlabBlock)Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, Blocks.POLISHED_BLACKSTONE_BRICKS);
        SLAB2FULL.put((SlabBlock)Blocks.MUD_BRICK_SLAB, Blocks.MUD_BRICKS);
        // Sandstones
        SLAB2FULL.put((SlabBlock)Blocks.SANDSTONE_SLAB, Blocks.SANDSTONE);
        SLAB2FULL.put((SlabBlock)Blocks.SMOOTH_SANDSTONE_SLAB, Blocks.SMOOTH_SANDSTONE);
        SLAB2FULL.put((SlabBlock)Blocks.CUT_SANDSTONE_SLAB, Blocks.CUT_SANDSTONE);
        SLAB2FULL.put((SlabBlock)Blocks.RED_SANDSTONE_SLAB, Blocks.RED_SANDSTONE);
        SLAB2FULL.put((SlabBlock)Blocks.SMOOTH_RED_SANDSTONE_SLAB, Blocks.SMOOTH_RED_SANDSTONE);
        SLAB2FULL.put((SlabBlock)Blocks.CUT_RED_SANDSTONE_SLAB, Blocks.CUT_RED_SANDSTONE);
        // Misc
        SLAB2FULL.put((SlabBlock)Blocks.QUARTZ_SLAB, Blocks.QUARTZ_BLOCK);
        SLAB2FULL.put((SlabBlock)Blocks.SMOOTH_QUARTZ_SLAB, Blocks.SMOOTH_QUARTZ);
        SLAB2FULL.put((SlabBlock)Blocks.PRISMARINE_SLAB, Blocks.PRISMARINE);
        SLAB2FULL.put((SlabBlock)Blocks.PRISMARINE_BRICK_SLAB, Blocks.PRISMARINE_BRICKS);
        SLAB2FULL.put((SlabBlock)Blocks.DARK_PRISMARINE_SLAB, Blocks.DARK_PRISMARINE);
        SLAB2FULL.put((SlabBlock)Blocks.DEEPSLATE_TILE_SLAB, Blocks.DEEPSLATE_TILES);
        SLAB2FULL.put((SlabBlock)Blocks.PURPUR_SLAB, Blocks.PURPUR_BLOCK);
        // Copper
        SLAB2FULL.put((SlabBlock)Blocks.CUT_COPPER_SLAB, Blocks.CUT_COPPER);
        SLAB2FULL.put((SlabBlock)Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.EXPOSED_CUT_COPPER);
        SLAB2FULL.put((SlabBlock)Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.WEATHERED_CUT_COPPER);
        SLAB2FULL.put((SlabBlock)Blocks.OXIDIZED_CUT_COPPER_SLAB, Blocks.OXIDIZED_CUT_COPPER);
        SLAB2FULL.put((SlabBlock)Blocks.WAXED_CUT_COPPER_SLAB, Blocks.CUT_COPPER); // We're replacing them with the regular ones instead of the waxed ones because the waxed ones are already used for other polys
        SLAB2FULL.put((SlabBlock)Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB, Blocks.EXPOSED_CUT_COPPER);
        SLAB2FULL.put((SlabBlock)Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB, Blocks.WEATHERED_CUT_COPPER);
        SLAB2FULL.put((SlabBlock)Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB, Blocks.OXIDIZED_CUT_COPPER);
    }
}
