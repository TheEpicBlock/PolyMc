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
package io.github.theepicblock.polymc.mixins.block;

import io.github.theepicblock.polymc.impl.ConfigManager;
import net.minecraft.block.Block;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Synced block events are called on the server, but executed on the client.
 * But with PolyMc the client often doesn't have enough information to process this event.
 * This code let's users of PolyMc make certain blocks be calculated on the server instead of the client.
 * See: config; misc.processSyncedBlockEventServerSide
 */
@Mixin(ServerWorld.class)
public class ProcessSyncedBlockEventServerSideImplementation {
    @Unique
    private final List<Block> serverCalculatedBlockEvents = new ArrayList<>();

    @Inject(method = "<init>", at = @At("TAIL"))
    public void initInject(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey worldKey, RegistryEntry registryEntry, WorldGenerationProgressListener worldGenerationProgressListener, ChunkGenerator chunkGenerator, boolean debugWorld, long seed, List spawners, boolean shouldTickTime, CallbackInfo ci) {
        List<String> serverCalculatedBlockEventsAsString = ConfigManager.getConfig().misc.getProcessSyncedBlockEventServerSide();
        for (String s : serverCalculatedBlockEventsAsString) {
            Block e = Registry.BLOCK.get(new Identifier(s));
            serverCalculatedBlockEvents.add(e);
        }
    }

    @Inject(method = "addSyncedBlockEvent(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;II)V", at = @At("HEAD"), cancellable = true)
    public void addSyncedBlockEventInject(BlockPos pos, Block block, int type, int data, CallbackInfo ci) {
        //if the events for this block should be processed serverside, execute it immediately. Instead of adding it to a queue to be sent to the client.
        if (serverCalculatedBlockEvents.contains(block)) {
            ((ServerWorld)(Object)this).getBlockState(pos).onSyncedBlockEvent(((ServerWorld)(Object)this), pos, type, data);
            ci.cancel();
        }
    }
}
