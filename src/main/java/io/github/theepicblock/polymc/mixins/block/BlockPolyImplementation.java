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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Block.class)
public class BlockPolyImplementation {
    /**
     * This is a fall-back implementation. It does not respect custom PolyMaps.
     * There are more targeted mixins that do respect custom maps.
     * But, due to how Minecraft's code is made, it's hard to catch all areas.
     */
    @ModifyVariable(method = "getRawIdFromState(Lnet/minecraft/block/BlockState;)I", at = @At("HEAD"))
    private static BlockState rawBlockStateOverwrite(BlockState state) {
        return PolyMc.getMainMap().getClientBlock(state);
    }
}
