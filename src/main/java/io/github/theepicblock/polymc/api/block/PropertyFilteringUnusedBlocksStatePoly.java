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
import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.Util;
import io.github.theepicblock.polymc.api.OutOfBoundsException;
import io.github.theepicblock.polymc.api.register.BlockStateManager;
import io.github.theepicblock.polymc.api.register.PolyRegistry;
import io.github.theepicblock.polymc.resource.JsonBlockState;
import io.github.theepicblock.polymc.resource.ResourcePackMaker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class PropertyFilteringUnusedBlocksStatePoly implements BlockPoly {
    private final ImmutableMap<BlockState,BlockState> states;
    private final Function<BlockState, BlockState> filter;

    /**
     * @param moddedBlock     the block this poly represents
     * @param stateProfile    the profile to use.
     * @param registry        registry used to register this poly
     * @param filter          function that should remove all blockstates that you want to filter
     * @throws OutOfBoundsException when the clientSideBlock doesn't have any more BlockStates left.
     */
    public PropertyFilteringUnusedBlocksStatePoly(Block moddedBlock, PolyRegistry registry, BlockStateProfile stateProfile, List<Property<?>> filter) throws OutOfBoundsException {
        this(moddedBlock, registry, stateProfile, (Property<?>[])filter.toArray());
    }

    /**
     * @param moddedBlock     the block this poly represents
     * @param stateProfile    the profile to use.
     * @param registry        registry used to register this poly
     * @param filter          function that should remove all blockstates that you want to filter
     * @throws OutOfBoundsException when the clientSideBlock doesn't have any more BlockStates left.
     */
    public PropertyFilteringUnusedBlocksStatePoly(Block moddedBlock, PolyRegistry registry, BlockStateProfile stateProfile, Property<?>[] filter) throws OutOfBoundsException {
        Collection<Property<?>> moddedProperties = moddedBlock.getStateManager().getProperties();
        for (Property<?> p : filter) {
            if (!moddedProperties.contains(p)) {
                throw new IllegalArgumentException(String.format("[%s]: %s doesn't have property %s", this.getClass().getName(), moddedBlock.getTranslationKey(), p.getName()));
            }
        }
        Object[] defaultValues = new Object[filter.length];
        int i = 0;
        for (Property<?> p : filter) {
            i++;
            defaultValues[i] = (moddedBlock.getDefaultState().get(p));
        }

        Function<BlockState, BlockState> filterFunction = (blockstate) -> {
            int i2 = 0;
            for (Property<?> p : filter) {
                i2++;
                blockstate = with(blockstate, p, defaultValues[i2]);
            }
            return blockstate;
        };

        states = getBlockStateMap(moddedBlock, registry, stateProfile, filterFunction);
        this.filter = filterFunction;
    }

    @SuppressWarnings("unchecked")
    private <T extends Comparable<T>> BlockState with(BlockState b, Property<T> property, Object value) {
        return b.with(property, (T)value);
    }

    /**
     * @param moddedBlock     the block this poly represents
     * @param stateProfile    the profile to use.
     * @param registry        registry used to register this poly
     * @param filter          function that should remove all blockstates that you want to filter
     * @throws OutOfBoundsException when the clientSideBlock doesn't have any more BlockStates left.
     */
    public PropertyFilteringUnusedBlocksStatePoly(Block moddedBlock, PolyRegistry registry, BlockStateProfile stateProfile, Function<BlockState, BlockState> filter) throws OutOfBoundsException {
        states = getBlockStateMap(moddedBlock, registry, stateProfile, filter);
        this.filter = filter;
    }

    private ImmutableMap<BlockState,BlockState> getBlockStateMap(Block moddedBlock, PolyRegistry registry, BlockStateProfile stateProfile, Function<BlockState,BlockState> filter) throws OutOfBoundsException {
        ImmutableMap<BlockState,BlockState> states;
        BlockStateManager manager = registry.getBlockStateManager();

        ImmutableList<BlockState> unFilteredModdedStates = moddedBlock.getStateManager().getStates();

        BlockState[] moddedStates = (BlockState[])unFilteredModdedStates.stream().map(filter).toArray();

        if (!manager.isAvailable(stateProfile, moddedStates.length)) {
            throw new OutOfBoundsException("Block doesn't have enough blockstates left. Profile: '"+stateProfile.name+"'");
        }

        HashMap<BlockState,BlockState> res = new HashMap<>();
        for (BlockState state : moddedStates) {
            res.put(state, manager.requestBlockState(stateProfile));
        }
        states = ImmutableMap.copyOf(res);
        return states;
    }

    @Override
    public BlockState getClientBlock(BlockState input) {
        return states.get(filter.apply(input));
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

            JsonElement moddedVariants = moddedBlockStates.get(moddedState);
            if (moddedVariants == null) PolyMc.LOGGER.warn("Couldn't get blockstate definition for "+moddedState);
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
