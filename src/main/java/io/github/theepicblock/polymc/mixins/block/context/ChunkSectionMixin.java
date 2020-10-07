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

import io.github.theepicblock.polymc.api.block.WorldProvider;
import net.minecraft.block.BlockState;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkSection.class)
public class ChunkSectionMixin implements WorldProvider {
    @Shadow @Final private static Palette<BlockState> palette;
    @Shadow @Final private PalettedContainer<BlockState> container;
    @Unique private World world;

    @Override
    public void polyMcSetWorld(World world) {
        this.world = world;
    }

    @Override
    public World polyMcGetWorld() {
        return world;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(CallbackInfo ci) {
        ((WorldProvider)this.container).polyMcSetWorld(world);
    }
}
