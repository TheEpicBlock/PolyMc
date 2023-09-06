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

import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.SharedValuesKey;
import io.github.theepicblock.polymc.api.resource.ModdedResources;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Manages which blockstates are allocated to which polys.
 */
public class BlockStateManager {
    public static final SharedValuesKey<BlockStateManager> KEY = new SharedValuesKey<>(BlockStateManager::new, ResourceContainer::new);

    private final Map<Block, List<BlockState>> availableBlockStates = new HashMap<>();
    private final Map<String, List<BlockState>> modelIds = new HashMap<>();
    private final PolyRegistry polyRegistry;

    public BlockStateManager(PolyRegistry polyRegistry) {
        this.polyRegistry = polyRegistry;
    }

    public BlockState requestBlockState(BlockStateProfile profile) throws StateLimitReachedException {
        try {
            return requestBlockState(profile.filter, profile.blocks, profile.onFirstRegister);
        } catch (StateLimitReachedException e) {
            throw new StateLimitReachedException("No states found in profile: " + profile.name);
        }
    }

    public BlockState requestBlockState(BlockStateProfile profile, @Nullable String modelId) throws StateLimitReachedException {
        try {
            return requestBlockState(profile.filter, profile.blocks, profile.onFirstRegister, modelId);
        } catch (StateLimitReachedException e) {
            throw new StateLimitReachedException("No states found in profile: " + profile.name);
        }
    }

    public BlockState requestBlockState(Predicate<BlockState> blockStatePredicate, Block[] searchSpace, BiConsumer<Block,PolyRegistry> onFirstRegister) throws StateLimitReachedException {
        return requestBlockState(blockStatePredicate, searchSpace, onFirstRegister, null);
    }

    public BlockState requestBlockState(Predicate<BlockState> blockStatePredicate, Block[] searchSpace, BiConsumer<Block,PolyRegistry> onFirstRegister, @Nullable String modelId) throws StateLimitReachedException {
        if (modelId != null && modelIds.containsKey(modelId)) {
            for (var possibleState : modelIds.get(modelId)) {
                if (blockStatePredicate.test(possibleState)) {
                    return possibleState;
                }
            }
        }

        for (var block : searchSpace) {
            var availableStates = availableBlockStates.computeIfAbsent(block, (b) -> {
                onFirstRegister.accept(b, this.polyRegistry);
                return new LinkedList<>(b.getStateManager().getStates().stream().filter(Util::isVanilla).toList());
            });

            // Return first block state that matches `blockStatePredicate`
            var iterator = availableStates.iterator();
            while (iterator.hasNext()) {
                BlockState next = iterator.next();
                if (blockStatePredicate.test(next)) {
                    iterator.remove();

                    if (modelId != null) {
                        modelIds.computeIfAbsent(modelId, (modelIdx) -> new ArrayList<>()).add(next);
                    }

                    return next;
                }
            }
        }
        throw new StateLimitReachedException("No states found in " + Arrays.toString(searchSpace));
    }

    private static class ResourceContainer implements SharedValuesKey.ResourceContainer {
        private final Map<Block, List<BlockState>> availableBlockStates;

        private ResourceContainer(BlockStateManager manager) {
            this.availableBlockStates = manager.availableBlockStates;
        }

        @Override
        public void addToResourcePack(ModdedResources moddedResources, PolyMcResourcePack pack, SimpleLogger logger) {

        }

        @Override
        public List<SharedValuesKey.DebugDumpSection> addDebugSections() {
            return List.of(new SharedValuesKey.DebugDumpSection("BLOCKS (STATE LEFT)", builder -> {
                for (var entry : availableBlockStates.entrySet()) {
                    builder.append("- ");
                    builder.append(Registries.BLOCK.getId(entry.getKey())).append(" - ").append(entry.getValue().size());
                    builder.append("\n");
                }
            }));
        }
    }


    @ApiStatus.Internal
    public Map<Block, List<BlockState>> getAvailableBlockStateMap() {
        return this.availableBlockStates;
    }

    public static class StateLimitReachedException extends Exception {
        public StateLimitReachedException(String s) {
            super(s);
        }
    }
}
