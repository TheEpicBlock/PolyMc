package io.github.theepicblock.polymc.impl.poly.wizard;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class CachedPolyMapFilteredPlayerView extends AbstractPacketConsumer {
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
    public void sendPacket(Packet<?> packet) {
        for (var player : players) {
            player.networkHandler.sendPacket(packet);
        }
    }
}
