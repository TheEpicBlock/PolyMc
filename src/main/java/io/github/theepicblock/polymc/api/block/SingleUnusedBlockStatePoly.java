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
 * Works the same as {@link UnusedBlockStatePoly}, but uses only a single output blockstate, instead of one per modded blockstate.
 */
public class SingleUnusedBlockStatePoly implements BlockPoly{
    private final BlockState newBlockState;
    
    /**
     * @param stateProfile    the profile to use.
     * @param registry        registry used to register this poly
     * @throws OutOfBoundsException when the clientSideBlock doesn't have any more BlockStates left.
     */
    public SingleUnusedBlockStatePoly(PolyRegistry registry, BlockStateProfile stateProfile) throws OutOfBoundsException {
        BlockStateManager manager = registry.getBlockStateManager();

        newBlockState = manager.requestBlockState(stateProfile);
    }

    @Override
    public BlockState getClientBlock(BlockState input) {
        return newBlockState;
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


        Identifier clientBlockId = Registry.BLOCK.getId(newBlockState.getBlock());
        JsonBlockState clientBlockStates = pack.getOrDefaultPendingBlockState(clientBlockId);
        String clientStateString = Util.getPropertiesFromBlockState(newBlockState);
        String moddedStateString = Util.getPropertiesFromBlockState(block.getDefaultState());

        JsonElement moddedVariants = parsedOriginalVariants.get(moddedStateString);
        if (moddedVariants == null) moddedVariants = parsedOriginalVariants.get(""); //TODO there should be a better way for this
        clientBlockStates.variants.put(clientStateString, moddedVariants);
        for (JsonBlockState.Variant v : JsonBlockState.getVariants(moddedVariants)) {
            Identifier vId = Identifier.tryParse(v.model);
            if (vId != null) pack.copyModel(new Identifier(v.model));
        }
    }
}
