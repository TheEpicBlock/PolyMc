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
package io.github.theepicblock.polymc.mixins.block.implementations;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.impl.mixin.ChunkPacketStaticHack;
import me.jellysquid.mods.lithium.common.world.chunk.LithiumHashPalette;
import net.minecraft.block.BlockState;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.world.chunk.ArrayPalette;
import net.minecraft.world.chunk.BiMapPalette;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Minecraft uses a different method to get ids when it sends chunks.
 * This Mixin makes sure that the blocks are polyd before they get sent to the client.
 */
@Mixin(value = {ArrayPalette.class, BiMapPalette.class, LithiumHashPalette.class})
public abstract class PaletteBlockPolyImplementation<T> {
    
    @Redirect(method = {"writePacket", "getPacketSize"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/IndexedIterable;getRawId(Ljava/lang/Object;)I"))
    public int GetIdRedirect(IndexedIterable instance, T object) {
        if (object instanceof BlockState) {
            var player = ChunkPacketStaticHack.player.get();

            PolyMap map = player == null ? PolyMc.getMainMap() : PolyMapProvider.getPolyMap(player);
            //noinspection unchecked
            return map.getClientStateRawId((BlockState)object, player);
        }

        return instance.getRawId(object);
    }
}
