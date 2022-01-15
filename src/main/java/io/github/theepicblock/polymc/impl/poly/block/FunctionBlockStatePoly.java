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
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.block.BlockStateMerger;
import io.github.theepicblock.polymc.api.resource.JsonBlockState;
import io.github.theepicblock.polymc.api.resource.ResourcePackMaker;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.BooleanContainer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.InputStreamReader;
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
        var moddedStateGroups = new ArrayList<BlockStateGroup>();
        var states = new HashMap<BlockState, BlockState>();

        // Sort all the modded states into groups
        for (var moddedState : moddedBlock.getStateManager().getStates()) {
            boolean foundGroup = false;
            for (var group : moddedStateGroups) {
                if (group.add(moddedState, merger)) {
                    foundGroup = true;
                    break;
                }
            }
            if (!foundGroup) {
                moddedStateGroups.add(new BlockStateGroup(moddedState, merger));
            }
        }

        var isUniqueCallback = new BooleanContainer();
        for (var group : moddedStateGroups) {
            isUniqueCallback.set(false);
            var clientState = registrationProvider.apply(group.getNeutralizedState(), isUniqueCallback);
            for (var moddedState : group.getStates()) {
                states.put(moddedState, clientState);
            }
            if (isUniqueCallback.get()) uniqueClientBlocks.add(clientState);
        }

        this.states = ImmutableMap.copyOf(states);
    }

    @Override
    public BlockState getClientBlock(BlockState input) {
        return states.get(input);
    }

    public void addToResourcePack(Block block, ResourcePackMaker pack) {
        Identifier moddedBlockId = Registry.BLOCK.getId(block);
        InputStreamReader blockStateReader = pack.getAsset(moddedBlockId.getNamespace(), ResourcePackMaker.BLOCKSTATES + moddedBlockId.getPath() + ".json");
        JsonBlockState moddedBlockStates = pack.getGson().fromJson(new JsonReader(blockStateReader), JsonBlockState.class);

        HashSet<BlockState> clientStatesDone = new HashSet<>();
        states.forEach((moddedState, clientState) -> {
            if (clientStatesDone.contains(clientState)) return;
            if (!uniqueClientBlocks.contains(clientState)) return;

            Identifier clientBlockId = Registry.BLOCK.getId(clientState.getBlock());
            JsonBlockState clientBlockStates = pack.getOrDefaultPendingBlockState(clientBlockId);
            String clientStateString = Util.getPropertiesFromBlockState(clientState);

            JsonElement moddedVariants = moddedBlockStates.getVariantBestMatching(moddedState);
            clientBlockStates.variants.put(clientStateString, moddedVariants);

            for (JsonBlockState.Variant v : JsonBlockState.getVariantsFromJsonElement(moddedVariants)) {
                Identifier vId = Identifier.tryParse(v.model);
                if (vId != null) pack.copyModel(new Identifier(v.model));
            }

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
