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
import io.github.theepicblock.polymc.resource.JsonBlockstate;
import io.github.theepicblock.polymc.resource.ResourcePackMaker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * This poly uses unused blockstates to display blocks
 */
public class UnusedBlockStatePoly implements BlockPoly{
    private final ImmutableMap<BlockState,BlockState> states;
    public static final Predicate<BlockState> DEFAULT_FILTER = (blockState) -> blockState != blockState.getBlock().getDefaultState();
    public static final BiConsumer<Block,PolyRegistry> DEFAULT_ON_FIRST_REGISTER = (block, polyRegistry) -> polyRegistry.registerBlockPoly(block,new SimpleReplacementPoly(block.getDefaultState()));
    /**
     *
     * @param moddedBlock the block this poly represents
     * @param clientSideBlock the block used to display this block on the client
     * @param registry registry used to register this poly
     * @throws OutOfBoundsException when the clientSideBlock doesn't have any more BlockStates left.
     */
    public UnusedBlockStatePoly(Block moddedBlock, Block clientSideBlock, PolyRegistry registry) throws OutOfBoundsException {
        this(moddedBlock,clientSideBlock,registry,DEFAULT_FILTER,DEFAULT_ON_FIRST_REGISTER);
    }

    /**
     *
     * @param moddedBlock the block this poly represents
     * @param clientSideBlock the block used to display this block on the client
     * @param registry registry used to register this poly
     * @param filter limits the blockstates that can be used. A blockstate can only be used if {@link Predicate#test(Object)} returns true. A blockstate that was rejected can't be used anymore, even when using a different filter. It is advised to use the same filter per block.
     * @param onFirstRegister this will be called if the clientSideBlock is first used. Useful for registering a poly for it.
     * @throws OutOfBoundsException when the clientSideBlock doesn't have any more BlockStates left.
     */
    public UnusedBlockStatePoly(Block moddedBlock, Block clientSideBlock, PolyRegistry registry, Predicate<BlockState> filter, BiConsumer<Block,PolyRegistry> onFirstRegister) throws OutOfBoundsException {
        BlockStateManager manager = registry.getBlockStateManager();

        ImmutableList<BlockState> moddedStates = moddedBlock.getStateManager().getStates();
        if (!manager.isAvailable(clientSideBlock,moddedStates.size(), filter)) {
            throw new OutOfBoundsException(clientSideBlock.getTranslationKey()+" doesn't have enough blockstates left for "+moddedBlock.getTranslationKey());
        }

        HashMap<BlockState,BlockState> res = new HashMap<>();
        for (BlockState state : moddedStates) {
            res.put(state,manager.requestBlockState(clientSideBlock,registry, filter, onFirstRegister));
        }
        states = ImmutableMap.copyOf(res);
    }

