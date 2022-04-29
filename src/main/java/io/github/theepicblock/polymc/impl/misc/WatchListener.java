package io.github.theepicblock.polymc.impl.misc;

import net.minecraft.server.network.ServerPlayerEntity;

public interface WatchListener {
    void polymc$addPlayer(ServerPlayerEntity playerEntity);

    void polymc$removePlayer(ServerPlayerEntity playerEntity);

    void polymc$removeAllPlayers();
}
