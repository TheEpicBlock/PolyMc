package io.github.theepicblock.polymc.mixins.component.transforms;


import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.TransformingComponent;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;

@Mixin(AttributeModifiersComponent.class)
public abstract class AttributeModifiersComponentMixin implements TransformingComponent {

    @Shadow @Final private List<AttributeModifiersComponent.Entry> modifiers;

    @Shadow public abstract boolean showInTooltip();

    @Shadow @Final private boolean showInTooltip;

    @Override
    public Object polymc$getTransformed(PacketContext player) {
        if (!polymc$requireModification(player)) {
            return this;
        }

        var list = new ArrayList<AttributeModifiersComponent.Entry>();
        var map = Util.tryGetPolyMap(player);
        for (var entry : this.modifiers) {
            if (map.canReceiveRegistryEntry(Registries.ATTRIBUTE, entry.attribute())) {
                list.add(entry);
            }
        }

        return new AttributeModifiersComponent(list, this.showInTooltip());
    }

    @Override
    public boolean polymc$requireModification(PacketContext context) {
        var map = Util.tryGetPolyMap(context);
        for (var entry : this.modifiers) {
            if (!map.canReceiveRegistryEntry(Registries.ATTRIBUTE, entry.attribute())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean polymc$showTooltip() {
        return this.showInTooltip;
    }
}
