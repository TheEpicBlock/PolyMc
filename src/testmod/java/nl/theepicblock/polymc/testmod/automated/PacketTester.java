package nl.theepicblock.polymc.testmod.automated;

import com.mojang.authlib.GameProfile;
import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.network.ChunkFilter;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PacketTester implements Closeable {
    private final ServerPlayerEntity playerEntity;
    private final FakeNetworkHandler fakeNetworkHandler;
    private final TestContext context;

    public PacketTester(TestContext context) {
        var world = context.getWorld();
        // Mock a player entity
        var profile = new GameProfile(UUID.randomUUID(), "Fake packet receiver");
        this.playerEntity = new ServerPlayerEntity(world.getServer(), world, profile, SyncedClientOptions.createDefault());
        this.playerEntity.setChunkFilter(new ChunkFilter.Cylindrical(new ChunkPos(context.getAbsolutePos(BlockPos.ORIGIN)), 5));
        this.fakeNetworkHandler = new FakeNetworkHandler(world.getServer(), this.playerEntity);
        this.context = context;

        // Since the regular fabric events don't get called on this
        PolyMapProvider.get(fakeNetworkHandler).refreshUsedPolyMap();

        world.spawnEntity(playerEntity);
        world.getChunkManager().threadedAnvilChunkStorage.updatePosition(playerEntity);
        this.playerEntity.setPosition(context.getAbsolute(Vec3d.ZERO));
        world.getChunkManager().threadedAnvilChunkStorage.updatePosition(playerEntity);
    }

    public <T extends Packet<?>> T reencode(T packet) {
        return this.fakeNetworkHandler.reencode(packet);
    }

    public void setMap(PolyMap map) {
        PolyMapProvider.get(this.playerEntity).setPolyMap(map);
    }

    public void clearPackets() {
        this.fakeNetworkHandler.sentPackets.clear();
    }

    /**
     * Finds any packets of a certain type that have been sent recently. Will error if there isn't exactly one packet found.
     */
    public <T extends Packet<?>> T getFirstOfType(Class<T> packetType) {
        this.context.assertTrue(!this.fakeNetworkHandler.sentPackets.isEmpty(), String.format("Expected one packet of type %s, but no packet of any type has been received", packetType));
        var packets = this.fakeNetworkHandler.sentPackets
                .stream()
                .filter(packet -> packet.getClass() == packetType)
                .map(packet -> (T)packet)
                .toList();
        this.context.assertTrue(packets.size() == 1, String.format("Expected one packet of type %s, found %d", packetType, packets.size()));
        return packets.get(0);
    }

    /**
     * Does the same as {@link #getFirstOfType(Class)}, but is limited to the scope of the Runnable
     */
    public <T extends Packet<?>> T capture(Class<T> packetType, Runnable run) {
        this.clearPackets();
        run.run();
        return getFirstOfType(packetType);
    }

    public List<Packet<?>> captureAll(Runnable run) {
        this.clearPackets();
        run.run();
        return new ArrayList<>(this.fakeNetworkHandler.sentPackets);
    }

    public void assertReceived(Packet<?> packet, String message) {
        this.context.assertTrue(this.fakeNetworkHandler.sentPackets.contains(packet), message);
    }

    public TestContext getTestContext() {
        return context;
    }

    @Override
    public void close() {
        this.playerEntity.remove(Entity.RemovalReason.KILLED);
    }
}
