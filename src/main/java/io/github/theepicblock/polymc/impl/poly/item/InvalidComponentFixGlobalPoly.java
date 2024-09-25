package io.github.theepicblock.polymc.impl.poly.item;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.item.ItemLocation;
import io.github.theepicblock.polymc.api.item.ItemTransformer;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.TransformingComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class InvalidComponentFixGlobalPoly implements ItemTransformer {
    @Override
    public ItemStack transform(ItemStack original, ItemStack input, PolyMap map, @Nullable ServerPlayerEntity player, @Nullable ItemLocation location) {
        for (var comp : input.getComponents()) {
            if (!map.canReceiveDataComponentType(comp.type())
                    || (comp.value() instanceof TransformingComponent t
                    && t.polymc$requireModification(Util.getContext(player)))) {
                return Util.copyWithItem(input, input.getItem(), player);
            }
        }

        return input;
    }
}
