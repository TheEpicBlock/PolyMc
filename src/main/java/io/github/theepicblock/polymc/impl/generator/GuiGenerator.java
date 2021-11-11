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
import io.github.theepicblock.polymc.api.gui.GuiPoly;
import io.github.theepicblock.polymc.impl.poly.gui.NaiveStackListingChestPoly;
import net.minecraft.screen.ScreenHandlerType;

/**
 * Class to automatically generate {@link GuiPoly}s for {@link ScreenHandlerType}s
 */
public class GuiGenerator {
    /**
     * Generates the most suitable {@link GuiPoly} for a given {@link ScreenHandlerType}
     */
    public static GuiPoly generatePoly(ScreenHandlerType<?> gui, PolyRegistry builder) {
        return new NaiveStackListingChestPoly();
    }

    /**
     * Generates the most suitable {@link GuiPoly} and directly adds it to the {@link PolyRegistry}
     * @see #generatePoly(ScreenHandlerType, PolyRegistry)
     */
    public static void addGuiToBuilder(ScreenHandlerType<?> gui, PolyRegistry builder) {
        builder.registerGuiPoly(gui, generatePoly(gui, builder));
    }
}
