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
package io.github.theepicblock.polymc.mixins;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientConnection.class)
public class PolyMapProviderImpl implements PolyMapProvider {
    @Unique private PolyMap polyMap;

    @Override
    public PolyMap getPolyMap() {
        if (FabricLoader.getInstance().isDevelopmentEnvironment() && polyMap == null) {
            PolyMc.LOGGER.error("Warning! Uninitialized client connection is being used! This shouldn't happen");
            Thread.dumpStack();
        }
        return polyMap;
    }

    @Override
    public void setPolyMap(PolyMap map) {
        polyMap = map;
    }
}
