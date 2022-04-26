package io.github.theepicblock.polymc.mixins.compat.immersive_portals;

import io.github.theepicblock.polymc.impl.mixin.PlayerContextContainer;
import io.github.theepicblock.polymc.mixins.CustomPacketAccessor;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import qouteall.imm_ptl.core.platform_specific.IPNetworking;
import qouteall.q_misc_util.dimension.DimId;

@Mixin(value = ServerPlayNetworkHandler.class, priority = 1010) // Inject after MixinServerGamePacketListenerImpl_E
public abstract class IPPacketRewriter {
    @Shadow public abstract ServerPlayerEntity getPlayer();

    @Shadow public abstract ClientConnection getConnection();

    private static final int CHUNK_DATA_PACKET = NetworkState.PLAY.getPacketIdToPacketMap(NetworkSide.CLIENTBOUND)
            .int2ObjectEntrySet()
            .stream()
            .filter(entry -> entry.getValue().equals(ChunkDataS2CPacket.class))
            .findFirst()
            .orElseThrow()
            .getIntKey();

    @ModifyVariable(
            method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Packet<?> modifyPacket(Packet<?> originalPacket) {
        if (originalPacket instanceof CustomPayloadS2CPacket packet) {
            var id = ((CustomPacketAccessor)packet).getChannel();
            if (id.equals(IPNetworking.id_stcRedirected)) {
                // Add the context and reserialize the packet
                var buf = packet.getData();
                var worldId = DimId.readWorldId(buf, false);
                var packetId = buf.readInt();
                if (packetId == CHUNK_DATA_PACKET) {
                    // We ain't deserializing that
                    buf.resetReaderIndex();
                    return originalPacket;
                }

                var innerPacket = NetworkState.PLAY.getPacketHandler(NetworkSide.CLIENTBOUND, packetId, buf);
                if (innerPacket == null) {
                    throw new NullPointerException("PolyMc tried to transform an invalid immersive portals redirect packet");
                }
                if (innerPacket instanceof PlayerContextContainer container) {
                    container.setPolyMcProvidedPlayer(this.getPlayer());
                }

                // Reserialize the packet
                return IPNetworking.createRedirectedMessage(worldId, innerPacket);
            }
        }
        return originalPacket;
    }
}
