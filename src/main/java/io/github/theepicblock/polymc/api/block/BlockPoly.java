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
package io.github.theepicblock.polymc.api.block;

import io.github.theepicblock.polymc.api.DebugInfoProvider;
import io.github.theepicblock.polymc.api.resource.ModdedResources;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public interface BlockPoly extends DebugInfoProvider<Block> {
    /**
     * Transforms an BlockState to it's client version
     * @param input original BlockState
     * @return BlockState that should be sent to the client
     */
    BlockState getClientBlock(BlockState input);

    /**
     * Callback to add all resources needed for this block to a resourcepack
     * @param block block this BlockPoly was registered to, for reference.
     * @param pack  resource pack to add to.
     */
    default void addToResourcePack(Block block, ModdedResources moddedResources, PolyMcResourcePack pack, SimpleLogger logger) {

    }

    default Wizard createWizard(ServerWorld world, Vec3d pos, Wizard.WizardState state) {
        return null;
    }

    default boolean hasWizard() {
        return false;
    }
}
