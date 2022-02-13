package io.github.theepicblock.polymc.mixins.item.locationproviders;

import io.github.theepicblock.polymc.api.item.ItemLocation;
import io.github.theepicblock.polymc.impl.mixin.ItemLocationStaticHack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTrackerUpdateS2CPacket.class)
public class TrackedDataLocationProvider {
    @Inject(method = "write(Lnet/minecraft/network/PacketByteBuf;)V", at = @At("HEAD"))
    private void beginWrite(PacketByteBuf buf, CallbackInfo ci) {
        ItemLocationStaticHack.location.set(ItemLocation.TRACKED_DATA);
    }

    @Inject(method = "write(Lnet/minecraft/network/PacketByteBuf;)V", at = @At("RETURN"))
    private void endWrite(PacketByteBuf buf, CallbackInfo ci) {
        ItemLocationStaticHack.location.set(null);
    }
}
