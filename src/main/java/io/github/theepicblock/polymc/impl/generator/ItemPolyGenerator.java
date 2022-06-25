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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Streams;
import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.item.CustomModelDataManager;
import io.github.theepicblock.polymc.api.item.ItemPoly;
import io.github.theepicblock.polymc.impl.poly.block.ListOfSlabs;
import io.github.theepicblock.polymc.impl.poly.item.ArmorColorManager;
import io.github.theepicblock.polymc.impl.poly.item.CustomModelDataPoly;
import io.github.theepicblock.polymc.impl.poly.item.DamageableItemPoly;
import io.github.theepicblock.polymc.impl.poly.item.FancyPantsItemPoly;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.*;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to automatically generate {@link ItemPoly}s for {@link Item}s
 */
public class ItemPolyGenerator {
    /**
     * Generates the most suitable {@link ItemPoly} for a given {@link Item}
     */
    public static ItemPoly generatePoly(Item item, PolyRegistry builder) {
        var cmdManager = builder.getSharedValues(CustomModelDataManager.KEY);

        if (item instanceof ArmorItem armorItem) {
            if (builder.getSharedValues(ArmorColorManager.KEY).isEmpty()) {
                FancyPantsItemPoly.onFirstRegister(builder);
            }
            return new FancyPantsItemPoly(builder, armorItem);
        }
        if (item instanceof ShieldItem) {
            return new DamageableItemPoly(cmdManager, item, Items.SHIELD);
        }
        if (item instanceof CompassItem) {
            return new CustomModelDataPoly(cmdManager, item, Items.COMPASS);
        }
        if (item instanceof CrossbowItem) {
            return new DamageableItemPoly(cmdManager, item, Items.CROSSBOW);
        }
        if (item instanceof RangedWeaponItem && item.getMaxUseTime(new ItemStack(item)) != 0) {
            return new DamageableItemPoly(cmdManager, item, Items.BOW);
        }
        if (item.isDamageable()) {
            if (item instanceof DyeableItem) {
                return new DamageableItemPoly(cmdManager, item, Items.LEATHER_HELMET);
            }
            return new DamageableItemPoly(cmdManager, item);
        }
        if (item.isFood()) {
            return new CustomModelDataPoly(cmdManager, item, CustomModelDataManager.FOOD_ITEMS);
        }
        if (item instanceof DyeableItem) {
            return new CustomModelDataPoly(cmdManager, item, Items.LEATHER_HORSE_ARMOR);
        }
        if (AbstractFurnaceBlockEntity.canUseAsFuel(new ItemStack(item))) {
            return new CustomModelDataPoly(cmdManager, item, CustomModelDataManager.FUEL_ITEMS);
        }
        if (item instanceof BlockItem blockItem) {
            return new CustomModelDataPoly(cmdManager, item, getBestVanillaItemsForBlockItem(blockItem));
        }
        return new CustomModelDataPoly(cmdManager, item);
    }

    /**
     * Attempts to create the best possible {@link ItemPoly} for a {@link BlockItem}.
     * Amongst other factors, this decision might be influenced by the placement logic and the placement sound of the source {@link BlockItem} and the {@link net.minecraft.block.Block} that's attached to it.
     */
    private static Item[] getBestVanillaItemsForBlockItem(BlockItem item) {
        var block = item.getBlock();
        if (block instanceof SlabBlock) {
            return BlockItemGroups.SLABS.getList(block);
        }

        var fakeWorld = new BlockPolyGenerator.FakedWorld(block.getDefaultState());

        //Get the state's collision shape.
        VoxelShape collisionShape;
        try {
            collisionShape = block.getDefaultState().getCollisionShape(fakeWorld, BlockPos.ORIGIN);
        } catch (Exception e) {
            PolyMc.LOGGER.warn("Failed to get collision shape for " + block.getDefaultState().toString());
            e.printStackTrace();
            collisionShape = VoxelShapes.UNBOUNDED;
        }
        if (Block.isShapeFullCube(collisionShape)) {
            return BlockItemGroups.FULL_BLOCKS.getList(block);
        }

        return CustomModelDataManager.BLOCK_ITEMS;
    }

    /**
     * Generates the most suitable {@link ItemPoly} and directly adds it to the {@link PolyRegistry}
     * @see #generatePoly(Item, PolyRegistry)
     */
    public static void addItemToBuilder(Item item, PolyRegistry builder) {
        try {
            builder.registerItemPoly(item, generatePoly(item, builder));
        } catch (Exception e) {
            PolyMc.LOGGER.error("Failed to generate a poly for item " + item.getTranslationKey());
            e.printStackTrace();
            PolyMc.LOGGER.error("Attempting to recover by using a default poly. Please report this");
            builder.registerItemPoly(item, (input, player, location) -> new ItemStack(Items.BARRIER));
        }
    }

    private enum BlockItemGroups {
        SLABS(Blocks.SMOOTH_STONE_SLAB, ListOfSlabs.SLAB2FULL.keySet()),
        FULL_BLOCKS(Blocks.BARRIER,
                Blocks.OAK_WOOD,
                Blocks.DARK_OAK_WOOD,
                Blocks.GRAVEL,
                Blocks.DIRT,
                Blocks.PODZOL,
                Blocks.MANGROVE_LEAVES,
                Blocks.BARRIER,
                Blocks.IRON_BLOCK,
                Blocks.GLASS,
                Blocks.WHITE_WOOL,
                Blocks.SNOW,
                Blocks.SLIME_BLOCK,
                Blocks.HONEY_BLOCK, // Technically not a full block
                Blocks.TUBE_CORAL_BLOCK,
                Blocks.MUSHROOM_STEM,
                Blocks.NETHERRACK,
                Blocks.NETHER_WART_BLOCK,
                Blocks.WARPED_HYPHAE,
                Blocks.SHROOMLIGHT,
                Blocks.SOUL_SAND,
                Blocks.SOUL_SOIL,
                Blocks.BASALT,
                Blocks.NETHER_BRICKS,
                Blocks.NETHER_GOLD_ORE,
                Blocks.NETHERITE_BLOCK,
                Blocks.ANCIENT_DEBRIS,
                Blocks.LODESTONE,
                Blocks.GILDED_BLACKSTONE,
                Blocks.AMETHYST_BLOCK,
                Blocks.COPPER_BLOCK,
                Blocks.DEEPSLATE,
                Blocks.SCULK,
                Blocks.BONE_BLOCK
        );

        private final Map<BlockSoundGroup, Item[]> blockItemsPerCategory;
        private final Item[] theDefault;

        <T extends Block> BlockItemGroups(Block theDefault, Iterable<T> blocks) {
            this(theDefault, Streams.stream(blocks).toArray(Block[]::new));
        }

        BlockItemGroups(Block theDefault, Block... blocks) {
            this.theDefault = new Item[]{theDefault.asItem()};
            blockItemsPerCategory = new HashMap<>();
            Arrays.stream(blocks).collect(
                            HashMultimap::<BlockSoundGroup, Item>create,
                            (multiMap, block) -> multiMap.put(block.getSoundGroup(block.getDefaultState()), block.asItem()),
                            Multimap::putAll
                    ).asMap().forEach((soundGroup, blockCollection) -> blockItemsPerCategory.put(soundGroup, blockCollection.toArray(Item[]::new)));
        }

        public Item[] getList(Block block) {
            return this.blockItemsPerCategory.getOrDefault(block.getSoundGroup(block.getDefaultState()), theDefault);
        }
    }
}
