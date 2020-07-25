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

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * This patch sends a BlockBreakingProgress packet to the person who is breaking a block to prevent desyncs.
 */
@Mixin(ServerWorld.class)
public class BlockBreakingDisplayPatch {
    /**
     * Minecraft checks if the uuid of the person breaking is the uuid of the player it's sending a packet to.
     * This prevents it from sending a packet to the same person who is doing the breaking.
     * We, however, want to send the packet to the person who's breaking in order to fix any desyncs.
     * Returning -1 means this check always returns false
     */
    @Redirect(method = "setBlockBreakingInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getEntityId()I"))
    public int yeet(ServerPlayerEntity serverPlayerEntity) {
        return -1;
    }
}
