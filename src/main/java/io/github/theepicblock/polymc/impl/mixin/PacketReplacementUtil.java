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
package io.github.theepicblock.polymc.impl.mixin;

import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Consumer;

public class PacketReplacementUtil {
    public static void syncWorldEvent(World world, PlayerEntity exception, int eventId, BlockPos pos, BlockState data) {
        if (world.getServer() != null) {
            sendToAround(world.getServer().getPlayerManager(), exception, pos.getX(), pos.getY(), pos.getZ(), 64, world.getRegistryKey(), (playerEntity) -> {
                playerEntity.networkHandler.sendPacket(new WorldEventS2CPacket(eventId, pos, Util.getPolydRawIdFromState(data, playerEntity), false));
            });
        }
    }

    public static void sendToAround(PlayerManager manager, PlayerEntity exception, double x, double y, double z, double distance, RegistryKey<World> worldKey, Consumer<ServerPlayerEntity> consumer) {
        for (int i = 0; i < manager.getPlayerList().size(); ++i) {
            ServerPlayerEntity serverPlayerEntity = manager.getPlayerList().get(i);
            if (serverPlayerEntity != exception && serverPlayerEntity.world.getRegistryKey() == worldKey) {
                double d = x - serverPlayerEntity.getX();
                double e = y - serverPlayerEntity.getY();
                double f = z - serverPlayerEntity.getZ();
                if (d * d + e * e + f * f < distance * distance) {
                    consumer.accept(serverPlayerEntity);
                }
            }
        }
    }
}
