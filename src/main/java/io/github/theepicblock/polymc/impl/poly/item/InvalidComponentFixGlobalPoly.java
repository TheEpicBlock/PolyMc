package io.github.theepicblock.polymc.impl.poly.item;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.item.ItemLocation;
import io.github.theepicblock.polymc.api.item.ItemTransformer;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.TransformingDataComponent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class InvalidComponentFixGlobalPoly implements ItemTransformer {
    @Override
    public ItemStack transform(ItemStack original, ItemStack input, PolyMap map, @Nullable ServerPlayerEntity player, @Nullable ItemLocation location) {
        for (var comp : input.getComponents()) {
            if (!map.canReceiveDataComponentType(comp.type())
                    || (comp.value() instanceof TransformingDataComponent t
                    && t.polymc$requireModification(player))) {
                return Util.copyWithItem(original, original.getItem(), player);
            }
        }

        return input;
    }
}
