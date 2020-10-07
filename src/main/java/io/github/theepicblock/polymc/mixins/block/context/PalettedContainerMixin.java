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
import io.github.theepicblock.polymc.api.block.WorldProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.collection.PackedIntegerArray;
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
public abstract class PalettedContainerMixin<T> implements WorldProvider {
    @Shadow public abstract void lock();

    @Shadow public abstract void unlock();

    @Shadow private Palette<T> palette;
    @Shadow private int paletteSize;
    @Shadow protected PackedIntegerArray data;
    @Shadow @Final private Palette<T> fallbackPalette;
    @Shadow @Final private IdList<T> idList;
    @Shadow @Final private T defaultValue;

    @Shadow protected abstract T get(int index);

    @Shadow protected abstract void set(int i, T object);

    @Shadow public abstract boolean hasAny(Predicate<T> predicate);

    @Unique private World world;
    @Unique private int nonConsistentPolyCount;
    @Unique private boolean hasSyncedConsistentPolyCount = false;

    @Override
    public void polyMcSetWorld(World world) {
        this.world = world;
    }

    @Override
    public World polyMcGetWorld() {
        return world;
    }

    @Inject(method = "setAndGetOldValue(ILjava/lang/Object;)Ljava/lang/Object;", at = @At("RETURN"), cancellable = true)
    public void setInPaletteListener(int index, T value, CallbackInfoReturnable<T> cir) {
        if (value instanceof BlockState) {
            PolyMap map = PolyMc.getMap();
            BlockPoly poly = map.getBlockPoly(((BlockState)value).getBlock());
            if (poly != null && poly.blockStateNotConsistent()) {
                nonConsistentPolyCount++;
            }

            T retValue = cir.getReturnValue();
            if (retValue != this.defaultValue) {
                Block block = ((BlockState)retValue).getBlock();
                BlockPoly poly2 = map.getBlockPoly(block);
                if (poly2 != null && poly2.blockStateNotConsistent()) {
                    nonConsistentPolyCount--;
                }
            }
        }
    }

    @Inject(method = "toPacket(Lnet/minecraft/network/PacketByteBuf;)V", at = @At("HEAD"), cancellable = true)
    public void toPacketInject(PacketByteBuf buf, CallbackInfo ci) {
        if (!hasSyncedConsistentPolyCount) {
            nonConsistentPolyCount = 0;
            for (int i = 0; i < this.data.getSize(); i++) {
                BlockState b = (BlockState)this.get(i);
                BlockPoly poly = PolyMc.getMap().getBlockPoly(b.getBlock());
                if (poly != null && poly.blockStateNotConsistent()) {
                    nonConsistentPolyCount++;
                }
            }
            hasSyncedConsistentPolyCount = true;
        }

        if (nonConsistentPolyCount > 0) {
            this.lock();
            PalettedContainer<T> clone = new PalettedContainer<>(this.fallbackPalette, this.idList, null, null, this.defaultValue);
            for (int i = 0; i < this.data.getSize(); i++) {
                BlockState b = (BlockState)this.get(i);
                BlockState polyd = PolyMc.getMap().getClientBlock(b, null, world);
                //noinspection all
                ((PalettedContainerMixin<T>)(Object)clone).set(i, (T)polyd);
            }
            this.unlock();
            clone.toPacket(buf);
            ci.cancel();
        }
    }

    @Inject(method = "getPacketSize", at = @At("HEAD"), cancellable = true)
    public void getPacketSizeInject(CallbackInfoReturnable<Integer> cir) {
        if (nonConsistentPolyCount > 0) {
            this.lock();
            PalettedContainer<T> clone = new PalettedContainer<>(this.fallbackPalette, this.idList, null, null, this.defaultValue);
            for (int i = 0; i < this.data.getSize(); i++) {
                BlockState b = (BlockState)this.get(i);
                BlockState polyd = PolyMc.getMap().getClientBlock(b, null, world);
                //noinspection all
                ((PalettedContainerMixin<T>)(Object)clone).set(i, (T)polyd);
            }
            this.unlock();
            cir.setReturnValue(clone.getPacketSize());
        }
    }
}
