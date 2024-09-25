package io.github.theepicblock.polymc.mixins.compat;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.ladysnake.cca.api.v3.component.sync.PlayerSyncPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(PlayerSyncPredicate.class)
public interface CCAPlayerSyncPredicateFix {
    @ModifyReturnValue(method = "isRequiredOnClient", at = @At("RETURN"), require = 0, remap = false)
    default boolean noSyncRequired(boolean original) {
        return false;
    }
}