    /**
     *
     * @param moddedBlock the block this poly represents
     * @param clientSideBlocks the blocks used to display this block on the client
     * @param registry registry used to register this poly
     * @param filter limits the blockstates that can be used. A blockstate can only be used if {@link Predicate#test(Object)} returns true. A blockstate that was rejected can't be used anymore, even when using a different filter. It is advised to use the same filter per block.
     * @param onFirstRegister this will be called if the clientSideBlock is first used. Useful for registering a poly for it.
     * @throws OutOfBoundsException when the clientSideBlock doesn't have any more BlockStates left.
     */
    public UnusedBlockStatePoly(Block moddedBlock, Block[] clientSideBlocks, PolyRegistry registry, Predicate<BlockState> filter, BiConsumer<Block,PolyRegistry> onFirstRegister) throws OutOfBoundsException {
        BlockStateManager manager = registry.getBlockStateManager();

        ImmutableList<BlockState> moddedStates = moddedBlock.getStateManager().getStates();
        if (!manager.isAvailable(clientSideBlocks,moddedStates.size(), filter)) {
            throw new OutOfBoundsException(clientSideBlocks[clientSideBlocks.length-1].getTranslationKey()+" doesn't have enough blockstates left for "+moddedBlock.getTranslationKey()+" even after checking others");
        }

        HashMap<BlockState,BlockState> res = new HashMap<>();
        for (BlockState state : moddedStates) {
            res.put(state,manager.requestBlockState(clientSideBlocks,registry, filter, onFirstRegister));
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
        InputStreamReader blockStateReader = pack.getAssetFromMod(moddedBlockId.getNamespace(),ResourcePackMaker.BLOCKSTATES+moddedBlockId.getPath()+".json");
        JsonBlockstate originalBlockStates = pack.getGson().fromJson(new JsonReader(blockStateReader),JsonBlockstate.class);
        Map<String, JsonElement> parsedOriginalVariants = new HashMap<>();
        originalBlockStates.variants.forEach((string, element) -> {
            parsedOriginalVariants.put(string.replace(" ",""),element);
        });

        states.forEach((moddedState,clientState) -> {
            Identifier clientBlockId = Registry.BLOCK.getId(clientState.getBlock());
            JsonBlockstate clientBlockStates = pack.getOrCreateBlockState(clientBlockId);
            String clientStateString = Util.getPropertiesFromBlockState(clientState);
            String moddedStateString = Util.getPropertiesFromBlockState(moddedState);

            JsonElement moddedVariants = parsedOriginalVariants.get(moddedStateString);
            clientBlockStates.variants.put(clientStateString, moddedVariants);
            for (JsonBlockstate.Variant v :JsonBlockstate.getVariants(moddedVariants)) {
                Identifier vId = Identifier.tryParse(v.model);
                if (vId != null) pack.copyModel(new Identifier(v.model));
            }
        });

//        Identifier modBlockId = Registry.BLOCK.getId(block);
//        Set<Block> uniqueBlocks = new ReferenceArraySet<>();
//        states.forEach((a,b) -> uniqueBlocks.add(b.getBlock()));
//        InputStreamReader blockStateReader = pack.getAssetFromMod(modBlockId.getNamespace(),ResourcePackMaker.BLOCKSTATES+modBlockId.getPath()+".json");
//        if (blockStateReader == null) {
//            PolyMc.LOGGER.warn("Couldn't get blockstate file for: " + block.getTranslationKey());
//            return;
//        }
//        JsonBlockstate originalBlockStates = pack.getGson().fromJson(new JsonReader(blockStateReader),JsonBlockstate.class);
//
//        for (Block clientBlock : uniqueBlocks) {
//            Identifier clientBlockId = Registry.BLOCK.getId(clientBlock);
//            JsonBlockstate newBlockStates = pack.getOrCreateBlockState(clientBlockId);
//
//            //paste all of the blockstates from the original block into an entry for the clientside block
//            originalBlockStates.variants.forEach((stateString,variant) -> {
//                BlockState state = Util.getBlockStateFromString(block,stateString);
//                BlockState clientState = states.get(state);
//                String clientStateString = Util.getPropertiesFromBlockState(clientState);
//
//                newBlockStates.variants.put(clientStateString,variant);
//            });
//
//            //make sure all the models are present
//            newBlockStates.variants.forEach((stateString,rawVariant) -> {
//                JsonBlockstate.Variant[] variants = JsonBlockstate.getVariants(rawVariant);
//                for (JsonBlockstate.Variant variant : variants) {
//                    if (variant.model != null && !variant.model.isEmpty()) {
//                        pack.copyModel(new Identifier(variant.model));
//                    }
//                }
//            });
//        }
    }

    @Override
    public String getDebugInfo(Block obj) {
        StringBuilder out = new StringBuilder();
        out.append(states.size()).append(" states");
        states.forEach((moddedState,clientState) -> {
            out.append("\n");
            out.append("    #");
            out.append(moddedState);
            out.append(" -> ");
            out.append(clientState);
        });
        return out.toString();
    }
}
