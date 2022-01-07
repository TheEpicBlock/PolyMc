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

import io.github.theepicblock.polymc.impl.mixin.ClientConnectionContextContainer;
import io.github.theepicblock.polymc.impl.mixin.PlayerContextContainer;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PacketByteBuf.class)
public class ByteBufPlayerContextContainer implements PlayerContextContainer {
    @Unique private ServerPlayerEntity player;

    @Override
    public ServerPlayerEntity getPolyMcProvidedPlayer() {

        // If the player is not set, we might be able to look for it on the ClientConnection
        if (player == null) {

            // See if the ClientConnection is set
            ClientConnection connection = ClientConnectionContextContainer.retrieve(this);

            if (connection != null) {
                player = PlayerContextContainer.retrieve(connection);
            }
        }

        return player;
    }

    @Override
    public void setPolyMcProvidedPlayer(ServerPlayerEntity v) {
        player = v;
    }
}
