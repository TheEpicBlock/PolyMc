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

import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.block.BlockStateManager;
import io.github.theepicblock.polymc.api.block.BlockStateProfile;
import io.github.theepicblock.polymc.api.resource.JsonBlockState;
import io.github.theepicblock.polymc.api.resource.ResourcePackMaker;
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.InputStreamReader;
import java.util.List;

@SuppressWarnings("PointlessBooleanExpression")
public class PoweredStateBlockPoly extends PropertyRetainingReplacementPoly{
    public PoweredStateBlockPoly(PolyRegistry registry, BlockStateProfile profile) throws BlockStateManager.StateLimitReachedException {
        super(getUnused(profile, registry));
    }

    private static Block getUnused(BlockStateProfile profile, PolyRegistry registry) throws BlockStateManager.StateLimitReachedException {
        BlockStateManager stateManager = registry.getBlockStateManager();
        if (profile.blocks.length == 0) throw new IllegalArgumentException("profile "+profile.name+" contains no blocks");
        int requiredStates = profile.blocks[0].getStateManager().getStates().size()/2; //divide by two to nullify the powered property

        List<BlockState> blockStates = stateManager.requestBlockStates(profile, requiredStates);

        return blockStates.get(0).getBlock();
    }

    @Override
    public BlockState getClientBlock(BlockState input) {
        return super.getClientBlock(input.with(Properties.POWERED, true));
    }

    @Override
    public void AddToResourcePack(Block block, ResourcePackMaker pack) {
        Identifier moddedBlockId = Registry.BLOCK.getId(block);
        InputStreamReader blockStateReader = pack.getAsset(moddedBlockId.getNamespace(), ResourcePackMaker.BLOCKSTATES + moddedBlockId.getPath() + ".json");
        JsonBlockState moddedBlockStates = pack.getGson().fromJson(new JsonReader(blockStateReader), JsonBlockState.class);

        Identifier clientBlockId = Registry.BLOCK.getId(this.clientBlock);
        JsonBlockState clientBlockStates = pack.getOrDefaultPendingBlockState(clientBlockId);

        clientBlock.getStateManager().getStates().stream().filter(state -> state.get(Properties.POWERED) == true).forEach((poweredState) -> {
            String clientStateString = Util.getPropertiesFromBlockState(poweredState);

            JsonElement moddedVariants = moddedBlockStates.getVariantBestMatching(poweredState);
            if (moddedVariants == null) pack.getLogger().warn("Couldn't get blockstate definition for "+poweredState);
            clientBlockStates.variants.put(clientStateString, moddedVariants);

            for (JsonBlockState.Variant v : JsonBlockState.getVariantsFromJsonElement(moddedVariants)) {
                Identifier vId = Identifier.tryParse(v.model);
                if (vId != null) pack.copyModel(new Identifier(v.model));
            }
        });
    }
}
