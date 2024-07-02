package io.github.theepicblock.polymc.mixins.block;

import io.github.theepicblock.polymc.impl.misc.BlockBreakingUtil;
import io.github.theepicblock.polymc.impl.mixin.BlockBreakingDuck;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTrackerEntry.class)
public class EnsureBreakingDisabled {
    @Shadow @Final private Entity entity;

    @Inject(method = "syncEntityData", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/server/network/EntityTrackerEntry;sendSyncPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal = 1))
    private void onSyncAttributes(CallbackInfo ci) {
        if (this.entity instanceof ServerPlayerEntity serverPlayer) {
            if (((BlockBreakingDuck)serverPlayer.interactionManager).polymc$isBreakingServerside()) {
                // Send it again to ensure the correct value is still there
                BlockBreakingUtil.sendBreakDisabler(serverPlayer);
            }
        }
    }
}
