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
package io.github.theepicblock.polymc.mixins.entity;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.entity.EntityPoly;
import io.github.theepicblock.polymc.impl.poly.entity.EntityTracker;
import io.github.theepicblock.polymc.impl.poly.entity.NoOpEntityTracker;
import io.github.theepicblock.polymc.impl.poly.entity.PolyEntityTracker;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.world.ThreadedAnvilChunkStorage.EntityTracker")
public class MixinTACSEntityTracker {
	@Shadow @Final private EntityTrackerEntry entry;

	@Unique @Final private EntityTracker polyTracker;

	@SuppressWarnings("ShadowFinalModification")
	@Inject(method = "<init>(Lnet/minecraft/entity/Entity;IIZ)V", at = @At("RETURN"))
	private void onInit(ThreadedAnvilChunkStorage tacs, Entity entity, int tickInterval, int i, boolean bl, CallbackInfo ci) {
		EntityPoly poly = PolyMc.getMap().getEntityPoly(entity);
		if (poly != null) {
			polyTracker = new PolyEntityTracker(entity, poly);
		} else {
			polyTracker = new NoOpEntityTracker(this.entry);
		}
	}

	@Redirect(method = {"stopTracking()V",
						"stopTracking(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
						"updateCameraPosition(Lnet/minecraft/server/network/ServerPlayerEntity;)V"},
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/EntityTrackerEntry;stopTracking(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
	private void onStopTracking(EntityTrackerEntry entityTrackerEntry, ServerPlayerEntity player) {
		this.polyTracker.stopTracking(player);
	}

	@Redirect(method = {"updateCameraPosition(Lnet/minecraft/server/network/ServerPlayerEntity;)V"},
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/EntityTrackerEntry;startTracking(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
	private void onStartTracking(EntityTrackerEntry entityTrackerEntry, ServerPlayerEntity player) {
		this.polyTracker.startTracking(player);
	}
}
