package io.github.theepicblock.polymc.impl.poly.wizard;

import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayerEntity;

public class SinglePlayerView extends AbstractPacketConsumer {
    private final ServerPlayerEntity player;

    public SinglePlayerView(ServerPlayerEntity player) {
        this.player = player;
    }

    @Override
    public void sendPacket(Packet<?> packet) {
        player.networkHandler.sendPacket(packet);
    }
}
