package io.github.theepicblock.polymc.api.wizard;

import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.Consumer;

public interface PlayerView {
    default void sendPacket(Packet<?> packet) {
        this.forEach(player -> player.networkHandler.sendPacket(packet));
    }

    void forEach(Consumer<ServerPlayerEntity> consumer);
}
