package io.github.theepicblock.polymc.impl.poly.wizard;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.api.wizard.PacketConsumer;
import io.github.theepicblock.polymc.impl.ConfigManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

@ApiStatus.Experimental
public class PacketCountManager {
    public final static PacketCountManager INSTANCE = new PacketCountManager();
    /**
     * Amount of ticks of packet count history that will be recorded
     */
    private final static int PACKET_HISTORY_SIZE = 40;
    private final static int ADJUSTMENT_COOLDOWN = 20;
    public final static int MAX_PACKETS = ConfigManager.getConfig().maxPacketsPerSecond/20;
    public final static int MIN_PACKETS = MAX_PACKETS - 5; // The minimum amount of packets before we start relaxing our restrictions
    public final static int MAX_RESTRICTION = 11;
    private final Map<ServerPlayerEntity, PlayerInfo> playerTrackers = new HashMap<>();
    /**
     * This provides an index into {@link PlayerInfo#packetCountHistory} into which we are currently reading.
     * This will be increased in every call of {@link #adjust(int)}
     */
    private int cursor;
    private int watchDistance = 0;
    private int watchRadius = 0;
    private final ThreadLocal<TrackingPacketConsumer> reusableConsumer = ThreadLocal.withInitial(TrackingPacketConsumer::new);

