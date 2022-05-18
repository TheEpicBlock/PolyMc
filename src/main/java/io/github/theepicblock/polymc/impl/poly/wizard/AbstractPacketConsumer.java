package io.github.theepicblock.polymc.impl.poly.wizard;

import io.github.theepicblock.polymc.api.wizard.PacketConsumer;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;

public abstract class AbstractPacketConsumer implements PacketConsumer {
    private final IntList deadEntities = new IntArrayList();

    @Override
    public void sendDeathPacket(int id) {
        deadEntities.add(id);
    }

    @Override
    public void sendBatched() {
        if (!deadEntities.isEmpty()) {
            this.sendPacket(new EntitiesDestroyS2CPacket(deadEntities)); // TODO once we start doing unsafe hacks, we can avoid copyi
        }
    }
}
