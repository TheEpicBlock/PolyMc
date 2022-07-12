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

import io.github.theepicblock.polymc.impl.mixin.CustomBlockBreakingCheck;
import net.minecraft.block.BlockState;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla-like clients usually process block breaking client-side.
 * These mixins give vanilla-like clients mining fatigue and reimplement the block breaking server-side.
 */
@Mixin(ServerPlayerInteractionManager.class)
public abstract class BlockBreakingPatch {
    @Shadow @Final protected ServerPlayerEntity player;
    @Shadow private int tickCounter;
    @Shadow private int startMiningTime;
    @Shadow protected ServerWorld world;
    @Shadow private int blockBreakingProgress;

    @Unique
    private int blockBreakingCooldown;
    @Unique
    private boolean hasMineFatigue = false;

    @Shadow
    public abstract void finishMining(BlockPos pos, int sequence, String reason);


    /**
     * This breaks the block serverside if the client hasn't broken it already
     */
    @Inject(method = "continueMining", at = @At("TAIL"))
    public void breakIfTakingTooLong(BlockState state, BlockPos pos, int i, CallbackInfoReturnable<Float> cir) {
        if (CustomBlockBreakingCheck.needsCustomBreaking(player, state.getBlock())) {
            int j = tickCounter - i;
            float f = state.calcBlockBreakingDelta(this.player, this.player.world, pos) * (float)(j);

            if (blockBreakingCooldown > 0) {
                --blockBreakingCooldown;
            }

            if (f >= 1.0F) {
                blockBreakingCooldown = 5;
                player.networkHandler.sendPacket(new BlockBreakingProgressS2CPacket(-1, pos, -1));
                finishMining(pos, 0, "destroyed");
            }
        }
    }

    @Inject(method = "continueMining", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, shift = At.Shift.AFTER, target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;blockBreakingProgress:I"))
    public void onUpdateBreakStatus(BlockState state, BlockPos pos, int i, CallbackInfoReturnable<Float> cir) {
        //Send a packet that resembles the current mining progress
        player.networkHandler.sendPacket(new BlockBreakingProgressS2CPacket(-1, pos, this.blockBreakingProgress));
    }

    @Inject(method = "processBlockBreakingAction", at = @At("HEAD"))
    public void packetReceivedInject(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
        if (CustomBlockBreakingCheck.needsCustomBreaking(player, world.getBlockState(pos).getBlock())) {
            if (action == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
                // This prevents the client from trying to break the block themselves.
                if (this.world.getBlockState(pos).calcBlockBreakingDelta(this.player, this.player.world, pos) < 1) {
                    hasMineFatigue = true;
                    player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(this.player.getId(), new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 20, -1, true, false)));
                }
            } else if (action == PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK) {
                removeFakeMiningFatigue();
                player.networkHandler.sendPacket(new BlockBreakingProgressS2CPacket(-1, pos, -1));
            }
        } else if (hasMineFatigue) {
            removeFakeMiningFatigue();
        }
    }

    @Inject(method = "processBlockBreakingAction", at = @At("TAIL"))
    public void enforceBlockBreakingCooldown(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
        if (CustomBlockBreakingCheck.needsCustomBreaking(player, world.getBlockState(pos).getBlock())) {
            if (action == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
                this.startMiningTime += blockBreakingCooldown;
            }
        } else if (hasMineFatigue) {
            removeFakeMiningFatigue();
        }
    }

    @Inject(method = "finishMining", at = @At("HEAD"))
    private void clearEffects(BlockPos pos, int sequence, String reason, CallbackInfo ci) {
        if (hasMineFatigue) {
            removeFakeMiningFatigue();
        }
    }

    @Unique
    private void removeFakeMiningFatigue() {
        player.networkHandler.sendPacket(new RemoveEntityStatusEffectS2CPacket(player.getId(), StatusEffects.MINING_FATIGUE));

        var effectInstance = this.player.getStatusEffect(StatusEffects.MINING_FATIGUE);
        if (effectInstance != null) {
            player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(this.player.getId(), effectInstance));
        }
    }
}