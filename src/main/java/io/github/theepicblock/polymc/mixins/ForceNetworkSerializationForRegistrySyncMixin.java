package io.github.theepicblock.polymc.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.VersionedIdentifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.SynchronizeRegistriesTask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.function.Consumer;

@Mixin(ServerConfigurationNetworkHandler.class)
public abstract class ForceNetworkSerializationForRegistrySyncMixin extends ServerCommonNetworkHandler {
    public ForceNetworkSerializationForRegistrySyncMixin(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) {
        super(server, connection, clientData);
    }

    @WrapOperation(method = "onSelectKnownPacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/SynchronizeRegistriesTask;onSelectKnownPacks(Ljava/util/List;Ljava/util/function/Consumer;)V"))
    private void wrapWithContext(SynchronizeRegistriesTask instance, List<VersionedIdentifier> clientKnownPacks, Consumer<Packet<?>> sender, Operation<Void> original) {
        PacketContext.runWithContext(this, () -> {
            original.call(instance, clientKnownPacks, sender);
        });
    }
}
