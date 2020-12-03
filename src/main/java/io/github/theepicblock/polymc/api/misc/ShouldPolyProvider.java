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

import net.minecraft.server.network.ServerPlayerEntity;

public interface ShouldPolyProvider {
	ShouldPolyEvent EVENT = new ShouldPolyEvent();

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

	enum Action {
		/**
		 * Overrides all other handlers and forces polying. (unless a handler called {@link #DONT_POLY} before your handler got executed)
		 * In general, you should use {@link #PASS} instead.
		 */
		DEFINITELY_POLY,
		/**
		 * Don't take any action. Let another handler decide. If there are no handlers left, polys will be enabled
		 */
		PASS,
		/**
		 * Disables polying for this client. (unless a handler called {@link #DEFINITELY_POLY} before your handler got executed)
		 */
		DONT_POLY
	}

	class ShouldPolyEvent extends Event<ShouldPolyHandler> {
		public ShouldPolyEvent() {
			super(new ShouldPolyHandler[]{});
		}

		public boolean invoke(ServerPlayerEntity playerEntity) {
			for (ShouldPolyHandler handler : handlers) {
				Action a = handler.apply(playerEntity);
				switch (a) {
					case DEFINITELY_POLY:
						return true;
					case DONT_POLY:
						return false;
					case PASS:
					default:
				}
			}
			return true;
		}
	}

	interface ShouldPolyHandler {
		Action apply(ServerPlayerEntity player);
	}
}
