package io.github.theepicblock.polymc.api.wizard;

import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.ApiStatus;

public interface PacketConsumer {
    void sendPacket(Packet<?> packet);

    @ApiStatus.Internal
    void sendDeathPacket(int id);

    @ApiStatus.Internal
    void sendBatched();
}
