package io.github.theepicblock.polymc.mixins.compat.immersive_portals;

import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.mixins.CustomPacketAccessor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.MobSpawnS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.platform_specific.IPNetworking;
import qouteall.q_misc_util.dimension.DimId;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class DisableCustomEntities {
    private static final int ENTITY_SPAWN_PACKET = NetworkState.PLAY.getPacketIdToPacketMap(NetworkSide.CLIENTBOUND)
            .int2ObjectEntrySet()
            .stream()
            .filter(entry -> entry.getValue().equals(EntitySpawnS2CPacket.class))
            .findFirst()
            .orElseThrow()
            .getIntKey();
    private static final int MOB_SPAWN_PACKET = NetworkState.PLAY.getPacketIdToPacketMap(NetworkSide.CLIENTBOUND)
            .int2ObjectEntrySet()
            .stream()
            .filter(entry -> entry.getValue().equals(MobSpawnS2CPacket.class))
            .findFirst()
            .orElseThrow()
            .getIntKey();

    @Shadow public abstract ServerPlayerEntity getPlayer();

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("HEAD"), cancellable = true)
    private void sendPacketInject(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> listener, CallbackInfo ci) {
        if (packet instanceof CustomPayloadS2CPacket cPacket) {
            if (((CustomPacketAccessor)packet).getChannel().equals(IPNetworking.id_stcSpawnEntity) &&
                    Util.isPolyMapVanillaLike(this.getPlayer())) {
                ci.cancel();
            }
            if (((CustomPacketAccessor)packet).getChannel().equals(IPNetworking.id_stcRedirected)) {
                var buf = cPacket.getData();
                var worldId = DimId.readWorldId(buf, false);
                var packetId = buf.readInt();
                if (packetId == ENTITY_SPAWN_PACKET || packetId == MOB_SPAWN_PACKET) {
                    var polyMap = PolyMapProvider.getPolyMap(this.getPlayer());
                    var entityId = buf.readVarInt();
                    var uuid = buf.readUuid();
                    var entityType = Registry.ENTITY_TYPE.get(buf.readVarInt());
                    buf.resetReaderIndex();

                    if (polyMap.getEntityPoly(entityType) != null) {
                        ci.cancel();
                    }
                }

            }

        }
    }
}
