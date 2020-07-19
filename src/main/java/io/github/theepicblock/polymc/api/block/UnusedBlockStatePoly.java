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
import com.google.gson.stream.JsonReader;
import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.Util;
import io.github.theepicblock.polymc.api.OutOfBoundsException;
import io.github.theepicblock.polymc.api.register.BlockStateManager;
import io.github.theepicblock.polymc.api.register.PolyRegistry;
import io.github.theepicblock.polymc.resource.JsonBlockstate;
import io.github.theepicblock.polymc.resource.ResourcePackMaker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * This poly uses unused blockstates to display blocks
 */
public class UnusedBlockStatePoly implements BlockPoly{
    private final ImmutableMap<BlockState,BlockState> states;
    /**
     *
     * @param moddedBlock the block this poly represents
     * @param clientSideBlock the block used to display this block on the client
     * @param registry registry used to register this poly
     * @throws OutOfBoundsException when the clientSideBlock doesn't have any more BlockStates left.
     */
    public UnusedBlockStatePoly(Block moddedBlock, Block clientSideBlock, PolyRegistry registry) throws OutOfBoundsException {
        BlockStateManager manager = registry.getBlockStateManager();

        ImmutableList<BlockState> moddedStates = moddedBlock.getStateManager().getStates();

        HashMap<BlockState,BlockState> res = new HashMap<>();
        for (BlockState state : moddedStates) {
            res.put(state,manager.requestBlockState(clientSideBlock,registry));
        }
        states = ImmutableMap.copyOf(res);
    }

    @Override
    public BlockState getClientBlock(BlockState input) {
        return states.get(input);
    }

    @Override
    public void AddToResourcePack(Block block, ResourcePackMaker pack) {
        Identifier modBlockId = Registry.BLOCK.getId(block);
        InputStreamReader blockStateReader = pack.getAssetFromMod(modBlockId.getNamespace(),ResourcePackMaker.BLOCKSTATES+modBlockId.getPath()+".json");
        if (blockStateReader == null) {
            PolyMc.LOGGER.warn("Couldn't get blockstate file for: " + block.getTranslationKey());
            return;
        }
        JsonBlockstate originalBlockStates = pack.getGson().fromJson(new JsonReader(blockStateReader),JsonBlockstate.class);

        Block clientBlock = states.get(block.getDefaultState()).getBlock();
        Identifier clientBlockId = Registry.BLOCK.getId(clientBlock);
        JsonBlockstate newBlockStates = pack.getOrCreateBlockState(clientBlockId);

        //paste all of the blockstates from the original block into an entry for the clientside block
        originalBlockStates.variants.forEach((stateString,variant) -> {
            BlockState state = Util.getBlockStateFromString(block,stateString);
            BlockState clientState = states.get(state);
            String clientStateString = Util.getPropertiesFromBlockState(clientState);

            newBlockStates.variants.put(clientStateString,variant);
        });

        //make sure all the models are present
        newBlockStates.variants.forEach((stateString,rawVariant) -> {
            JsonBlockstate.Variant[] variants = JsonBlockstate.getVariants(rawVariant);
            for (JsonBlockstate.Variant variant : variants) {
                if (variant.model != null && !variant.model.isEmpty()) {
                    pack.copyModel(new Identifier(variant.model));
                }
            }
        });
    }
}
