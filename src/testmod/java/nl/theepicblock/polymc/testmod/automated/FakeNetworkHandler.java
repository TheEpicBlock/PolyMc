package nl.theepicblock.polymc.testmod.automated;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ChunkDataSender;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;

public class FakeNetworkHandler extends ServerPlayNetworkHandler {
    public ArrayList<Packet<?>> sentPackets = new ArrayList<>();

    public FakeNetworkHandler(MinecraftServer server, ServerPlayerEntity player) {
        super(server, new FakeClientConnection(), player, ConnectedClientData.createDefault(player.getGameProfile()));
        try {
            var field = this.getClass().getField("chunkDataSender");
            field.setAccessible(true);
            field.set(this, new FakeDataSender(true));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void send(Packet<?> packet, @Nullable PacketCallbacks callbacks) {
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
            packet.write(bytebuf);
        });
        PacketContext.clearContext();
        var id = NetworkState.PLAY.getHandler(NetworkSide.CLIENTBOUND).getId(packet);
        if (id == -1) {
            throw new UnsupportedOperationException("Can't find packet id of "+packet.getClass() + ". Is it not clientbound?");
        }
        var reconstructedPacket = NetworkState.PLAY.getHandler(NetworkSide.CLIENTBOUND).createPacket(id, bytebuf);

        return (T)reconstructedPacket;
    }

    private static final class FakeClientConnection extends ClientConnection {
        private FakeClientConnection() {
            super(NetworkSide.CLIENTBOUND);
        }

        @Override
        public void setPacketListener(PacketListener packetListener) {
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
