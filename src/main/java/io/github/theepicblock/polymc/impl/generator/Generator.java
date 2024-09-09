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

import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.block.BlockStateProfile;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.poly.item.Tooltip2LoreTransformer;
import io.github.theepicblock.polymc.impl.poly.item.InvalidComponentFixGlobalPoly;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.Comparator;
import java.util.function.BiConsumer;

public class Generator {
    /**
     * Automatically generates all polys that are missing in the specified builder
     * @param builder builder to add polys to
     */
    public static void generateMissing(PolyRegistry builder) {
        generateMissingPolys(builder, Registries.BLOCK, BlockPolyGenerator::addBlockToBuilder, builder::hasBlockPoly);
        generateMissingPolys(builder, Registries.ITEM, ItemPolyGenerator::addItemToBuilder, builder::hasItemPoly);
        generateMissingPolys(builder, Registries.SCREEN_HANDLER, GuiGenerator::addGuiToBuilder, builder::hasGuiPoly);
        generateMissingPolys(builder, Registries.ENTITY_TYPE, EntityPolyGenerator::addEntityToBuilder, builder::hasEntityPoly);

        // Todo: Replace with more generic logic!
        if (FabricLoader.getInstance().isModLoaded("terraform-wood-api-v1")) {
            for (var block : BlockStateProfile.LEAVES_PROFILE.blocks) {
                if (!builder.hasBlockPoly(block)) {
                    BlockStateProfile.LEAVES_PROFILE.onFirstRegister.accept(block, builder);
                }
            }
        }
    }

    private static <T> void generateMissingPolys(PolyRegistry builder, Registry<T> registry, BiConsumer<T, PolyRegistry> generator, BooleanFunction<T> contains) {
        registry.getEntrySet()
                .stream()
                .filter(entry -> !Util.isVanilla(entry.getKey().getValue()))
                .filter(entry -> !contains.accept(entry.getValue()))
                .sorted(Comparator.comparing(a -> a.getKey().getValue()))  // Compares the identifier
                .forEach(entry -> generator.accept(entry.getValue(), builder));
    }

    /**
     * Registers global {@link io.github.theepicblock.polymc.api.item.ItemTransformer}s that are included with PolyMc by default for vanilla compatibility
     */
    public static void addDefaultGlobalItemPolys(PolyRegistry registry) {
        registry.registerGlobalItemPoly(new Tooltip2LoreTransformer());
        registry.registerGlobalItemPoly(new InvalidComponentFixGlobalPoly());
    }

    @FunctionalInterface
    private interface BooleanFunction<T> {
        boolean accept(T t);
    }
}
