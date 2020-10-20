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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import io.github.theepicblock.polymc.Util;
import io.github.theepicblock.polymc.api.OutOfBoundsException;
import io.github.theepicblock.polymc.api.register.BlockStateManager;
import io.github.theepicblock.polymc.api.register.PolyRegistry;
import io.github.theepicblock.polymc.resource.JsonBlockState;
import io.github.theepicblock.polymc.resource.ResourcePackMaker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * This poly uses unused blockstates to display blocks
 */
public class UnusedBlockStatePoly implements BlockPoly {
    private final ImmutableMap<BlockState,BlockState> states;

    /**
     * @param moddedBlock     the block this poly represents
     * @param stateProfile    the profile to use.
     * @param registry        registry used to register this poly
     * @throws OutOfBoundsException when the clientSideBlock doesn't have any more BlockStates left.
     */
    public UnusedBlockStatePoly(Block moddedBlock, PolyRegistry registry, BlockStateProfile stateProfile) throws OutOfBoundsException {
        BlockStateManager manager = registry.getBlockStateManager();

        ImmutableList<BlockState> moddedStates = moddedBlock.getStateManager().getStates();
        if (!manager.isAvailable(stateProfile, moddedStates.size())) {
            throw new OutOfBoundsException("Block doesn't have enough blockstates left. Profile: '"+stateProfile.name+"'");
        }

        HashMap<BlockState,BlockState> res = new HashMap<>();
        for (BlockState state : moddedStates) {
            res.put(state, manager.requestBlockState(stateProfile));
        }
        states = ImmutableMap.copyOf(res);
    }

    @Override
    public BlockState getClientBlock(BlockState input) {
        return states.get(input);
    }

    @Override
    public void AddToResourcePack(Block block, ResourcePackMaker pack) {
        Identifier moddedBlockId = Registry.BLOCK.getId(block);
        InputStreamReader blockStateReader = pack.getAsset(moddedBlockId.getNamespace(), ResourcePackMaker.BLOCKSTATES + moddedBlockId.getPath() + ".json");
        JsonBlockState originalBlockStates = pack.getGson().fromJson(new JsonReader(blockStateReader), JsonBlockState.class);
        Map<String,JsonElement> parsedOriginalVariants = new HashMap<>();
        originalBlockStates.variants.forEach((string, element) -> {
            parsedOriginalVariants.put(string.replace(" ", ""), element);
        });

        states.forEach((moddedState, clientState) -> {
            Identifier clientBlockId = Registry.BLOCK.getId(clientState.getBlock());
            JsonBlockState clientBlockStates = pack.getOrDefaultPendingBlockState(clientBlockId);
            String clientStateString = Util.getPropertiesFromBlockState(clientState);
            String moddedStateString = Util.getPropertiesFromBlockState(moddedState);

            JsonElement moddedVariants = parsedOriginalVariants.get(moddedStateString);
            if (moddedVariants == null) moddedVariants = parsedOriginalVariants.get(""); //TODO there should be a better way for this
            clientBlockStates.variants.put(clientStateString, moddedVariants);
            for (JsonBlockState.Variant v : JsonBlockState.getVariants(moddedVariants)) {
                Identifier vId = Identifier.tryParse(v.model);
                if (vId != null) pack.copyModel(new Identifier(v.model));
            }
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
        });
        return out.toString();
    }
}
