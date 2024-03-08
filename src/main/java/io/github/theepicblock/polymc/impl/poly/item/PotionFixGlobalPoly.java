package io.github.theepicblock.polymc.impl.poly.item;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.item.ItemLocation;
import io.github.theepicblock.polymc.api.item.ItemTransformer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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

            // Potions also derive their default names from their potion data
            // which will be invalid for custom potions
            // So we should set the name to the correct one (if it doesn't already have a custom name)
            var i = input.getItem();
            if (i == Items.POTION || i == Items.LINGERING_POTION || i == Items.SPLASH_POTION || i == Items.TIPPED_ARROW) {
                if (!input.contains(DataComponentTypes.CUSTOM_NAME) && location != ItemLocation.TRACKED_DATA) {
                    output.set(DataComponentTypes.CUSTOM_NAME, CustomModelDataPoly.negateCustomNameStyling(input, input.getName()));
                }
            }
            return output;
        }

        return input;
    }
}
