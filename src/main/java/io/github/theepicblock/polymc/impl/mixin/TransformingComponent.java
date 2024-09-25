package io.github.theepicblock.polymc.impl.mixin;

import xyz.nucleoid.packettweaker.PacketContext;

public interface TransformingComponent {
    static boolean requireTransform(Object object, PacketContext player) {
        return object instanceof TransformingComponent t && t.polymc$requireModification(player);
    }

    static boolean requireTransformForTooltip(Object object, PacketContext player) {
        return object instanceof TransformingComponent t && t.polymc$showTooltip() && t.polymc$requireModification(player);
    }

    Object polymc$getTransformed(PacketContext context);
    boolean polymc$requireModification(PacketContext context);
    default boolean polymc$showTooltip() {
        return false;
    };
}
