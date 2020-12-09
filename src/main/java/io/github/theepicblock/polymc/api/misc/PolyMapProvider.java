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
package io.github.theepicblock.polymc.api.misc;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyMap;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PolyMapProvider {
	PolyMapProviderEvent EVENT = new PolyMapProviderEvent();

	/**
	 * @return the {@link PolyMap} that is used by this provider.
	 */
	PolyMap getPolyMap();

	/**
	 * Refreshes the map used by this player. It will call {@link #EVENT} again.
	 * <p>
	 * Warning: whilst this method allows you to refresh the {@link PolyMap} on the fly it is *not* recommended.
	 * This function won't affect new packets!
	 * </p>
	 */
	void refreshUsedPolyMap();

	/**
	 * @return the {@link PolyMap} that is used for this player.
	 */
	static PolyMap getPolyMap(ServerPlayerEntity player) {
		return ((PolyMapProvider)player).getPolyMap();
	}

	class PolyMapProviderEvent extends Event<PolyMapGetter> {
		public PolyMapProviderEvent() {
			super(new PolyMapGetter[]{});
		}

		public PolyMap invoke(ServerPlayerEntity playerEntity) {
			for (PolyMapGetter handler : handlers) {
				PolyMap map = handler.getMap(playerEntity);
				if (map == null) continue;
			}
			return PolyMc.getMap();
		}
	}

	interface PolyMapGetter {
		PolyMap getMap(ServerPlayerEntity player);
	}
}