    public static void registerEvents() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof ServerPlayerEntity player) INSTANCE.onPlayerLoad(player);
        });
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (entity instanceof ServerPlayerEntity player) INSTANCE.onPlayerUnload(player);
        });
    }

    private void onPlayerLoad(ServerPlayerEntity player) {
        playerTrackers.put(player, new PlayerInfo());
    }

    private void onPlayerUnload(ServerPlayerEntity player) {
        playerTrackers.remove(player);
    }

    public void adjust(int tick) {
        var nextCursor = cursor+1;
        if (nextCursor == PACKET_HISTORY_SIZE) nextCursor = 0;

        for (var tracker : playerTrackers.values()) {

            if (tick % ADJUSTMENT_COOLDOWN == 0) {
                var average = tracker.calculateAveragePacketCount();
                if (average > MAX_PACKETS) {
                    tracker.increaseRestrictions();
                } else if (average < MIN_PACKETS) {
                    tracker.decreaseRestrictions();
                }
            }

            // Clear the next history field in preparation of the next tick
            tracker.packetCountHistory[nextCursor] = 0;
        }
        this.cursor = nextCursor;
    }

    public void updateWatchRadius(int watchDistance) {
        this.watchDistance = watchDistance;
        this.watchRadius = getWatchRadiusFromDistance(watchDistance);
    }

    public static int getWatchRadiusFromDistance(int watchDistance) {
        return (watchDistance+3)*16;
    }

    public PacketConsumer getView(Set<PlayerAssociatedNetworkHandler> listeners, PolyMap map, Vec3d pos, int tick, int seed) {
        var reusableConsumer = this.reusableConsumer.get();
        reusableConsumer.reset(this.cursor);

        int pSeed = 0;
        for (var listener : listeners) {
            var player = listener.getPlayer();
            if (PolyMapProvider.getPolyMap(player) == map) {
                var info = playerTrackers.get(player);
                // The player might've been unloaded, despite still being a listener for this entity
                if (info == null) continue;
                if (info.shouldSend(player.getPos(), pos, true, tick, seed+(pSeed++), this.watchRadius)) {
                    reusableConsumer.addListener(listener, info);
                }
            }
        }

        return reusableConsumer;
    }

    public PacketConsumer getView(ServerWorld world, ChunkPos pos, PolyMap map, int tick, int seed) {
        var reusableConsumer = this.reusableConsumer.get();
        reusableConsumer.reset(this.cursor);
        var chunkPos = new Vec3d(pos.getCenterX(), 0, pos.getCenterZ());

        int pSeed = 0;
        for (var player : PlayerLookup.world(world)) {

            if (player.getChunkFilter().isWithinDistance(pos) &&
                    PolyMapProvider.getPolyMap(player) == map) {
                var info = playerTrackers.get(player);
                // Just in case the player was unloaded but not yet removed from the world
                if (info == null) continue;
                if (info.shouldSend(player.getPos(), chunkPos, false, tick, seed+(pSeed++), this.watchRadius)) {
                    reusableConsumer.addListener(player.networkHandler, info);
                }
            }
        }

        return reusableConsumer;
    }

    public PlayerInfo getTrackerInfoForPlayer(ServerPlayerEntity player) {
        return playerTrackers.get(player);
    }

    public static class PlayerInfo {
        private final int[] packetCountHistory = new int[PACKET_HISTORY_SIZE];
        private byte restrictionLevel;

        private PlayerInfo() {
            // -1 indicates a missing entry
            Arrays.fill(packetCountHistory, -1);
            this.resetRestrictionLevel();
        }

        public int calculateAveragePacketCount() {
            var cumSum = 0;
            for (int i = 0; i < PACKET_HISTORY_SIZE; i++) {
                if (packetCountHistory[i] != -1) {
                    cumSum += packetCountHistory[i];
                }
            }
            return cumSum/PACKET_HISTORY_SIZE;
        }

        public int[] getPacketHistory() {
            return packetCountHistory;
        }

        public byte getRestrictionLevel() {
            return restrictionLevel;
        }

        protected void increaseRestrictions() {
            this.restrictionLevel = (byte)Math.min(this.restrictionLevel + 1, MAX_RESTRICTION);
        }

        protected void decreaseRestrictions() {
            this.restrictionLevel = (byte)Math.max(this.restrictionLevel - 1, 0);
        }

        public void setRestrictionLevel(byte level) {
            this.restrictionLevel = level;
        }

        public void resetRestrictionLevel() {
            setRestrictionLevel((byte)0);
        }

        /**
         * This is the main method that controls the restricting of packets
         */
        protected boolean shouldSend(Vec3d playerPos, Vec3d wizardPos, boolean isChunk, int tickCount, int seed, int watchRadius) {
            if (restrictionLevel == 0) {
                return true;
            }

            double distance;
            if (isChunk) {
                distance = playerPos.squaredDistanceTo(wizardPos);
            } else {
                var xDiff = Math.abs(playerPos.x-wizardPos.x);
                var zDiff = Math.abs(playerPos.z-wizardPos.z);
                distance = xDiff*xDiff+zDiff*zDiff;
                distance -= 8*8;
            }
            byte distanceLevel;
            if (distance < 8*8) {
                distanceLevel = 0;
            } else if (distance < 20*20) {
                distanceLevel = 1;
            } else if (distance < (watchRadius*0.3)*(watchRadius*0.3)) {
                distanceLevel = 2;
            } else {
                distanceLevel = 3;
            }

            return (tickCount+seed) % switch (restrictionLevel) {
                case 1 -> switch (distanceLevel) {
                    case 0,1,2 -> 1;
                    default -> 2;
                };
                case 2 -> switch (distanceLevel) {
                    case 0,1 -> 1;
                    case 2 -> 2;
                    default -> 4;
                };
                case 3 -> switch (distanceLevel) {
                    case 0,1 -> 1;
                    case 2 -> 3;
                    default -> 5;
                };
                case 4 -> switch (distanceLevel) {
                    case 0,1 -> 1;
                    case 2 -> 5;
                    default -> 7;
                };
                case 5 -> switch (distanceLevel) {
                    case 0 -> 1;
                    case 1 -> 2;
                    case 2 -> 5;
                    default -> 7;
                };
                case 6 -> switch (distanceLevel) {
                    case 0 -> 1;
                    case 1 -> 2;
                    default -> 9;
                };
                case 7 -> switch (distanceLevel) {
                    case 0 -> 1;
                    case 1 -> 2;
                    case 2 -> 7;
                    default -> 9;
                };
                case 8 -> switch (distanceLevel) {
                    case 0 -> 2;
                    case 1 -> 3;
                    case 2 -> 8;
                    default -> 10;
                };
                case 9 -> switch (distanceLevel) {
                    case 0 -> 3;
                    case 1 -> 5;
                    default -> 10;
                };
                case 10 -> switch (distanceLevel) {
                    case 0 -> 6;
                    case 1 -> 7;
                    default -> 10;
                };
                default -> switch (distanceLevel) {
                    case 0 -> 10;
                    case 1 -> 13;
                    case 2 -> 18;
                    default -> 20;
                };
            } == 0;
        }
    }

    public static class TrackingPacketConsumer implements PacketConsumer {
        private final ArrayList<PlayerAssociatedNetworkHandler> listeners = new ArrayList<>();
        private final ArrayList<PlayerInfo> trackers = new ArrayList<>();
        private int cursor = 0;

        public void reset(int cursor) {
            this.cursor = cursor;
            listeners.clear();
            trackers.clear();
        }

        public void addListener(PlayerAssociatedNetworkHandler listener, PlayerInfo tracker) {
            this.listeners.add(listener);
            this.trackers.add(tracker);
        }

        @Override
        public void sendPacket(Packet<?> packet) {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).sendPacket(packet);
                trackers.get(i).packetCountHistory[cursor] += 1;
            }
        }

        @Override
        public void sendDeathPacket(int id) {
            this.sendPacket(new EntitiesDestroyS2CPacket(id));
        }

        @Override
        public void sendBatched() {
            // TODO
        }
    }
}
