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
package io.github.theepicblock.polymc.impl.mixin;

import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerContextContainer {
    ServerPlayerEntity getPolyMcProvidedPlayer();

    void setPolyMcProvidedPlayer(ServerPlayerEntity v);

    static ServerPlayerEntity retrieve(Object o) {
        return ((PlayerContextContainer)o).getPolyMcProvidedPlayer();
    }
}
