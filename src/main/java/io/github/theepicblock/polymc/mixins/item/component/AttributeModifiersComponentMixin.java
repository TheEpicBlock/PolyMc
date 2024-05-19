package io.github.theepicblock.polymc.mixins.item.component;


import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.TransformingDataComponent;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin(AttributeModifiersComponent.class)
public abstract class AttributeModifiersComponentMixin implements TransformingDataComponent {

    @Shadow @Final private List<AttributeModifiersComponent.Entry> modifiers;

    @Shadow public abstract boolean showInTooltip();

    @Shadow @Final private boolean showInTooltip;

    @Override
    public Object polymc$getTransformed(ServerPlayerEntity player) {
        if (!polymc$requireModification(player)) {
            return this;
        }

        var list = new ArrayList<AttributeModifiersComponent.Entry>();
        var map = Util.tryGetPolyMap(player);
        for (var entry : this.modifiers) {
            if (map.canReceiveEntityAttribute(entry.attribute())) {
                list.add(entry);
            }
        }

        return new AttributeModifiersComponent(list, this.showInTooltip());
    }

    @Override
    public boolean polymc$requireModification(ServerPlayerEntity player) {
        var map = Util.tryGetPolyMap(player);
        for (var entry : this.modifiers) {
            if (!map.canReceiveEntityAttribute(entry.attribute())) {
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
