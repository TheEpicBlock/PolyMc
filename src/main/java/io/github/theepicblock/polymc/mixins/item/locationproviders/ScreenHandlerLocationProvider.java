package io.github.theepicblock.polymc.mixins.item.locationproviders;

import io.github.theepicblock.polymc.api.item.ItemLocation;
import io.github.theepicblock.polymc.impl.mixin.ItemLocationStaticHack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandlerSlotUpdateS2CPacket.class)
public class ScreenHandlerLocationProvider {
    @Inject(method = "write(Lnet/minecraft/network/RegistryByteBuf;)V", at = @At("HEAD"))
    private void beginWrite(RegistryByteBuf buf, CallbackInfo ci) {
        ItemLocationStaticHack.location.set(ItemLocation.INVENTORY);
    }

    @Inject(method = "write(Lnet/minecraft/network/RegistryByteBuf;)V", at = @At("RETURN"))
    private void endWrite(RegistryByteBuf buf, CallbackInfo ci) {
        ItemLocationStaticHack.location.set(null);
    }
}
