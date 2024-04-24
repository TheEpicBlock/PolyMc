package io.github.theepicblock.polymc.impl.poly.item;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.item.ItemLocation;
import io.github.theepicblock.polymc.api.item.ItemTransformer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PotionFixGlobalPoly implements ItemTransformer {
    @Override
    public ItemStack transform(ItemStack original, ItemStack input, PolyMap map, @Nullable ServerPlayerEntity player, @Nullable ItemLocation location) {
        if (input.contains(DataComponentTypes.POTION_CONTENTS)) {
            // Copy if needed
            var output = original == input ? input.copy() : input;
            var ogPotionComponent = output.get(DataComponentTypes.POTION_CONTENTS);
            assert ogPotionComponent != null; // We just checked that the input contains this component

            // The colour calculation will be wrong on the client, because custom potions will be missing
            // So we do the calculation on the server and force that to be the colour
            var colour = ogPotionComponent.getColor();
            output.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(ogPotionComponent.potion(), Optional.of(colour), ogPotionComponent.customEffects()));
            return output;
        }

        return input;
    }
}
