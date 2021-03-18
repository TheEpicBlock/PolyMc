package io.github.theepicblock.polymc.impl.misc;

import net.minecraft.server.network.ServerPlayerEntity;

public interface WatchListener {
    void addPlayer(ServerPlayerEntity playerEntity);
    void removePlayer(ServerPlayerEntity playerEntity);
}
