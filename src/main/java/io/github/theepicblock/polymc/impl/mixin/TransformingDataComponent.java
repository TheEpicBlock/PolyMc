package io.github.theepicblock.polymc.impl.mixin;

import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.server.network.ServerPlayerEntity;

public interface TransformingDataComponent {
    static boolean requireTransform(Object object, ServerPlayerEntity player) {
        return object instanceof TransformingDataComponent t && t.polymc$requireModification(player);
    }

    static boolean requireTransformForTooltip(Object object, ServerPlayerEntity player) {
        return object instanceof TransformingDataComponent t && t.polymc$showTooltip() && t.polymc$requireModification(player);
    }

    Object polymc$getTransformed(ServerPlayerEntity player);
    boolean polymc$requireModification(ServerPlayerEntity player);
    default boolean polymc$showTooltip() {
        return false;
    };
}
