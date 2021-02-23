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
package io.github.theepicblock.polymc.mixins.block.context;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.impl.mixin.HasNonConsistentBlockPolyProvider;
import io.github.theepicblock.polymc.impl.mixin.NonPolydPacketProvider;
import io.github.theepicblock.polymc.impl.mixin.PacketSizeProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(PalettedContainer.class)
public abstract class PalettedContainerMixin<T> implements PacketSizeProvider {

    @Shadow private Palette<T> palette;
    @Shadow protected PackedIntegerArray data;

    @Override
    public int getPacketSize(ServerPlayerEntity playerEntity) {
        if (this.palette instanceof PacketSizeProvider) {
            PacketSizeProvider palette2 = (PacketSizeProvider)palette;
            return 1 + palette2.getPacketSize(playerEntity) + PacketByteBuf.getVarIntSizeBytes(this.data.getSize()) + this.data.getStorage().length * 8;
        }
        return 1 + this.palette.getPacketSize() + PacketByteBuf.getVarIntSizeBytes(this.data.getSize()) + this.data.getStorage().length * 8;
    }
}
