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
package io.github.theepicblock.polymc.mixins;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.impl.registry.sync.RemapException;
import net.fabricmc.fabric.impl.registry.sync.RemappableRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@SuppressWarnings("OverwriteTarget")
@Mixin(value = SimpleRegistry.class, priority = 2000)
public class FabricRemapDisabler implements RemappableRegistry {

    /**
     * @author TheEpicBlock
     * @reason Vanilla ids should always be in the right place. The modded ids aren't used for networking so this is not needed.
     */
    @Overwrite(remap = false)
    @Dynamic("remap is added at runtime by the fabric-registry-sync-v0 using `MixinIdRegistry`")
    public void remap(String s, Object2IntMap<Identifier> object2IntMap, RemapMode remapMode) throws RemapException {}

    /**
     * @author TheEpicBlock
     * @reason Vanilla ids should always be in the right place. The modded ids aren't used for networking so this is not needed.
     */
    @Overwrite(remap = false)
    @Dynamic("unmap is added at runtime by the fabric-registry-sync-v0 using `MixinIdRegistry`")
    public void unmap(String s) throws RemapException {}
}
