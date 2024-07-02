package io.github.theepicblock.polymc.mixins.item.component;

import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.TransformingDataComponent;
import net.minecraft.component.type.SuspiciousStewEffectsComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(SuspiciousStewEffectsComponent.class)
public abstract class SuspiciousStewComponentMixin implements TransformingDataComponent {

    @Shadow @Final private List<SuspiciousStewEffectsComponent.StewEffect> effects;

    @Override
    public Object polymc$getTransformed(ServerPlayerEntity player) {
        if (!polymc$requireModification(player)) {
            return this;
        }

        return new SuspiciousStewEffectsComponent(List.of());
    }

    @Override
    public boolean polymc$requireModification(ServerPlayerEntity player) {
        var map = Util.tryGetPolyMap(player);
        for (var effect : this.effects) {
            if (!map.canReceiveStatusEffect(effect.effect())) {
                return true;
            }
        }
        return false;
    }
}
