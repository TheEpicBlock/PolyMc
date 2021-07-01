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
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Spawner;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
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
    private final List<Block> ServerCalculatedBlockEvents = new ArrayList<>();

    @Inject(method = "<init>", at = @At("TAIL"))
    public void InitInject(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey<World> registryKey, DimensionType dimensionType, WorldGenerationProgressListener worldGenerationProgressListener, ChunkGenerator chunkGenerator, boolean bl, long l, List<Spawner> list, boolean bl2, CallbackInfo ci) {
        List<String> ServerCalculatedBlockEventsAsString = ConfigManager.getConfig().misc.getProcessSyncedBlockEventServerSide();
        for (String s : ServerCalculatedBlockEventsAsString) {
            Block e = Registry.BLOCK.get(new Identifier(s));
            ServerCalculatedBlockEvents.add(e);
        }
    }

    @Inject(method = "addSyncedBlockEvent(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;II)V", at = @At("HEAD"), cancellable = true)
    public void addSyncedBlockEventInject(BlockPos pos, Block block, int type, int data, CallbackInfo ci) {
        //if the events for this block should be processed serverside, execute it immediately. Instead of adding it to a queue to be sent to the client.
        if (ServerCalculatedBlockEvents.contains(block)) {
            ((ServerWorld)(Object)this).getBlockState(pos).onSyncedBlockEvent(((ServerWorld)(Object)this), pos, type, data);
            ci.cancel();
        }
    }
}
