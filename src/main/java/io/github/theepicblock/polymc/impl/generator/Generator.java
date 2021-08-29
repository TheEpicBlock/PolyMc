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
import io.github.theepicblock.polymc.impl.poly.item.Enchantment2LoreTransformer;

public class Generator {
    /**
     * Automatically generates all polys that are missing in the specified builder
     * @param builder builder to add polys to
     */
    public static void generateMissing(PolyRegistry builder) {
        ItemPolyGenerator.generateMissing(builder);
        BlockPolyGenerator.generateMissing(builder);
        GuiGenerator.generateMissing(builder);
        EntityPolyGenerator.generateMissing(builder);
    }

    /**
     * Registers global {@link io.github.theepicblock.polymc.api.item.ItemTransformer}s that are included with PolyMc by default for vanilla compatibility
     */
    public static void addDefaultGlobalItemPolys(PolyRegistry registry) {
        registry.registerGlobalItemPoly(new Enchantment2LoreTransformer());
    }
}
