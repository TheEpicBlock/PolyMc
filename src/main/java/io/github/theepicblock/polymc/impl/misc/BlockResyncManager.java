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
package io.github.theepicblock.polymc.impl.misc;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class BlockResyncManager {
	public static boolean shouldForceSync(BlockState serverSideState, Direction direction) {
		Block block = serverSideState.getBlock();
		if (block == Blocks.NOTE_BLOCK) {
			return direction == Direction.UP;
		}
		return false;
	}

	public static void onBlockUpdate(BlockPos pos, World world, ServerPlayerEntity player, List<BlockPos> exceptions) {
		BlockPos.Mutable mPos = new BlockPos.Mutable();
		for (Direction d : Direction.values()) {
			mPos.set(pos.getX() + d.getOffsetX(), pos.getY() + d.getOffsetY(), pos.getZ() + d.getOffsetZ());
			if (exceptions != null && exceptions.contains(mPos)) continue;
			BlockState state = world.getBlockState(mPos);
			BlockPoly poly = PolyMc.getMainMap().getBlockPoly(state.getBlock());
			if (poly != null) {
				BlockState serverSideState = poly.getClientBlock(state);
				if (BlockResyncManager.shouldForceSync(serverSideState, d)) {
					BlockPos nPos = mPos.toImmutable();
					player.networkHandler.sendPacket(new BlockUpdateS2CPacket(nPos, state));
					List<BlockPos> newExceptions;
					if (exceptions == null) {
						newExceptions = new ArrayList<>();
						newExceptions.add(pos);
					} else {
						exceptions.add(pos);
						newExceptions = exceptions;
					}
					onBlockUpdate(nPos, world, player, newExceptions);
				}
			}
		}
	}
}
