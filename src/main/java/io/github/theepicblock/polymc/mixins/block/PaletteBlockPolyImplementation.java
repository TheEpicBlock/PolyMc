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

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.impl.UnRemappedPacketProvider;
import me.jellysquid.mods.lithium.common.world.chunk.LithiumHashPalette;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.world.chunk.ArrayPalette;
import net.minecraft.world.chunk.BiMapPalette;
import net.minecraft.world.chunk.Palette;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Minecraft uses a different method to get ids when it sends chunks.
 * Therefor {@link BlockPolyImplementation} doesn't work on chunks.
 * This Mixin makes sure that the blocks are polyd before they get sent to the client.
 */
@Mixin(value = {ArrayPalette.class, BiMapPalette.class, LithiumHashPalette.class})
public abstract class PaletteBlockPolyImplementation<T> implements UnRemappedPacketProvider {
    @Unique private boolean noRemaps;

    @ModifyArg(method = {"toPacket", "getPacketSize"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/IdList;getRawId(Ljava/lang/Object;)I"))
    public T GetIdRedirect(T object) {
        if (!noRemaps && object instanceof BlockState) {
            //noinspection unchecked
            return (T)PolyMc.getMap().getClientBlock((BlockState)object);
        }
        return object;
    }


    @Override
    public void toPacketUnRemapped(PacketByteBuf buf) {
        noRemaps = true;
        ((Palette<T>)this).toPacket(buf);
        noRemaps = false;
    }

    @Override
    public int getUnRemappedPacketSize() {
        noRemaps = true;
        int ret = ((Palette<T>)this).getPacketSize();
        noRemaps = false;
        return ret;
    }
}
