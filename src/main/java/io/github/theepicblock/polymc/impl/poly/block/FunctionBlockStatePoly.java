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
package io.github.theepicblock.polymc.impl.poly.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.block.BlockStateMerger;
import io.github.theepicblock.polymc.api.resource.ModdedResources;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.BooleanContainer;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.BiFunction;

/**
 * This poly uses a {@link BlockStateMerger} to merge {@link BlockState}s into groups and then calls a function to get the corresponding client {@link BlockState} for this group.
 */
public class FunctionBlockStatePoly implements BlockPoly {
    private final ImmutableMap<BlockState,BlockState> states;
    private final ArrayList<BlockState> uniqueClientBlocks = new ArrayList<>();

    public FunctionBlockStatePoly(Block moddedBlock, BiFunction<BlockState, BooleanContainer, BlockState> registrationProvider) {
        this(moddedBlock, registrationProvider, BlockStateMerger.DEFAULT);
    }

        /**
         * @param moddedBlock the block this poly represents
         * @param registrationProvider provides a new client block state for a modded block state.
         *                             The {@link BooleanContainer} is a workaround for java not having multiple return values.
         *                             If set to true the client block returned is assumed to be used only for this modded block.
         *                             And thus this poly will overwrite its textures with the modded one.
         *                             If set to false it is assumed the client block may be shared with other blocks with do not have the same texture as the modded block.
         * @param merger function to use to merge block states which use the same model on the client
         */
    public FunctionBlockStatePoly(Block moddedBlock, BiFunction<BlockState, BooleanContainer, BlockState> registrationProvider, BlockStateMerger merger) {
        var server2ClientStates = new HashMap<BlockState, BlockState>();

        // Sort all the modded states into groups:
        // Each group has one block state which is the canonical block state of that group,
        // this canonical version is defined by a BlockStateMerger
        // The BlockStateMerger's purpose is to remove all properties and stuff that don't change the model, for example
        // the distance and persistence fields on leaves: the merger will normalize the distance property by setting it to 0.
        // this way, all the different leaves will be grouped together under the [distance=0,persistent=false]
        // Ofc, you might not want leaves to be grouped this way if you have different models for them,
        // that's why the BlockStateMerger can be swapped out with a different one
        Multimap<BlockState, BlockState> moddedStateGroups = LinkedHashMultimap.create();
        for (var moddedState : moddedBlock.getStateManager().getStates()) {
            moddedStateGroups.put(merger.normalize(moddedState), moddedState);
        }

        var isUniqueCallback = new BooleanContainer();
        moddedStateGroups.asMap().forEach((normalizedState, group) -> {
            // Only call registrationProvider once for each normalized state,
            // then we apply the result to all blocks in the group
            isUniqueCallback.set(false);
            var clientState = registrationProvider.apply(normalizedState, isUniqueCallback);
            for (var moddedState : group) {
                server2ClientStates.put(moddedState, clientState);
            }
            if (isUniqueCallback.get()) uniqueClientBlocks.add(clientState);

        });

        this.states = ImmutableMap.copyOf(server2ClientStates);
    }

    @Override
    public BlockState getClientBlock(BlockState input) {
        return states.get(input);
    }

    @Override
    public void addToResourcePack(Block block, ModdedResources moddedResources, PolyMcResourcePack pack, SimpleLogger logger) {
        var moddedBlockId = Registries.BLOCK.getId(block);
        // Read the modded block state file. This tells us which model is used for which block state
        var moddedBlockState = moddedResources.getBlockState(moddedBlockId.getNamespace(), moddedBlockId.getPath());
        if (moddedBlockState == null) {
            logger.error("Can't find blockstate definition for "+moddedBlockId+", can't generate resources for it");
            return;
        }

        HashSet<BlockState> clientStatesDone = new HashSet<>();
        // Iterate modded block states
        this.states.forEach((moddedState, clientState) -> {
            if (clientStatesDone.contains(clientState)) return;
            if (!uniqueClientBlocks.contains(clientState)) return;

            var clientBlockId = Registries.BLOCK.getId(clientState.getBlock());
            var clientBlockStates = pack.getOrDefaultBlockState(clientBlockId.getNamespace(), clientBlockId.getPath());
            var clientStateString = Util.getPropertiesFromBlockState(clientState);

            // Get the model that the modded block state uses and assign it to the client block state
            var moddedVariants = moddedBlockState.getVariantsBestMatching(moddedState);
            clientBlockStates.setVariant(clientStateString, moddedVariants);

            pack.importRequirements(moddedResources, moddedVariants, logger);

            clientStatesDone.add(clientState);
        });
    }

    @Override
    public String getDebugInfo(Block obj) {
        StringBuilder out = new StringBuilder();
        out.append(states.size()).append(" states");
        states.forEach((moddedState, clientState) -> {
            out.append("\n");
            out.append("    #");
            out.append(moddedState);
            out.append(" -> ");
            out.append(clientState);
            if (uniqueClientBlocks.contains(clientState)) {
                out.append(" (UNIQUE)");
            }
        });
        return out.toString();
    }
}
