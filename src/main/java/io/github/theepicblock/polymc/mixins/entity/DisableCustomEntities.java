package io.github.theepicblock.polymc.mixins.entity;

import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.impl.mixin.EntityTrackerEntryDuck;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

@Mixin(EntityTrackerEntry.class)
public class DisableCustomEntities {
    @Redirect(method = "startTracking", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/EntityTrackerEntry;sendPackets(Ljava/util/function/Consumer;)V"))
    public void polymc$maybeBlockSpawnPacket(EntityTrackerEntry instance, Consumer<Packet<ClientPlayPacketListener>> sender, ServerPlayerEntity player) {
        if (((EntityTrackerEntryDuck)this).polymc$getWizards().get(PolyMapProvider.getPolyMap(player)) == null) {
            instance.sendPackets(sender);
        }
    }
}
