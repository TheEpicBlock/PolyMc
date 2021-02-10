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

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class PolyMapProviderImplementation implements PolyMapProvider {
	@Unique private PolyMap polyMap;

	@Override
	public PolyMap getPolyMap() {
		return polyMap;
	}

	@Override
	public void setPolyMap(PolyMap map) {
		polyMap = map;
	}

	@Inject(method = "copyFrom", at = @At("RETURN"))
	private void copyInject(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
		this.refreshUsedPolyMap();
	}

	@Mixin(ServerPlayNetworkHandler.class)
	private static class NetworkHandlerMixin {
		@Shadow public ServerPlayerEntity player;

		@Inject(method = "<init>", at = @At("RETURN"))
		private void initInject(CallbackInfo ci) {
			((PolyMapProvider)(player)).refreshUsedPolyMap();
		}
	}
}
