package io.github.theepicblock.polymc.impl.poly.wizard;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.List;

public class PolyMapFilteredPlayerView extends AbstractPacketConsumer {
    private final List<ServerPlayerEntity> allPlayers;
    private final PolyMap filter;

    public PolyMapFilteredPlayerView(List<ServerPlayerEntity> allPlayers, PolyMap filter) {
        this.allPlayers = allPlayers;
        this.filter = filter;
    }

    @Override
    public void sendPacket(Packet<?> packet) {
        for (ServerPlayerEntity player : allPlayers) {
            if (PolyMapProvider.getPolyMap(player) == filter) {
                player.networkHandler.sendPacket(packet);
            }
        }
    }

    public static List<ServerPlayerEntity> getAll(ServerWorld world, BlockPos pos) {
        return getAll(world, new ChunkPos(pos));
    }

    public static List<ServerPlayerEntity> getAll(ServerWorld world, ChunkPos pos) {
        return world.getChunkManager().threadedAnvilChunkStorage.getPlayersWatchingChunk(pos, false);
    }
}
