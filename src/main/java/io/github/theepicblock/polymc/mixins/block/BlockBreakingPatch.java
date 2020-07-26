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
package io.github.theepicblock.polymc.mixins.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class BlockBreakingPatch {
    @Shadow public ServerPlayerEntity player;
    @Shadow private int tickCounter;
    @Shadow public abstract void finishMining(BlockPos pos, PlayerActionC2SPacket.Action action, String reason);
    @Shadow public abstract boolean tryBreakBlock(BlockPos pos);

    @Inject(method = "continueMining", at = @At("TAIL"))
    public void breakIfTakingTooLong(BlockState state, BlockPos pos, int i, CallbackInfoReturnable<Float> cir) {
        int j = tickCounter - i;
        float f = state.calcBlockBreakingDelta(this.player, this.player.world, pos) * (float)(j + 1);
        if (f >= 0.75F) {
            player.networkHandler.sendPacket(new WorldEventS2CPacket(2001, pos, Block.getRawIdFromState(state), false));
            finishMining(pos, PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,"destroyed");
        }
    }
}
