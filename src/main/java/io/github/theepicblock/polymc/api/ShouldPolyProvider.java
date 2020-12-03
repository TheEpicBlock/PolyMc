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
package io.github.theepicblock.polymc.api;

import net.minecraft.server.network.ServerPlayerEntity;

public interface ShouldPolyProvider {
	/**
	 * Queries if the packets sent to this source are being polyd or not.
	 * @return {@code true} if the packets are being polyd. {@code false} otherwise.
	 */
	boolean hasPolying();

	/**
	 * Refreshes the cache on whether this source should be polyd.
	 * <p>
	 * Calling this function whilst the player is already fully logged in is considered experimental.
	 * This will only affect new things.
	 * </p>
	 */
	void refreshPolyCache();

	/**
	 * Queries if the packets sent to this player are being polyd or not.
	 * @return {@code true} if the packets are being polyd. {@code false} otherwise.
	 */
	static boolean hasPolying(ServerPlayerEntity player) {
		return ((ShouldPolyProvider)player).hasPolying();
	}
}
