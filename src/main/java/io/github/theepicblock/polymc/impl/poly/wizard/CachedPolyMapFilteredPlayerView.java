package io.github.theepicblock.polymc.impl.poly.wizard;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.api.wizard.PlayerView;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CachedPolyMapFilteredPlayerView implements PlayerView {
    private final List<ServerPlayerEntity> players;

    public CachedPolyMapFilteredPlayerView(List<ServerPlayerEntity> allPlayers, PolyMap filter) {
        players = new ArrayList<>();
        allPlayers.forEach(player -> {
            if (PolyMapProvider.getPolyMap(player) == filter) {
                players.add(player);
            }
        });
    }

    @Override
    public void forEach(Consumer<ServerPlayerEntity> consumer) {
        for (var player : players) {
            consumer.accept(player);
        }
    }
}
