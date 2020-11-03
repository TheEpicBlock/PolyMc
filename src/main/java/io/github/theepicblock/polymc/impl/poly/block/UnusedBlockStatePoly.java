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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.block.BlockStateManager;
import io.github.theepicblock.polymc.api.block.BlockStateProfile;
import io.github.theepicblock.polymc.api.resource.JsonBlockState;
import io.github.theepicblock.polymc.api.resource.ResourcePackMaker;
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * This poly uses unused blockstates to display blocks
 */
public class UnusedBlockStatePoly implements BlockPoly {
    private final ImmutableMap<BlockState,BlockState> states;

    /**
     * @param moddedBlock     the block this poly represents
     * @param stateProfile    the profile to use.
     * @param registry        registry used to register this poly
     * @throws BlockStateManager.StateLimitReachedException when the clientSideBlock doesn't have any more BlockStates left.
     */
    public UnusedBlockStatePoly(Block moddedBlock, PolyRegistry registry, BlockStateProfile stateProfile) throws BlockStateManager.StateLimitReachedException {
        BlockStateManager manager = registry.getBlockStateManager();

        ImmutableList<BlockState> moddedStates = moddedBlock.getStateManager().getStates();
        if (!manager.isAvailable(stateProfile, moddedStates.size())) {
            throw new BlockStateManager.StateLimitReachedException("Block doesn't have enough blockstates left. Profile: '"+stateProfile.name+"'");
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
        JsonBlockState moddedBlockStates = pack.getGson().fromJson(new JsonReader(blockStateReader), JsonBlockState.class);

        states.forEach((moddedState, clientState) -> {
            Identifier clientBlockId = Registry.BLOCK.getId(clientState.getBlock());
            JsonBlockState clientBlockStates = pack.getOrDefaultPendingBlockState(clientBlockId);
            String clientStateString = Util.getPropertiesFromBlockState(clientState);

            JsonElement moddedVariants = moddedBlockStates.getVariantBestMatching(moddedState);
            clientBlockStates.variants.put(clientStateString, moddedVariants);

            for (JsonBlockState.Variant v : JsonBlockState.getVariantsFromJsonElement(moddedVariants)) {
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
