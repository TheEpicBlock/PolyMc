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
package io.github.theepicblock.polymc.mixins.block.desync;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.impl.misc.BlockDesyncManager;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class BreakListener {
	@Shadow public ServerWorld world;

	@Shadow public ServerPlayerEntity player;

	@Inject(method = "tryBreakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"))
	private void onBlockBreakInject(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		BlockPos.Mutable mPos = new BlockPos.Mutable();
		for (Direction d : Direction.values()) {
			mPos.set(pos.getX() + d.getOffsetX(), pos.getY() + d.getOffsetY(), pos.getZ() + d.getOffsetZ());
			BlockState state = this.world.getBlockState(mPos);
			BlockPoly poly = PolyMc.getMap().getBlockPoly(state.getBlock());
			if (poly != null) {
				BlockState serverSideState = poly.getClientBlock(state);
				if (BlockDesyncManager.shouldForceSync(serverSideState, d)) {
					player.networkHandler.sendPacket(new BlockUpdateS2CPacket(mPos.toImmutable(), state));
				}
			}
		}
	}
}
