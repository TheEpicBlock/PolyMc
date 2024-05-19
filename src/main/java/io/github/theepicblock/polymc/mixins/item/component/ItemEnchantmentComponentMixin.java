package io.github.theepicblock.polymc.mixins.item.component;

import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.TransformingDataComponent;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(ItemEnchantmentsComponent.class)
public abstract class ItemEnchantmentComponentMixin implements TransformingDataComponent {
    @Shadow public abstract boolean isEmpty();

    @Shadow public abstract Set<RegistryEntry<Enchantment>> getEnchantments();

    @Shadow @Final private boolean showInTooltip;

    @Override
    public Object polymc$getTransformed(ServerPlayerEntity player) {
        if (!polymc$requireModification(player)) {
            return this;
        }
        var map = Util.tryGetPolyMap(player);

        var b = new ItemEnchantmentsComponent.Builder((ItemEnchantmentsComponent) (Object) this);
        b.remove(map::canReceiveEnchantment);
        return b.build();
    }

    @Override
    public boolean polymc$requireModification(ServerPlayerEntity player) {
        var map = Util.tryGetPolyMap(player);
        if (!this.isEmpty()) {
            for (var ench : this.getEnchantments()) {
                if (!map.canReceiveEnchantment(ench)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean polymc$showTooltip() {
        return this.showInTooltip;
    }
}
