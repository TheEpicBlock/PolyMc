/*
 * PolyMc
 * Copyright (C) 2020-2021 TheEpicBlock_TEB
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

import io.github.theepicblock.polymc.mixins.block.implementations.ChunkDataPlayerProvider;
import net.minecraft.server.network.ServerPlayerEntity;

public class ChunkPacketStaticHack {
    /**
     * The chunk palette is serialized on the construction of {@link net.minecraft.network.packet.s2c.play.ChunkData}, which is made on the construction of the {@link net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket}.
     * PolyMc wants to have a reference to the player whilst we serialize the palette so we can account for per player polymaps.
     * For most packets we just add a reference when the packet object in {@link io.github.theepicblock.polymc.mixins.context.NetworkHandlerContextProvider} and use that in a mixin to the packet's toPacket method.
     * But as things are being done in constructors here, before the packet reaches that method, that won't work. Therefore, we instead place a reference to the player right here, inside the method that the packet is being constructed in.
     *
     * @see ChunkDataPlayerProvider
     */
    public static ThreadLocal<ServerPlayerEntity> player = new ThreadLocal<>();
}
