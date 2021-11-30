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
package io.github.theepicblock.polymc.mixins.context;

import io.github.theepicblock.polymc.impl.mixin.PlayerContextContainer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({AdvancementUpdateS2CPacket.class,
        EntityEquipmentUpdateS2CPacket.class,
        InventoryS2CPacket.class,
        ScreenHandlerSlotUpdateS2CPacket.class,
        SetTradeOffersS2CPacket.class,
        EntityTrackerUpdateS2CPacket.class,
        ParticleS2CPacket.class,
        SynchronizeTagsS2CPacket.class,
        EntitySpawnS2CPacket.class})
public class PacketPlayerContextContainer implements PlayerContextContainer {
    @Unique
    private ServerPlayerEntity player;

    @Override
    public ServerPlayerEntity getPolyMcProvidedPlayer() {
        return player;
    }

    @Override
    public void setPolyMcProvidedPlayer(ServerPlayerEntity v) {
        player = v;
    }

    /**
     * This mixin passes the player context onto the ByteBuffer
     * @see ByteBufPlayerContextContainer
     */
    @Inject(method = "write(Lnet/minecraft/network/PacketByteBuf;)V", at = @At("HEAD"))
    private void writeInject(PacketByteBuf buf, CallbackInfo ci) {
        ((PlayerContextContainer)buf).setPolyMcProvidedPlayer(player);
    }
}
