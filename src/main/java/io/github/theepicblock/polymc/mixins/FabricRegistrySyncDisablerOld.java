package io.github.theepicblock.polymc.mixins;

import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Implementation of {@link FabricRegistrySyncDisabler} for fabric-registry-sync-v0 0.8.6 and below
 *
 * @see io.github.theepicblock.polymc.impl.Config#isMixinAutoDisabled(String)
 * @deprecated will be removed once enough people are updated
 */
@SuppressWarnings("ALL")
@Deprecated
@Mixin(RegistrySyncManager.class)
public class FabricRegistrySyncDisablerOld {
    @Inject(method = "createPacket", at = @At("HEAD"), cancellable = true, remap = false)
    private static void createPacketInject(CallbackInfoReturnable<Packet<?>> cir) {
        cir.setReturnValue(null);
    }
}
