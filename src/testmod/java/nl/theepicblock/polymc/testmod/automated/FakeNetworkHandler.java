package nl.theepicblock.polymc.testmod.automated;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.io.IOException;
import java.util.ArrayList;

public class FakeNetworkHandler extends ServerPlayNetworkHandler {
    public ArrayList<Packet<?>> sentPackets = new ArrayList<>();

    public FakeNetworkHandler(MinecraftServer server, ServerPlayerEntity player) {
        super(server, new ClientConnection(NetworkSide.CLIENTBOUND), player);
    }

    @Override
    public void sendPacket(Packet<?> packet, @Nullable PacketCallbacks callbacks) {
        // reserialize packet
        this.sentPackets.add(reencode(packet));
    }

    public <T extends Packet<?>> T reencode(T packet) {
        var bytebuf = PacketByteBufs.create();
        try {
            PacketContext.writeWithContext(packet, bytebuf, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        var id = NetworkState.PLAY.getPacketId(NetworkSide.CLIENTBOUND, packet);
        var reconstructedPacket = NetworkState.PLAY.getPacketHandler(NetworkSide.CLIENTBOUND, id, bytebuf);

        return (T)reconstructedPacket;
    }
}
