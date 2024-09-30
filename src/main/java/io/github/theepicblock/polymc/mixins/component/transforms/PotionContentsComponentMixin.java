package io.github.theepicblock.polymc.mixins.component.transforms;

import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.TransformingComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.Optional;

@Mixin(PotionContentsComponent.class)
public abstract class PotionContentsComponentMixin implements TransformingComponent {
    @Shadow @Final private Optional<RegistryEntry<Potion>> potion;

    @Shadow @Final private List<StatusEffectInstance> customEffects;

    @Shadow public abstract int getColor();

    @Override
    public Object polymc$getTransformed(PacketContext player) {
        if (!polymc$requireModification(player)) {
            return this;
        }

        return new PotionContentsComponent(Optional.empty(), Optional.of(this.getColor()), List.of());
    }

    @Override
    public boolean polymc$requireModification(PacketContext player) {
        var map = Util.tryGetPolyMap(player);
        if (this.potion.isPresent() && !map.canReceivePotion(this.potion.get())) {
            return true;
        }

        for (StatusEffectInstance statusEffectInstance : this.customEffects) {
            if (this.potion.isPresent() && !map.canReceiveStatusEffect(statusEffectInstance.getEffectType())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean polymc$showTooltip() {
        // Potions always have tooltip, (at least on `PotionItem`s)
        return true;
    }
}
