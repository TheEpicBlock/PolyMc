package nl.theepicblock.polymc.testmod.automated;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.*;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.state.PlayStateFactories;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ChunkDataSender;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;

public class FakeNetworkHandler extends ServerPlayNetworkHandler {
    public ArrayList<Packet<?>> sentPackets = new ArrayList<>();
    private final NetworkState<?> state;

    public FakeNetworkHandler(MinecraftServer server, ServerPlayerEntity player) {
        super(server, new FakeClientConnection(), player, ConnectedClientData.createDefault(player.getGameProfile(), false));
        try {
            var field = this.getClass().getField("chunkDataSender");
            field.setAccessible(true);
            field.set(this, new FakeDataSender(true));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        state = PlayStateFactories.S2C.bind(RegistryByteBuf.makeFactory(this.server.getRegistryManager()));
    }

    @Override
    public void send(Packet<?> packet, PacketCallbacks callbacks) {
        if (packet instanceof BundleS2CPacket bundle) {
            for (var packet2 : bundle.getPackets()) {
                this.send(packet2, callbacks);
            }
            return;
        }

        // reserialize packet
        this.sentPackets.add(reencode(packet));
    }

    public <T extends Packet<?>> T reencode(T packet) {
        if (packet instanceof BundleS2CPacket) {
            throw new IllegalArgumentException("Can't reencode bundles as of now");
        }

        var bytebuf = PacketByteBufs.create();
        // TODO not use internal stuff here
        PacketContext.setContext(this.connection, packet);
        PacketContext.runWithContext(this, packet, () -> {
            state.codec().encode(bytebuf, (Packet<? super PacketListener>)packet);
        });
        var reconstructedPacket = state.codec().decode(bytebuf);

        return (T)reconstructedPacket;
    }

    private static final class FakeClientConnection extends ClientConnection {
        private FakeClientConnection() {
            super(NetworkSide.CLIENTBOUND);
        }
    }

    private static final class FakeDataSender extends ChunkDataSender {
        public FakeDataSender(boolean local) {
            super(local);
        }

        public boolean isInNextBatch(long chunkPos) {
            return false;
        }
    }
}
