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
package io.github.theepicblock.polymc.mixins.block.implementations;

import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.CustomBlockBreakingCheck;
import io.github.theepicblock.polymc.impl.mixin.PacketReplacementUtil;
import io.github.theepicblock.polymc.mixins.block.FallbackBaseImplementation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * In the {@link Block#onBreak(World, BlockPos, BlockState, PlayerEntity)} method, there is a call to create a WorldEvent for the breakage.
 * Normally the remapping of that would be caught by {@link FallbackBaseImplementation} but that method doesn't respect individuals their PolyMaps.
 */
@Mixin(Block.class)
public class WorldEventImplementation {
    /**
     * Replaces the call to {@link World#syncWorldEvent(PlayerEntity, int, BlockPos, int)} with a call to {@link PacketReplacementUtil#syncWorldEvent(World, PlayerEntity, int, BlockPos, BlockState)}
     * to respect different PolyMaps
     */
    @Redirect(method = "spawnBreakParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;syncWorldEvent(Lnet/minecraft/entity/player/PlayerEntity;ILnet/minecraft/util/math/BlockPos;I)V"))
    public void worldEventPoly(World world, PlayerEntity player, int eventId, BlockPos pos, int data,
                               World worldParent, PlayerEntity playerParent, BlockPos posParent, BlockState stateParent) {
        var spe = (ServerPlayerEntity)player;

        // Minecraft assumes the player who breaks the block knows it's breaking a block.
        // However, as PolyMc reimplements block breaking server-side, the one breaking the block needs to be notified too
        var exception = CustomBlockBreakingCheck.needsCustomBreaking(spe, stateParent.getBlock()) ? null : player;
        PacketReplacementUtil.syncWorldEvent(world, exception, 2001, pos, stateParent);
    }
}
