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

import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonNetworkHandler.class)
public class CustomEffectsDisabler {

    @Inject(method = "send", at = @At("HEAD"), cancellable = true)
    public void sendPacketInject(Packet<?> packet, PacketCallbacks callbacks, CallbackInfo ci) {
        if (((Object) this) instanceof ServerPlayNetworkHandler handler) {
            var map = Util.tryGetPolyMap(handler);
            if ((packet instanceof EntityStatusEffectS2CPacket packet1 && !map.canReceiveStatusEffect(packet1.getEffectId()))
                    || (packet instanceof RemoveEntityStatusEffectS2CPacket packet2 && !map.canReceiveStatusEffect(packet2.effect()))) {
                ci.cancel();
            }
        }
    }
}
