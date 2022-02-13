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

import io.github.theepicblock.polymc.impl.mixin.PacketReplacementUtil;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * In the {@link BedBlock#onBreak(World, BlockPos, BlockState, PlayerEntity)} method, there is a call to create a WorldEvent for the breakage.
 */
@Mixin(BedBlock.class)
public class BedBlockImplementation {
    /**
     * Removes the call to {@link World#syncWorldEvent(PlayerEntity, int, BlockPos, int)} so it can be replaced
     * @see #worldEventReplacement(World, BlockPos, BlockState, PlayerEntity, CallbackInfo, BedPart, BlockPos, BlockState)
     */
    @Redirect(method = "onBreak", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;syncWorldEvent(Lnet/minecraft/entity/player/PlayerEntity;ILnet/minecraft/util/math/BlockPos;I)V"))
    public void worldEventDisabler(World world, PlayerEntity player, int eventId, BlockPos pos, int data) {
        //Disabled
    }

    /**
     * Replaces the call to {@link World#syncWorldEvent(PlayerEntity, int, BlockPos, int)} with a call to {@link PacketReplacementUtil#syncWorldEvent(World, PlayerEntity, int, BlockPos, BlockState)}
     * to respect different PolyMaps
     */
    @Inject(method = "onBreak", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;syncWorldEvent(Lnet/minecraft/entity/player/PlayerEntity;ILnet/minecraft/util/math/BlockPos;I)V"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    public void worldEventReplacement(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo ci, BedPart var5, BlockPos var6, BlockState var7) {
        PacketReplacementUtil.syncWorldEvent(world, player, 2001, var6, var7);
    }
}
