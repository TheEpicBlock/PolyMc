package io.github.theepicblock.polymc.mixins.item.component;

import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.TransformingDataComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Optional;

@Mixin(FoodComponent.class)
public abstract class FoodComponentMixin implements TransformingDataComponent {
    @Shadow @Final private int nutrition;

    @Shadow @Final private boolean canAlwaysEat;

    @Shadow @Final private float eatSeconds;

    @Shadow @Final private List<FoodComponent.StatusEffectEntry> effects;

    @Shadow @Final private float saturation;

    @Shadow @Final private Optional<ItemStack> usingConvertsTo;

    @Override
    public Object polymc$getTransformed(ServerPlayerEntity player) {
        if (!polymc$requireModification(player)) {
            return this;
        }

        return new FoodComponent(this.nutrition, this.saturation, this.canAlwaysEat, this.eatSeconds, this.usingConvertsTo, List.of());
    }

    @Override
    public boolean polymc$requireModification(ServerPlayerEntity player) {
        var map = Util.tryGetPolyMap(player);
        for (var effect : this.effects) {
            if (!map.canReceiveStatusEffect(effect.effect().getEffectType())) {
                return true;
            }
        }
        return false;
    }
}
