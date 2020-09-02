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

import net.minecraft.block.BlockState;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class BlockBreakingPatch {
    @Shadow
    public ServerPlayerEntity player;
    @Shadow
    private int tickCounter;

    @Shadow
    public abstract void finishMining(BlockPos pos, PlayerActionC2SPacket.Action action, String reason);

    @Shadow
    private int startMiningTime;
    private int blockBreakingCooldown;

    @Inject(method = "continueMining", at = @At("TAIL"))
    public void breakIfTakingTooLong(BlockState state, BlockPos pos, int i, CallbackInfoReturnable<Float> cir) {
        int j = tickCounter - i;
        float f = state.calcBlockBreakingDelta(this.player, this.player.world, pos) * (float)(j);

        if (blockBreakingCooldown > 0) {
            --blockBreakingCooldown;
        }

        if (f >= 1.0F) {
            blockBreakingCooldown = 5;
            //player.networkHandler.sendPacket(new WorldEventS2CPacket(2001, pos, Block.getRawIdFromState(state), false));
            player.networkHandler.sendPacket(new BlockBreakingProgressS2CPacket(123, pos, -1));
            finishMining(pos, PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, "destroyed");
        }
    }

    @Inject(method = "continueMining", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setBlockBreakingInfo(ILnet/minecraft/util/math/BlockPos;I)V"))
    public void onUpdateBreakStatus(BlockState state, BlockPos pos, int i, CallbackInfoReturnable<Float> cir) {
        int j = tickCounter - i;
        float f = state.calcBlockBreakingDelta(this.player, this.player.world, pos) * (float)(j + 1);
        int k = (int)(f * 10.0F);
        //TODO Replace with a local capture
        player.networkHandler.sendPacket(new BlockBreakingProgressS2CPacket(123, pos, k));
    }

    @Inject(method = "processBlockBreakingAction", at = @At("HEAD"))
    public void packetRecievedInject(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, CallbackInfo ci) {
        if (action == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
            player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(player.getEntityId(), new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 20, -1, true, false)));
        } else if (action == PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK) {
            player.networkHandler.sendPacket(new BlockBreakingProgressS2CPacket(123, pos, -1));
        }
    }

    @Inject(method = "processBlockBreakingAction", at = @At("TAIL"))
    public void enforceBlockBreakingCooldown(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, CallbackInfo ci) {
        if (action == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
            this.startMiningTime += blockBreakingCooldown;
        }
    }
}