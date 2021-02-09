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

import net.minecraft.server.network.ServerPlayerEntity;

public class ChunkPacketStaticHack {
	/**
	 * {@link net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket} calculates a bunch of stuff in its constructor.
	 * We need to pass in the context of the player to a mixin in this constructor.
	 * That's where this field comes into play.
	 * It is set by {@link io.github.theepicblock.polymc.mixins.context.block.TACSMixin} before the invocation of the constructor and cleared afterwards
	 */
	public static ServerPlayerEntity player;
}
